package com.example.trackexpence.models;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trackexpence.activities.ExpenseActivity;
import com.example.trackexpence.activities.MainActivity;
import com.example.trackexpence.activities.SplashActivity;

import com.example.trackexpence.R;

public class MenuManager extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getCanonicalName();

    public void goToActivity(Context context, Class<?> activity, Bundle pBundle) {
        try {
            Intent intent = new Intent(context, activity);

            if (pBundle != null) {
                intent.putExtras(pBundle);
            }

            context.startActivity(intent);
            this.finish();
        } catch (Exception e) {
            Log.e(TAG, "goToActivity---Error: " + e);
        }
    }

    public boolean ChangeActivity(Context context, MenuItem menu) {
        int id = menu.getItemId();

        switch (id) {
            case R.id.MainActivity:
                this.goToActivity(context, MainActivity.class, null);
                break;
            case R.id.ExpenseActivity:
                this.goToActivity(context, ExpenseActivity.class, null);
                break;
            case R.id.ExpenseReminder:
                Bundle bundle= new Bundle();
                bundle.putString("action", "Recycler");
                this.goToActivity(context, ReminderActivity.class, bundle);
                break;
            case R.id.CurrencyConverter:
                this.goToActivity(context, CurrencyConverterActivity.class, null);
                break;
            case R.id.ProfileActivity:
                this.goToActivity(context, ProfileActivity.class, null);
                break;
            case R.id.Logout:
                FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                auth.signOut();
                this.goToActivity(context, SplashActivity.class, null);
                break;
            default:
                Log.e(TAG, "ChangeActivity---Error reading menu id");
        }
        return true;
    }
}
