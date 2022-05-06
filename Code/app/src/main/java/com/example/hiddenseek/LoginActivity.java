package com.example.hiddenseek;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.*;
import androidx.annotation.NonNull;

import android.content.Intent;

import com.example.hiddenseek.data.FirebaseListener;


import com.google.android.gms.tasks.Task;

import android.util.Log;


import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.auth.ErrorCodes;

import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.hiddenseek.data.FirebaseListener.isReady;
import static com.example.hiddenseek.data.FirebaseListener.userInfo;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "SignedInActivity";
    private Activity currentActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        currentActivity=this;
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            // already signed in
            requestPermissionsIfNecessary(this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });

        } else {
            // not signed in
            login();
        }
    }

    private static final int RC_SIGN_IN = 123;
    private static final int RC_FINISH_Main =367;
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_FINISH_Main) {
            logout();
            Log.e("finish main","now");
        }
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                FirebaseUser user = mAuth.getCurrentUser();
                // set Database
                Log.e("task","kk");
                DatabaseReference mDatabase =  FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                mDatabase.child("username").setValue(user.getDisplayName());
                mDatabase.child("Email").setValue(user.getEmail());
                Task<DataSnapshot> task=mDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            if (!task.getResult().child("score").exists())
                                mDatabase.child("score").setValue(1000);
                            if (!task.getResult().child("icon").exists())
                                mDatabase.child("icon").setValue("icon1.png");
                            if (!task.getResult().child("sun").exists())
                                mDatabase.child("sun").setValue(0);
                            if (!task.getResult().child("fertilizer").exists())
                                mDatabase.child("fertilizer").setValue(0);
                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                        }
                    }
                });

                Log.e("start main","now");
                showSnackbar(R.string.welcome);
                requestPermissionsIfNecessary(this,new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                showSnackbar(R.string.unknown_error);
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

    private void login() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                new AuthUI.IdpConfig.EmailBuilder().build()))
                        .build(),
                RC_SIGN_IN);
    }

    private void logout(){

        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        FirebaseListener.setcurrentActivityNow(null);
                        login();
                    }
                });
    }

    //Authen System features
    private void showSnackbar(@StringRes int errorMessageRes) {
        Toast.makeText(getApplicationContext(), errorMessageRes, Toast.LENGTH_SHORT).show();
    }
    private final static int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    public void requestPermissionsIfNecessary(Activity c, String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(c, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    c,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
        else{
            FirebaseListener.addMsgListener(this);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (isReady()) {
                        Intent intent = new Intent(currentActivity, MainActivity.class);
                        startActivityForResult(intent, RC_FINISH_Main);
                        cancel();
                    }
                }
            }, 0,500);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        requestPermissionsIfNecessary(this, permissions);

    }
}