package com.tecnologiasmoviles.iua.fitmusic.view;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Song;
import com.tecnologiasmoviles.iua.fitmusic.utils.MediaPlayerManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.SongsAdapter;
import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import de.hdodenhof.circleimageview.CircleImageView;

public class ListMusicActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = ListMusicActivity.class.getSimpleName();

    private MediaPlayerManager mediaPlayerManager;

    List<Song> songList;

    ListView listViewSongs;

    CircleImageView songCoverImageViewListMusic;
    LinearLayout linearLayoutListMusic;
    TextView songTitleTextViewListMusic;
    TextView songArtistTextViewListMusic;
    TextView songProgressDurationTextViewListMusic;
    TextView songTotalDurationTextViewListMusic;

    private int idSong;
    private int idLastSong;

    private static long currentDurationGlobal;
    private static long totalDuration;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_music);

        Toolbar myToolbarListMusic = findViewById(R.id.my_toolbar_list_music_activity);
        setSupportActionBar(myToolbarListMusic);

        assert getSupportActionBar() != null;

        getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_music_list));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idSong = -1;
        idLastSong = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY);

        Log.d(LOG_TAG, "idSong: " + idSong + ", idLastSong: " + idLastSong);

        songCoverImageViewListMusic = findViewById(R.id.songCoverImageViewListMusic);
        linearLayoutListMusic = findViewById(R.id.linearLayoutListMusic);
        songTitleTextViewListMusic = findViewById(R.id.songTitleTextViewListMusic);
        songArtistTextViewListMusic = findViewById(R.id.songArtistTextViewListMusic);
        songProgressDurationTextViewListMusic = findViewById(R.id.songProgressDurationTextViewListMusic);

        songTotalDurationTextViewListMusic = findViewById(R.id.songTotalDurationTextViewListMusic);

        mediaPlayerManager = MediaPlayerManager.getInstance();
        mediaPlayerManager.create();

        doStuff();

        SharedPrefsManager.getInstance(this).getSharedPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    public void doStuff() {
        listViewSongs = findViewById(R.id.listView);
        songList = new ArrayList<>();
        getMusic();
        SongsAdapter songsAdapter = new SongsAdapter(this, songList);
//        for (Song s: songList) {
//            Log.d(LOG_TAG, s.toString());
//        }
        listViewSongs.setAdapter(songsAdapter);

        updateCurrentSongUI();
    }

    private void updateCurrentSongUI() {
        int id = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY);

        Log.d(LOG_TAG, "ID: " + id);

        if (id == -1) {
            songCoverImageViewListMusic.setImageURI(Uri.parse(songList.get(0).getSongCoverUri()));
            songTitleTextViewListMusic.setText(songList.get(0).getSongTitle());
            songArtistTextViewListMusic.setText(songList.get(0).getArtist());
            updateCurrentDurationTextView();
            totalDuration = songList.get(0).getSongDurationMs();
            songTotalDurationTextViewListMusic.setText(TimeUtils.milliSecondsToTimer(totalDuration));

//            saveIdSongToSharedPreferences(
//                    getString(R.string.id_song_key),
//                    1);
//            id = readIdSongFromSharedPreferences(ID_SONG_KEY);
        } else {
            songCoverImageViewListMusic.setImageURI(Uri.parse(songList.get(id-1).getSongCoverUri()));
            songTitleTextViewListMusic.setText(songList.get(id-1).getSongTitle());
            songArtistTextViewListMusic.setText(songList.get(id-1).getArtist());
            updateCurrentDurationTextView();
            totalDuration = songList.get(id-1).getSongDurationMs();
            songTotalDurationTextViewListMusic.setText(TimeUtils.milliSecondsToTimer(totalDuration));
        }

    }

    public void getMusic() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor songCursor = contentResolver.query(
                songUri,
                null,
                null,
                null,
                null);

        Uri musicDirUri = Uri.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int songAlbumId = songCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int songDurationMs = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            int id = 0;
            do {
                String currentLocation = songCursor.getString(songLocation);

                if (currentLocation.contains(musicDirUri.getPath())) {
                    String currentTitle = songCursor.getString(songTitle);
                    String currentArtist = songCursor.getString(songArtist);
                    int currentAlbumId = songCursor.getInt(songAlbumId);
                    String currentAlbumArt = getAlbumArtUri(currentAlbumId);
                    long currentSongDurationMs = songCursor.getLong(songDurationMs);

                    if (currentAlbumArt != null) {
                        Song s = new Song(++id, currentTitle, currentArtist, currentAlbumId, currentAlbumArt, currentLocation, currentSongDurationMs);
                        songList.add(s);
                    }
                }
            } while (songCursor.moveToNext());
        }

    }

    private String getAlbumArtUri(int albumId) {
        Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor albumCursor = getContentResolver().query(
                albumUri,
                null,
                MediaStore.Audio.Albums._ID + " = ?",
                new String[]{String.valueOf(albumId)},
                null);

        String currentAlbumArt = null;

        if (albumCursor != null && albumCursor.moveToFirst()) {
            int songAlbumArt = albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            currentAlbumArt = albumCursor.getString(songAlbumArt);
        }
        return currentAlbumArt;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateCurrentDurationTextView() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mediaPlayerManager.getMediaPlayer() != null) {
                long currentDuration = mediaPlayerManager.getMediaPlayer().getCurrentPosition();
                currentDurationGlobal = (currentDuration > 0) ? currentDuration: 0;
                // Displaying time completed playing
                songProgressDurationTextViewListMusic.setText(TimeUtils.milliSecondsToTimer(currentDurationGlobal));

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);

                Log.d(LOG_TAG, currentDurationGlobal + ", " + totalDuration);
                if (currentDurationGlobal >= totalDuration) {
                    Log.d(LOG_TAG, "Song finished..");
                    stepForward();
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentSongUI();
        SharedPrefsManager.getInstance(this).getSharedPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPrefsManager.getInstance(this).getSharedPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void stepForward() {
        int id = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY) + 1;

        Log.d(LOG_TAG, "ID: " + id);
        if (id > 0) {
            int idSongNext = id <= songList.size() ? songList.get(id - 1).getId() : 1;

            SharedPrefsManager.getInstance(this).saveInt(SharedPrefsKeys.ID_SONG_KEY, idSongNext);

            if (mediaPlayerManager.getMediaPlayer() != null) {
                mediaPlayerManager.getMediaPlayer().stop();
                mediaPlayerManager.getMediaPlayer().release();
                mediaPlayerManager.setMediaPlayer(null);
            }

            Uri uri = Uri.parse(songList.get(idSongNext-1).getSongUri());

            try {
                mediaPlayerManager.create();
                mediaPlayerManager.getMediaPlayer().reset();
                mediaPlayerManager.getMediaPlayer().setDataSource(this, uri);
                mediaPlayerManager.setDataSource(uri);
                mediaPlayerManager.getMediaPlayer().prepare();
                mediaPlayerManager.play();

                updateCurrentSongUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy: List");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPrefsKeys.ID_SONG_KEY)) {
            updateCurrentSongUI();

            int id = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY);
            idSong = songList.get(id-1).getId();

            Log.d(LOG_TAG, "idSong: " + id + ", idLastSong: " + idLastSong);

            Uri uri = Uri.parse(songList.get(id-1).getSongUri());

            try {
                if (idLastSong != idSong) {
                    if (mediaPlayerManager.getMediaPlayer() != null) {
                        mediaPlayerManager.getMediaPlayer().stop();
                        mediaPlayerManager.getMediaPlayer().release();
                        mediaPlayerManager.setMediaPlayer(null);
                    }
                }

                mediaPlayerManager.create();
                mediaPlayerManager.getMediaPlayer().setDataSource(this, uri);
                mediaPlayerManager.setDataSource(uri);
                mediaPlayerManager.getMediaPlayer().prepare();
                mediaPlayerManager.play();

                idLastSong = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        else if (key.equals(SONG_FINISHED_KEY)) {
//            boolean songFinished = readSongFinishedFromSharedPreferences(SONG_FINISHED_KEY);
//
//            Log.d(LOG_TAG, "songFinished: " + songFinished);
//
//            if (songFinished) {
//                Log.d(LOG_TAG, "Song finished..");
//                stepForward();
//                saveSongFinishedVariableToSharedPreferences(SONG_FINISHED_KEY, false);
//            }
//        }
    }

}