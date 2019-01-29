package com.tecnologiasmoviles.iua.fitmusic.view;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.PolyUtil;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;
import com.tecnologiasmoviles.iua.fitmusic.utils.MapsUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.RacesJSONParser;
import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class LastRaceFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private static final String LOG_TAG = LastRaceFragment.class.getSimpleName();

    private MapView mMapView;
    private static final int DEFAULT_ZOOM = 15;

    private ImageView mBSArrowDown;
    private ImageView mBSArrowUp;

    private View bsRaceInfoLastRace;
    private LottieAnimationView loadingRaceDataInMapAnimationView;

    private TextView raceDescriptionTextViewLastRace;
    private TextView raceDateTextViewLastRace;
    private TextView raceDistanceTextViewLastRace;
    private TextView raceDurationTextViewLastRace;
    private TextView raceRythmnTextViewLastRace;

    private LinearLayout linearLayoutNoRaces;
    private LinearLayout mLastRaceMapToolbarLinearLayout;

    private Carrera lastRaceData;
    private GoogleMap mMap;

    private LatLng northEastBoundCoords;
    private LatLng southWestBoundCoords;

    public LastRaceFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_last_race, container, false);

        linearLayoutNoRaces = view.findViewById(R.id.linearLayoutNoRaces);
        bsRaceInfoLastRace = view.findViewById(R.id.bs_race_info_last_race);
        loadingRaceDataInMapAnimationView = view.findViewById(R.id.loading_race_data_in_map_animation);
        mLastRaceMapToolbarLinearLayout = view.findViewById(R.id.lastRaceMapToolbar);

        ImageButton mBtnInitialLocation = view.findViewById(R.id.btnInitialLocation);
        mBtnInitialLocation.setOnClickListener(this);

        raceDescriptionTextViewLastRace = view.findViewById(R.id.raceDescriptionTextViewBS);
        raceDateTextViewLastRace = view.findViewById(R.id.raceDateTextViewBS);
        raceDistanceTextViewLastRace = view.findViewById(R.id.raceDistanceTextViewBS);
        raceDurationTextViewLastRace = view.findViewById(R.id.raceDurationTextViewBS);
        raceRythmnTextViewLastRace = view.findViewById(R.id.raceRythmnTextViewBS);

        mMapView = view.findViewById(R.id.map);
        setMap(view, savedInstanceState);

        return view;
    }

    private void setMap(View view, Bundle savedInstanceState) {
        try {
            MapsInitializer.initialize(Objects.requireNonNull(getActivity()));
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(this);


            mBSArrowDown = view.findViewById(R.id.bs_arrow_down);
            mBSArrowUp = view.findViewById(R.id.bs_arrow_up);

            BottomSheetBehavior.from(bsRaceInfoLastRace).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View view, int newState) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        mBSArrowDown.setVisibility(View.VISIBLE);
                        mBSArrowUp.setVisibility(View.GONE);
                    } else {
                        mBSArrowDown.setVisibility(View.GONE);
                        mBSArrowUp.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onSlide(@NonNull View view, float v) {

                }
            });

        } catch (InflateException e){
            Log.e(LOG_TAG, "Inflate exception");
        }
    }

    private void setRaceFields(Carrera lastRace) {
        raceDescriptionTextViewLastRace.setText(lastRace.getDescripcion());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        String raceDateFormatted = formatter.format(lastRace.getFechaCarrera()) + " hs";

        raceDateTextViewLastRace.setText(raceDateFormatted);

        float raceDistanceToKms = lastRace.getDistancia() / 1000f;
        raceDistanceTextViewLastRace.setText(String.format("%.2f", raceDistanceToKms));

        raceDurationTextViewLastRace.setText(TimeUtils.milliSecondsToTimer(lastRace.getDuracion()));
        raceRythmnTextViewLastRace.setText(TimeUtils.milliSecondsToTimer(lastRace.getRitmo()));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatActivity containerActivity = (AppCompatActivity) getActivity();

        assert containerActivity != null;
        assert containerActivity.getSupportActionBar() != null;

        containerActivity.getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_last_race));
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMapView != null) {
            mMapView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        if (lastRaceData != null) {
            new LoadRaceDataAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnInitialLocation) {
            LatLngBounds bounds = new LatLngBounds(southWestBoundCoords, northEastBoundCoords);

            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        }
    }

    private class LoadRaceDataAsync extends AsyncTask<Void, String, Void> {

        public LoadRaceDataAsync() {}

        @Override
        protected void onPreExecute() {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.

            linearLayoutNoRaces.setVisibility(View.GONE);

            try {
                boolean success = mMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(Objects.requireNonNull(getActivity()), R.raw.style));

                if (!success) {
                    Log.e(LOG_TAG, "Style parsing failed.");
                }
            } catch (Resources.NotFoundException e) {
                Log.e(LOG_TAG, "Can't find style. Error: ", e);
            }
        }

        @Override
        protected Void doInBackground(Void... aVoid) {
            try {
                File file = new File(Objects.requireNonNull(getActivity()).getFilesDir(), "races_data.json");
                FileInputStream stream = new FileInputStream(file);

                List<Carrera> carreras = RacesJSONParser.getRacesJSONStream(stream);


//            if (carreras.size() > 0) {
//                lastRaceData = carreras.get(carreras.size()-1);

                Punto p1 = new Punto(UUID.randomUUID(), -31.42267,-64.18459);
                p1.setIsStartingRacePoint(true);
                Punto p2 = new Punto(UUID.randomUUID(), -31.423252,-64.182687);
                Punto p3 = new Punto(UUID.randomUUID(), -31.42604,-64.18383);
                Punto p4 = new Punto(UUID.randomUUID(), -31.427876,-64.184676);
                Punto p5 = new Punto(UUID.randomUUID(), -31.427702,-64.185181);
                Punto p6 = new Punto(UUID.randomUUID(), -31.42601,-64.18606);
                Punto p7 = new Punto(UUID.randomUUID(), -31.422644,-64.187598);
                Punto p8 = new Punto(UUID.randomUUID(), -31.42040,-64.18874);
                Punto p9 = new Punto(UUID.randomUUID(), -31.420588,-64.187501);
                Punto p10 = new Punto(UUID.randomUUID(), -31.42169,-64.18327);
                p10.setIsLastRacePoint(true);

                Tramo tramo1 = new Tramo();
                tramo1.setIdTramo(UUID.randomUUID());
                tramo1.setDistanciaTramo(530);
                //7’20’’
                tramo1.setRitmoTramo(830188);
                tramo1.setIsFastestSection(false);
                tramo1.setSectionPolyline("tfx~Dt`wfKpB}J@CnFbBdF`BvAb@");

                List<Punto> puntosTramo1 = new ArrayList<>();
                puntosTramo1.add(p1);
                puntosTramo1.add(p2);
                puntosTramo1.add(p3);

                tramo1.setPuntosTramo(puntosTramo1);

            Tramo tramo2 = new Tramo();
            tramo2.setIdTramo(UUID.randomUUID());

            //530 + 520
            tramo2.setDistanciaTramo(1050);
            //6’19’’
            // ANTERIOR: 830188
            // ACTUAL: 728846
            // TOTAL: 779517
            tramo2.setRitmoTramo(779517);
            tramo2.setIsFastestSection(true);
            tramo2.setSectionPolyline("v{x~D|{vfKnDfAdCz@`@J^Bc@zBCJyChA{DvA");

            List<Punto> puntosTramo2 = new ArrayList<>();
            puntosTramo2.add(p3);
            puntosTramo2.add(p4);
            puntosTramo2.add(p5);
            puntosTramo2.add(p6);

            tramo2.setPuntosTramo(puntosTramo2);

            Tramo tramo3 = new Tramo();
            tramo3.setIdTramo(UUID.randomUUID());
            //1050+650
            tramo3.setDistanciaTramo(1700);
            //8’05’’
            // ANTERIOR: 779517
            // ACTUAL: 746153
            // TOTAL: 762835
            tramo3.setRitmoTramo(762835);
            tramo3.setIsFastestSection(false);
            tramo3.setSectionPolyline("p{x~DziwfKiG~B{HrCkFlBoAb@mDzAq@T");

            List<Punto> puntosTramo3 = new ArrayList<>();
            puntosTramo3.add(p6);
            puntosTramo3.add(p7);
            puntosTramo3.add(p8);

            tramo3.setPuntosTramo(puntosTramo3);

            Tramo tramo4 = new Tramo();
            tramo4.setIdTramo(UUID.randomUUID());
            //1700+550
            tramo4.setDistanciaTramo(2250);
            //7’10’’
            // ANTERIOR: 762835
            // ACTUAL: 781818
            // TOTAL: 772326
            tramo4.setRitmoTramo(772326);
            tramo4.setIsFastestSection(false);
            tramo4.setSectionPolyline("nxw~DrzwfKUFN}Af@gDdAsGhAkGnAgH");

            List<Punto> puntosTramo4 = new ArrayList<>();
            puntosTramo4.add(p8);
            puntosTramo4.add(p9);
            puntosTramo4.add(p10);

            tramo4.setPuntosTramo(puntosTramo4);

                List<Tramo> tramos = new ArrayList<>();
                tramos.add(tramo1);
            tramos.add(tramo2);
            tramos.add(tramo3);
            tramos.add(tramo4);

                lastRaceData = new Carrera();
                lastRaceData.setIdCarrera(UUID.randomUUID());
                lastRaceData.setDescripcion("TROTE");
                lastRaceData.setDistancia(2250);
                lastRaceData.setDuracion(1734000);
                lastRaceData.setRitmo(772326);
                Date raceDate = new Date();
                lastRaceData.setFechaCarrera(raceDate);
                lastRaceData.setTramos(tramos);

                publishProgress("DRAW_RACE_DATA");

//            } else {
//                linearLayoutNoRaces.setVisibility(View.VISIBLE);
//                mMapView.setVisibility(View.GONE);
//                bsRaceInfoLastRace.setVisibility(View.GONE);
//
//                Log.d(LOG_TAG, "ARRAY VACIO");
//            }

            } catch (IOException e) {
                e.printStackTrace();
            }
//            if (getActivity() != null) {
//                boolean isMapRaceDataLoadedSuccessfully = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.MAP_RACE_DATA_LOADED_SUCCESSFULLY_KEY);
//                Log.d(LOG_TAG, "isMapRaceDataLoadedSuccessfully do antes: " + isMapRaceDataLoadedSuccessfully);
//
//                if (isMapRaceDataLoadedSuccessfully) {
//                    return null;
//                }
//
//                do {
//                    isMapRaceDataLoadedSuccessfully = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.MAP_RACE_DATA_LOADED_SUCCESSFULLY_KEY);
//                    Log.d(LOG_TAG, "isMapRaceDataLoadedSuccessfully do mientras: " + isMapRaceDataLoadedSuccessfully);
//                } while (!isMapRaceDataLoadedSuccessfully);
//            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values[0].equals("DRAW_RACE_DATA")) {
                List<Tramo> raceSections = lastRaceData.getTramos();

                for (int i = 0; i < raceSections.size(); i++) {
                    List<Punto> sectionPoints = raceSections.get(i).getPuntosTramo();

                    Punto lastSectionPoint = sectionPoints.get(sectionPoints.size()-1);

                    // START MARKER
                    if (i == 0) {
                        Punto startRacePoint = sectionPoints.get(0);
                        MapsUtils.addStartRacePointMarker(getActivity(), startRacePoint, "Start", mMap);
                        MapsUtils.addLastSectionPointMarker(getActivity(), lastSectionPoint, null, mMap);
                    }

                    // FINISH MARKER
                    if (i == raceSections.size()-1) {
                        Punto lastRacePoint = sectionPoints.get(sectionPoints.size()-1);
                        MapsUtils.addLastRacePointMarker(getActivity(), lastRacePoint, "Finish", mMap);
                    }

                    // DISPLAY DISTANCE IN AN INFO WINDOW
                    if (i % 2 != 0 && i < raceSections.size()-1) {
                        long sectionDistance = raceSections.get(i).getDistanciaTramo();
                        MapsUtils.addDistanceIcon(getActivity(), lastSectionPoint, sectionDistance, mMap);
                    }

                    // FIRST AND LAST SECTION POINTS MARKER
                    if (i > 0 && i < raceSections.size()-1) {
                        List<Punto> nextSectionPoints = raceSections.get(i+1).getPuntosTramo();
                        Punto firstPointNextSection = nextSectionPoints.get(0);
                        MapsUtils.addLastSectionPointMarker(getActivity(), lastSectionPoint, firstPointNextSection, mMap);
                    }

                    // DECODE SECTION POLYLINE
                    List<LatLng> decodedPath = PolyUtil.decode(raceSections.get(i).getSectionPolyline());

                    // DRAW SECTION POLYLINE
                    if (raceSections.get(i).getIsFastestSection()) {
                        Polyline fastestSectionPolyline = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
                        fastestSectionPolyline.setTag("fastestSection");
                        MapsUtils.stylePolyline(fastestSectionPolyline);
                    } else {
                        Polyline sectionPolyline = mMap.addPolyline(new PolylineOptions().addAll(decodedPath));
                        sectionPolyline.setTag("section");
                        MapsUtils.stylePolyline(sectionPolyline);
                    }

                }

                // Move camera to point to that location
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(midPoint.getLat(), midPoint.getLon()), DEFAULT_ZOOM));
                // googleMap.getUiSettings().setScrollGesturesEnabled(false);

