package com.tecnologiasmoviles.iua.fitmusic.view;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tecnologiasmoviles.iua.fitmusic.BuildConfig;
import com.tecnologiasmoviles.iua.fitmusic.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ContainerActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "ContainerActivity";

    private LastRaceFragment lastRaceFragment;
    private NewRaceFragment newRaceFragment;
    private RacesListFragment racesListFragment;

    private static final String APP_IS_OPENED_KEY = "app_is_opened";
    private static final String IS_RUNNING_KEY = "is_running";

    CallbackManager callbackManager;

    private SharedPreferences sharedPref;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

//        FacebookSdk.sdkInitialize(this);
//        AppEventsLogger.activateApp(this);
//
//        callbackManager = CallbackManager.Factory.create();
//
//        LoginManager.getInstance().registerCallback(callbackManager,
//                new FacebookCallback<LoginResult>() {
//                    @Override
//                    public void onSuccess(LoginResult loginResult) {
//                        // App code
//                        Log.d(LOG_TAG, "Result: " + loginResult);
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        // App code
//                    }
//
//                    @Override
//                    public void onError(FacebookException exception) {
//                        // App code
//                        Log.d(LOG_TAG, "Result: " + exception);
//                    }
//                });

        BottomNavigationView mMainNav = findViewById(R.id.mainNav);

        lastRaceFragment = new LastRaceFragment();
        newRaceFragment = new NewRaceFragment();
        racesListFragment = new RacesListFragment();

        setFragment(lastRaceFragment);

        mMainNav.setOnNavigationItemSelectedListener(this);

        saveAppIsOpenedFromSharedPreferences(true);

        sharedPref = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(ContainerActivity.this, "(" + location.getLatitude() + ", " + location.getLongitude() + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.icon_last_race:
                setFragment(lastRaceFragment);
                return true;
            case R.id.icon_new_race:
                setFragment(newRaceFragment);
                return true;
            case R.id.icon_list_races:
                setFragment(racesListFragment);
                return true;
            default:
                return false;
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFrame, fragment);
        transaction.commit();
    }

    private void saveAppIsOpenedFromSharedPreferences(boolean isOpened) {
        SharedPreferences sharedPref = getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(APP_IS_OPENED_KEY, isOpened);
        editor.apply();
    }

    private boolean readIsRunningFromSharedPreferences() {
        SharedPreferences sharedPref = getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        return sharedPref.getBoolean(
                IS_RUNNING_KEY,
                false);
    }

    private void saveIsRunningToSharedPreferences(boolean isRunning) {
        SharedPreferences sharedPref = getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean(IS_RUNNING_KEY, isRunning);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.d(LOG_TAG, "onBackPressed: Container Activity");

        boolean isRunning = readIsRunningFromSharedPreferences();
        Log.d(LOG_TAG, "isRunning: " + isRunning);

        if (isRunning) {
            AlertDialog dialogExitApplication = (AlertDialog) createDialogExitApplication();
            dialogExitApplication.show();
        } else {
            moveTaskToBack(true);
        }

    }

    public Dialog createDialogExitApplication() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_exit_application, null))
                // Add action buttons
                .setPositiveButton("Si", (dialog, id) -> {
//                        saveAppIsOpenedFromSharedPreferences(false);
                    saveIsRunningToSharedPreferences(false);
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        return builder.create();
    }
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        moveTaskToBack(true);
//    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "Container onStart");
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Container onPause");
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(IS_RUNNING_KEY)) {
            boolean isRunning = sharedPreferences.getBoolean(IS_RUNNING_KEY, false);
            Log.d(LOG_TAG, "isRunning: " + isRunning);
            if (isRunning) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locationListener);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locationListener);
                }
            }
        }
    }

}