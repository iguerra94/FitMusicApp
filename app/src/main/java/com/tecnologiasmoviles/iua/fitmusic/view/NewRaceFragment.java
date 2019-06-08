package com.tecnologiasmoviles.iua.fitmusic.view;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.androidnetworking.AndroidNetworking;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Song;
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;
import com.tecnologiasmoviles.iua.fitmusic.model.exception.RaceModelException;
import com.tecnologiasmoviles.iua.fitmusic.services.FitMusicForegroundService;
import com.tecnologiasmoviles.iua.fitmusic.utils.FirebaseRefs;
import com.tecnologiasmoviles.iua.fitmusic.utils.LocationService;
import com.tecnologiasmoviles.iua.fitmusic.utils.MediaPlayerManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.RaceUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.RacesJSONParser;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.SongUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.dialogs.DialogFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class NewRaceFragment extends Fragment implements View.OnClickListener, MediaPlayer.OnCompletionListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int MY_PERMISSION_REQUEST = 1;

    /* LAYOUT FIELDS */

    private EditText raceDescriptionEditText;
    private TextView newRaceDescriptionTV;
    private TextView newRaceDateTV;

    private MediaPlayerManager mediaPlayerManager;

    private ImageButton btnPlayNewRace;
    private ImageButton btnPauseNewRace;

    private CircleImageView songCoverImageViewNewRace;
    private TextView songTitleTextViewNewRace;
    private TextView songArtistTextViewNewRace;

    private CoordinatorLayout newRaceFragmentView;

    private FrameLayout layoutContent;
    private FrameLayout layoutNewRaceData;
    private FrameLayout flStartRace;
    private FrameLayout flFinishRace;

    private TextView newRaceDistanceTextView;
    private TextView newRaceRythmnTextView;
    private TextView newRaceRythmnUnitTextView;
    private TextView newRaceDurationTextView;
    private TextView lastUpdateTextView;

    private View bsMusic;

    private LottieAnimationView animationView;
    private RelativeLayout relativeLayoutNewRace;

    /* END LAYOUT FIELDS */

    private long initialTime;

    private List<Song> songList;

    private Carrera carrera;

    private Handler mHandlerTimer = new Handler();

    private AlertDialog dialogRegisterRaceQuestion;
    private AlertDialog dialogRegisteringRace;
    private AlertDialog dialogRaceNotRegistered;
