package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;
import com.tecnologiasmoviles.iua.fitmusic.view.ContainerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LocationService {

    private static final String LOG_TAG = LocationService.class.getSimpleName();

    private static FusedLocationProviderClient fusedLocationProviderClient;

    public static FusedLocationProviderClient getFusedLocationProviderClientInstance(Context context) {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }
        return fusedLocationProviderClient;
    }

    public static LocationRequest buildLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(20000);
        return locationRequest;
    }

    public static LocationCallback buildLocationCallback(Context context, String key, String duration) {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Punto> puntos = SharedPrefsManager.getInstance(context).readListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY);

                Punto p = new Punto(UUID.randomUUID(), locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());

                // First Section with zero points
                if (puntos.size() == 0) {
                    Tramo section = new Tramo();
                    List<Punto> listPoints = new ArrayList<>();

                    section.setIdTramo(UUID.randomUUID());

                    p.setIsStartingRacePoint(true);
                    listPoints.add(p);

                    SharedPrefsManager.getInstance(context).saveSectionObject(SharedPrefsKeys.RACE_ACTUAL_SECTION_KEY, section);
                    SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY, listPoints);
                    SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY, listPoints);
                } else if (puntos.size() == 1) {
                    puntos.add(p);
                    SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY, puntos);

                    List<Punto> listPoints = SharedPrefsManager.getInstance(context).readListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY);
                    listPoints.add(p);
                    SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY, listPoints);
                } else {
                    String origin = puntos.get(puntos.size() -2).getLat() + "," + puntos.get(puntos.size() -2).getLon();
                    String destination = puntos.get(puntos.size() -1).getLat() + "," + puntos.get(puntos.size() -1).getLon();

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

                                    long distanceAccumulated = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
                                    long currentDistance = distance.getLong("value");
                                    long newDistance = distanceAccumulated + currentDistance;

                                    if (SharedPrefsManager.getInstance(context).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY)) {
                                        SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY, false);
                                    }

                                    SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, newDistance);

                                    long currentRaceTime = new Date().getTime();

                                    SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_TIME_KEY, currentRaceTime);

                                    long lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);

                                    if (newDistance >= lastUpdatedRythmnDistance + 500) {
                                        SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, true);
                                    }

                                    boolean shouldMeasureRythmn = SharedPrefsManager.getInstance(context).readBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY);

                                    if (shouldMeasureRythmn) {
                                        long rythmn = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);

                                        if (rythmn == 0) {// First time to meausure rythhmn
                                            long initialRaceTime = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY);

                                            long deltaTime = currentRaceTime - initialRaceTime;
                                            float distanceInKms = (newDistance / 1000f);
                                            distanceInKms = Math.round(distanceInKms * 100f) / 100f;

                                            long newRythmn = (int) (deltaTime / distanceInKms);

                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, newRythmn);
                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY, currentRaceTime);
                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, newDistance);
                                            SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, false);

                                            Tramo tramo = SharedPrefsManager.getInstance(context).readSectionObject(SharedPrefsKeys.RACE_ACTUAL_SECTION_KEY);
                                            List<Punto> listPoints = SharedPrefsManager.getInstance(context).readListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY);

                                            tramo.setDistanciaTramo(newDistance);
                                            tramo.setRitmoTramo(newRythmn);
                                            tramo.setPuntosTramo(listPoints);

                                            new EncodeListPointsAsyncTask(context).execute(listPoints);

                                            List<Tramo> tramos = SharedPrefsManager.getInstance(context).readListSections(SharedPrefsKeys.RACE_SECTIONS_KEY);
                                            tramos.add(tramo);

                                            SharedPrefsManager.getInstance(context).saveListSections(SharedPrefsKeys.RACE_SECTIONS_KEY, tramos);

                                            // Init next section
                                            Tramo newSection = new Tramo();
                                            List<Punto> newSectionPointsList = new ArrayList<>();

                                            newSection.setIdTramo(UUID.randomUUID());
                                            newSectionPointsList.add(listPoints.get(listPoints.size() -1));

                                            SharedPrefsManager.getInstance(context).saveSectionObject(SharedPrefsKeys.RACE_ACTUAL_SECTION_KEY, newSection);
                                            SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY, newSectionPointsList);
                                        } else {
                                            long lastUpdatedRythmnTime = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY);
                                            long deltaTime = currentRaceTime - lastUpdatedRythmnTime;

                                            lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);
                                            float distanceInKms = ((newDistance - lastUpdatedRythmnDistance) / 1000f);
                                            distanceInKms = Math.round(distanceInKms * 100f) / 100f;

                                            long currentRythmn = (int) (deltaTime / distanceInKms);

                                            long newRythmn = (int) ((rythmn + currentRythmn) / 2);

                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, newRythmn);
                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY, currentRaceTime);
                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, newDistance);
                                            SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, false);

                                            Tramo tramo = SharedPrefsManager.getInstance(context).readSectionObject(SharedPrefsKeys.RACE_ACTUAL_SECTION_KEY);
                                            List<Punto> listPoints = SharedPrefsManager.getInstance(context).readListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY);

                                            tramo.setDistanciaTramo(newDistance);
                                            tramo.setRitmoTramo(newRythmn);
                                            tramo.setPuntosTramo(listPoints);

                                            new EncodeListPointsAsyncTask(context).execute(listPoints);

                                            List<Tramo> tramos = SharedPrefsManager.getInstance(context).readListSections(SharedPrefsKeys.RACE_SECTIONS_KEY);
                                            tramos.add(tramo);

                                            SharedPrefsManager.getInstance(context).saveListSections(SharedPrefsKeys.RACE_SECTIONS_KEY, tramos);

                                            // Init next section
                                            Tramo newSection = new Tramo();
                                            List<Punto> newSectionPointsList = new ArrayList<>();

                                            newSection.setIdTramo(UUID.randomUUID());
                                            newSectionPointsList.add(listPoints.get(listPoints.size() -1));

                                            SharedPrefsManager.getInstance(context).saveSectionObject(SharedPrefsKeys.RACE_ACTUAL_SECTION_KEY, newSection);
                                            SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY, newSectionPointsList);
                                        }
                                    }

                                    puntos.add(p);
                                    SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY, puntos);

                                    List<Punto> listPoints = SharedPrefsManager.getInstance(context).readListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY);
                                    listPoints.add(p);
                                    SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY, listPoints);


                                    Date now = new Date();

                                    @SuppressLint("SimpleDateFormat")
                                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

                                    String dateFormatted = formatter.format(now) + " hs";
                                    SharedPrefsManager.getInstance(context).saveString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY, dateFormatted);
                                    SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.LAST_UPDATE_TIME_MS_KEY, now.getTime());
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
        return locationCallback;
    }

}