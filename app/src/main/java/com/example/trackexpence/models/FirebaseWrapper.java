package com.example.trackexpence.models;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FirebaseWrapper {
    // used to manage callbacks
    public static class Callback {
        private final static String TAG = FirebaseWrapper.class.getCanonicalName();
        private final Method method;
        private final Object thiz;

        public Callback(Method method, Object thiz) {
            this.method = method;
            this.thiz = thiz;
        }

        public static Callback newIstance (Object thiz, String name, Class<?>... prms) {
            Class<?> clazz = thiz.getClass();
            try {
                return new Callback(clazz.getMethod(name, prms), thiz);
            } catch (NoSuchMethodException e) {
                Log.w(TAG, "Cannot find method " + name + " in class " + clazz.getCanonicalName());
                throw new RuntimeException(e);
            }
        }

        public void invoke(Object... objs) {
            try {
                this.method.invoke(thiz, objs);
            } catch (IllegalAccessException | InvocationTargetException e) {
                Log.w(TAG, "Something went wrong during the callback. Message: "+ e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public static class Auth {
        private final static String TAG = Auth.class.getCanonicalName();
        private final FirebaseAuth auth;

        public Auth() {
            this.auth = FirebaseAuth.getInstance();
        }

        public boolean isAuthenticated() {
            return this.auth.getCurrentUser() != null;
        }

        public FirebaseUser getUser() {
            return this.auth.getCurrentUser();
        }

        public void signOut() {
            this.auth.signOut();
        }

        public void signIn(String email, String password, FirebaseWrapper.Callback callback) {
            this.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            callback.invoke(task.isSuccessful());
                        }
                    });
        }

        public void signUp(String email, String password, FirebaseWrapper.Callback callback) {
            String uid = null;
            this.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = task.getResult().getUser();
                                // Access the user ID
                                String uid = user.getUid();
                                // method called to insert the auth user in database users

                                Log.d("TAG", "User ID: " + uid);
                            }
                            else {
                                Log.e("TAG", "Error signing up: " + task.getException());
                            }
                            callback.invoke(task.isSuccessful());
                        }
                    });
        }

        public String getUid() {
            if (this.isAuthenticated()) {
                return this.getUser().getUid();
            }
            else {
                return "";
            }
        }

        // update the user auth email or password
        public boolean UpdateUserAuth(ChangeEmailPasswordCallback callback, String email, String password) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final boolean[] bOK = {true};
            final String[] xRetVal = {""};
            try {
                if (!email.isEmpty()) {
                    user.verifyBeforeUpdateEmail(email).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            xRetVal[0] = "Update E-Mail request sent! Verify your email";
                        }
                        else {
                            bOK[0] = false;
                            xRetVal[0] = task.getException().getMessage();
                            Log.e(TAG, "Error updating user email in firebase auth. Firebase error: " + task.getException().getMessage());
                        }
                        callback.onChangeSent(xRetVal[0]);
                    });
                }
                if (!password.isEmpty()) {
                    user.updatePassword(password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            xRetVal[0] = "Password update successfully!";
                        }
                        else {
                            bOK[0] = false;
                            xRetVal[0] = "Firebase Auth error: " + task.getException().getMessage();
                            Log.e(TAG, "Error updating user password in firebase auth");
                        }
                        callback.onChangeSent(xRetVal[0]);
                    });
                }
            } catch (Exception e) {
                bOK[0] = false;
                Log.e(TAG, "UpdateAuth---Error " + e);
            }

            return bOK[0];
        }

        public interface ChangeEmailPasswordCallback {
            void onChangeSent(String pRetval);
        }

        public void ResetAuthPassword(ResetPasswordCallback callback, String pEmail) {
            final boolean[] bRetval = {false};
            try {
                FirebaseAuth.getInstance().sendPasswordResetEmail(pEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            bRetval[0] = true;
                        }
                        else {
                            bRetval[0] = false;
                        }
                        callback.onResetSent(bRetval[0]);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "ResetAuthPassword---Error: " + e);
            }
        }

        public interface ResetPasswordCallback {
            void onResetSent(boolean pRetval);
        }

        public void checkPasswordProfile(CheckPasswordProfileCallback callback, String pEmail, String pPassword) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            final boolean[] bRetVal = {false};
            auth.signInWithEmailAndPassword(pEmail, pPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        bRetVal[0] = false;
                    }
                    else {
                        bRetVal[0] = true;
                    }
                    callback.onPasswordChecked(bRetVal[0]);
                }
            });
        }

        public interface CheckPasswordProfileCallback {
            void onPasswordChecked(boolean pRetval);
        }
    }
}
