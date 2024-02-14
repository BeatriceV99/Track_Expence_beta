package com.example.trackexpence.activities;


import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

@SuppressLint("CustomSlashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getCanonicalName();
    private static final int PERMISSION_REQUEST_CODE = (new Random()).nextInt() & Integer.MAX_VALUE;

}
