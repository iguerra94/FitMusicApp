package com.tecnologiasmoviles.iua.fitmusic.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.databinding.FragmentLastRaceBinding;
import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;
import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

public class LastRaceFragment extends Fragment implements
//        OnMapReadyCallback,
        View.OnClickListener {

    private static final String LOG_TAG = LastRaceFragment.class.getSimpleName();

    //private MapView mMapView;
    private static final int DEFAULT_ZOOM = 15;

    private ImageView mBSArrowDown;
    private ImageView mBSArrowUp;

    private FragmentLastRaceBinding binding;

    private Carrera lastRaceData;
//    private GoogleMap mMap;

    public LastRaceFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_last_race, container, false);

        View view = binding.getRoot();

        binding.btnInitialLocation.setEnabled(false);
        binding.btnInitialLocation.setOnClickListener(this);

/*
        mMapView.setVisibility(View.GONE);

        setMap(view, savedInstanceState);
*/
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatActivity containerActivity = (AppCompatActivity) getActivity();

        assert containerActivity != null;
        assert containerActivity.getSupportActionBar() != null;

        containerActivity.getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_last_race));
    }

/*
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
*/

    private void setRaceFields(Carrera lastRace) {
        binding.bsRaceInfoLastRace.raceDescriptionTextViewBS.setText(lastRace.getDescripcion());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
        String raceDateFormatted = formatter.format(lastRace.getFechaCarrera()) + " hs";

        binding.bsRaceInfoLastRace.raceDateTextViewBS.setText(raceDateFormatted);

        float raceDistanceToKms = lastRace.getDistancia() / 1000f;
        binding.bsRaceInfoLastRace.raceDistanceTextViewBS.setText(String.format("%.2f", raceDistanceToKms));

        binding.bsRaceInfoLastRace.raceDurationTextViewBS.setText(TimeUtils.milliSecondsToTimer(lastRace.getDuracion()));
        binding.bsRaceInfoLastRace.raceRythmnTextViewBS.setText(TimeUtils.milliSecondsToTimer(lastRace.getRitmo()));
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
/*
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        loadingRaceDataInMapAnimationView.setVisibility(View.VISIBLE);

        if (SharedPrefsManager.getInstance(getActivity()).readLatLngBounds(SharedPrefsKeys.LAST_RACE_MAP_BOUNDS_KEY) != null) {
            mMap.setOnMapLoadedCallback(() -> {
                LatLngBounds bounds = SharedPrefsManager.getInstance(getActivity()).readLatLngBounds(SharedPrefsKeys.LAST_RACE_MAP_BOUNDS_KEY);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 12f));

                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
                mMap.setMinZoomPreference(12f);
                mMap.setMaxZoomPreference(17f);
                mMap.setLatLngBoundsForCameraTarget(bounds);
            });
        }

        //new LoadRaceDataAsync(getActivity(), mMap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

//        if (lastRaceData != null) {
//        }
    }
*/

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnInitialLocation) {
//            mMap.setOnMapLoadedCallback(() -> {
//                LatLngBounds bounds = SharedPrefsManager.getInstance(getActivity()).readLatLngBounds(SharedPrefsKeys.LAST_RACE_MAP_BOUNDS_KEY);
//
//                if (bounds != null) {
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.getCenter(), 12f));
//
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
//                }
//
//            });
        }
    }

}