package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LocationService {

    private static final String LOG_TAG = LocationService.class.getSimpleName();

    public static LocationRequest buildLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(20000);
        return locationRequest;
    }

    public static LocationCallback buildLocationCallback(Context context, String key) {
        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Punto p = new Punto(UUID.randomUUID(), locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                List<Punto> puntos = SharedPrefsManager.getInstance(context).readListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY);
                puntos.add(p);
                SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY, puntos);

                String pointKey = FirebaseRefs.getRacesRef().child(key).push().getKey();
                assert pointKey != null;

                FirebaseRefs.getRacesRef().child(key).child(pointKey).child("lat").setValue(p.getLat());
                FirebaseRefs.getRacesRef().child(key).child(pointKey).child("lon").setValue(p.getLon());

                if (puntos.size() == 1) {
                    FirebaseRefs.getRacesRef().child(key).child(pointKey).child("distanceAccumulated").setValue(0f + " KM");
                    FirebaseRefs.getRacesRef().child(key).child(pointKey).child("rythmn").setValue(TimeUtils.milliSecondsToTimer(0) + "/KM");

                    long initialRaceTime = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY);

                    long currentTime = new Date().getTime();
                    long currentDuration = currentTime - initialRaceTime;

                    SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_TIME_KEY, currentTime);
                    FirebaseRefs.getRacesRef().child(key).child(pointKey).child("time").setValue(currentTime);
                    FirebaseRefs.getRacesRef().child(key).child(pointKey).child("duration").setValue(TimeUtils.milliSecondsToTimer(currentDuration));
                }

                if (puntos.size() > 1) {
                    String origin = puntos.get(puntos.size() - 1).getLat() + "," + puntos.get(puntos.size() - 1).getLon();
                    String destination = puntos.get(puntos.size() - 2).getLat() + "," + puntos.get(puntos.size() - 2).getLon();

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

                                        FirebaseRefs.getRacesRef().child(key).child(pointKey).child("distance").setValue(String.format("%.2f", currentDistance / 1000f) + " KM");
                                        FirebaseRefs.getRacesRef().child(key).child(pointKey).child("distanceAccumulated").setValue(String.format("%.2f", distanceAccumulated / 1000f) + " KM");

                                        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, newDistance);

                                        long initialRaceTime = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY);

                                        long currentTime = new Date().getTime();
                                        long currentDuration = currentTime - initialRaceTime;

                                        FirebaseRefs.getRacesRef().child(key).child(pointKey).child("duration").setValue(TimeUtils.milliSecondsToTimer(currentDuration));

                                        if (newDistance >= 1000) { // Distance is greater than or equal to 1 km.
                                            long lastCurrentRaceTime = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_CURRENT_TIME_KEY);
                                            long currentRaceTime = new Date().getTime();
                                            long deltaTime = currentRaceTime - lastCurrentRaceTime;

                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_TIME_KEY, currentRaceTime);

                                            FirebaseRefs.getRacesRef().child(key).child(pointKey).child("time").setValue(currentRaceTime);
                                            FirebaseRefs.getRacesRef().child(key).child(pointKey).child("deltaTime").setValue(TimeUtils.milliSecondsToTimer(deltaTime));

                                            long rythmn = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);
                                            long newRythmn = 0;

                                            if (rythmn == 0) {// First time to meausure rythhmn
                                                newRythmn = (int) (deltaTime / (currentDistance / 1000f));
                                            }
                                            if (newDistance >= distanceAccumulated + 500) {
                                                newRythmn = (int) ((rythmn + (deltaTime / (currentDistance / 1000f))) / 2);
                                            }
                                            FirebaseRefs.getRacesRef().child(key).child(pointKey).child("rythmn").setValue(TimeUtils.milliSecondsToTimer(newRythmn) + "/KM");
                                            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, newRythmn);
                                        } else {
                                            FirebaseRefs.getRacesRef().child(key).child(pointKey).child("rythmn").setValue(TimeUtils.milliSecondsToTimer(0) + "/KM");
                                        }

                                        Date now = new Date();

                                        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

                                        String dateFormatted = formatter.format(now) + " hs";
                                        SharedPrefsManager.getInstance(context).saveString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY, dateFormatted);
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