package com.example.trackexpence.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.trackexpence.R;
import com.example.trackexpence.activities.EnterActivity;
import com.example.trackexpence.models.FirebaseWrapper;

public class PasswordFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    Context PMContext;
    private final static String TAG = PasswordFragment.class.getCanonicalName();
    EditText email;

    public PasswordFragment() {
        // Required empty public constructor
    }

    public static PasswordFragment newInstance(String param1, String param2) {
        PasswordFragment fragment = new PasswordFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView goToLogin = view.findViewById(R.id.switchPasswordToLoginLabel);
        Button save = view.findViewById(R.id.PF_save);
        email = view.findViewById(R.id.userEmailForgot);
        PMContext = getContext();

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EnterActivity)PasswordFragment.this.requireActivity()).renderFragment(true, null);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String xEmail = email.getText().toString();
                    FirebaseWrapper.Auth firebaseAuth = new FirebaseWrapper.Auth();

                    firebaseAuth.ResetAuthPassword(new FirebaseWrapper.Auth.ResetPasswordCallback() {
                        @Override
                        public void onResetSent(boolean pRetval) {
                            if (pRetval) {
                                Toast.makeText(PMContext, getString(R.string.forgotPassword_toast_ok), Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(PMContext, getString(R.string.forgotPassword_toast_ko), Toast.LENGTH_LONG).show();
                            }
                            ((EnterActivity)PasswordFragment.this.requireActivity()).renderFragment(true, null);
                        }
                    }, xEmail);
                } catch (Exception e) {
                    Log.e(TAG, "PasswordFragment (save)---Error: " + e);
                }
            }
        });
    }
}
