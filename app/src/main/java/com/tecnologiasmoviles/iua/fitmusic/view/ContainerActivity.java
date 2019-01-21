package com.tecnologiasmoviles.iua.fitmusic.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.android.PolyUtil;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;
import com.tecnologiasmoviles.iua.fitmusic.utils.FirebaseRefs;
import com.tecnologiasmoviles.iua.fitmusic.utils.LocationService;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        Punto p1 = new Punto(UUID.randomUUID(), -31.419004,-64.197000);
        Punto p2 = new Punto(UUID.randomUUID(), -31.418970,-64.195476);
        Punto p3 = new Punto(UUID.randomUUID(), -31.418782,-64.194099);

        List<Punto> puntos = new ArrayList<>();
        puntos.add(p1);
        puntos.add(p2);
        puntos.add(p3);

        new EncodeListPointsAsyncTask().execute(puntos);

//        new TestRaceAsyncTask().execute();
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

        boolean isRunning = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);

        if (!isRunning) {
            // Reset all race SharedPrefsKeys
            SharedPrefsManager.initRaceSharedPrefsKeys(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "Container onPause");
        SharedPrefsManager.getInstance(this).getSharedPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPrefsKeys.IS_RUNNING_KEY)) {
            boolean isRunning = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);
            Log.d(LOG_TAG, "isRunning: " + isRunning);
            if (isRunning) {
                // Push new race in firebase and get the key of the race
                String refKey = FirebaseRefs.getRacesRef().push().getKey();
                SharedPrefsManager.getInstance(this).saveString(SharedPrefsKeys.RACE_CURRENT_FIREBASE_KEY, refKey);

                LocationRequest locationRequest;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    locationRequest = LocationService.buildLocationRequest();
                    locationCallback = LocationService.buildLocationCallback(ContainerActivity.this, refKey, "");

                    LocationService.getFusedLocationProviderClientInstance(this).requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                } else {
                    locationRequest = LocationService.buildLocationRequest();
                    locationCallback = LocationService.buildLocationCallback(ContainerActivity.this, refKey, "");

                    LocationService.getFusedLocationProviderClientInstance(this).requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        boolean isRunning = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);
        Log.d(LOG_TAG, "onStop isRunning: " + isRunning);

        if (!isRunning) {
            if (locationCallback != null) {
                LocationService.getFusedLocationProviderClientInstance(this).removeLocationUpdates(locationCallback);
                locationCallback = null;
            }
        }

        Log.d(LOG_TAG, "onStop Container Activity");
    }