//    private AlertDialog dialogLoadingMusicFromFirebase;

    private LocationCallback locationCallback;

    public NewRaceFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_race, container, false);

        setupToolbar();
        bindViews(view);

        createMediaPlayer();
        getSongsList();

        SharedPrefsManager.getInstance(getActivity()).getSharedPrefs().registerOnSharedPreferenceChangeListener(this);

        AndroidNetworking.initialize(getActivity());

        return view;
    }

    private void bindViews(View view) {
        animationView = view.findViewById(R.id.animation_view);
        relativeLayoutNewRace = view.findViewById(R.id.relativeLayoutNewRace);

        newRaceFragmentView = view.findViewById(R.id.new_race_fragment);

        layoutContent = view.findViewById(R.id.layoutContent);
        layoutNewRaceData = view.findViewById(R.id.layoutNewRaceData);

        bsMusic = view.findViewById(R.id.bs_music);

        flStartRace = view.findViewById(R.id.flStartRace);

        FloatingActionButton fabStartRace = view.findViewById(R.id.fabStartRace);
        fabStartRace.setOnClickListener(this);

        TextView txtStartRace = view.findViewById(R.id.txtStartRace);
        txtStartRace.setOnClickListener(this);

        flFinishRace = view.findViewById(R.id.flFinishRace);

        FloatingActionButton fabFinishRace = view.findViewById(R.id.fabFinishRace);
        fabFinishRace.setOnClickListener(this);

        TextView txtFinishRace = view.findViewById(R.id.txtFinishRace);
        txtFinishRace.setOnClickListener(this);

        newRaceDistanceTextView = view.findViewById(R.id.newRaceDistanceTextView);

        newRaceRythmnTextView = view.findViewById(R.id.newRaceRythmnTextView);
        newRaceRythmnTextView.setEnabled(false);

        newRaceRythmnUnitTextView = view.findViewById(R.id.newRaceRythmnUnitTextView);
        newRaceRythmnUnitTextView.setEnabled(false);

        newRaceDurationTextView = view.findViewById(R.id.newRaceDurationTextView);

        lastUpdateTextView = view.findViewById(R.id.lastUpdateTextView);

        songCoverImageViewNewRace = view.findViewById(R.id.songCoverImageViewNewRace);
        songCoverImageViewNewRace.setOnClickListener(this);

        LinearLayout linearLayoutNewRace = view.findViewById(R.id.llNewRace);
        linearLayoutNewRace.setOnClickListener(this);

        songTitleTextViewNewRace = view.findViewById(R.id.songTitleTextViewNewRace);
        songArtistTextViewNewRace = view.findViewById(R.id.songArtistTextViewNewRace);

        ImageButton btnStepBackwardNewRace = view.findViewById(R.id.btnStepBackwardNewRace);
        btnStepBackwardNewRace.setImageResource(R.drawable.ic_step_backward_solid);
        btnStepBackwardNewRace.setOnClickListener(this);

        btnPlayNewRace = view.findViewById(R.id.btnPlayNewRace);
        btnPlayNewRace.setImageResource(R.drawable.ic_play_solid);
        btnPlayNewRace.setOnClickListener(this);

        btnPauseNewRace = view.findViewById(R.id.btnPauseNewRace);
        btnPauseNewRace.setImageResource(R.drawable.ic_pause_solid);
        btnPauseNewRace.setOnClickListener(this);

        ImageButton btnStepForwardNewRace = view.findViewById(R.id.btnStepForwardNewRace);
        btnStepForwardNewRace.setImageResource(R.drawable.ic_step_forward_solid);
        btnStepForwardNewRace.setOnClickListener(this);

        raceDescriptionEditText = view.findViewById(R.id.raceDescriptionEditText);
        newRaceDescriptionTV = view.findViewById(R.id.newRaceDescriptionTextView);
        newRaceDateTV = view.findViewById(R.id.newRaceDateTextView);
    }

    private void setupToolbar() {
        AppCompatActivity containerActivity = (AppCompatActivity) getActivity();

        assert containerActivity != null;
        assert containerActivity.getSupportActionBar() != null;

        containerActivity.getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_new_race));
    }

    private void createMediaPlayer() {
        mediaPlayerManager = MediaPlayerManager.getInstance();
        mediaPlayerManager.create();

        mediaPlayerManager.getMediaPlayer().setOnCompletionListener(this);
    }

    private void getSongsList() {
        if (SharedPrefsManager.getInstance(getActivity()).readListSongs(SharedPrefsKeys.LIST_SONGS_KEY) == null) {
            songList = SongUtils.getMusic(Objects.requireNonNull(getActivity()).getContentResolver());

            if (songList.size() > 0) {
                SharedPrefsManager.getInstance(getActivity()).saveListSongs(SharedPrefsKeys.LIST_SONGS_KEY, songList);
                MediaPlayerManager.initPlayer(getActivity());
            } else {
//                new LoadMusicFromFirebaseAsyncTask().execute();
            }

        }
    }

    private void startRace() {
        // retrieve list of songs
        songList = SharedPrefsManager.getInstance(getActivity()).readListSongs(SharedPrefsKeys.LIST_SONGS_KEY);

        // setup mediaplayer
        if (mediaPlayerManager.getMediaPlayer() == null) {
            mediaPlayerManager.create();
            mediaPlayerManager.getMediaPlayer().setOnCompletionListener(this);
        }

        // set current song ui
        updateCurrentSongUI();

        // start foreground service
        FitMusicForegroundService.startForegroundServiceIntent(getActivity());

        //1) setup description race field
        String raceDescription = setupDescriptionRaceField();

        //2) setup initial race time
        Date initialRaceTime = setupInitialRaceTime();

        //3) format initial race time
        // this method returns a list with two values
        // the first is the date formatted using date and time
        // the second is the date formatted using time only
        List<String> formattedTimes = formatInitialRaceTime(initialRaceTime);

        //4) setup race date field
        setupRaceDateField(formattedTimes.get(0));

        //5) setup initial race values in shared prefs
        List<String> raceDataList = new ArrayList<>();
        raceDataList.add(formattedTimes.get(0));
        raceDataList.add(formattedTimes.get(1));
        raceDataList.add(raceDescription);

        setupInitialRaceValuesInSharedPrefs(raceDataList);

        //6) execute start race animation
        executeStartRaceAnimation();

        //7) init race duration handler
        initRaceDurationHandler();
    }

    private String setupDescriptionRaceField() {
        newRaceDescriptionTV.setText("");

        String raceDescription = raceDescriptionEditText.getText().toString();
        newRaceDescriptionTV.setText(raceDescription);

        raceDescriptionEditText.setText("");

        return raceDescription;
    }

    private Date setupInitialRaceTime() {
        Date now = new Date();

        SharedPrefsManager.getInstance(getActivity()).saveLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY, now.getTime());

        return now;
    }

    private List<String> formatInitialRaceTime(Date initialRaceTime) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterDateTime = new SimpleDateFormat("dd/MM/yyyy - HH:mm");

        String dateFormattedTime = formatterTime.format(initialRaceTime) + " hs";
        String dateFormattedDateTime = formatterDateTime.format(initialRaceTime) + " hs";

        List<String> formattedTimes = new ArrayList<>();
        formattedTimes.add(dateFormattedDateTime);
        formattedTimes.add(dateFormattedTime);

        return formattedTimes;
    }

    private void setupRaceDateField(String dateFormattedDateTime) {
        newRaceDateTV.setText("");
        newRaceDateTV.setText(dateFormattedDateTime);
    }

    private void setupInitialRaceValuesInSharedPrefs(List<String> raceDataList) {
        String dateFormattedDateTime = raceDataList.get(0);
        String dateFormattedTime = raceDataList.get(1);
        String raceDescription = raceDataList.get(2);

        SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.IS_RUNNING_KEY, true);
        SharedPrefsManager.getInstance(getActivity()).saveListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY, new ArrayList<>());
        SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY, dateFormattedTime);
        SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.RACE_DATE_STRING_KEY, dateFormattedDateTime);
        SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.RACE_DESCRIPTION_KEY, raceDescription);
    }

    private void executeStartRaceAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int x = layoutContent.getRight();
            int y = layoutContent.getBottom();

            int startRadius = 0;
            int endRadius = (int) Math.hypot(newRaceFragmentView.getWidth(), newRaceFragmentView.getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(layoutContent, x, y, startRadius, endRadius);

            animationView.setVisibility(View.GONE);
            relativeLayoutNewRace.setVisibility(View.GONE);
            flStartRace.setVisibility(View.GONE);

            anim.start();

            layoutNewRaceData.setVisibility(View.VISIBLE);
            flFinishRace.setVisibility(View.VISIBLE);
            bsMusic.setVisibility(View.VISIBLE);
        }
    }

    private void initRaceDurationHandler() {
        updateTimerUI();
    }

    private void finishRace() {
        int x = layoutContent.getRight();
        int y = layoutContent.getBottom();

        int startRadius = Math.max(layoutContent.getWidth(), layoutContent.getHeight());
        int endRadius = 0;

        mHandlerTimer.removeCallbacks(mUpdateTimerTask);

        songList = null;

        if (mediaPlayerManager.getMediaPlayer() != null) {
            mediaPlayerManager.getMediaPlayer().stop();
            mediaPlayerManager.getMediaPlayer().release();
            mediaPlayerManager.setMediaPlayer(null);
        }

        Animator anim;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(layoutNewRaceData, x, y, startRadius, endRadius);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    newRaceDurationTextView.setText(getString(R.string.initialRaceDurationString));
                    lastUpdateTextView.setText("");
                    flFinishRace.setVisibility(View.GONE);
                    bsMusic.setVisibility(View.GONE);

                    animationView.setVisibility(View.VISIBLE);
                    relativeLayoutNewRace.setVisibility(View.VISIBLE);

                    flStartRace.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    layoutNewRaceData.setVisibility(View.GONE);
                    SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.IS_RACE_FINISHING_KEY, false);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            anim.start();
        }

    }

    private void updateTimerUI() {
        updateCurrentTimeTextView();
    }

    private void updateCurrentTimeTextView() {
        mHandlerTimer.postDelayed(mUpdateTimerTask, 1000);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimerTask = new Runnable() {
        public void run() {
            initialTime = getInitialTime();
            long currentTime = new Date().getTime();
            long currentDuration = currentTime - initialTime;

            newRaceDurationTextView.setText(TimeUtils.milliSecondsToTimer(currentDuration));

            // Running this thread after 1000 milliseconds
            mHandlerTimer.postDelayed(this, 1000);
        }
    };

    private void updateCurrentSongUI() {
        int id = SharedPrefsManager.getInstance(getActivity()).readInt(SharedPrefsKeys.ID_SONG_KEY);

        if (id == -1) {
            songCoverImageViewNewRace.setImageURI(Uri.parse(songList.get(0).getSongCoverUri()));
            songTitleTextViewNewRace.setText(songList.get(0).getSongTitle());
            songArtistTextViewNewRace.setText(songList.get(0).getArtist());
        } else {
            songCoverImageViewNewRace.setImageURI(Uri.parse(songList.get(id - 1).getSongCoverUri()));
            songTitleTextViewNewRace.setText(songList.get(id - 1).getSongTitle());
            songArtistTextViewNewRace.setText(songList.get(id - 1).getArtist());
        }
    }

    private void saveRaceDateMsInFirebaseDatabase(String registrationToken) {
        // get users ref in firebase database
        DatabaseReference usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");
        // save registration token in firebase database with child last_race_timestamp set current timestamp.

        Date date = new Date();
        long time = date.getTime();
        usersDBRef.child(registrationToken + "/last_race_date_miliseconds").setValue(time);
    }

    private void registerRace() {
//        Toast.makeText(getActivity(), "registering", Toast.LENGTH_SHORT).show();
        carrera = new Carrera();

        carrera.setIdCarrera(UUID.randomUUID());

        carrera.setDescripcion(SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.RACE_DESCRIPTION_KEY));
        carrera.setDistancia(SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY));

        String raceDuration = SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.RACE_DURATION_KEY);
        carrera.setDuracion(TimeUtils.parseDurationFromStringToMs(raceDuration));
        carrera.setRitmo(SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY));

        Date raceDate = new Date(SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY));
        carrera.setFechaCarrera(raceDate);

        List<Tramo> tramos = SharedPrefsManager.getInstance(getActivity()).readListSections(SharedPrefsKeys.RACE_SECTIONS_KEY);

        // Last Race Section
        List<Punto> puntosUltimoTramo = tramos.get(tramos.size() - 1).getPuntosTramo();
        // Last Race Point
        puntosUltimoTramo.get(puntosUltimoTramo.size() - 1).setIsLastRacePoint(true);

        int raceFastestSectionIndex = SharedPrefsManager.getInstance(getActivity()).readInt(SharedPrefsKeys.RACE_CURRENT_FASTEST_SECTION_INDEX_KEY);

        tramos.get(raceFastestSectionIndex).setIsFastestSection(true);
        carrera.setTramos(tramos);

        if (getActivity() != null) {
            try {
                File racesJSONFile = new File(getActivity().getFilesDir(), "races_data.json");
                RacesJSONParser.saveRaceData(getActivity(), racesJSONFile, carrera);

                racesJSONFile = new File(getActivity().getFilesDir(), "races_data.json");

                StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                StorageReference racesRef = storageRef.child("races_data_" + new Date().getTime() + ".json");

                UploadTask uploadTask = racesRef.putFile(Uri.fromFile(racesJSONFile));

                // Register observers to listen for when the download is done or if it fails
                uploadTask.addOnFailureListener(exception -> {
                    Toast.makeText(getActivity(), exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }).addOnSuccessListener(taskSnapshot -> {
//                    Toast.makeText(getActivity(), "OK", Toast.LENGTH_LONG).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void verificarCamposVacios() throws RaceModelException {
        if (raceDescriptionEditText.getText().toString().isEmpty()) {
            throw new RaceModelException("Debes ingresar una descripcion para la carrera");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.songCoverImageViewNewRace || v.getId() == R.id.llNewRace) {
            openMusicListActivity();
        }
        if (v.getId() == R.id.btnStepBackwardNewRace) {
            MediaPlayerManager.stepBackward(getActivity());
            MediaPlayerManager.play();
            updateCurrentSongUI();
            setPauseBtnVisible();
            mediaPlayerManager.getMediaPlayer().setOnCompletionListener(this);
        }
        if (v.getId() == R.id.btnPlayNewRace) {
            setPauseBtnVisible();
            MediaPlayerManager.play();
            SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.MEDIA_PLAYER_PLAYING_KEY, true);
        }
        if (v.getId() == R.id.btnPauseNewRace) {
            setPlayBtnVisible();
            MediaPlayerManager.pause();
            SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.MEDIA_PLAYER_PLAYING_KEY, false);
        }
        if (v.getId() == R.id.btnStepForwardNewRace) {
            MediaPlayerManager.stepForward(getActivity());
            MediaPlayerManager.play();
            updateCurrentSongUI();
            setPauseBtnVisible();
            mediaPlayerManager.getMediaPlayer().setOnCompletionListener(this);
        }
        if (v.getId() == R.id.fabStartRace || v.getId() == R.id.txtStartRace) {
            try {
                verificarCamposVacios();

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSION_REQUEST);
                    } else {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSION_REQUEST);
                    }
                } else {
                    startRace();
                }
            } catch (RaceModelException e) {
                CoordinatorLayout coordinatorLayout = getActivity().findViewById(R.id.cl);
                Snackbar snack = Snackbar.make(coordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG);
                snack.show();
            }
        }
        if (v.getId() == R.id.fabFinishRace || v.getId() == R.id.txtFinishRace) {
            dialogRegisterRaceQuestion = (AlertDialog) DialogFactory.getInstance().getRegisterRaceQuestionDialog().create(getActivity(), R.layout.dialog_register_race_question);
            dialogRegisterRaceQuestion.setCancelable(false);
            dialogRegisterRaceQuestion.show();
        }
    }

    private void openMusicListActivity() {
        Intent musicListActivityIntent = new Intent(getActivity(), ListMusicActivity.class);
        startActivity(musicListActivityIntent);
    }

    private void setPauseBtnVisible() {
        btnPlayNewRace.setVisibility(View.GONE);
        btnPauseNewRace.setVisibility(View.VISIBLE);
    }

    private void setPlayBtnVisible() {
        btnPauseNewRace.setVisibility(View.GONE);
        btnPlayNewRace.setVisibility(View.VISIBLE);
    }

    private void setInitialTime(long initialTime) {
        this.initialTime = initialTime;
    }

    private long getInitialTime() {
        return this.initialTime;
    }

    @Override
    public void onStart() {
        super.onStart();
        setInitialTime(SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY));

        SharedPrefsManager.getInstance(getActivity()).getSharedPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isRunning = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);

        if (isRunning) {
            animationView.setVisibility(View.GONE);
            relativeLayoutNewRace.setVisibility(View.GONE);
            flStartRace.setVisibility(View.GONE);

            newRaceDateTV.setText(SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.RACE_DATE_STRING_KEY));
            newRaceDescriptionTV.setText(SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.RACE_DESCRIPTION_KEY));

            initialTime = getInitialTime();
            long currentTime = new Date().getTime();
            long currentDuration = currentTime - initialTime;

            newRaceDurationTextView.setText(TimeUtils.milliSecondsToTimer(currentDuration));

            layoutNewRaceData.setVisibility(View.VISIBLE);
            flFinishRace.setVisibility(View.VISIBLE);
            bsMusic.setVisibility(View.VISIBLE);

            if (songList == null) {
                songList = SongUtils.getMusic(Objects.requireNonNull(getActivity()).getContentResolver());
            }
            if (songList.size() > 0) {
                updateCurrentSongUI();
            }

            long distance = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
            float distanceToKms = (distance / 1000f);
            newRaceDistanceTextView.setText(String.format("%.2f", distanceToKms));

            String lastUpdateTime = SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY);
            lastUpdateTextView.setText("Ultima actualización: " + lastUpdateTime);

            long rythmn = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);
            if (rythmn > 0 && !newRaceRythmnUnitTextView.isEnabled()) {
                newRaceRythmnTextView.setText(TimeUtils.milliSecondsToTimer(rythmn));
                newRaceRythmnTextView.setEnabled(true);
                newRaceRythmnUnitTextView.setEnabled(true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPrefsManager.getInstance(getActivity()).getSharedPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // This code is only for the very first race
                        startRace();

                        String refKey = FirebaseRefs.getRacesRef().push().getKey();
                        SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.RACE_CURRENT_FIREBASE_KEY, refKey);

                        LocationRequest locationRequest = LocationService.buildLocationRequest();
                        locationCallback = LocationService.buildLocationCallback(getActivity(), refKey);

                        LocationService.getFusedLocationProviderClientInstance(getActivity()).requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                } else {
                    Objects.requireNonNull(getActivity()).finish();
                }
            }
        }
    }

    private class RegisterRaceAsyncTask extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            dialogRegisterRaceQuestion.dismiss();
            dialogRegisteringRace = (AlertDialog) DialogFactory.getInstance().getRegisterinRaceDialog().create(getActivity(), R.layout.dialog_registering_race);
            dialogRegisteringRace.setCancelable(false);
            dialogRegisteringRace.show();

            if (LocationService.getLocationCallback() != null) {
                LocationService.getFusedLocationProviderClientInstance(getActivity()).removeLocationUpdates(LocationService.getLocationCallback());
            }

