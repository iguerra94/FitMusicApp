package com.tecnologiasmoviles.iua.fitmusic.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DetailRaceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = DetailRaceActivity.class.getSimpleName();

    private MapView mMapView;
    private static final int DEFAULT_ZOOM = 16;

    ImageView mBSArrowDown;
    ImageView mBSArrowUp;

    View bsRaceInfoRaceDetail;

    TextView raceDescriptionTextViewRaceDetail;
    TextView raceDateTextViewRaceDetail;
    TextView raceDistanceTextViewRaceDetail;
    TextView raceDurationTextViewRaceDetail;
    TextView raceRythmnTextViewRaceDetail;

    private Carrera raceData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_race);

        configureToolbar();

        bindViews();

        raceData = (Carrera) getIntent().getSerializableExtra("raceData");

        setRaceFields(raceData);

        setMap(savedInstanceState);
    }

    private void configureToolbar() {
        Toolbar myToolbarDetailActivity = findViewById(R.id.my_toolbar_detail_activity);
        setSupportActionBar(myToolbarDetailActivity);

        assert getSupportActionBar() != null;

        getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_detail_race));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void bindViews() {
        mMapView = findViewById(R.id.race_detail_map);
        bsRaceInfoRaceDetail = findViewById(R.id.bs_race_info);

        raceDescriptionTextViewRaceDetail = findViewById(R.id.raceDescriptionTextViewBS);
        raceDateTextViewRaceDetail = findViewById(R.id.raceDateTextViewBS);
        raceDistanceTextViewRaceDetail = findViewById(R.id.raceDistanceTextViewBS);
        raceDurationTextViewRaceDetail = findViewById(R.id.raceDurationTextViewBS);
        raceRythmnTextViewRaceDetail = findViewById(R.id.raceRythmnTextViewBS);
    }

    private void setMap(Bundle savedInstanceState) {
        try {
            MapsInitializer.initialize( this );
            mMapView = findViewById(R.id.race_detail_map);
            mMapView.onCreate(savedInstanceState);
            mMapView.getMapAsync(this);

            mBSArrowDown = findViewById(R.id.bs_arrow_down);
            mBSArrowUp = findViewById(R.id.bs_arrow_up);

            BottomSheetBehavior.from(bsRaceInfoRaceDetail).setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
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

    private void setRaceFields(Carrera raceDetail) {
        raceDescriptionTextViewRaceDetail.setText(raceDetail.getDescripcion());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        String raceDateFormatted = formatter.format(raceDetail.getFechaCarrera()) + " hs";

        raceDateTextViewRaceDetail.setText(raceDateFormatted);

        float raceDistanceToKms = raceDetail.getDistancia() / 1000f;
        raceDistanceTextViewRaceDetail.setText(String.format("%.2f", raceDistanceToKms));

        raceDurationTextViewRaceDetail.setText(TimeUtils.milliSecondsToTimer(raceDetail.getDuracion()));
        raceRythmnTextViewRaceDetail.setText(TimeUtils.milliSecondsToTimer(raceDetail.getRitmo()));
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        List<Tramo> raceSections = raceData.getTramos();

        for (int i = 0; i < raceSections.size(); i++) {
            List<Punto> sectionPoints = raceSections.get(i).getPuntosTramo();

            // START MARKER
            if (i == 0) {
                Punto startRacePoint = sectionPoints.get(0);
                MapsUtils.addStartRacePointMarker(this, startRacePoint, "Start", googleMap);
            }

            // FINISH MARKER
            if (i == raceSections.size()-1) {
                Punto lastRacePoint = sectionPoints.get(sectionPoints.size()-1);
                MapsUtils.addLastRacePointMarker(this, lastRacePoint, "Finish", googleMap);
            }

            // DISPLAY DISTANCE IN AN INFO WINDOW
            if (i % 2 != 0) {
                Punto lastSectionPoint = sectionPoints.get(i);
                long sectionDistance = raceSections.get(i).getDistanciaTramo();
                MapsUtils.addDistanceIcon(this, lastSectionPoint, sectionDistance, googleMap);
            }

            // LAST SECTION POINT MARKER
            if (i > 0 && i < raceSections.size()-1) {
//                Punto lastSectionPoint = sectionPoints.get(i);
//                MapsUtils.addLastSectionPointMarker(this, lastSectionPoint, googleMap);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}