//                SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.MAP_RACE_DATA_LOADED_SUCCESSFULLY_KEY, true);
//
//                boolean isMapRaceDataLoadedSuccessfully = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.MAP_RACE_DATA_LOADED_SUCCESSFULLY_KEY);
//                Log.d(LOG_TAG, "isMapRaceDataLoadedSuccessfully pre: " + isMapRaceDataLoadedSuccessfully);
            }

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mMapView.setVisibility(View.VISIBLE);
            mLastRaceMapToolbarLinearLayout.setVisibility(View.VISIBLE);
            loadingRaceDataInMapAnimationView.setVisibility(View.VISIBLE);
            bsRaceInfoLastRace.setVisibility(View.VISIBLE);

            setRaceFields(lastRaceData);

            new CaptureListPointsBoundsAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    private class CaptureListPointsBoundsAsyncTask extends AsyncTask<Void, String, Void> {

        public CaptureListPointsBoundsAsyncTask() {}

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
                    .addPathParameter("key", "AIzaSyBe4BWfH1NlOgidZQ8Spbq02GFSLe5BHv0")
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

                                publishProgress("MOVE_CAMERA_BOUNDS");

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
                LatLngBounds bounds = new LatLngBounds(southWestBoundCoords, northEastBoundCoords);

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                mMap.setMinZoomPreference(DEFAULT_ZOOM);
                mMap.setMaxZoomPreference(16f);
                mMap.setLatLngBoundsForCameraTarget(bounds);

                loadingRaceDataInMapAnimationView.setVisibility(View.GONE);
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

}