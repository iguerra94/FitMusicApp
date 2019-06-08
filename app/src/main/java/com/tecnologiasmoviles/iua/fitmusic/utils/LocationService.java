package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.util.Log;
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
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LocationService {

    private static final String LOG_TAG = LocationService.class.getSimpleName();

    private static FusedLocationProviderClient fusedLocationProviderClient;
    private static LocationCallback locationCallback;

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
        locationRequest.setFastestInterval(30000);
        return locationRequest;
    }

    public static LocationCallback getLocationCallback() {
        return locationCallback;
    }

    public static LocationCallback buildLocationCallback(Context context, String refKey) {
        if (locationCallback == null) {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    List<Punto> puntos = SharedPrefsManager.getInstance(context).readListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY);

                    Punto p = new Punto(UUID.randomUUID(), locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());

                    if (puntos == null) {
                        puntos = new ArrayList<>();
                    }

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
                        SharedPrefsManager.getInstance(context).saveListSections(SharedPrefsKeys.RACE_SECTIONS_KEY, new ArrayList<>());
                    } else {
                        puntos.add(p);
                        SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY, puntos);

                        List<Punto> listPoints = SharedPrefsManager.getInstance(context).readListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY);
                        listPoints.add(p);
                        SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY, listPoints);

                        String origin = puntos.get(puntos.size() -2).getLat() + "," + puntos.get(puntos.size() -2).getLon();
                        String destination = puntos.get(puntos.size() -1).getLat() + "," + puntos.get(puntos.size() -1).getLon();

                        AndroidNetworking.get("https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&mode={mode}&key={key}")
                                .addPathParameter("origin", origin)
                                .addPathParameter("destination", destination)
                                .addPathParameter("mode", "walking")
                                .addPathParameter("key", context.getString(R.string.directions_api_key))
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

                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, newDistance);

                                            if (SharedPrefsManager.getInstance(context).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY)) {
                                                SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY, false);
//                                                Toast.makeText(context, "RACE_GETTING_LAST_POINT_KEY: " + SharedPrefsManager.getInstance(context).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY), Toast.LENGTH_SHORT).show();
                                            }

                                            long currentRaceTime = new Date().getTime();
                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_TIME_KEY, currentRaceTime);

                                            String key = FirebaseRefs.getRacesRef().child(refKey).push().getKey();

                                            if (SharedPrefsManager.getInstance(context).readListSections(SharedPrefsKeys.RACE_SECTIONS_KEY) != null) {
                                                FirebaseRefs.getRacesRef().child(refKey).child(key).child("currentDistance").setValue(String.format("%.2f", newDistance/1000f));
                                                FirebaseRefs.getRacesRef().child(refKey).child(key).child("tramo").setValue(SharedPrefsManager.getInstance(context).readListSections(SharedPrefsKeys.RACE_SECTIONS_KEY).size() +1);
                                                FirebaseRefs.getRacesRef().child(refKey).child(key).child("currentRaceTime").setValue(TimeUtils.milliSecondsToTimer(currentRaceTime-SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY)));
                                            }

                                            long lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);

                                            if (newDistance >= lastUpdatedRythmnDistance + 500) {
                                                SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, true);
                                            }

                                            long rythmnAccumulated = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);

                                            if (SharedPrefsManager.getInstance(context).readListSections(SharedPrefsKeys.RACE_SECTIONS_KEY) != null) {
                                                FirebaseRefs.getRacesRef().child(refKey).child(key).child("currentRythmn").setValue(TimeUtils.milliSecondsToTimer(rythmnAccumulated));
                                            }

                                            boolean shouldMeasureRythmn = SharedPrefsManager.getInstance(context).readBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY);

                                            if (shouldMeasureRythmn) {
                                                if (rythmnAccumulated == 0) {// First time to meausure rythmn
                                                    long initialRaceTime = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY);

                                                    long deltaTime = currentRaceTime - initialRaceTime;
                                                    float distanceInKms = (newDistance / 1000f);
                                                    distanceInKms = Math.round(distanceInKms * 100f) / 100f;

                                                    long newRythmn = RaceUtils.measureCurrentRythmn(deltaTime, distanceInKms);

                                                    RaceUtils.updateRaceSharedPrefsOptions(context, newRythmn, currentRaceTime, newDistance);

                                                    RaceUtils.setSectionData(context, newDistance, newRythmn, true);

                                                    RaceUtils.determineCurrentFastestSection(context, newRythmn, true);

                                                    // Init next section data
                                                    RaceUtils.initNewSectionData(context, listPoints);
                                                } else {
                                                    long lastUpdatedRythmnTime = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY);
                                                    long deltaTime = currentRaceTime - lastUpdatedRythmnTime;

                                                    lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);
                                                    float distanceInKms = ((newDistance - lastUpdatedRythmnDistance) / 1000f);
                                                    distanceInKms = Math.round(distanceInKms * 100f) / 100f;

                                                    long currentRythmn = RaceUtils.measureCurrentRythmn(deltaTime, distanceInKms);
                                                    long newRythmn = RaceUtils.measureAverageRythmn(rythmnAccumulated, currentRythmn);

                                                    RaceUtils.updateRaceSharedPrefsOptions(context, newRythmn, currentRaceTime, newDistance);

                                                    RaceUtils.setSectionData(context, newDistance, newRythmn, false);

                                                    RaceUtils.determineCurrentFastestSection(context, currentRythmn, false);

                                                    // Init next section data
                                                    RaceUtils.initNewSectionData(context, listPoints);
                                                }
                                            }

                                            RaceUtils.setLastUpdateTimeData(context);
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
        return locationCallback;
    }

}