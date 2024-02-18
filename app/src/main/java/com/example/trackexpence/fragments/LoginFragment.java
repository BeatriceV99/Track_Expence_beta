package com.example.trackexpence.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.trackexpence.R;
import com.example.trackexpence.activities.EnterActivity;
import com.example.trackexpence.models.FirebaseWrapper;

public class LoginFragment extends LogFragment {
    // @RequiresApi(api = Build.VERSION_CODES.TIRAMISU);

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        this.initArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View externalView = inflater.inflate(R.layout.fragment_login, container, false);
        TextView link = externalView.findViewById(R.id.switchLoginToRegisterLabel);

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EnterActivity)LoginFragment.this.requireActivity()).renderFragment(false, "passwordFragment");
            }
        });

        Button button = externalView.findViewById(R.id.logButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email = externalView.findViewById(R.id.userEmail);
                EditText password = externalView.findViewById(R.id.userPassword);
                boolean bError = false;

                if (email.getText().toString().isEmpty()) {
                    bError = true;
                    email.setError(getString(R.string.errorEmail));
                }
                if (password.getText().toString().isEmpty()) {
                    bError = true;
                    password.setError(getString(R.string.errorPassword));
                }
                if (!bError) {
                    // Perform SignIn
                    FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                    auth.signIn(
                            email.getText().toString(),
                            password.getText().toString(),
                            FirebaseWrapper.Callback.newIstance(
                                    LoginFragment.this.requireActivity(),
                                    LoginFragment.this.callbackName,
                                    LoginFragment.this.callbackPrms
                            )
                    );
                }
            }
        });

        TextView forgotPassword = externalView.findViewById(R.id.forgotPassword);

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EnterActivity)LoginFragment.this.requireActivity()).renderFragment(false, "forgotPassword");
            }
        });

        return externalView;
    }
}
