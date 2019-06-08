package com.tecnologiasmoviles.iua.fitmusic.utils.asyncTasks;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;
import com.tecnologiasmoviles.iua.fitmusic.utils.MapsUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
/*
public class CaptureListPointsBoundsAsyncTask extends AsyncTask<Void, String, Void> {

    private LatLng northEastBoundCoords;
    private LatLng southWestBoundCoords;


    public CaptureListPointsBoundsAsyncTask() {}

    /*
    @Override
    protected void onPreExecute() {
        List<Tramo> raceSections = lastRaceData.getTramos();
        List<Punto> listPoints = new ArrayList<>();

        for (int i = 0; i < raceSections.size(); i++) {
            Punto firstSectionPoint = raceSections.get(i).getPuntosTramo().get(0);
            listPoints.add(firstSectionPoint);

            if (i == raceSections.size()-1) {
                List<Punto> lastSectionPoints = raceSections.get(i).getPuntosTramo();
                Punto lastRacePoint = lastSectionPoints.get(lastSectionPoints.size()-1);
                listPoints.add(lastRacePoint);
            }
        }

        List<LatLng> latLngList = new ArrayList<>();

        for (Punto p : listPoints) {
            latLngList.add(new LatLng(p.getLat(), p.getLon()));
        }

        AndroidNetworking.get("https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&waypoints={waypoints}&mode={mode}&key={key}")
                .addPathParameter("origin", MapsUtils.createUrl(latLngList.get(0)))
                .addPathParameter("destination", MapsUtils.createUrl(latLngList.get(latLngList.size()-1)))
                .addPathParameter("waypoints", MapsUtils.createUrlWaypoints(latLngList.subList(1, latLngList.size()-1), false))
                .addPathParameter("mode", "walking")
                .addPathParameter("key", getString(R.string.directions_api_key))
                .setTag("test")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject routes = response.getJSONArray("routes").getJSONObject(0);
                            JSONObject bounds = routes.getJSONObject("bounds");

                            JSONObject northEastBound = bounds.getJSONObject("northeast");
                            JSONObject southWestBound = bounds.getJSONObject("southwest");

                            northEastBoundCoords = new LatLng(northEastBound.getDouble("lat"), northEastBound.getDouble("lng"));
                            southWestBoundCoords = new LatLng(southWestBound.getDouble("lat"), southWestBound.getDouble("lng"));

                            mMapView.setVisibility(View.VISIBLE);

                            publishProgress("MOVE_CAMERA_BOUNDS");
//                                Log.d(LOG_TAG, "id: " + SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.LAST_RACE_ID_KEY));
//                                Log.d(LOG_TAG, "bounds: " + SharedPrefsManager.getInstance(getActivity()).readLatLngBounds(SharedPrefsKeys.LAST_RACE_MAP_BOUNDS_KEY));

//                                Toast.makeText(getActivity(), "northEastBoundCoords: " + northEastBoundCoords, Toast.LENGTH_SHORT).show();
//                                Toast.makeText(getActivity(), "southWestBoundCoords: " + southWestBoundCoords, Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onProgressUpdate(String... values) {
        if (values[0].equals("MOVE_CAMERA_BOUNDS")) {
            mMap.setOnMapLoadedCallback(() -> {
                LatLngBounds bounds = new LatLngBounds(southWestBoundCoords, northEastBoundCoords);
                SharedPrefsManager.getInstance(getActivity()).saveLatLngBounds(SharedPrefsKeys.LAST_RACE_MAP_BOUNDS_KEY, bounds);

                mBtnInitialLocation.setEnabled(true);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 12f));

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                mMap.setMinZoomPreference(12f);
                mMap.setMaxZoomPreference(17f);
                mMap.setLatLngBoundsForCameraTarget(bounds);

                loadingRaceDataInMapAnimationView.setVisibility(View.GONE);
            });
        }
    }

    @Override
    protected Void doInBackground(Void... aVoids) {
        while (southWestBoundCoords == null || northEastBoundCoords == null) {}
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
    }

}

*/