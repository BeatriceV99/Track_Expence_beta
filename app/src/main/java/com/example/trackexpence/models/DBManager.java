package com.example.trackexpence.models;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DBManager {
    private final static String TAG = DBManager.class.getCanonicalName();
    public static void WriteData(String pReference, Boolean key, String[] pKeys, String[][] pChildren, Object[][] pValues) {
        Log.d(TAG, "WriteData start");
        String DBUrl = GetDBUrl();
        FirebaseDatabase database = FirebaseDatabase.getInstance(DBUrl);

        int c_key = 0;

        try {
            for (String k : pKeys) {
                // manual key
                if (key) {
                    int c_children = 0;
                    for (int i = 0; i < pChildren[c_key].length; i++) {
                        if (pChildren[c_key][c_children] != null) {
                            DatabaseReference dReference = database.getReference(pReference).child(pKeys[c_key]).child(pChildren[c_key][c_children]);
                            dReference.setValue(pValues[c_key][c_children]);
                            c_children++;
                        }
                    }
                    c_key++;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e);
        }
    }

    public static boolean DeleteData(String name, String id, DeleteDataCallback callback) {
        final boolean[] bRetVal = {false};
        final boolean[] bError = {false};
        try {
            Log.d(TAG, "DeleteData---Start deleting data");
            FirebaseWrapper.Auth mAuth = new FirebaseWrapper.Auth();
            // get current user id
            String xUserID = mAuth.getUid();
            // get url of database
            String xDBUrl = GetDBUrl();
            FirebaseDatabase database = FirebaseDatabase.getInstance(xDBUrl);

            DatabaseReference reference = database.getReference(name);

            DatabaseReference recordToDelete = reference.child(xUserID).child(id);

            recordToDelete.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    bError[0] = false;
                    bRetVal[0] = true;
                }
                else {
                    bError[0] = true;
                    bRetVal[0] = false;
                }
                callback.onDataDeleted(bRetVal[0]);
            });
        } catch (Exception e) {
            bError[0] = true;
            bRetVal[0] = false;
            Log.e(TAG, "DeleteData---Error deleting data");
        }
        finally {
            if (bError[0]) {
                Log.e(TAG, "DeleteData---Delete process executed with errors");
                bRetVal[0] = false;
            }
            else {
                Log.d(TAG, "DeleteData--- Delete process executed successfully");
                bRetVal[0] = true;
            }
        }
        return bRetVal[0];
    }

    public interface DeleteDataCallback {
        void onDataDeleted(boolean pRetVal);
    }

    public static String GetDBUrl() {
        String DBUrl = "https://track-expense-beta-default-rtdb.europe-west1.firebasedatabase.app/";
        return  DBUrl;
    }
}