//    private class TestRaceAsyncTask extends AsyncTask<Void, Void, Void> {
//        List<Integer> distancias = new ArrayList<>();
//        List<Integer> duraciones = new ArrayList<>();
//
//        @Override
//        protected void onPreExecute() {
//            SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, 0);
//            SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, 0);
//            SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, 0);
//            SharedPrefsManager.getInstance(ContainerActivity.this).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, false);
//
//            distancias.add(529);
//            distancias.add(216);
//            distancias.add(258);
//            distancias.add(353);
//            distancias.add(551);
//
//            duraciones.add(427000);
//            duraciones.add(612000);
//            duraciones.add(802000);
//            duraciones.add(1075000);
//            duraciones.add(1465000);
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            for (int i = 0; i < distancias.size(); i++) {
//                long distanceAccumulated = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
//                long currentDistance = distancias.get(i);
//                long newDistance = distanceAccumulated + currentDistance;
//
//                long currentDuration = duraciones.get(i);
//
//                float distanceKms = (newDistance / 1000f);
//
//                SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, newDistance);
//
//                Log.d(LOG_TAG, "ITERACION: " + (i+1));
//
//                if (newDistance < 1000) {
//                    Log.d(LOG_TAG, "DISTANCIA: " + String.format("%.2f", distanceKms) + " km, DURACION: " + TimeUtils.milliSecondsToTimer(currentDuration));
//                }
//
//                if (newDistance >= 1000) { // Distance is greater than or equal to 1 km.
//                    long rythmn = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);
//
//                    if (rythmn == 0) {// First time to meausure rythhmn
//                        float distanceInKms = (newDistance / 1000f);
//                        distanceInKms = Math.round(distanceInKms*100f)/100f;
//
//                        long newRythmn = (int) (currentDuration / distanceInKms);
//
//                        Log.d(LOG_TAG, "DISTANCIA: " + String.format("%.2f", distanceInKms) + " km, DURACION: " + TimeUtils.milliSecondsToTimer(currentDuration) + ", RITMO TOTAL: " + TimeUtils.milliSecondsToTimer(newRythmn));
//
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, newRythmn);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY, currentDuration);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, newDistance);
//                    }
//
//                    long lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);
//
//                    if (newDistance >= lastUpdatedRythmnDistance + 500) {
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, true);
//                    }
//
//                    boolean shouldMeasureRythmn = SharedPrefsManager.getInstance(ContainerActivity.this).readBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY);
//
//                    if (shouldMeasureRythmn) {
//                        long lastUpdatedRythmnTime = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY);
//                        long deltaTime = currentDuration - lastUpdatedRythmnTime;
//
//                        lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);
//                        float distanceInKms = ((newDistance - lastUpdatedRythmnDistance) / 1000f);
//                        distanceInKms = Math.round(distanceInKms*100f)/100f;
//
//                        long currentRythmn = (int) (deltaTime / distanceInKms);
//
//                        long newRythmn = (int) ((rythmn + currentRythmn) / 2);
//
//                        Log.d(LOG_TAG, "DISTANCIA: " + String.format("%.2f", newDistance/1000f) + " km, DELTA DISTANCIA: " + String.format("%.2f", distanceInKms) + " km, DELTA DURACION: " + TimeUtils.milliSecondsToTimer(deltaTime) + ", DURACION: " + TimeUtils.milliSecondsToTimer(currentDuration) + ", RITMO ACTUAL: " + TimeUtils.milliSecondsToTimer(currentRythmn) + ", RITMO TOTAL: " + TimeUtils.milliSecondsToTimer(newRythmn));
//
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, newRythmn);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY, currentDuration);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, newDistance);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, false);
//                    } else {
//                        if (rythmn > 0) {
//                            float distanceInKms = (newDistance / 1000f);
//                            Log.d(LOG_TAG, "DISTANCIA: " + String.format("%.2f", distanceInKms) + " km, DURACION: " + TimeUtils.milliSecondsToTimer(currentDuration) + ", RITMO TOTAL: " + TimeUtils.milliSecondsToTimer(rythmn));
//                        }
//                    }
//
//                }
//            }
//            return null;
//        }
//
//    }

    //    private class TestRaceAsyncTask extends AsyncTask<Void, Void, Void> {
