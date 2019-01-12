package com.tecnologiasmoviles.iua.fitmusic.view;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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

    /* LAYOUT FIELDS */

    private EditText raceDescriptionEditText;
    private TextView newRaceDescriptionTV;
    private TextView newRaceDateTV;

    private MediaPlayerManager mediaPlayerManager;

    private ImageButton btnPlayPauseNewRace;

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

    public NewRaceFragment() {}

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
        btnStepBackwardNewRace.setOnClickListener(this);

        btnPlayPauseNewRace = view.findViewById(R.id.btnPlayPauseNewRace);
        btnPlayPauseNewRace.setImageResource(R.drawable.ic_play_solid);
        btnPlayPauseNewRace.setOnClickListener(this);

        ImageButton btnStepForwardNewRace = view.findViewById(R.id.btnStepForwardNewRace);
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
                    SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.IS_RUNNING_KEY, true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    Date now = new Date();

                    carrera.setFechaCarrera(now);

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterDateTime = new SimpleDateFormat("dd/MM/yyyy - HH:mm");
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat formatterTime = new SimpleDateFormat("HH:mm");

                    String dateFormattedDateTime = formatterDateTime.format(now) + " hs";
                    String raceDescription = raceDescriptionEditText.getText().toString();

                    String dateFormattedTime = formatterTime.format(now) + " hs";

                    SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY, dateFormattedTime);
                    SharedPrefsManager.getInstance(getActivity()).saveLong(SharedPrefsKeys.LAST_UPDATE_TIME_MS_KEY, now.getTime());

                    newRaceDateTV.setText(dateFormattedDateTime);
                    newRaceDescriptionTV.setText(raceDescription);
                    raceDescriptionEditText.setText("");

                    SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.RACE_DATE_STRING_KEY, dateFormattedDateTime);
                    SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.RACE_DESCRIPTION_KEY, raceDescription);

                    SharedPrefsManager.getInstance(getActivity()).saveLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY, carrera.getFechaCarrera().getTime());

                    List<Punto> puntos = new ArrayList<>();

                    SharedPrefsManager.getInstance(getActivity()).saveListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY, puntos);

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
        int x = layoutContent.getRight();
        int y = layoutContent.getBottom();

        int startRadius = Math.max(layoutContent.getWidth(), layoutContent.getHeight());
        int endRadius = 0;

        Log.d(LOG_TAG, "x: " + x + ", y: " + y + ", startRadius: " + startRadius + ", endRadius: " + endRadius);

        songList = null;
        SharedPrefsManager.getInstance(getActivity()).saveInt(SharedPrefsKeys.ID_SONG_KEY, -1);

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

            SharedPrefsManager.getInstance(getActivity()).saveBoolean(SharedPrefsKeys.IS_RUNNING_KEY, false);
            SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.RACE_DATE_STRING_KEY, "");
            SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.RACE_DESCRIPTION_KEY, "");
            SharedPrefsManager.getInstance(getActivity()).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, 0);

            mHandlerTimer.removeCallbacks(mUpdateTimerTask);
            newRaceDurationTextView.setText(getString(R.string.initialRaceDurationString));
            lastUpdateTextView.setText("");

            if (mediaPlayerManager.getMediaPlayer() != null) {
                mediaPlayerManager.getMediaPlayer().stop();
                mediaPlayerManager.getMediaPlayer().release();
                mediaPlayerManager.setMediaPlayer(null);
            }
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

    private void updateCurrentSongUI() {
        int id = SharedPrefsManager.getInstance(getActivity()).readInt(SharedPrefsKeys.ID_SONG_KEY);

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
        togglePlayBtn();
    }

    private void saveRaceDateMsInFirebaseDatabase(String registrationToken) {
        // get users ref in firebase database
        DatabaseReference usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");
        // save registration token in firebase database with child last_race_timestamp set current timestamp.

        Date date = new Date();
        long time = date.getTime();
        usersDBRef.child(registrationToken+"/last_race_date_miliseconds").setValue(time);
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
                SharedPrefsManager.getInstance(getActivity()).saveInt(SharedPrefsKeys.ID_SONG_KEY, 1);

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
            togglePlayBtn();
        }
        if (v.getId() == R.id.btnStepForwardNewRace) {
            stepForward();
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
            AlertDialog dialog = (AlertDialog) createDialogdRegisterRaceQuestion();
            dialog.show();
        }
    }

    private void openMusicListActivity() {
        Intent musicListActivityIntent = new Intent(getActivity(), ListMusicActivity.class);
        startActivity(musicListActivityIntent);
    }

    private void togglePlayBtn() {
        if (mediaPlayerManager.getMediaPlayer().isPlaying()) {
            btnPlayPauseNewRace.setImageResource(R.drawable.ic_pause_solid);
        } else {
            btnPlayPauseNewRace.setImageResource(R.drawable.ic_play_solid);
        }
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
        Log.d(LOG_TAG, "ON START");
        setInitialTime(SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY));
        SharedPrefsManager.getInstance(getActivity()).getSharedPrefs().registerOnSharedPreferenceChangeListener(this);
        Log.d(LOG_TAG, "initialTime: " + getInitialTime());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "ON RESUME");
        boolean isRunning = SharedPrefsManager.getInstance(getActivity()).readBoolean(SharedPrefsKeys.IS_RUNNING_KEY);
        Log.d(LOG_TAG, "isRunning: " + isRunning);

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

            if (songList.size() > 0) {
                updateCurrentSongUI();
            }

            long distance = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
            float distanceToKms = (distance/1000f);
            newRaceDistanceTextView.setText(String.format("%.2f", distanceToKms));

            String lastUpdateTime = SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY);
            lastUpdateTextView.setText("Ultima actualización: " + lastUpdateTime);

            long rythmn = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);
            if (rythmn > 0) {
                newRaceRythmnTextView.setText(TimeUtils.milliSecondsToTimer(rythmn));
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "ON PAUSE");
        SharedPrefsManager.getInstance(getActivity()).getSharedPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void stepBackward() {
        int id = SharedPrefsManager.getInstance(getActivity()).readInt(SharedPrefsKeys.ID_SONG_KEY) - 1;
        int idSongPrev = id > 0 ? songList.get(id - 1).getId() : songList.get(songList.size() - 1).getId();

        SharedPrefsManager.getInstance(getActivity()).saveInt(SharedPrefsKeys.ID_SONG_KEY, idSongPrev);

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

    private void stepForward() {
        int id = SharedPrefsManager.getInstance(getActivity()).readInt(SharedPrefsKeys.ID_SONG_KEY) + 1;

        // The user step forward with no songs played before
        if (id == 0) {
            id = 2;
        }

        if (id >= 0) {
            int idSongNext = id <= songList.size() ? songList.get(id - 1).getId() : 1;

            SharedPrefsManager.getInstance(getActivity()).saveInt(SharedPrefsKeys.ID_SONG_KEY, idSongNext);

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

    private Dialog createDialogdRegisterRaceQuestion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_register_race_question, null))
                // Add action buttons
                .setPositiveButton("Si", (dialog, id) -> {
                    SharedPrefsManager.getInstance(getActivity()).saveString(SharedPrefsKeys.RACE_DURATION_KEY, newRaceDurationTextView.getText().toString());

                    String registrationToken = SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.REGISTRATION_TOKEN_KEY);
                    saveRaceDateMsInFirebaseDatabase(registrationToken);
                    finishRace();

                    dialog.dismiss();

                    Intent raceRegisteredIntent = new Intent(getActivity(), RaceRegisteredActivity.class);
                    startActivity(raceRegisteredIntent);
                })
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        return builder.create();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        startRace();
                        return;
                    }
                } else {
                    Objects.requireNonNull(getActivity()).finish();
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        long distance = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY);
        long rythmn = SharedPrefsManager.getInstance(getActivity()).readLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY);
        if (key.equals(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY)) {
            float distanceToKms = (distance/1000f);
            newRaceDistanceTextView.setText(String.format("%.2f", distanceToKms));

            if (distance >= 1000) {
                newRaceRythmnTextView.setEnabled(true);
                newRaceRythmnUnitTextView.setEnabled(true);
            }
        }
        if (key.equals(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY)) {
            newRaceRythmnTextView.setText(TimeUtils.milliSecondsToTimer(rythmn));
        }
        if (key.equals(SharedPrefsKeys.LAST_UPDATE_TIME_KEY)) {
            String lastUpdateTime = SharedPrefsManager.getInstance(getActivity()).readString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY);
            lastUpdateTextView.setText("Ultima actualización: " + lastUpdateTime);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(LOG_TAG, "Song finished..");
        stepForward();
    }

}