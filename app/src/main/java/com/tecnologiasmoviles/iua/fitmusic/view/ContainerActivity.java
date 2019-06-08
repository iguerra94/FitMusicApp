package com.tecnologiasmoviles.iua.fitmusic.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.services.FitMusicForegroundService;
import com.tecnologiasmoviles.iua.fitmusic.utils.Constants;
import com.tecnologiasmoviles.iua.fitmusic.utils.FirebaseRefs;
import com.tecnologiasmoviles.iua.fitmusic.utils.LocationService;
import com.tecnologiasmoviles.iua.fitmusic.utils.MediaPlayerManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.dialogs.DialogFactory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class ContainerActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

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
        boolean isRunning = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);

        if (isRunning) {
            AlertDialog dialogExitApplication = (AlertDialog) DialogFactory.getInstance().getExitApplicationDialog().create(this, R.layout.dialog_exit_application);
            dialogExitApplication.show();
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                    locationCallback = LocationService.buildLocationCallback(ContainerActivity.this, refKey);

                    LocationService.getFusedLocationProviderClientInstance(this).requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                } else {
                    locationRequest = LocationService.buildLocationRequest();
                    locationCallback = LocationService.buildLocationCallback(ContainerActivity.this, refKey);

                    LocationService.getFusedLocationProviderClientInstance(this).requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        boolean isRunning = SharedPrefsManager.getInstance(this).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);

        if (!isRunning) {
            if (locationCallback != null) {
                LocationService.getFusedLocationProviderClientInstance(this).removeLocationUpdates(locationCallback);
                locationCallback = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FitMusicForegroundService.stopForegroundServiceIntent(this);
        MediaPlayerManager.finishMediaPlayer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String url;
        if (item.getItemId() == R.id.tos) {
            url = Constants.URL.URL_TERMS;
            openWebPage(url);
        }
        if (item.getItemId() == R.id.privacy) {
            url = Constants.URL.URL_PRIVACY;
            openWebPage(url);
        }
        return super.onOptionsItemSelected(item);
    }

    public void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}