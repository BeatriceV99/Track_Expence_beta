package com.example.trackexpence.activities;

import static com.example.trackexpence.models.expenses.GetExpenses.getExpenseRecord;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trackexpence.R;
import com.example.trackexpence.models.DBManager;
import com.example.trackexpence.models.FirebaseWrapper;
import com.example.trackexpence.models.MenuManager;
import com.example.trackexpence.models.expenses.GetExpenses;
import com.example.trackexpence.models.expenses.MyExpenses;
import com.example.trackexpence.models.reminders.MyReminders;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

public class ExpenseActivity extends AppCompatActivity {
    MenuManager menu = new MenuManager();
    private String PMStatus;
    Context PMContext;
    private static final String TAG = ExpenseActivity.class.getCanonicalName();

    private Calendar calendar;
    private TextView date;
    private TextView date_reminder;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            PMContext = getBaseContext();

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            setContentView(R.layout.activity_expense);

            Bundle b = getIntent().getExtras();
            String xId = null;
            String xCaller = null;
            if (b != null) {
                xId = b.getString("id");
                xCaller = b.getString("caller");
                // if received expense id, read data from database
                if (xId != null) {
                    final String xxId = xId;
                    PMStatus = "EDIT";
                    getExpenseRecord(new GetExpenses.ExpenseCallback() {
                        @Override
                        public void onExpenseLoaded(MyExpenses oExpense, String pId) {
                            EditText E_expense = findViewById(R.id.EA_expense_title);
                            EditText E_amount = findViewById(R.id.EA_amount);
                            Spinner E_category_spinner = findViewById(R.id.category_spinner);
                            EditText E_category_extra = findViewById(R.id.category_extra);
                            TextView E_date = findViewById(R.id.EA_date);
                            TextView E_date_reminder = findViewById(R.id.EA_date_reminder);
                            Switch E_reminder_switch = findViewById(R.id.EA_reminder);
                            EditText E_notes = findViewById(R.id.EA_notes);

                            String xExpenseTitle = oExpense.getTitle();
                            String xAmount = oExpense.getAmount();
                            String xCategory = oExpense.getCategory();
                            String xCategoryExtra = oExpense.getCategoryExtra();
                            String xDate = oExpense.getDate();
                            String xNotes = oExpense.getNotes();

                            E_expense.setText(xExpenseTitle);
                            E_amount.setText(xAmount);
                            E_category_spinner.setSelection(getSpinnerIdByName(xCategory));
                            E_category_extra.setText(xCategoryExtra);
                            E_date.setText(xDate);
                            E_notes.setText(xNotes);

                            getReminderDate(new GetReminderDateCallback() {
                                @Override
                                public void onDataRetrieved(String pData) {
                                    if (pData != null) {
                                        E_reminder_switch.setChecked(true);
                                        E_reminder_switch.setEnabled(false);
                                        E_date_reminder.setVisibility(View.VISIBLE);
                                        E_date_reminder.setEnabled(false);
                                        E_date_reminder.setText(pData);
                                    }
                                }
                            }, xxId);
                        }
                    }, xId);
                }
                else {
                    PMStatus = "INSERT";
                }
            }
            else {
                PMStatus = "INSERT";
            }

            final String[] category = {""};