//            long lastUpdateTimeMs = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.LAST_UPDATE_TIME_MS_KEY);
//
//            if (lastUpdateTimeMs == 0) {
//                lastUpdateTimeMs = getInitialTime();
//            }
//
//            long currentTimeMs = new Date().getTime();
//            long deltaTimeMs = currentTimeMs - lastUpdateTimeMs;
//
//            if (deltaTimeMs >= 10000) {
//                SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY, true);
//            }

            long raceCurrentDistance = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);

//        if (raceCurrentDistance >= 500) {
            long currentRaceTime = new Date().getTime();

            long rythmnAccumulated = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);

            long lastUpdatedRythmnTime = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY);
            long deltaTime = currentRaceTime - lastUpdatedRythmnTime;

            long lastUpdatedRythmnDistance = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY);
            float distanceInKms = ((raceCurrentDistance - lastUpdatedRythmnDistance) / 1000f);
            distanceInKms = Math.round(distanceInKms * 100f) / 100f;

            long newRythmn = 0;
            long currentRythmn = 0;
            if (raceCurrentDistance >= 500) {
                currentRythmn = RaceUtils.measureCurrentRythmn(deltaTime, distanceInKms);
                newRythmn = RaceUtils.measureAverageRythmn(rythmnAccumulated, currentRythmn);
            }

            RaceUtils.updateRaceSharedPrefsOptions(getActivity(), newRythmn, currentRaceTime, raceCurrentDistance);

            SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.RACE_GETTING_LAST_SECTION_POLYLINE_KEY, true);

            RaceUtils.setSectionData(getActivity(), raceCurrentDistance, newRythmn, false);

            RaceUtils.determineCurrentFastestSection(getActivity(), currentRythmn, false);
