package com.example.trackexpence.models.reminders;

import android.util.Log;

import com.example.trackexpence.models.DBManager;
import com.example.trackexpence.models.FirebaseWrapper;
import com.example.trackexpence.utilities.Strings;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyReminders {
    private static final String TAG = MyReminders.class.getCanonicalName();

    String xId = null;
    String xId_exp = null;
    String xTitleExpense = null;
    Double xAmount = 0.0;
    String xCategory = null;
    String xCategory_extra = null;
    String xDate = null;
    String xNotes = null;

    public MyReminders(String id, String id_exp, String TitleExpense, double amount, String category, String category_extra, String date, String notes) {
        try {
            if (id == null) {
                xId = Strings.getRandomString(8);
            }
            else {
                xId = id;
            }

            xId_exp = id_exp;
            xTitleExpense = TitleExpense;
            xAmount = amount;
            xCategory = category;
            xCategory_extra = category_extra;
            xDate = date;
            xNotes = notes;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting data in MyReminders. " + e) ;
        }
    }

    public void Save() {
        try {
            FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
            FirebaseDatabase database = FirebaseDatabase.getInstance(DBManager.GetDBUrl());
            DatabaseReference dReference = database.getReference("reminders").child(auth.getUid()).child(xId);

            DatabaseReference title_expense = dReference.child("title_expense");
            DatabaseReference amount = dReference.child("amount");
            DatabaseReference category = dReference.child("category");
            DatabaseReference category_extra = dReference.child("category_extra");
            DatabaseReference date = dReference.child("date");
            DatabaseReference notes = dReference.child("notes");
            DatabaseReference id_exp = dReference.child("id_exp");

            title_expense.setValue(xTitleExpense);
            amount.setValue(xAmount);
            category.setValue(xCategory);
            category_extra.setValue(xCategory_extra);
            date.setValue(xDate);
            notes.setValue(xNotes);
            id_exp.setValue(xId_exp);
        } catch (Exception e) {
            Log.e(TAG, "Error saving data" + e);
        }
    }

    public String getId() {
        return xId;
    }
    public String getIdExp() {
        return xId_exp;
    }
    public String getTitle() {
        return xTitleExpense;
    }
    public String getAmount() {
        return String.valueOf(xAmount);
    }
    public String getCategory() {
        return xCategory;
    }
    public String getCategoryExtra() {
        return xCategory_extra;
    }
    public String getDate() {
        return xDate;
    }
    public String getNotes() {
        return xNotes;
    }
}