            EditText expense = findViewById(R.id.EA_expense_title);
            EditText amount = findViewById(R.id.EA_amount);
            EditText notes = findViewById(R.id.EA_notes);
            date = findViewById(R.id.EA_date);
            // get current date
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = currentDate.format(formatter);
            date.setText(formattedDate);
            // expense date
            date.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePickerDialog();
                }
            });
            // reminder date
            date_reminder = findViewById(R.id.EA_date_reminder);
            date_reminder.setText(formattedDate);
            date_reminder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDatePickerDialogReminder();
                }
            });
            calendar = Calendar.getInstance();
            // get switch
            Switch reminder_switch = findViewById(R.id.EA_reminder);
            Button save_button = findViewById(R.id.EA_save_button);
            Spinner category_spinner = findViewById(R.id.category_spinner);
            EditText category_extra = findViewById(R.id.category_extra);
            // insert categories into spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            category_spinner.setAdapter(adapter);
            /* Switch category start */
            category_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    if (position != 0) {
                        category[0] = selectedItem;
                    }
                    else {
                        category[0] = "";
                    }

                    if (selectedItem.equals("Other")) {
                        category_extra.setVisibility(View.VISIBLE);
                    }
                    else {
                        category_extra.setVisibility(View.GONE);
                        category_extra.setText("");
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            final Boolean[] IsReminder = {reminder_switch.isChecked()};

            reminder_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // check switch status
                    IsReminder[0] = reminder_switch.isChecked();
                }
            });

            String xxId = xId;
            String final_xCaller = xCaller;

            save_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean bError = false;
                    if (expense.getText().toString().isEmpty()) {
                        expense.setError(getString(R.string.EA_error_title));
                        bError = true;
                    }
                    if (amount.getText().toString().isEmpty()) {
                        amount.setError(getString(R.string.EA_error_amount));
                        bError = true;
                    }
                    if (category[0].isEmpty()) {
                        ((TextView) category_spinner.getSelectedView()).setError(getString(R.string.EA_error_category));
                        bError = true;
                    }

                    if (!bError) {
                        try {
                            MyExpenses mye = new MyExpenses(xxId, expense.getText().toString(), Double.parseDouble(amount.getText().toString()), category[0], category_extra.getText().toString(), date.getText().toString(), notes.getText().toString());
                            String id = mye.Save();
                            if (IsReminder[0]) {
                                MyReminders myr = new MyReminders(null, id, expense.getText().toString(), Double.parseDouble(amount.getText().toString()), category[0], category_extra.getText().toString(), date_reminder.getText().toString(), notes.getText().toString());
                                myr.Save();
                                if (PMStatus.equals("INSERT")) {
                                    Toast.makeText(PMContext, getString(R.string.EA_toast_ok_rem) + " " + date_reminder.getText().toString(), Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(PMContext, getString(R.string.EA_toast_ok_rem_edit) + " " + date_reminder.getText().toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                            else {
                                if(PMStatus.equals("INSERT")){
                                    Toast.makeText(PMContext, getString(R.string.EA_toast_ok), Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(PMContext, getString(R.string.EA_toast_ok_edit), Toast.LENGTH_LONG).show();
                                }
                            }

                            if (final_xCaller != null) {
                                // manage go to different activity on save based on caller
                                if (final_xCaller.equals("ExpensesListActivity")) {
                                    menu.goToActivity(ExpenseActivity.this, ExpensesListActivity.class, null);
                                } else if (final_xCaller.equals("MainActivity")) {
                                    menu.goToActivity(ExpenseActivity.this, MainActivity.class, null);
                                } else {
                                    menu.goToActivity(ExpenseActivity.this, ExpensesListActivity.class, null);
                                }
                            }
                            else {
                                menu.goToActivity(ExpenseActivity.this, MainActivity.class, null);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error inserting expense. " + e);
                            Toast.makeText(PMContext, getString(R.string.EA_toast_ko), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

            //set reminder date visible if switch checked
            reminder_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        date_reminder.setVisibility(View.VISIBLE);
                    } else {
                        date_reminder.setVisibility(View.INVISIBLE);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "onCreate---Error: " + e);
        }
    }

    //used to set date using the calendar
    private void showDatePickerDialog() {
        try{
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            updateSelectedDate();
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        catch (Exception e){
            Log.e(TAG,"showDatePickerDialog---Error: " + e);
        }
    }

    private void showDatePickerDialogReminder() {
        try{
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            calendar.set(Calendar.YEAR, year);
                            calendar.set(Calendar.MONTH, month);
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                            updateSelectedDateReminder();
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        }
        catch (Exception e) {
            Log.e(TAG, "showDatePickerDialogReminder--Error: "+e);
        }
    }

    private void updateSelectedDate() {
        try{
            String dateFormat = "dd-MM-yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
            String formattedDate = sdf.format(calendar.getTime());
            date.setText(formattedDate);
        }
        catch (Exception e) {
            Log.e(TAG, "updateSelectedDate---Error: "+e);
        }
    }

    private void updateSelectedDateReminder() {
        try{
            String dateFormat = "dd-MM-yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
            String formattedDate = sdf.format(calendar.getTime());
            date_reminder.setText(formattedDate);
        }
        catch (Exception e){
            Log.e(TAG, "updateSelectedDateReminder---Error: "+e);
        }
    }

    private int getSpinnerIdByName(String pName) {
        int xPosition = 0;
        try {
            switch (pName) {
                case "Free Time":
                    xPosition = 1;
                    break;
                case "Household":
                    xPosition = 2;
                    break;
                case "Food":
                    xPosition = 3;
                    break;
                case "Education":
                    xPosition = 4;
                    break;
                case "Health":
                    xPosition = 5;
                    break;
                case "Other":
                    xPosition = 6;
                    break;
                default:
                    xPosition = 0;
                    break;
            }
            return xPosition;
        } catch (Exception e) {
            Log.e(TAG, "getSpinnerIdByName---Error: " + e);
            return xPosition;
        }
    }

    private String getReminderDate(GetReminderDateCallback callback, String pId_exp){
        final String[] xRetVal = {null};
        FirebaseWrapper.Auth mAuth=new FirebaseWrapper.Auth();
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
                            String date=reminderSnapshot.child("date").getValue().toString();
                            String id_exp = reminderSnapshot.child("id_exp").getValue().toString();
                            if(id_exp.equals(pId_exp)){
                                xRetVal[0] =date;
                                callback.onDataRetrieved(xRetVal[0]);
                            }
                        }
                    }
                    else {
                        Log.e(TAG,"getReminderDate---Error reading reminder date from firebase. Error: " + task.getException().getMessage());
                    }
                }
            });
        }
        catch (Exception e) {
            Log.e(TAG,"getReminderDate---Error reading reading reminder date. Error: " + e);
        }
        return xRetVal[0];
    }

    public interface GetReminderDateCallback {
        void onDataRetrieved(String pData);
    }
}
