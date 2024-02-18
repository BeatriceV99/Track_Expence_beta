package com.example.trackexpence.fragments;

import static com.example.trackexpence.models.expenses.GetExpenses.getExpenses;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackexpence.adapters.ExpensesListAdapter;
import com.example.trackexpence.models.DBManager;
import com.example.trackexpence.models.FirebaseWrapper;
import com.example.trackexpence.models.expenses.GetExpenses;
import com.example.trackexpence.models.expenses.MyExpenses;
import com.example.trackexpence.utilities.DataComparatorE;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.example.trackexpence.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

public class ExpenseListFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ExpensesListAdapter itemAdapter;
    final String[] xCategory = {null};
    int xMonth;
    final String[] xYear = {null};
    private final static String TAG = ExpenseListFragment.class.getCanonicalName();
    Calendar calendar = Calendar.getInstance();
    String xCurrentYear = Integer.toString(calendar.get(Calendar.YEAR));
    int xCurrentMonth= calendar.get(Calendar.MONTH)+1;

    private String mParam1;
    private String mParam2;

    TextView empty_expense;

    public ExpenseListFragment() {
        // Required empty public constructor
    }

    public static ExpenseListFragment newInstance(String param1, String param2) {
        ExpenseListFragment fragment = new ExpenseListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //change the layout by caller name
        try {
            String xCaller = this.getArguments().getString("caller");
            if (xCaller.equals("ExpensesListActivity")){
                return inflater.inflate(R.layout.fragment_expense_list_from_activity, container, false);
            }
            else if (xCaller.equals("MainActivity")) {
                return inflater.inflate(R.layout.fragment_expense_list, container, false);
            }
        }
        catch (Exception e){
            Log.e(TAG, "onCreateView---Error in onCreateView: "+e);
            return null;
        }
        return null;
    }

    @Override
    public void
    onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);
            // getting the expensesList
            Context PMContext = getContext();
            View PMView = view;
            Spinner SPCategory = view.findViewById((R.id.filter_category));
            Spinner SPMonth = view.findViewById((R.id.filter_month));
            Spinner SPYear = view.findViewById((R.id.filter_year));

            SPMonth.setSelection(xCurrentMonth);
            SPYear.setSelection(0);

            populateYearSpinner(PMContext, SPYear);

            final boolean[] bFirstLoad1 = {true};
            final boolean[] bFirstLoad2 = {true};
            final boolean[] bFirstLoad3 = {true};

            empty_expense = view.findViewById(R.id.empty_expense);
            getExpenses(new GetExpenses.ExpensesCallback() {
                @Override
                public void onExpensesLoaded(ArrayList<MyExpenses> expensesList) {
                    Collections.sort(expensesList, new DataComparatorE());
                    loadRecycler(PMContext, PMView, expensesList);

                    if (expensesList.isEmpty()) {
                        empty_expense.setVisibility(View.VISIBLE);
                    }
                }
            });

            SPCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    if(position==0){
                        xCategory[0]=null;
                    }
                    else{
                        xCategory[0] = SPCategory.getSelectedItem().toString();
                    }

                    if(!bFirstLoad1[0]) {
                        filterRecycler();
                    }
                    bFirstLoad1[0] =false;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    xCategory[0] = null;
                }
            });

            SPMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    if(!bFirstLoad2[0]) {
                        xMonth=position;
                        filterRecycler();
                    }
                    else {
                        xMonth=xCurrentMonth;
                        filterRecycler();
                    }
                    bFirstLoad2[0] =false;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    xMonth=xCurrentMonth;
                }
            });

            SPYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    if(!bFirstLoad3[0]) {
                        xYear[0] = SPYear.getSelectedItem().toString();
                        filterRecycler();
                    }
                    else {
                        xYear[0]=xCurrentYear;
                        filterRecycler();
                    }
                    bFirstLoad3[0] =false;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    xYear[0]=xCurrentYear;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "onViewCreated---Error: "+e);
        }
    }

    public void goBack(View view) {
        getActivity().getFragmentManager().popBackStack();
    }

    private void loadRecycler(Context pContext, View pView,ArrayList<MyExpenses> pExpenseList){
        try {
            ArrayList<MyExpenses> expense_list=pExpenseList;
            // assign expenseList to ItemAdapter
            itemAdapter = new ExpensesListAdapter(expense_list, pContext);
            // set the LayoutManager that this RecyclerView will use
            RecyclerView recyclerView = pView.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            // adapter instance is set to the recyclerview to inflate the items
            recyclerView.setAdapter(itemAdapter);

            ProgressBar progress_bar=pView.findViewById(R.id.progressBar);
            progress_bar.setVisibility(View.GONE);
        } catch (Exception e){
            Log.e(TAG, "loadRecycler---Error: "+e);
        }
    }

    private void filterRecycler(){
        if(itemAdapter!=null) {
            itemAdapter.filter(xCategory[0], xMonth, xYear[0]);
        }
    }

    private void populateYearSpinner(Context pContext, Spinner pSpinner) {
        FirebaseWrapper.Auth mAuth=new FirebaseWrapper.Auth();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        final String[] xYear = {null};
        try {
            //get current user id
            String UserID = mAuth.getUid();
            //get url of database
            String DBUrl = DBManager.GetDBUrl();
            FirebaseDatabase database = FirebaseDatabase.getInstance(DBUrl);

            DatabaseReference reference = database.getReference("expenses");

            reference.child(UserID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        HashSet<String> yearsSet = new HashSet<String>();
                        for (DataSnapshot expenseSnapshot : dataSnapshot.getChildren()) {
                            String date = expenseSnapshot.child("date").getValue().toString();
                            Date xDate = null;
                            try {
                                xDate = dateFormat.parse(date);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(xDate);
                            xYear[0] = Integer.toString(cal.get(Calendar.YEAR));
                            if (xYear[0] != null) {
                                yearsSet.add(xYear[0]);
                            }
                        }
                        //convert set to list
                        ArrayList<String> yearsList = new ArrayList<>(yearsSet);
                        //sort the list in descending order
                        Collections.sort(yearsList, Collections.reverseOrder());
                        // Create an ArrayAdapter and set it to the Spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(pContext, android.R.layout.simple_spinner_item, yearsList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        pSpinner.setAdapter(adapter);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG,"populateYearSpinner---Error: "+e);
        }
    }
}
