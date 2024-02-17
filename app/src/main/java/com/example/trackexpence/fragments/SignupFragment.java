package com.example.trackexpence.fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.trackexpence.R;
import com.example.trackexpence.activities.EnterActivity;
import com.example.trackexpence.models.DBManager;
import com.example.trackexpence.models.FirebaseWrapper;

public class SignupFragment extends LogFragment {
    private final static String TAG = SignupFragment.class.getCanonicalName();
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU);
    static String xUid = null;
    static String xFirstname = null;
    static String xLastname = null;
    static String xEmail = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initArguments();
    }

    public static void SetUid(String uid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            xUid = uid;
        }
    }

    public static void SaveProfile() {
        String keys[] = new String[1];
        String children[][] = new String[1][3];
        Object[][] values = new Object[1][3];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            keys[0] = xUid;
        }

        children[0][0] = "firstname";
        children[0][1] = "lastname";
        children[0][2] = "email";

        values[0][0] = xFirstname;
        values[0][1] = xLastname;
        values[0][2] = xEmail;

        DBManager.WriteData("users", true, keys, children, values);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View externalView = inflater.inflate(R.layout.fragment_signup, container, false);
        TextView link = externalView.findViewById(R.id.switchRegisterToLoginLabel);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EnterActivity) SignupFragment.this.requireActivity()).renderFragment(true, null);
            }
        });

        Button button = externalView.findViewById(R.id.logButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    EditText firstname = externalView.findViewById(R.id.firstname);
                    EditText lastname = externalView.findViewById(R.id.lastname);
                    EditText email = externalView.findViewById(R.id.userEmail);
                    EditText password = externalView.findViewById(R.id.userPassword);
                    EditText password2 = externalView.findViewById(R.id.userPasswordAgain);

                    xFirstname = firstname.getText().toString();
                    xLastname = lastname.getText().toString();
                    xEmail = email.getText().toString();
                    boolean bError = false;

                    if (firstname.getText().toString().isEmpty()) {
                        bError = true;
                        firstname.setError(getString(R.string.errorFirstname));
                    }
                    if (lastname.getText().toString().isEmpty()) {
                        bError = true;
                        lastname.setError(getString(R.string.errorLastname));
                    }
                    if (email.getText().toString().isEmpty()) {
                        bError = true;
                        email.setError(getString(R.string.errorEmail));
                    }
                    if (password.getText().toString().isEmpty()) {
                        bError = true;
                        password.setError(getString(R.string.errorPassword));
                    }
                    if (password2.getText().toString().isEmpty()) {
                        bError = true;
                        password2.setError(getString(R.string.errorPassword2));
                    }

                    if (!password.getText().toString().equals(password2.getText().toString())) {
                        bError = true;
                        Toast.makeText(SignupFragment.this.requireActivity(), getString(R.string.errorPassword2), Toast.LENGTH_LONG).show();
                    }

                    if (!bError) {
                        // do signup
                        FirebaseWrapper.Auth auth = new FirebaseWrapper.Auth();
                        auth.signUp(
                                email.getText().toString(),
                                password.getText().toString(),
                                FirebaseWrapper.Callback.newIstance(
                                        SignupFragment.this.requireActivity(),
                                        SignupFragment.this.callbackName,
                                        SignupFragment.this.callbackPrms
                                )
                        );
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Save button onClick---Error: " + e);
                }
            }
        });

        return externalView;
    }
}
