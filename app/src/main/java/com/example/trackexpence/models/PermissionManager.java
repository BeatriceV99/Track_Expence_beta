package com.example.trackexpence.models;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    public final static String[] NEEDED_PERMISSIONS = {Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.POST_NOTIFICATIONS};
    private final static String TAG = PermissionManager.class.getCanonicalName();
    private final Activity activity;

    public PermissionManager(Activity activity) {
        this.activity = activity;
    }

    public boolean askNeededPermissions(int requestCode, boolean performRequest) {
        List<String> missingPermissions = new ArrayList<>();

        for (String permission : NEEDED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this.activity, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission " + permission + " is granted by the user.");
            }
            else {
                missingPermissions.add(permission);
            }
        }

        if (missingPermissions.isEmpty()) {
            return false;
        }
        if (performRequest) {
            Log.d(TAG, "Request for missing permissions " + missingPermissions);
            ActivityCompat.requestPermissions(this.activity, missingPermissions.toArray(new String[missingPermissions.size()]), requestCode);
        }
        return true;
    }
}
