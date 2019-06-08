package com.tecnologiasmoviles.iua.fitmusic.utils.asyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;
import com.tecnologiasmoviles.iua.fitmusic.utils.MapsUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;
import com.tecnologiasmoviles.iua.fitmusic.view.LastRaceFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.fragment.app.FragmentActivity;

/*public class LoadRaceDataAsync extends AsyncTask<Void, String, Void> {

    WeakReference<Context> mWeakActivity;
    GoogleMap mMap;

    public LoadRaceDataAsync(Context context, GoogleMap mMap) {
        this.mWeakActivity = new WeakReference<>(context);
        this.mMap = mMap;
    }

    @Override
    protected void onPreExecute() {
        // Customise the styling of the base map using a JSON object defined
        // in a raw resource file.

        Activity activity = (Activity) mWeakActivity.get();

        if (activity != null) {
            LinearLayout linearLayoutNoRaces = activity.findViewById(R.id.linearLayoutNoRaces);
            linearLayoutNoRaces.setVisibility(View.GONE);
        }

        try {
            assert activity != null;
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(activity, R.raw.style));

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
            File file = new File(Objects.requireNonNull().getFilesDir(), "races_data.json");
            FileInputStream stream = new FileInputStream(file);

//                List<Carrera> carreras = RacesJSONParser.getRacesJSONStream(stream);

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
            lastRaceData.setIdCarrera(UUID.fromString("a3ec0e91-5375-4322-accf-e66e1e9bf83d"));
            lastRaceData.setDescripcion("TROTE");
            lastRaceData.setDistancia(2250);
            lastRaceData.setDuracion(1734000);
            lastRaceData.setRitmo(772326);
            Date raceDate = new Date();
            lastRaceData.setFechaCarrera(raceDate);
            lastRaceData.setTramos(tramos);

//                if (SharedPrefsManager.getInstance(getActivity()).readLatLngBounds(SharedPrefsKeys.LAST_RACE_MAP_BOUNDS_KEY) != null) {
//                    LatLngBounds bounds = SharedPrefsManager.getInstance(getActivity()).readLatLngBounds(SharedPrefsKeys.LAST_RACE_MAP_BOUNDS_KEY);
//
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
//                    mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
//                    mMap.setMinZoomPreference(DEFAULT_ZOOM);
//                    mMap.setMaxZoomPreference(16f);
//                    mMap.setLatLngBoundsForCameraTarget(bounds);
//                }

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
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mLastRaceMapToolbarLinearLayout.setVisibility(View.VISIBLE);
        bsRaceInfoLastRace.setVisibility(View.VISIBLE);

        setRaceFields(lastRaceData);

        if (SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.LAST_RACE_ID_KEY).isEmpty() ||
                !SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.LAST_RACE_ID_KEY).equals(String.valueOf(lastRaceData.getIdCarrera())) ||
                SharedPrefsManager.getInstance(getActivity()).readLatLngBounds(SharedPrefsKeys.LAST_RACE_MAP_BOUNDS_KEY) == null) {

            SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.LAST_RACE_ID_KEY, String.valueOf(lastRaceData.getIdCarrera()));
            mBtnInitialLocation.setEnabled(false);
            new LastRaceFragment.CaptureListPointsBoundsAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            mMapView.setVisibility(View.VISIBLE);
            loadingRaceDataInMapAnimationView.setVisibility(View.GONE);
            mBtnInitialLocation.setEnabled(true);
        }
    }

}
*/