package com.tecnologiasmoviles.iua.fitmusic.utils.asyncTasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.tecnologiasmoviles.iua.fitmusic.utils.LocationService;
import com.tecnologiasmoviles.iua.fitmusic.utils.RaceUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;
import com.tecnologiasmoviles.iua.fitmusic.view.RaceRegisteredActivity;

import java.util.Date;

import androidx.appcompat.app.AlertDialog;

/*
public class RegisterRaceAsyncTask extends AsyncTask<Void, String, Void> {

    private Context context;

    public RegisterRaceAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialogRegisterRaceQuestion.dismiss();
        dialogRegisteringRace = (AlertDialog) DialogUtils.createDialogRegisteringRace();
        dialogRegisteringRace.setCancelable(false);
        dialogRegisteringRace.show();

        if (LocationService.getLocationCallback() != null) {
            LocationService.getFusedLocationProviderClientInstance(context).removeLocationUpdates(LocationService.getLocationCallback());
        }

//            long lastUpdateTimeMs = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.LAST_UPDATE_TIME_MS_KEY);
//
//            if (lastUpdateTimeMs == 0) {
//                lastUpdateTimeMs = getInitialTime();
//            }
//
//            long currentTimeMs = new Date().getTime();
//            long deltaTimeMs = currentTimeMs - lastUpdateTimeMs;
//
//            if (deltaTimeMs >= 10000) {
//                SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY, true);
//            }

        long raceCurrentDistance = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);

//        if (raceCurrentDistance >= 500) {
        long currentRaceTime = new Date().getTime();

        long rythmnAccumulated = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);

        long lastUpdatedRythmnTime = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY);
        long deltaTime = currentRaceTime - lastUpdatedRythmnTime;

        long lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);
        float distanceInKms = ((raceCurrentDistance - lastUpdatedRythmnDistance) / 1000f);
        distanceInKms = Math.round(distanceInKms * 100f) / 100f;

        long newRythmn = 0;
        long currentRythmn = 0;
        if (raceCurrentDistance >= 500) {
            currentRythmn = RaceUtils.measureCurrentRythmn(deltaTime, distanceInKms);
            newRythmn = RaceUtils.measureAverageRythmn(rythmnAccumulated, currentRythmn);
        }

        RaceUtils.updateRaceSharedPrefsOptions(context, newRythmn, currentRaceTime, raceCurrentDistance);

        SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_GETTING_LAST_SECTION_POLYLINE_KEY, true);

        RaceUtils.setSectionData(context, raceCurrentDistance, newRythmn, false);

        RaceUtils.determineCurrentFastestSection(context, currentRythmn, false);
//        }

    }

    @Override
    protected Void doInBackground(Void... voids) {
        SharedPrefsManager.getInstance(context).saveString(SharedPrefsKeys.RACE_DURATION_KEY, newRaceDurationTextView.getText().toString());

        String registrationToken = SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.REGISTRATION_TOKEN_KEY);
        saveRaceDateMsInFirebaseDatabase(registrationToken);

//            boolean isGettingLastPoint;
//            boolean isGettingLastSectionPolyline;
//
//            do {
//                isGettingLastPoint = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY);
//                isGettingLastSectionPolyline = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_SECTION_POLYLINE_KEY);
//            } while (isGettingLastPoint || isGettingLastSectionPolyline);

        boolean isGettingLastSectionPolyline;

        do {
            isGettingLastSectionPolyline = SharedPrefsManager.getInstance(context).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_SECTION_POLYLINE_KEY);
        } while (isGettingLastSectionPolyline);


        publishProgress("REGISTER_RACE");

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (values[0].equals("REGISTER_RACE")) {
            registerRace();
            finishRace();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        dialogRegisteringRace.dismiss();
        Intent raceRegisteredIntent = new Intent(context, RaceRegisteredActivity.class);
        context.startActivity(raceRegisteredIntent);
    }

}*/
