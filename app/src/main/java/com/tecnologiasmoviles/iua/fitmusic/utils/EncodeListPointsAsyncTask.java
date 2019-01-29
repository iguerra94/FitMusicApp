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
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EncodeListPointsAsyncTask extends AsyncTask<List<Punto>, Void, Void> {

    private static final String LOG_TAG = EncodeListPointsAsyncTask.class.getSimpleName();

    private Context context;
//    private List<String> encodedPolylines;

    public EncodeListPointsAsyncTask(Context context) {
        this.context = context;
    }

//    @Override
//    protected void onPreExecute() {
//        this.encodedPolylines = SharedPrefsManager.getInstance(this.context).readListEncodedPolylines(SharedPrefsKeys.ENCODED_POLYLINES_LIST_KEY);
//    }

    @Override
    protected Void doInBackground(List<Punto>... puntos) {
        List<LatLng> latLngList = new ArrayList<>();

        List<Punto> listPoints = puntos[0];

        for (Punto p : listPoints) {
            latLngList.add(new LatLng(p.getLat(), p.getLon()));
        }

        AndroidNetworking.get("https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&waypoints=optimize:true{waypoints}&mode={mode}&key={key}")
                .addPathParameter("origin", MapsUtils.createUrl(latLngList.get(0)))
                .addPathParameter("destination", MapsUtils.createUrl(latLngList.get(latLngList.size()-1)))
                .addPathParameter("waypoints", MapsUtils.createUrlWaypoints(latLngList.subList(1, latLngList.size()-1), true))
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

//                            encodedPolylines.add(ENCODED_POLYLINE);
//                            Log.d(LOG_TAG, encodedPolylines.toString());
//                            SharedPrefsManager.getInstance(context).saveListEncodedPolylines(SharedPrefsKeys.ENCODED_POLYLINE_KEY, encodedPolylines);

                            JSONArray legs = routes.getJSONArray("legs");

                            JSONObject startLocation = legs.getJSONObject(0).getJSONObject("start_location");
                            JSONObject endLocation = legs.getJSONObject(legs.length()-1).getJSONObject("end_location");


                            listPoints.get(0).setLat(startLocation.getDouble("lat"));
                            listPoints.get(0).setLon(startLocation.getDouble("lng"));

                            listPoints.get(listPoints.size()-1).setLat(endLocation.getDouble("lat"));
                            listPoints.get(listPoints.size()-1).setLon(endLocation.getDouble("lng"));

                            List<Tramo> tramos = SharedPrefsManager.getInstance(context).readListSections(SharedPrefsKeys.RACE_SECTIONS_KEY);
                            int currentSectionIndex = SharedPrefsManager.getInstance(context).readInt(SharedPrefsKeys.RACE_CURRENT_SECTION_INDEX_KEY);

                            tramos.get(currentSectionIndex).setPuntosTramo(listPoints);
                            tramos.get(currentSectionIndex).setSectionPolyline(ENCODED_POLYLINE);

                            SharedPrefsManager.getInstance(context).saveListSections(SharedPrefsKeys.RACE_SECTIONS_KEY, tramos);

                            boolean isGettingLastSectionPolyline = SharedPrefsManager.getInstance(context).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_SECTION_POLYLINE_KEY);

                            if (isGettingLastSectionPolyline) {
                                SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_GETTING_LAST_SECTION_POLYLINE_KEY, false);
                            }
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