//        }

        }

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.RACE_DURATION_KEY, newRaceDurationTextView.getText().toString());

            String registrationToken = SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.REGISTRATION_TOKEN_KEY);
            saveRaceDateMsInFirebaseDatabase(registrationToken);

//            boolean isGettingLastPoint;
//            boolean isGettingLastSectionPolyline;
//
//            do {
//                isGettingLastPoint = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY);
//                isGettingLastSectionPolyline = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_SECTION_POLYLINE_KEY);
//            } while (isGettingLastPoint || isGettingLastSectionPolyline);

            boolean isGettingLastSectionPolyline;

            do {
                isGettingLastSectionPolyline = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.RACE_GETTING_LAST_SECTION_POLYLINE_KEY);
            } while (isGettingLastSectionPolyline);


            publishProgress("REGISTER_RACE");

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values[0].equals("REGISTER_RACE")) {
                registerRace();
                finishRace();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialogRegisteringRace.dismiss();
            Intent raceRegisteredIntent = new Intent(getActivity(), RaceRegisteredActivity.class);
            startActivity(raceRegisteredIntent);
        }

    }

/*
    private class LoadMusicFromFirebaseAsyncTask extends AsyncTask<Void, String, List<Song>> implements SongUtils.FinishedLoadingMusicCallback {
        @Override
        protected void onPreExecute() {
            dialogLoadingMusicFromFirebase = (AlertDialog) DialogFactory.getInstance().getLoadingMusicFromFirebaseDialog().create(getActivity(), R.layout.dialog_loading_music_from_cloud);
            dialogLoadingMusicFromFirebase.setCancelable(false);
            dialogLoadingMusicFromFirebase.show();

//            SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.IS_LOADING_MUSIC_KEY, true);
        }

        @Override
        protected List<Song> doInBackground(Void... voids) {
            SongUtils.getMusicFromFirebase(getActivity(), this);

            List<Song> songs;

            do {
                songs = SharedPrefsManager.getInstance(getActivity()).readListSongs(SharedPrefsKeys.LIST_SONGS_KEY);
            } while (songs == null);

            return songs;
        }

        @Override
        protected void onPostExecute(List<Song> songs) {
            new DownloadFilesAsyncTask(getActivity()).execute(songs);
        }

        @Override
        public void onFinishedLoadingMusicCallback(List<Song> songs) {
//            for (int i = 0; i < songs.size(); i++) {
//                songs.get(i).setSongCoverUri(songCoverPaths.get(i));
//                Toast.makeText(getActivity(), "Cover: " + songs.get(i).getSongCoverUri(), Toast.LENGTH_LONG).show();
//            }
            SharedPrefsManager.getInstance(getActivity()).saveListSongs(SharedPrefsKeys.LIST_SONGS_KEY, songs);
//            Toast.makeText(getActivity(), "last path segment: " + Uri.parse(songs.get(0).getSongCoverUri()), Toast.LENGTH_SHORT).show();
//
//            for (:
//                 ) {
//
//            }
//            Toast.makeText(getActivity(), "last path segment: " + Uri.parse(songs.get(0).getSongCoverUri()).getLastPathSegment().split("/")[2], Toast.LENGTH_SHORT).show();
//            Toast.makeText(getActivity(), "songs 1 size: " + songs.size(), Toast.LENGTH_LONG).show();
        }
    }
*/

    @Override
    public void onCompletion(MediaPlayer mp) {
        long currentDuration = mediaPlayerManager.getMediaPlayer().getCurrentPosition();
        long currentDurationGlobal = (currentDuration > 0) ? currentDuration : 0;

        if (currentDurationGlobal > 0) {
            MediaPlayerManager.stepForward(getActivity());
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        long distance = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
        long rythmn = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);
        boolean mediaPlayerPlaying = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.MEDIA_PLAYER_PLAYING_KEY);

        if (key.equals(SharedPrefsKeys.MEDIA_PLAYER_PLAYING_KEY)) {
            if (mediaPlayerPlaying) {
                setPauseBtnVisible();
            }
            if (!mediaPlayerPlaying) {
                setPlayBtnVisible();
            }
        }
        if (key.equals(SharedPrefsKeys.ID_SONG_KEY)) {
            if (songList.size() > 0) {
                updateCurrentSongUI();
                setPauseBtnVisible();
            }
        }
        if (key.equals(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY)) {
            float distanceToKms = (distance / 1000f);
            newRaceDistanceTextView.setText(String.format("%.2f", distanceToKms));
        }
        if (key.equals(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY)) {
            if (rythmn > 0 && !newRaceRythmnUnitTextView.isEnabled()) {
                newRaceRythmnTextView.setText(TimeUtils.milliSecondsToTimer(rythmn));
                newRaceRythmnTextView.setEnabled(true);
                newRaceRythmnUnitTextView.setEnabled(true);
            }
        }
        if (key.equals(SharedPrefsKeys.LAST_UPDATE_TIME_KEY)) {
            String lastUpdateTime = SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY);
            lastUpdateTextView.setText("Ultima actualización: " + lastUpdateTime);
        }
    }

}