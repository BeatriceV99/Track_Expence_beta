package com.example.trackexpence.models.expenses;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.trackexpence.models.DBManager;
import com.example.trackexpence.models.FirebaseWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class GetExpenses {
    private final static String TAG = GetExpenses.class.getCanonicalName();

    public static void getExpenses(ExpensesCallback callback) {
        ArrayList<MyExpenses> expensesList = new ArrayList<MyExpenses>();
        FirebaseWrapper.Auth mAuth = new FirebaseWrapper.Auth();

        try {
            // get current user id
            String UserID = mAuth.getUid();
            // get url of database
            String DBUrl = DBManager.GetDBUrl();
            FirebaseDatabase database = FirebaseDatabase.getInstance(DBUrl);

            DatabaseReference reference = database.getReference("expenses");

            reference.child(UserID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                            String id = expenseSnapshot.getKey().toString();
                            String expense_title = expenseSnapshot.child("expense_title").getValue().toString();
                            double amount = Double.parseDouble(expenseSnapshot.child("amount").getValue().toString());
                            String category = expenseSnapshot.child("category").getValue().toString();
                            String category_extra = expenseSnapshot.child("category_extra").getValue().toString();
                            String date = expenseSnapshot.child("date").getValue().toString();
                            String notes = expenseSnapshot.child("notes").getValue().toString();

                            MyExpenses expense = new MyExpenses(id, expense_title, amount, category, category_extra, date, notes);

                            expensesList.add(expense);
                        }
                        callback.onExpensesLoaded(expensesList);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error reading expenses. Error: " + e);
        }
    }

    public interface ExpensesCallback {
        void onExpensesLoaded(ArrayList<MyExpenses> expensesList);
    }

    public static void getExpenseRecord(ExpenseCallback callback, String pId) {
        FirebaseWrapper.Auth mAuth = new FirebaseWrapper.Auth();
        MyExpenses expense;
        try {
            // get current user id
            String UserID = mAuth.getUid();
            // get url of database
            String DBUrl = DBManager.GetDBUrl();
            FirebaseDatabase database = FirebaseDatabase.getInstance(DBUrl);

            DatabaseReference reference = database.getReference("expenses");
            reference.child(UserID).child(pId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();

                        String id = dataSnapshot.getKey().toString();
                        String expense_title = dataSnapshot.child("expense_title").getValue().toString();
                        double amount = Double.parseDouble(dataSnapshot.child("amount").getValue().toString());
                        String category = dataSnapshot.child("category").getValue().toString();
                        String category_extra = dataSnapshot.child("category_extra").getValue().toString();
                        String date = dataSnapshot.child("date").getValue().toString();
                        String notes = dataSnapshot.child("notes").getValue().toString();

                        MyExpenses expense = new MyExpenses(id, expense_title, amount, category, category_extra, date, notes);
                        callback.onExpenseLoaded(expense, pId);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error reading expense. Error: " + e);
        }
    }

    public interface ExpenseCallback {
        void onExpenseLoaded(MyExpenses expense, String pId);
    }
}
