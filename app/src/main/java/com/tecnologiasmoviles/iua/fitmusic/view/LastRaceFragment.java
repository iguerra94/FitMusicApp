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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;
import com.tecnologiasmoviles.iua.fitmusic.utils.RacesJSONParser;
import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class LastRaceFragment extends Fragment
//        implements OnMapReadyCallback
{

    private static final String LOG_TAG = LastRaceFragment.class.getSimpleName();

    //    private GoogleMap mMap;
//    private MapView mMapView;
    private static final int DEFAULT_ZOOM = 16;

    private ImageView mBSArrowDown;
    private ImageView mBSArrowUp;

    private View bsRaceInfoLastRace;

    private TextView raceDescriptionTextViewLastRace;
    private TextView raceDateTextViewLastRace;
    private TextView raceDistanceTextViewLastRace;
    private TextView raceDurationTextViewLastRace;
    private TextView raceRythmnTextViewLastRace;

    public LastRaceFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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
            File file = new File(getActivity().getFilesDir(), "races_data.json");
            FileInputStream stream = new FileInputStream(file);

            List<Carrera> carreras = RacesJSONParser.getRacesJSONStream(stream);

//            mMapView = view.findViewById(R.id.map);
            setMap(view, savedInstanceState);

            if (carreras.size() > 0) {
                linearLayoutNoRaces.setVisibility(View.GONE);
//                mMapView.setVisibility(View.VISIBLE);
                bsRaceInfoLastRace.setVisibility(View.VISIBLE);

                Carrera ultimaCarrera = carreras.get(carreras.size()-1);

                setRaceFields(ultimaCarrera);
            } else {
                linearLayoutNoRaces.setVisibility(View.VISIBLE);
//                mMapView.setVisibility(View.GONE);
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
//            MapsInitializer.initialize( getActivity() );
//            mMapView.onCreate(savedInstanceState);
//            mMapView.getMapAsync(this);

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
//        if (mMapView != null) {
//            mMapView.onPause();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mMapView != null) {
//            mMapView.onDestroy();
//        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
//        if (mMapView != null) {
//            mMapView.onLowMemory();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (mMapView != null) {
//            mMapView.onResume();
//        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//        // Set LatLng coords of CRUC-IUA
//        LatLng iuaLatLng = new LatLng(-31.433575, -64.275736);
//
//        // Add marker on that location
//        mMap.addMarker(new MarkerOptions()
//                .title("CRUC-IUA")
//                .snippet("Instituto Universitario Aeronautico.")
//                .position(iuaLatLng));
//
//        // Move camera to point to that location
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(iuaLatLng,
//                DEFAULT_ZOOM));
//    }

}