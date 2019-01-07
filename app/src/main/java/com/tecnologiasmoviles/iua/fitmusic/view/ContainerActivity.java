package com.tecnologiasmoviles.iua.fitmusic.view;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ContainerActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = ContainerActivity.class.getSimpleName();

    private LastRaceFragment lastRaceFragment;
    private NewRaceFragment newRaceFragment;
    private RacesListFragment racesListFragment;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        BottomNavigationView mMainNav = findViewById(R.id.mainNav);

        lastRaceFragment = new LastRaceFragment();
        newRaceFragment = new NewRaceFragment();
        racesListFragment = new RacesListFragment();

        setFragment(lastRaceFragment);

        mMainNav.setOnNavigationItemSelectedListener(this);

        SharedPrefsManager.getInstance(this).saveBoolean(SharedPrefsKeys.APP_IS_OPENED_KEY, true);

        SharedPrefsManager.getInstance(this).getSharedPrefs().registerOnSharedPreferenceChangeListener(this);

        AndroidNetworking.initialize(getApplicationContext());
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

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.d(LOG_TAG, "onBackPressed: Container Activity");

        boolean isRunning = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);
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
                    SharedPrefsManager.getInstance(this).saveBoolean(SharedPrefsKeys.IS_RUNNING_KEY, false);
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        return builder.create();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "Container onStart");
        SharedPrefsManager.getInstance(this).getSharedPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Container onPause");
        SharedPrefsManager.getInstance(this).getSharedPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(20000);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Punto p = new Punto(UUID.randomUUID(), locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                List<Punto> puntos = SharedPrefsManager.getInstance(ContainerActivity.this).readListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY);
                puntos.add(p);
                SharedPrefsManager.getInstance(ContainerActivity.this).saveListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY, puntos);

                Toast.makeText(ContainerActivity.this, "Punto " + puntos.size() + ": " + p.toString(), Toast.LENGTH_SHORT).show();

                if (puntos.size() > 1) {
                    String origin = puntos.get(puntos.size()-1).getLat() + "," + puntos.get(puntos.size()-1).getLon();
                    String destination = puntos.get(puntos.size()-2).getLat() + "," + puntos.get(puntos.size()-2).getLon();

                    AndroidNetworking.get("https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&mode={mode}&key={key}")
                            .addPathParameter("origin", origin)
                            .addPathParameter("destination", destination)
                            .addPathParameter("mode", "walking")
                            .addPathParameter("key", "AIzaSyA15bpgte2SVrhimPmJJKF65rDo01lPP0E")
                            .setTag("test")
                            .setPriority(Priority.HIGH)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONObject routes = response.getJSONArray("routes").getJSONObject(0);
                                        JSONObject legs = (JSONObject) routes.getJSONArray("legs").get(0);
                                        JSONObject distance = legs.getJSONObject("distance");

                                        long distanceAccumulated = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
                                        long newDistance = distanceAccumulated + distance.getLong("value");

                                        float rythmn = SharedPrefsManager.getInstance(ContainerActivity.this).readFloat(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);

                                        long initialTime = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY);
                                        long currentTime = new Date().getTime();
                                        long currentDuration = currentTime - initialTime;

                                        float minutes = (currentDuration % (1000*60*60)) / (1000*60);

                                        float newRythmn;
                                        if (puntos.size() == 2) {
                                            newRythmn = minutes/newDistance;
                                        } else {
                                            newRythmn = (rythmn + (minutes/newDistance)) / 2;
                                        }

                                        Toast.makeText(ContainerActivity.this, "Distance: " + newDistance + ", Ritmo anterior: " + rythmn + ", Ritmo actual: " + newRythmn + ", Tiempo actual: " + TimeUtils.milliSecondsToTimer(currentDuration), Toast.LENGTH_LONG).show();

                                        SharedPrefsManager.getInstance(ContainerActivity.this).saveFloat(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, newRythmn);
                                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, newDistance);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(ANError anError) {
                                    Log.d(LOG_TAG, "ERROR: " + anError.getErrorBody());
                                }
                            });
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPrefsKeys.IS_RUNNING_KEY)) {
            boolean isRunning = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);
            Log.d(LOG_TAG, "isRunning: " + isRunning);
            if (isRunning) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    SharedPrefsManager.getInstance(this).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, 0);

                    buildLocationRequest();
                    buildLocationCallback();

                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                } else {
                    SharedPrefsManager.getInstance(this).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, 0);

                    buildLocationRequest();
                    buildLocationCallback();

                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            } else {
                if (fusedLocationProviderClient != null) {
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    fusedLocationProviderClient = null;

//                    locationRequest = new LocationRequest();
//                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//                    locationRequest.setInterval(100);
//                    locationRequest.setFastestInterval(100);
//
//                    locationCallback = new LocationCallback() {
//                        @Override
//                        public void onLocationResult(LocationResult locationResult) {
//                            Punto p = new Punto(UUID.randomUUID(), locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
//                            List<Punto> puntos = readListPointsFromSharedPreferences();
//                            puntos.add(p);
//                            saveListPointsToSharedPreferences(puntos);
//
//                            Toast.makeText(ContainerActivity.this, "Punto " + puntos.size() + ": " + p.toString(), Toast.LENGTH_SHORT).show();
//
//                            if (puntos.size() > 1) {
//                                String origin = puntos.get(puntos.size()-1).getLat() + "," + puntos.get(puntos.size()-1).getLon();
//                                String destination = puntos.get(puntos.size()-2).getLat() + "," + puntos.get(puntos.size()-2).getLon();
//
//                                AndroidNetworking.get("https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&mode={mode}&key={key}")
//                                        .addPathParameter("origin", origin)
//                                        .addPathParameter("destination", destination)
//                                        .addPathParameter("mode", "walking")
//                                        .addPathParameter("key", "AIzaSyA15bpgte2SVrhimPmJJKF65rDo01lPP0E")
//                                        .setTag("test")
//                                        .setPriority(Priority.MEDIUM)
//                                        .build()
//                                        .getAsJSONObject(new JSONObjectRequestListener() {
//                                            @Override
//                                            public void onResponse(JSONObject response) {
//                                                try {
//                                                    JSONObject routes = response.getJSONArray("routes").getJSONObject(0);
//                                                    JSONObject legs = (JSONObject) routes.getJSONArray("legs").get(0);
//                                                    JSONObject distance = legs.getJSONObject("distance");
//                                                    long distanceAccumulated = readDistanceFromSharedPreferences();
//                                                    long newDistance = distanceAccumulated + distance.getLong("value");
//
//                                                    Toast.makeText(ContainerActivity.this, "Total distance: " + newDistance, Toast.LENGTH_SHORT).show();
//
//                                                    saveDistanceToSharedPreferences(newDistance);
//
//                                                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//
//                                            @Override
//                                            public void onError(ANError anError) {
//                                                Log.d(LOG_TAG, "ERROR: " + anError.getErrorBody());
//                                            }
//                                        });
//                            }
//                        }
//                    };
//
//                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//                    fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            }
        }
    }

}