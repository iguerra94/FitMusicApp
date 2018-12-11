package com.tecnologiasmoviles.iua.fitmusic.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tecnologiasmoviles.iua.fitmusic.BuildConfig;
import com.tecnologiasmoviles.iua.fitmusic.R;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String LOG_TAG = SplashActivity.class.getSimpleName();

    private static final String REGISTRATION_TOKEN_KEY = "registration_token";
    private static final String APP_IS_OPENED_KEY = "app_is_opened";

    private String[] permissions = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    };

    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {
            File file = new File(getFilesDir(), "races_data.json");
            createFileIfNotExists(file);
            initializeIdSongToSharedPreferences(getString(R.string.id_song_key));
            initializeIsRunningVariableToSharedPreferences("is_running");
        } catch (IOException e1) {
            e1.printStackTrace();
        }

//        removeRegistrationTokenFromSharedPreferences();
//        Get token

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!arePermissionsEnabled()){
                requestMultiplePermissions();
//                permissions granted, continue flow normally
//                String registrationToken = readRegistrationTokenFromSharedPreferences();
//                Log.d(LOG_TAG, "TOKEN LEIDO: " + registrationToken);
//
//                if (registrationToken.isEmpty()) {
//                    createRegistrationToken();
//                    subscribeClientToTopic();
//                }
//
//                boolean appIsOpened = readAppIsOpenedFromSharedPreferences();
//
//                if (appIsOpened) {
//                    Log.d(LOG_TAG, "appIsOpened: " + appIsOpened);
//                    goToMainActivity(this.getCurrentFocus());
//                }
            } else {
                boolean appIsOpened = readAppIsOpenedFromSharedPreferences();

                if (appIsOpened) {
                    Log.d(LOG_TAG, "appIsOpened: " + appIsOpened);
                    goToMainActivity(this.getCurrentFocus());
                }
            }
        }

//        boolean canRead = askReadPermission();
//        Log.d(LOG_TAG, "canRead: " + canRead);
//
//        if (canRead) {
//        }
    }

    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, ContainerActivity.class);
        // set the new task and clear flags
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static void createFileIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();

            JSONArray array = new JSONArray();

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(array.toString());
                Log.d(LOG_TAG, array.toString());
                writer.close();
            }
        }
    }

    private void initializeIdSongToSharedPreferences(String key) {
        SharedPreferences sharedPref = getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(key, -1);
        editor.apply();
    }

    private void initializeIsRunningVariableToSharedPreferences(String key) {
        SharedPreferences sharedPref = getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(key, false);
        editor.apply();
    }

    private void createRegistrationToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(instanceIdResult -> {
                    String newToken = instanceIdResult.getToken();
                    String lastToken = readRegistrationTokenFromExternalStorage();

                    Log.d(LOG_TAG, "Last Token: " + lastToken);

                    if (lastToken.isEmpty()) { // Usuario instala la app por primera vez
                        saveRegistrationTokenToFirebaseDatabase(newToken);
                    } else {
                        updateRegistrationTokenInFirebaseDatabase(lastToken, newToken);
                    }

                    saveRegistrationTokenToExternalStorage(newToken);
                    saveRegistrationTokenToSharedPreferences(newToken);
                });
    }

    private void saveRegistrationTokenToSharedPreferences(String registrationToken) {
        SharedPreferences sharedPref = getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString(REGISTRATION_TOKEN_KEY, registrationToken);
        editor.apply();

        Log.d(LOG_TAG, "Save to SP success!");
    }

    private String readRegistrationTokenFromExternalStorage() {
        String registrationToken = "";

        try {
            File regTokenFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "registration_token.txt");

            if (regTokenFile.exists()) {
                FileInputStream fis = new FileInputStream(regTokenFile);

                if (fis != null) {
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader buff = new BufferedReader(isr);

                    String line = null;
                    while ((line = buff.readLine()) != null) {
                        registrationToken += line;
                    }
                    fis.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return registrationToken;
    }

    private void saveRegistrationTokenToExternalStorage(String registrationToken) {
        File regTokenFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "registration_token.txt");

        FileOutputStream outputStream = null;
        try {
            regTokenFile.delete();
            regTokenFile.createNewFile();

            //second argument of FileOutputStream constructor indicates whether to append or create new file if one exists
            outputStream = new FileOutputStream(regTokenFile);

            outputStream.write(registrationToken.getBytes());
            outputStream.flush();
            outputStream.close();

            Log.d(LOG_TAG, "File write success!");
        } catch (Exception e) {
            Log.d(LOG_TAG, "File write error!");
            e.printStackTrace();
        }

    }

    private void saveRegistrationTokenToFirebaseDatabase(String registrationToken) {
        // get users ref in firebase database
        DatabaseReference usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");
        // save registration token in firebase database with child last_race_timestamp set to value 0 (zero).
        usersDBRef.child(registrationToken+"/last_race_date_miliseconds").setValue(0);
        Log.d(LOG_TAG, "Save To Firebase Success!");
    }

    private void updateRegistrationTokenInFirebaseDatabase(String actualRegistationToken, String updatedRegistationToken) {
        // get users ref in firebase database
        DatabaseReference usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");
        // save registration token in firebase database with child last_race_timestamp set to value 0 (zero).
        usersDBRef.child(actualRegistationToken+"/last_race_date_miliseconds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    long last_race_date_miliseconds = dataSnapshot.getValue(Long.class);
                    usersDBRef.child(updatedRegistationToken+"/last_race_date_miliseconds").setValue(last_race_date_miliseconds);
                    usersDBRef.child(actualRegistationToken).setValue(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean readAppIsOpenedFromSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        return sharedPref.getBoolean(
                APP_IS_OPENED_KEY,
                false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean arePermissionsEnabled(){
        for(String permission : permissions){
            if(checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestMultiplePermissions(){
        List<String> remainingPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                remainingPermissions.add(permission);
            }
        }
        requestPermissions(remainingPermissions.toArray(new String[remainingPermissions.size()]), 101);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 101){
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    if(shouldShowRequestPermissionRationale(permissions[i])){
                        new AlertDialog.Builder(this)
                                .setMessage("Your error message here")
                                .setPositiveButton("Allow", (dialog, which) -> requestMultiplePermissions())
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .create()
                                .show();
                    }
                    return;
                }
            }
            //all is good, continue flow
            createRegistrationToken();

            boolean appIsOpened = readAppIsOpenedFromSharedPreferences();

            if (appIsOpened) {
                Log.d(LOG_TAG, "appIsOpened: " + appIsOpened);
                goToMainActivity(this.getCurrentFocus());
            }
        }
    }

}