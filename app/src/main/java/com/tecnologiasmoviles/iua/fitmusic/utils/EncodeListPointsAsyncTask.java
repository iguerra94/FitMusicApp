package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.maps.model.LatLng;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EncodeListPointsAsyncTask extends AsyncTask<List<Punto>, Void, Void> {

    private static final String LOG_TAG = EncodeListPointsAsyncTask.class.getSimpleName();

    private Context context;
    private List<String> encodedPolylines;

    public EncodeListPointsAsyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        this.encodedPolylines = SharedPrefsManager.getInstance(this.context).readListEncodedPolylines(SharedPrefsKeys.ENCODED_POLYLINES_LIST_KEY);
    }

    @Override
    protected Void doInBackground(List<Punto>... puntos) {
        List<LatLng> latLngList = new ArrayList<>();

        for (Punto p : puntos[0]) {
            latLngList.add(new LatLng(p.getLat(), p.getLon()));
        }

        AndroidNetworking.get("https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&waypoints=optimize:true{waypoints}&mode={mode}&key={key}")
                .addPathParameter("origin", MapsUtils.createUrl(latLngList.get(0)))
                .addPathParameter("destination", MapsUtils.createUrl(latLngList.get(latLngList.size()-1)))
                .addPathParameter("waypoints", MapsUtils.createUrlWaypoints(latLngList.subList(1, latLngList.size()-1)))
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

                            encodedPolylines.add(ENCODED_POLYLINE);
                            Log.d(LOG_TAG, encodedPolylines.toString());
                            SharedPrefsManager.getInstance(context).saveListEncodedPolylines(SharedPrefsKeys.ENCODED_POLYLINE_KEY, encodedPolylines);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d(LOG_TAG, "ERROR: " + anError.getErrorBody());
                    }
                });

        return null;
    }

}