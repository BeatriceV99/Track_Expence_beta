package com.example.trackexpence.models.expenses;

import android.util.Log;

import com.example.trackexpence.models.DBManager;
import com.example.trackexpence.models.FirebaseWrapper;
import com.example.trackexpence.utilities.Strings;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyExpenses {
    private static final String TAG = MyExpenses.class.getCanonicalName();
    String xId = null;
    String xExpense_title = null;
    double xAmount = 0.0;
    String xCategory = null;
    String xCategoryExtra = null;
    String xDate = null;
    String xNotes = null;

    public MyExpenses(String id, String expense_title, double amount, String category, String category_extra, String date, String notes) {
        // set fields
        try {
            if (id != null && id != "" && id != "N") {
                xId = id;
            }
            else {
                // calculate id only for new records
                xId = Strings.getRandomString(8);
            }

            xExpense_title = expense_title;
            xAmount = amount;
            xCategory = category;
            xCategoryExtra = category_extra;
            xDate = date;
            xNotes = notes;
        } catch (Exception e) {
            Log.e(TAG, "Error setting expense fields. Error: " + e);
        }
    }

    public String getId() {
        return xId;
    }
    public String getTitle() {
        return xExpense_title;
    }
    public String getAmount() {
        return String.valueOf(xAmount);
    }
    public String getCategory() {
        return xCategory;
    }
    public String getCategoryExtra() {
        return xCategoryExtra;
    }
    public String getDate() {
        return xDate;
    }
    public String getNotes() {
        return xNotes;
    }

    public String Save() {
        try {
            FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
            FirebaseDatabase database = FirebaseDatabase.getInstance(DBManager.GetDBUrl());

            DatabaseReference dReference = database.getReference("expenses").child(auth.getUid()).child(xId);
            DatabaseReference expense_title = dReference.child("expense_title");
            DatabaseReference amount = dReference.child("amount");
            DatabaseReference category = dReference.child("amount");
            DatabaseReference category_extra = dReference.child("category_extra");
            DatabaseReference date = dReference.child("date");
            DatabaseReference notes = dReference.child("notes");

            expense_title.setValue(xExpense_title);
            amount.setValue(xAmount);
            category.setValue(xCategory);
            category_extra.setValue(xCategoryExtra);
            date.setValue(xDate);
            notes.setValue(xNotes);
        } catch (Exception e) {
            Log.e(TAG, "Error saving data. " + e);
        }

        return xId;
    }
}
