package com.tecnologiasmoviles.iua.fitmusic.view;

import android.Manifest;
import android.animation.Animator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tecnologiasmoviles.iua.fitmusic.BuildConfig;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Song;
import com.tecnologiasmoviles.iua.fitmusic.model.exception.RaceModelException;
import com.tecnologiasmoviles.iua.fitmusic.utils.DateUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.MediaPlayerManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.RacesJSONParser;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.SongUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

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

    private static final String LOG_TAG = NewRaceFragment.class.getSimpleName();

    private static final int MY_PERMISSION_REQUEST = 1;

    EditText raceDescriptionEditText;
    TextView newRaceDescriptionTV;
    TextView newRaceDateTV;

    private MediaPlayerManager mediaPlayerManager;

    ImageButton btnStepBackwardNewRace;
    ImageButton btnPlayPauseNewRace;
    ImageButton btnStepForwardNewRace;

    CircleImageView songCoverImageViewNewRace;
    TextView songTitleTextViewNewRace;
    TextView songArtistTextViewNewRace;

    CoordinatorLayout newRaceFragmentView;

    FrameLayout layoutContent;
    FrameLayout layoutNewRaceData;
    LinearLayout linearLayoutNewRace;
    FloatingActionButton fabStartRace;
    FrameLayout flStartRace;
    TextView txtStartRace;
    FloatingActionButton fabFinishRace;
    FrameLayout flFinishRace;
    TextView txtFinishRace;

    TextView newRaceDistanceTextView;
    TextView newRaceDistanceUnitTextView;
    TextView newRaceRythmnTextView;
    TextView newRaceRythmnUnitTextView;
    TextView newRaceDurationTextView;

    private long initialTime;

    View bsMusic;

    LottieAnimationView animationView;
    RelativeLayout relativeLayoutNewRace;

    List<Song> songList;

    private boolean isRunning;

    private Carrera carrera;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Handler mHandlerTimer = new Handler();

    public NewRaceFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_race, container, false);

        AppCompatActivity containerActivity = (AppCompatActivity) getActivity();

        assert containerActivity != null;
        assert containerActivity.getSupportActionBar() != null;

        containerActivity.getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_new_race));

        animationView = view.findViewById(R.id.animation_view);
        relativeLayoutNewRace = view.findViewById(R.id.relativeLayoutNewRace);

        newRaceFragmentView = view.findViewById(R.id.new_race_fragment);

        layoutContent = view.findViewById(R.id.layoutContent);
        layoutNewRaceData = view.findViewById(R.id.layoutNewRaceData);

        bsMusic = view.findViewById(R.id.bs_music);

        flStartRace = view.findViewById(R.id.flStartRace);

        fabStartRace = view.findViewById(R.id.fabStartRace);
        fabStartRace.setOnClickListener(this);

        txtStartRace = view.findViewById(R.id.txtStartRace);
        txtStartRace.setOnClickListener(this);

        flFinishRace = view.findViewById(R.id.flFinishRace);

        fabFinishRace = view.findViewById(R.id.fabFinishRace);
        fabFinishRace.setOnClickListener(this);

        txtFinishRace = view.findViewById(R.id.txtFinishRace);
        txtFinishRace.setOnClickListener(this);

        newRaceDistanceTextView = view.findViewById(R.id.newRaceDistanceTextView);
        newRaceDistanceUnitTextView = view.findViewById(R.id.newRaceDistanceUnitTextView);

        newRaceRythmnTextView = view.findViewById(R.id.newRaceRythmnTextView);
        newRaceRythmnUnitTextView = view.findViewById(R.id.newRaceRythmnUnitTextView);

        newRaceDurationTextView = view.findViewById(R.id.newRaceDurationTextView);

        isRunning = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);

        songCoverImageViewNewRace = view.findViewById(R.id.songCoverImageViewNewRace);
        songCoverImageViewNewRace.setOnClickListener(this);

        linearLayoutNewRace = view.findViewById(R.id.llNewRace);
        linearLayoutNewRace.setOnClickListener(this);

        songTitleTextViewNewRace = view.findViewById(R.id.songTitleTextViewNewRace);
        songArtistTextViewNewRace = view.findViewById(R.id.songArtistTextViewNewRace);

        btnStepBackwardNewRace = view.findViewById(R.id.btnStepBackwardNewRace);
        btnStepBackwardNewRace.setOnClickListener(this);

        btnPlayPauseNewRace = view.findViewById(R.id.btnPlayPauseNewRace);
        btnPlayPauseNewRace.setImageResource(R.drawable.ic_play_solid);
        btnPlayPauseNewRace.setOnClickListener(this);

        btnStepForwardNewRace = view.findViewById(R.id.btnStepForwardNewRace);
        btnStepForwardNewRace.setOnClickListener(this);

        mediaPlayerManager = MediaPlayerManager.getInstance();
        mediaPlayerManager.create();
        mediaPlayerManager.getMediaPlayer().setOnCompletionListener(this);

        carrera = new Carrera();

        raceDescriptionEditText = view.findViewById(R.id.raceDescriptionEditText);
        newRaceDescriptionTV = view.findViewById(R.id.newRaceDescriptionTextView);
        newRaceDateTV = view.findViewById(R.id.newRaceDateTextView);

        SharedPrefsManager.getInstance(getActivity()).getSharedPrefs().registerOnSharedPreferenceChangeListener(this);

        AndroidNetworking.initialize(getActivity());

        return view;
    }

    private void startRace() {
        int x = layoutContent.getRight();
        int y = layoutContent.getBottom();

        int startRadius = 0;
        int endRadius = (int) Math.hypot(newRaceFragmentView.getWidth(), newRaceFragmentView.getHeight());

        Log.d(LOG_TAG, "x: " + x + ", y: " + y + ", startRadius: " + startRadius + ", endRadius: " + endRadius);

        songList = SongUtils.getMusic(Objects.requireNonNull(getActivity()).getContentResolver());

        if (mediaPlayerManager.getMediaPlayer() == null) {
            mediaPlayerManager.create();
            mediaPlayerManager.getMediaPlayer().setOnCompletionListener(this);
        }

        if (songList.size() > 0) {
            updateCurrentSongUI();
        } else {
            Log.d(LOG_TAG, "HOLA");
            songList = SongUtils.getMusicFromFirebase();
            Log.d(LOG_TAG, "songList: " + songList);
        }

        Animator anim;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(layoutContent, x, y, startRadius, endRadius);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    newRaceDateTV.setText("");
                    newRaceDescriptionTV.setText("");
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Date now = new Date();

                    carrera.setFechaCarrera(now);

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm");

                    String dateFormatted = formatter.format(now) + " hs";
                    String raceDescription = raceDescriptionEditText.getText().toString();

                    newRaceDateTV.setText(dateFormatted);
                    newRaceDescriptionTV.setText(raceDescription);
                    raceDescriptionEditText.setText("");

                    SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.IS_RUNNING_KEY, true);
                    //TODO
                    saveDateAndDescriptionToSharedPrefs(dateFormatted, raceDescription);
                    saveRaceDateInitialTimeMsToSharedPrefs(carrera.getFechaCarrera().getTime());

                    List<Punto> puntos = new ArrayList<>();

                    saveListPointsToSharedPreferences(puntos);

                    setInitialTime(carrera.getFechaCarrera().getTime());
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            animationView.setVisibility(View.GONE);
            relativeLayoutNewRace.setVisibility(View.GONE);
            flStartRace.setVisibility(View.GONE);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            anim.start();

            layoutNewRaceData.setVisibility(View.VISIBLE);
            flFinishRace.setVisibility(View.VISIBLE);
            bsMusic.setVisibility(View.VISIBLE);

            updateTimerUI();
        }
    }

    private void finishRace() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (Objects.requireNonNull(getActivity()).checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//
//            buildLocationRequest();
//            buildLocationCallback();
//
//            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
//        } else {
//            buildLocationRequest();
//            buildLocationCallback();
//
//            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
//            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
//        }

        int x = layoutContent.getRight();
        int y = layoutContent.getBottom();

        int startRadius = Math.max(layoutContent.getWidth(), layoutContent.getHeight());
        int endRadius = 0;

        Log.d(LOG_TAG, "x: " + x + ", y: " + y + ", startRadius: " + startRadius + ", endRadius: " + endRadius);

        songList = null;
        saveIdSongToSharedPreferences(
                getString(R.string.id_song_key),
                -1);

        Animator anim;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            anim = ViewAnimationUtils.createCircularReveal(layoutNewRaceData, x, y, startRadius, endRadius);
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    flFinishRace.setVisibility(View.GONE);
                    bsMusic.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    layoutNewRaceData.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            anim.start();

            animationView.setVisibility(View.VISIBLE);
            relativeLayoutNewRace.setVisibility(View.VISIBLE);
            flStartRace.setVisibility(View.VISIBLE);

            saveIsRunningVariableToSharedPreferences("is_running", false);
            saveDateAndDescriptionToSharedPrefs("", "");

            mHandlerTimer.removeCallbacks(mUpdateTimerTask);
            newRaceDurationTextView.setText("00'00''");

            if (mediaPlayerManager.getMediaPlayer() != null) {
                mediaPlayerManager.getMediaPlayer().stop();
                mediaPlayerManager.getMediaPlayer().release();
                mediaPlayerManager.setMediaPlayer(null);
            }
        }
    }

    public void updateTimerUI() {
        updateCurrentTimeTextView();
    }

    public void updateCurrentTimeTextView() {
        mHandlerTimer.postDelayed(mUpdateTimerTask, 1000);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimerTask = new Runnable() {
        public void run() {
            initialTime = getInitialTime();
            long currentTime = new Date().getTime();
            long currentDuration = currentTime - initialTime;

            newRaceDurationTextView.setText(TimeUtils.milliSecondsToTimer(currentDuration));

            Log.d(LOG_TAG, "Current Time: " + TimeUtils.milliSecondsToTimer(currentDuration));

            // Running this thread after 1000 milliseconds
            mHandlerTimer.postDelayed(this, 1000);
        }
    };

    private void saveDateAndDescriptionToSharedPrefs(String dateFormatted, String raceDescription) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("race_date", dateFormatted);
        editor.putString("race_description", raceDescription);
        editor.apply();
    }

    private String readRaceDateFromSharedPreferences() {
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        return sharedPref.getString(
                "race_date",
                "");
    }

    private String readRaceDescriptionFromSharedPreferences() {
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        return sharedPref.getString(
                "race_description",
                "");
    }

    private void updateCurrentSongUI() {
        int id = readIdSongFromSharedPreferences(ID_SONG_KEY);

        Log.d(LOG_TAG, "Id: " + id);

        if (id == -1) {
            songCoverImageViewNewRace.setImageURI(Uri.parse(songList.get(0).getSongCoverUri()));
            songTitleTextViewNewRace.setText(songList.get(0).getSongTitle());
            songArtistTextViewNewRace.setText(songList.get(0).getArtist());
        } else {
            songCoverImageViewNewRace.setImageURI(Uri.parse(songList.get(id - 1).getSongCoverUri()));
            songTitleTextViewNewRace.setText(songList.get(id - 1).getSongTitle());
            songArtistTextViewNewRace.setText(songList.get(id - 1).getArtist());
        }
        togglePlayBtn(false);
    }

    private void saveRaceDateMsInFirebaseDatabase(String registrationToken) {
        // get users ref in firebase database
        DatabaseReference usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");
        // save registration token in firebase database with child last_race_timestamp set current timestamp.

        Date date = new Date();
        long time = date.getTime();
        usersDBRef.child(registrationToken+"/last_race_date_miliseconds").setValue(time);
    }

    private int readIdSongFromSharedPreferences(String key) {
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        return sharedPref.getInt(
                key,
                getResources().getInteger(R.integer.id_song_default_value));
    }

    public void registerRace() {
        try {
            verificarCamposVacios();

            carrera.setDescripcion(raceDescriptionEditText.getText().toString());
            carrera.setDistancia(100);

            carrera.setDuracion(DateUtils.setTimeOfDate(2, 3, 2));
            carrera.setRitmo(DateUtils.setTimeOfDate(2, 3, 2));

            if (getActivity() != null) {

                try {
                    File racesJSONFile = new File(getActivity().getFilesDir(), "races_data.json");

                    RacesJSONParser.saveRaceData(getActivity(), racesJSONFile, carrera);
                    vaciarCampos();
                    Toast.makeText(getActivity(), "Se ha creado la carrera de manera exitosa!", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (RaceModelException e) {
            CoordinatorLayout coordinatorLayout = Objects.requireNonNull(getActivity()).findViewById(R.id.cl);
            Snackbar snack = Snackbar.make(coordinatorLayout, e.getMessage(), Snackbar.LENGTH_LONG);
            snack.show();
        }
    }

    private void vaciarCampos() {
        raceDescriptionEditText.setText("");
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
            stepBackward();
        }
        if (v.getId() == R.id.btnPlayPauseNewRace) {
            if (mediaPlayerManager.getDataSource() == null) {
                saveIdSongToSharedPreferences(
                        getString(R.string.id_song_key),
                        1);

                Uri uri = Uri.parse(songList.get(0).getSongUri());

                try {
                    mediaPlayerManager.getMediaPlayer().reset();
                    mediaPlayerManager.getMediaPlayer().setDataSource(Objects.requireNonNull(getActivity()), uri);
                    mediaPlayerManager.setDataSource(uri);
                    mediaPlayerManager.getMediaPlayer().prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            mediaPlayerManager.play();
            togglePlayBtn(false);
        }
        if (v.getId() == R.id.btnStepForwardNewRace) {
            stepForward();
        }
        if (v.getId() == R.id.fabStartRace || v.getId() == R.id.txtStartRace) {
//            isRunning = readIsRunningVariableFromSharedPreferences("is_running");
//
//            if (!isRunning) {
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
//            }
//            else {
//                registerRace();
//            }
        }
        if (v.getId() == R.id.fabFinishRace || v.getId() == R.id.txtFinishRace) {
            AlertDialog dialog = (AlertDialog) createDialogdRegisterRaceQuestion();
            dialog.show();
        }
//        if (v.getId() == R.id.btnShareRaceDetail) {
//            shareRaceDetails();
//        }
    }

    private void openMusicListActivity() {
        Intent musicListActivityIntent = new Intent(getActivity(), ListMusicActivity.class);
        startActivity(musicListActivityIntent);
    }

    private void togglePlayBtn(boolean completed) {
        if (completed) {
            btnPlayPauseNewRace.setImageResource(R.drawable.ic_play_solid);
        } else {

            if (mediaPlayerManager.getMediaPlayer().isPlaying()) {
                btnPlayPauseNewRace.setImageResource(R.drawable.ic_pause_solid);
            } else {
                btnPlayPauseNewRace.setImageResource(R.drawable.ic_play_solid);
            }
        }
    }

    public void setInitialTime(long initialTime) {
        this.initialTime = initialTime;
    }

    public long getInitialTime() {
        return this.initialTime;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "ON START");
        setInitialTime(readRaceDateInitialTimeMsFromSharedPreferences());
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        Log.d(LOG_TAG, "initialTime: " + getInitialTime());
        long distance = readDistanceFromSharedPreferences();
        newRaceDistanceTextView.setText(String.valueOf(distance));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "ON RESUME");
        isRunning = readIsRunningVariableFromSharedPreferences("is_running");
        Log.d(LOG_TAG, "isRunning: " + isRunning);

        if (isRunning) {
            animationView.setVisibility(View.GONE);
            relativeLayoutNewRace.setVisibility(View.GONE);
            flStartRace.setVisibility(View.GONE);

            newRaceDateTV.setText(readRaceDateFromSharedPreferences());
            newRaceDescriptionTV.setText(readRaceDescriptionFromSharedPreferences());

            initialTime = getInitialTime();
            long currentTime = new Date().getTime();
            long currentDuration = currentTime - initialTime;

            newRaceDurationTextView.setText(TimeUtils.milliSecondsToTimer(currentDuration));
//            updateTimerUI();

//            long currentDuration = readRaceDateCurrentTimeMsFromSharedPreferences();
//

            layoutNewRaceData.setVisibility(View.VISIBLE);
            flFinishRace.setVisibility(View.VISIBLE);
            bsMusic.setVisibility(View.VISIBLE);

            if (songList.size() > 0) {
                updateCurrentSongUI();
            }
//            else {
//                songList = SongUtils.getMusicFromFirebase();
//                for (Song s: songList) {
//                    Log.d(LOG_TAG, s.toString());
//                }
//            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "ON PAUSE");
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void stepBackward() {
        int id = readIdSongFromSharedPreferences(ID_SONG_KEY) - 1;
        int idSongPrev = id > 0 ? songList.get(id - 1).getId() : songList.get(songList.size() - 1).getId();

        saveIdSongToSharedPreferences(
                getString(R.string.id_song_key),
                idSongPrev);

        if (mediaPlayerManager.getMediaPlayer() != null) {
            mediaPlayerManager.getMediaPlayer().stop();
            mediaPlayerManager.getMediaPlayer().release();
            mediaPlayerManager.setMediaPlayer(null);
        }

        Uri uri = Uri.parse(songList.get(idSongPrev - 1).getSongUri());

        try {
            mediaPlayerManager.create();
            mediaPlayerManager.getMediaPlayer().reset();
            mediaPlayerManager.getMediaPlayer().setOnCompletionListener(this);
            mediaPlayerManager.getMediaPlayer().setDataSource(Objects.requireNonNull(getActivity()), uri);
            mediaPlayerManager.setDataSource(uri);
            mediaPlayerManager.getMediaPlayer().prepare();
            mediaPlayerManager.play();

            if (songList.size() > 0) {
                updateCurrentSongUI();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stepForward() {
        int id = readIdSongFromSharedPreferences(ID_SONG_KEY) + 1;
        Log.d(LOG_TAG, "ID: " + id);

        if (id > 0) {
            int idSongNext = id <= songList.size() ? songList.get(id - 1).getId() : 1;

            saveIdSongToSharedPreferences(
                    getString(R.string.id_song_key),
                    idSongNext);

            if (mediaPlayerManager.getMediaPlayer() != null) {
                mediaPlayerManager.getMediaPlayer().stop();
                mediaPlayerManager.getMediaPlayer().release();
                mediaPlayerManager.setMediaPlayer(null);
            }

            Uri uri = Uri.parse(songList.get(idSongNext - 1).getSongUri());

            try {
                mediaPlayerManager.create();
                mediaPlayerManager.getMediaPlayer().reset();
                mediaPlayerManager.getMediaPlayer().setOnCompletionListener(this);
                mediaPlayerManager.getMediaPlayer().setDataSource(Objects.requireNonNull(getActivity()), uri);
                mediaPlayerManager.setDataSource(uri);
                mediaPlayerManager.getMediaPlayer().prepare();
                mediaPlayerManager.play();

                if (songList.size() > 0) {
                    updateCurrentSongUI();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "Song finished..");
        stepForward();
//        saveSongFinishedVariableToSharedPreferences(SONG_FINISHED_KEY, true);
    }

    public Dialog createDialogdRegisterRaceQuestion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_register_race_question, null))
                // Add action buttons
                .setPositiveButton("Si", (dialog, id) -> {
                    saveRaceDurationToSharedPrefs(newRaceDurationTextView.getText().toString());
                    saveDistanceUnitToSharedPreferences(newRaceDistanceUnitTextView.getText().toString());

                    String registrationToken = readRegistrationTokenFromSharedPreferences();
                    saveRaceDateMsInFirebaseDatabase(registrationToken);
                    finishRace();

                    dialog.dismiss();

                    Intent raceRegisteredIntent = new Intent(getActivity(), RaceRegisteredActivity.class);
                    startActivity(raceRegisteredIntent);
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        return builder.create();
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10);
        locationRequest.setNumUpdates(1);
        locationRequest.setExpirationTime(2000);
        locationRequest.setFastestInterval(10);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Punto p = new Punto(UUID.randomUUID(), locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                List<Punto> puntos = readListPointsFromSharedPreferences();
                puntos.add(p);
                saveListPointsToSharedPreferences(puntos);

                Toast.makeText(getActivity(), "Punto " + puntos.size() + ": " + p.toString(), Toast.LENGTH_LONG).show();

                if (puntos.size() > 1) {
                    String origin = puntos.get(puntos.size()-1).getLat() + "," + puntos.get(puntos.size()-1).getLon();
                    String destination = puntos.get(puntos.size()-2).getLat() + "," + puntos.get(puntos.size()-2).getLon();

                    AndroidNetworking.get("https://maps.googleapis.com/maps/api/directions/json?origin={origin}&destination={destination}&mode={mode}&key={key}")
                            .addPathParameter("origin", origin)
                            .addPathParameter("destination", destination)
                            .addPathParameter("mode", "walking")
                            .addPathParameter("key", "AIzaSyA15bpgte2SVrhimPmJJKF65rDo01lPP0E")
                            .setTag("test")
                            .setPriority(Priority.HIGH)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        JSONObject routes = response.getJSONArray("routes").getJSONObject(0);
                                        JSONObject legs = (JSONObject) routes.getJSONArray("legs").get(0);
                                        JSONObject distance = legs.getJSONObject("distance");
                                        long distanceAccumulated = readDistanceFromSharedPreferences();
                                        long newDistance = distanceAccumulated + distance.getLong("value");

                                        Toast.makeText(getActivity(), "Total distance: " + newDistance, Toast.LENGTH_LONG).show();

                                        saveDistanceToSharedPreferences(newDistance);
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
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startRace();
                    }
                } else {
                    Objects.requireNonNull(getActivity()).finish();
                }
                return;
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        long distance = readDistanceFromSharedPreferences();
        float rythmn = readRythmnFromSharedPreferences();
        if (key.equals(RACE_CURRENT_DISTANCE_KEY)) {
            newRaceDistanceTextView.setText(String.valueOf(distance));
        }
        if (key.equals(RACE_CURRENT_RYTHMN_KEY)) {
//            newRaceRythmnTextView.setText(String.format("%.2f", rythmn));
            newRaceRythmnTextView.setText(rythmn+"");
        }
    }
}