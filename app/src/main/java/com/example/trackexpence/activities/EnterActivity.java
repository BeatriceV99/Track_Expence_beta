package com.example.trackexpence.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.trackexpence.R;
import com.example.trackexpence.fragments.LogFragment;
import com.example.trackexpence.fragments.LoginFragment;
import com.example.trackexpence.fragments.PasswordFragment;
import com.example.trackexpence.fragments.SignupFragment;

public class EnterActivity extends AppCompatActivity {
    private static final String TAG = EnterActivity.class.getCanonicalName();
    private FragmentManager fragmentManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        // on create, open login fragment
        renderFragment(true, null);
    }

    public void renderFragment(boolean isLogin, String pAction) {
        Fragment fragment = null;
        try {
            if (isLogin) {
                fragment = LogFragment.newInstance(LoginFragment.class, "signinCallback", boolean.class);
            }
            else {
                if (pAction.equals("forgotPassword")) {
                    fragment = new PasswordFragment();
                }
                else {
                    fragment = LogFragment.newInstance(SignupFragment.class, "signupCallback", boolean.class);
                }
            }
            if (this.fragmentManager == null) {
                this.fragmentManager = this.getSupportFragmentManager();
            }

            FragmentTransaction fragmentTransaction = this.fragmentManager.beginTransaction();
            fragmentTransaction.setReorderingAllowed(true);
            fragmentTransaction.replace(R.id.loginRegisterFragment, fragment);

            fragmentTransaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "renderFragment---Error: " + e);
        }
    }

    // called from LogFragment.newInstance
    public void signinCallback(boolean result) {
        if (!result) {
            Toast.makeText(this, getString(R.string.ENA_toast_ko), Toast.LENGTH_LONG).show();
        } else {
            // go to splash activity to check permissions after user login
            Intent intent = new Intent(this, SplashActivity.class);
            this.startActivity(intent);
            this.finish();
        }
    }
}
