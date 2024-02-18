package com.example.trackexpence.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackexpence.R;
import com.example.trackexpence.activities.ExpenseActivity;
import com.example.trackexpence.models.DBManager;
import com.example.trackexpence.models.FirebaseWrapper;
import com.example.trackexpence.models.MenuManager;
import com.example.trackexpence.models.expenses.MyExpenses;
import com.example.trackexpence.models.reminders.MyReminders;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ExpensesListAdapter extends RecyclerView.Adapter<ExpensesListAdapter.ViewHolder> {
    private static ArrayList<MyExpenses> expenses_list;
    private Context PMContext;
    private ArrayList<MyExpenses> allExpenseList;
    private final static String TAG = ExpensesListAdapter.class.getCanonicalName();

    // This class defines the ViewHolder object for each item in the RecyclerView
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView Title;
        private final TextView Amount;
        private final TextView Category;
        private final TextView Category_Extra;
        private final TextView Date;
        private final TextView Notes;
        private final ImageButton ButtonEdit;
        private final ImageButton ButtonDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            Title = itemView.findViewById(R.id.eventTitle);
            Amount = itemView.findViewById(R.id.eventAmount);
            Category = itemView.findViewById(R.id.eventCategory);
            Category_Extra = itemView.findViewById(R.id.eventCategoryExtra);
            Date = itemView.findViewById(R.id.eventDate);
            ButtonEdit=itemView.findViewById(R.id.buttonEdit);
            ButtonDelete=itemView.findViewById(R.id.buttonDelete);
            Notes=itemView.findViewById(R.id.eventNotes);
        }
    }

    public ExpensesListAdapter(ArrayList<MyExpenses> pExpenses_list, Context pContext) {
        this.expenses_list = pExpenses_list;
        this.allExpenseList=pExpenses_list;
        this.PMContext=pContext;
    }

    @Override
    public ExpensesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item and return a new ViewHolder object
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return expenses_list.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            int xPosition=position;
            MyExpenses currentExpense = expenses_list.get(position);

            holder.ButtonEdit.setTag(currentExpense.getId());
            holder.ButtonDelete.setTag(currentExpense.getId());
            holder.Title.setText(currentExpense.getTitle());
            holder.Amount.setText(currentExpense.getAmount());
            holder.Category.setText(currentExpense.getCategory());
            holder.Category_Extra.setText(currentExpense.getCategoryExtra());
            holder.Date.setText(currentExpense.getDate());
            holder.Notes.setText(currentExpense.getNotes());
            // set an onClickListener for the edit button
            holder.ButtonEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        MenuManager menu_manager=new MenuManager();
                        String xId = (String) view.getTag();

                        Bundle id = new Bundle();
                        id.putString("id", xId);
                        id.putString("caller", "");

                        menu_manager.goToActivity(view.getContext(), ExpenseActivity.class, id);

                    } catch (Exception e){
                        Log.e(TAG, "Error trying to open ExpenseActivity. Error: " + e);
                    }
                }
            });
            // set an onClickListener for the delete button
            holder.ButtonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = (String) view.getTag();

                    Log.d(TAG, "Start deleting record id " + id );
                    DBManager.DeleteData("expenses", id,new DBManager.DeleteDataCallback() {

                        @Override
                        public void onDataDeleted(boolean pRetVal) {
                            //if record deleted successfully alert user and refresh the expense RecyclerView
                            if(pRetVal){
                                //delete reminders associated to expense
                                checkRemindersOnDelete(id);
                                Toast.makeText(view.getContext(), PMContext.getString(R.string.ELF_toast_ok), Toast.LENGTH_LONG).show();
                                //remove the item from dataset
                                if (xPosition != RecyclerView.NO_POSITION) {
                                    expenses_list.remove(xPosition);
                                    //notify the adapter that the data has changed
                                    notifyDataSetChanged();
                                }
                            }
                            else {
                                Toast.makeText(view.getContext(), PMContext.getString(R.string.ELF_toast_ko), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "ERROR in onBindViewHolder. Error: " + e) ;
        }
    }

    public void filter(String pCategory, int pMonth, String pYear) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        ArrayList<MyExpenses> filteredList = new ArrayList<>();
        for (MyExpenses item : allExpenseList) {
            try {
                Date xDate = dateFormat.parse(item.getDate());
                Calendar cal = Calendar.getInstance();
                cal.setTime(xDate);
                int xMonth = cal.get(Calendar.MONTH) + 1;
                String xYear = String.valueOf(cal.get(Calendar.YEAR));

                boolean categoryMatch = pCategory == null || item.getCategory().trim().equals(pCategory);
                boolean monthMatch = pMonth <= 0 || xMonth == pMonth;
                boolean yearMatch = pYear == null || xYear.trim().equals(pYear);

                if (categoryMatch && monthMatch && yearMatch) {
                    filteredList.add(item);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        expenses_list = filteredList.isEmpty() ? allExpenseList : filteredList;
        if(pCategory!=null || pMonth!=0 || pYear!=null){
            expenses_list=filteredList;
        }
        notifyDataSetChanged();
    }

    public void checkRemindersOnDelete(String pID){
        FirebaseWrapper.Auth mAuth=new FirebaseWrapper.Auth();
        MyReminders reminder;
        try {
            //get current user id
            String UserID = mAuth.getUid();
            //get url of database
            String DBUrl = DBManager.GetDBUrl();
            FirebaseDatabase database = FirebaseDatabase.getInstance(DBUrl);

            DatabaseReference reference = database.getReference("reminders");
            reference.child(UserID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        for (DataSnapshot reminderSnapshot : dataSnapshot.getChildren()) {
                            String xId= reminderSnapshot.getKey();
                            String id_exp = reminderSnapshot.child("id_exp").getValue().toString();
                            //if expense is associated to a reminder, delete it
                            if(id_exp.equals(pID)) {
                                DBManager.DeleteData("reminders", xId,new DBManager.DeleteDataCallback() {
                                    @Override
                                    public void onDataDeleted(boolean pRetVal) {

                                    }
                                });
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG,"Error reading expense. Error: "+e);
        }
    }
}
