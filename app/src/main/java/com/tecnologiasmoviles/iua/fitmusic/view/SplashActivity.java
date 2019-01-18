package com.tecnologiasmoviles.iua.fitmusic.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;

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

    private String[] permissions = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INTERNET
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        try {
            File file = new File(getFilesDir(), "races_data.json");
//            file.delete();
            createFileIfNotExists(file);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        // Reset all race SharedPrefsKeys
        SharedPrefsManager.initRaceSharedPrefsKeys(this);

//        removeRegistrationTokenFromSharedPreferences();
//        Get token

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!arePermissionsEnabled()){
                requestMultiplePermissions();
            } else {
                boolean appIsOpened = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.APP_IS_OPENED_KEY);

                if (appIsOpened) {
                    Log.d(LOG_TAG, "appIsOpened: " + appIsOpened);
                    goToMainActivity(this.getCurrentFocus());
                }
            }
        }
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
                    SharedPrefsManager.getInstance(this).saveString(SharedPrefsKeys.REGISTRATION_TOKEN_KEY, newToken);
                });
    }

    private String readRegistrationTokenFromExternalStorage() {
        String registrationToken = "";

        try {
            File regTokenFile = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "registration_token.txt");

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
        usersDBRef.child(actualRegistationToken).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long last_race_date_miliseconds = dataSnapshot.child("last_race_date_miliseconds").getValue(Long.class);
                usersDBRef.child(updatedRegistationToken+"/last_race_date_miliseconds").setValue(last_race_date_miliseconds);
                usersDBRef.child(actualRegistationToken).setValue(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
                                .setMessage("The permissions are not granted!")
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

            boolean appIsOpened = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.APP_IS_OPENED_KEY);

            if (appIsOpened) {
                Log.d(LOG_TAG, "appIsOpened: " + appIsOpened);
                goToMainActivity(this.getCurrentFocus());
            }
        }
    }

}