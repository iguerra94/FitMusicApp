package com.tecnologiasmoviles.iua.fitmusic.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.InflateException;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;

import java.text.SimpleDateFormat;

public class DetailRaceActivity extends AppCompatActivity
//        implements OnMapReadyCallback
{

    private static final String LOG_TAG = DetailRaceActivity.class.getSimpleName();

    //    private MapView mMapView;
    private static final int DEFAULT_ZOOM = 16;

    ImageView mBSArrowDown;
    ImageView mBSArrowUp;

    View bsRaceInfoRaceDetail;

    TextView raceDescriptionTextViewRaceDetail;
    TextView raceDateTextViewRaceDetail;
    TextView raceDistanceTextViewRaceDetail;
    TextView raceDurationTextViewRaceDetail;
    TextView raceRithmTextViewRaceDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_race);

        Toolbar myToolbarDetailActivity = findViewById(R.id.my_toolbar_detail_activity);
        setSupportActionBar(myToolbarDetailActivity);

        assert getSupportActionBar() != null;

        getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_detail_race));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

//        mMapView = findViewById(R.id.race_detail_map);
        bsRaceInfoRaceDetail = findViewById(R.id.bs_race_info);

        raceDescriptionTextViewRaceDetail = findViewById(R.id.raceDescriptionTextViewBS);
        raceDateTextViewRaceDetail = findViewById(R.id.raceDateTextViewBS);
        raceDistanceTextViewRaceDetail = findViewById(R.id.raceDistanceTextViewBS);
        raceDurationTextViewRaceDetail = findViewById(R.id.raceDurationTextViewBS);
        raceRithmTextViewRaceDetail = findViewById(R.id.raceRithmTextViewBS);

        Carrera raceDetail = (Carrera) getIntent().getSerializableExtra("raceData");

        Log.d(LOG_TAG, raceDetail.getFechaCarrera().toString());

        setRaceFields(raceDetail);

        setMap(savedInstanceState);
    }

    private void setMap(Bundle savedInstanceState) {
        try {
//            MapsInitializer.initialize( this );
//            mMapView = findViewById(R.id.race_detail_map);
//            mMapView.onCreate(savedInstanceState);
//            mMapView.getMapAsync(this);

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
        raceDateTextViewRaceDetail.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(raceDetail.getFechaCarrera()));
        raceDistanceTextViewRaceDetail.setText(String.valueOf(raceDetail.getDistancia()));
        raceDurationTextViewRaceDetail.setText(new SimpleDateFormat("HH:mm:ss").format(raceDetail.getDuracion()));
        raceRithmTextViewRaceDetail.setText(new SimpleDateFormat("HH:mm:ss").format(raceDetail.getRitmo()));
    }

    @Override
    public void onPause() {
        super.onPause();
//        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
//        mMapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
//        mMapView.onResume();
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        // Set LatLng coords of CRUC-IUA
//        LatLng iuaLatLng = new LatLng(-31.433575, -64.275736);
//
//        // Add marker on that location
//        googleMap.addMarker(new MarkerOptions()
//                .title("CRUC-IUA")
//                .snippet("Instituto Universitario Aeronautico.")
//                .position(iuaLatLng));
//
//        // Move camera to point to that location
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(iuaLatLng,
//                DEFAULT_ZOOM));
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
