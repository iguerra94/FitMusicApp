package com.tecnologiasmoviles.iua.fitmusic.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class LastRaceFragment extends Fragment implements OnMapReadyCallback {

    private static final String LOG_TAG = LastRaceFragment.class.getSimpleName();

    private MapView mMapView;
    private static final int DEFAULT_ZOOM = 16;

    private ImageView mBSArrowDown;
    private ImageView mBSArrowUp;

    private View bsRaceInfoLastRace;

    private TextView raceDescriptionTextViewLastRace;
    private TextView raceDateTextViewLastRace;
    private TextView raceDistanceTextViewLastRace;
    private TextView raceDurationTextViewLastRace;
    private TextView raceRythmnTextViewLastRace;

    private Carrera lastRaceData;

    public LastRaceFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_last_race, container, false);

        LinearLayout linearLayoutNoRaces = view.findViewById(R.id.linearLayoutNoRaces);
        bsRaceInfoLastRace = view.findViewById(R.id.bs_race_info_last_race);

        raceDescriptionTextViewLastRace = view.findViewById(R.id.raceDescriptionTextViewBS);
        raceDateTextViewLastRace = view.findViewById(R.id.raceDateTextViewBS);
        raceDistanceTextViewLastRace = view.findViewById(R.id.raceDistanceTextViewBS);
        raceDurationTextViewLastRace = view.findViewById(R.id.raceDurationTextViewBS);
        raceRythmnTextViewLastRace = view.findViewById(R.id.raceRythmnTextViewBS);

        try {
            File file = new File(Objects.requireNonNull(getActivity()).getFilesDir(), "races_data.json");
            FileInputStream stream = new FileInputStream(file);

            List<Carrera> carreras = RacesJSONParser.getRacesJSONStream(stream);

            mMapView = view.findViewById(R.id.map);
            setMap(view, savedInstanceState);

            if (carreras.size() > 0) {
                linearLayoutNoRaces.setVisibility(View.GONE);
                mMapView.setVisibility(View.VISIBLE);
                bsRaceInfoLastRace.setVisibility(View.VISIBLE);

                lastRaceData = carreras.get(carreras.size()-1);

                setRaceFields(lastRaceData);
            } else {
                linearLayoutNoRaces.setVisibility(View.VISIBLE);
                mMapView.setVisibility(View.GONE);
                bsRaceInfoLastRace.setVisibility(View.GONE);

                Log.d(LOG_TAG, "ARRAY VACIO");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

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
        if (lastRaceData != null) {
            List<Tramo> raceSections = lastRaceData.getTramos();

            for (int i = 0; i < raceSections.size(); i++) {
                List<Punto> sectionPoints = raceSections.get(i).getPuntosTramo();

                // START MARKER
                if (i == 0) {
                    Punto startRacePoint = sectionPoints.get(0);
                    MapsUtils.addStartRacePointMarker(getActivity(), startRacePoint, googleMap);
                }

                // FINISH MARKER
                if (i == raceSections.size()-1) {
                    Punto lastRacePoint = sectionPoints.get(sectionPoints.size()-1);
                    MapsUtils.addLastRacePointMarker(getActivity(), lastRacePoint, googleMap);
                }

                // DISPLAY DISTANCE IN AN INFO WINDOW
                if (i % 2 != 0) {
                    Punto lastSectionPoint = sectionPoints.get(i);
                    long sectionDistance = raceSections.get(i).getDistanciaTramo();
                    MapsUtils.addDistanceIcon(getActivity(), lastSectionPoint, sectionDistance, googleMap);
                }

                // DECODE SECTION POLYLINE
                List<LatLng> decodedPath = PolyUtil.decode(raceSections.get(i).getSectionPolyline());

                // DRAW SECTION POLYLINE
                if (raceSections.get(i).getIsFastestSection()) {
                    Polyline fastestSectionPolyline = googleMap.addPolyline(new PolylineOptions().addAll(decodedPath));
                    fastestSectionPolyline.setTag("fastestSection");
                    MapsUtils.stylePolyline(fastestSectionPolyline);
                } else {
                    Polyline sectionPolyline = googleMap.addPolyline(new PolylineOptions().addAll(decodedPath));
                    sectionPolyline.setTag("section");
                    MapsUtils.stylePolyline(sectionPolyline);
                }

            }

            int sectionIndex = raceSections.size()/2;
            Punto midPoint = raceSections.get(sectionIndex).getPuntosTramo().get(0);

            // Move camera to point to that location
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(midPoint.getLat(), midPoint.getLon()), DEFAULT_ZOOM));
            googleMap.getUiSettings().setScrollGesturesEnabled(false);
        }
    }

}