//        List<Integer> distancias = new ArrayList<>();
//        List<Integer> duraciones = new ArrayList<>();
//
//        @Override
//        protected void onPreExecute() {
//            SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, 0);
//            SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, 0);
//            SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, 0);
//            SharedPrefsManager.getInstance(ContainerActivity.this).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, false);
//
//            distancias.add(529);
//            distancias.add(216);
//            distancias.add(258);
//            distancias.add(353);
//            distancias.add(551);
//
//            duraciones.add(427000);
//            duraciones.add(612000);
//            duraciones.add(802000);
//            duraciones.add(1075000);
//            duraciones.add(1465000);
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            for (int i = 0; i < distancias.size(); i++) {
//                long distanceAccumulated = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
//                long currentDistance = distancias.get(i);
//                long newDistance = distanceAccumulated + currentDistance;
//
//                long currentDuration = duraciones.get(i);
//
//                float distanceKms = (newDistance / 1000f);
//
//                SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, newDistance);
//
//                Log.d(LOG_TAG, "ITERACION: " + (i+1));
//
//                if (newDistance < 1000) {
//                    Log.d(LOG_TAG, "DISTANCIA: " + String.format("%.2f", distanceKms) + " km, DURACION: " + TimeUtils.milliSecondsToTimer(currentDuration));
//                }
//
//                if (newDistance >= 1000) { // Distance is greater than or equal to 1 km.
//                    long rythmn = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);
//
//                    if (rythmn == 0) {// First time to meausure rythhmn
//                        float distanceInKms = (newDistance / 1000f);
//                        distanceInKms = Math.round(distanceInKms*100f)/100f;
//
//                        long newRythmn = (int) (currentDuration / distanceInKms);
//
//                        Log.d(LOG_TAG, "DISTANCIA: " + String.format("%.2f", distanceInKms) + " km, DURACION: " + TimeUtils.milliSecondsToTimer(currentDuration) + ", RITMO TOTAL: " + TimeUtils.milliSecondsToTimer(newRythmn));
//
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, newRythmn);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY, currentDuration);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, newDistance);
//                    }
//
//                    long lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);
//
//                    if (newDistance >= lastUpdatedRythmnDistance + 500) {
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, true);
//                    }
//
//                    boolean shouldMeasureRythmn = SharedPrefsManager.getInstance(ContainerActivity.this).readBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY);
//
//                    if (shouldMeasureRythmn) {
//                        long lastUpdatedRythmnTime = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY);
//                        long deltaTime = currentDuration - lastUpdatedRythmnTime;
//
//                        lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(ContainerActivity.this).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);
//                        float distanceInKms = ((newDistance - lastUpdatedRythmnDistance) / 1000f);
//                        distanceInKms = Math.round(distanceInKms*100f)/100f;
//
//                        long currentRythmn = (int) (deltaTime / distanceInKms);
//
//                        long newRythmn = (int) ((rythmn + currentRythmn) / 2);
//
//                        Log.d(LOG_TAG, "DISTANCIA: " + String.format("%.2f", newDistance/1000f) + " km, DELTA DISTANCIA: " + String.format("%.2f", distanceInKms) + " km, DELTA DURACION: " + TimeUtils.milliSecondsToTimer(deltaTime) + ", DURACION: " + TimeUtils.milliSecondsToTimer(currentDuration) + ", RITMO ACTUAL: " + TimeUtils.milliSecondsToTimer(currentRythmn) + ", RITMO TOTAL: " + TimeUtils.milliSecondsToTimer(newRythmn));
//
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, newRythmn);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY, currentDuration);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, newDistance);
//                        SharedPrefsManager.getInstance(ContainerActivity.this).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, false);
//                    } else {
//                        if (rythmn > 0) {
//                            float distanceInKms = (newDistance / 1000f);
//                            Log.d(LOG_TAG, "DISTANCIA: " + String.format("%.2f", distanceInKms) + " km, DURACION: " + TimeUtils.milliSecondsToTimer(currentDuration) + ", RITMO TOTAL: " + TimeUtils.milliSecondsToTimer(rythmn));
//                        }
//                    }
//
//                }
//            }
//            return null;
//        }
//
//    }

    public String createUrl(LatLng point) {
        return point.latitude + "," + point.longitude;
    }

    public String createUrlWaypoints(List<LatLng> points) {
        StringBuilder url = new StringBuilder();
        for (LatLng point: points) {
            url.append("|").append(point.latitude).append(",").append(point.longitude);
        }
        return url.toString();
    }

    private class EncodeListPointsAsyncTask extends AsyncTask<List<Punto>, Void, String> {

        @Override
        protected void onPreExecute() {
            SharedPrefsManager.getInstance(ContainerActivity.this).saveString(SharedPrefsKeys.ENCODED_POLYLINE_KEY, "");
        }

        @Override
        protected String doInBackground(List<Punto>... puntos) {
            List<LatLng> latLngList = new ArrayList<>();

            for (Punto p : puntos[0]) {
                latLngList.add(new LatLng(p.getLat(), p.getLon()));
            }

            AndroidNetworking.get("https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&waypoints=optimize:true{waypoints}&mode={mode}&key={key}")
                    .addPathParameter("origin", createUrl(latLngList.get(0)))
                    .addPathParameter("destination", createUrl(latLngList.get(latLngList.size()-1)))
                    .addPathParameter("waypoints", createUrlWaypoints(latLngList.subList(1, latLngList.size()-1)))
                    .addPathParameter("mode", "walking")
                    .addPathParameter("key", "AIzaSyBe4BWfH1NlOgidZQ8Spbq02GFSLe5BHv0")
                    .setTag("test")
                    .setPriority(Priority.HIGH)
                    .build()
                    .getAsJSONObject(new JSONObjectRequestListener() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject routes = response.getJSONArray("routes").getJSONObject(0);
                                JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");

                                String ENCODED_POLYLINE = overviewPolyline.getString("points");

                                SharedPrefsManager.getInstance(ContainerActivity.this).saveString(SharedPrefsKeys.ENCODED_POLYLINE_KEY, ENCODED_POLYLINE);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.d(LOG_TAG, "ERROR: " + anError.getErrorBody());
                        }
                    });

            String ENCODED_POLYLINE;

            do {
                ENCODED_POLYLINE = SharedPrefsManager.getInstance(ContainerActivity.this).readString(SharedPrefsKeys.ENCODED_POLYLINE_KEY);
            } while (ENCODED_POLYLINE.length() == 0);

            return ENCODED_POLYLINE;
        }

        @Override
        protected void onPostExecute(String ENCODED_POLYLINE) {
            Log.d(LOG_TAG, ENCODED_POLYLINE);
        }

    }

}