package com.tecnologiasmoviles.iua.fitmusic.view;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
    TextView songTitleTextViewListMusic;
    TextView songArtistTextViewListMusic;
    TextView songProgressDurationTextViewListMusic;
    TextView songTotalDurationTextViewListMusic;

    private int idCurrentSong;
    private int idLastSong;

    private static long totalDuration;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_music);

        setupToolbar();

        bindViews();

        idCurrentSong = -1;
        idLastSong = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY);

        mediaPlayerManager = MediaPlayerManager.getInstance();
        mediaPlayerManager.create();

        setupListViewSongsAdapter();

        updateCurrentSongUI();

        SharedPrefsManager.getInstance(this).getSharedPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    private void setupToolbar() {
        Toolbar myToolbarListMusic = findViewById(R.id.my_toolbar_list_music_activity);
        setSupportActionBar(myToolbarListMusic);

        assert getSupportActionBar() != null;

        getSupportActionBar().setTitle(getResources().getString(R.string.toolbar_title_music_list));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void bindViews() {
        songCoverImageViewListMusic = findViewById(R.id.songCoverImageViewListMusic);
        songTitleTextViewListMusic = findViewById(R.id.songTitleTextViewListMusic);
        songArtistTextViewListMusic = findViewById(R.id.songArtistTextViewListMusic);
        songProgressDurationTextViewListMusic = findViewById(R.id.songProgressDurationTextViewListMusic);
        songTotalDurationTextViewListMusic = findViewById(R.id.songTotalDurationTextViewListMusic);

        listViewSongs = findViewById(R.id.listView);
    }

    public void setupListViewSongsAdapter() {
        songList = SharedPrefsManager.getInstance(this).readListSongs(SharedPrefsKeys.LIST_SONGS_KEY);
        SongsAdapter songsAdapter = new SongsAdapter(this, songList);
        listViewSongs.setAdapter(songsAdapter);
    }

    private void updateCurrentSongUI() {
        int id = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY);

        if (id == -1) {
            songCoverImageViewListMusic.setImageURI(Uri.parse(songList.get(0).getSongCoverUri()));
            songTitleTextViewListMusic.setText(songList.get(0).getSongTitle());
            songArtistTextViewListMusic.setText(songList.get(0).getArtist());
            updateCurrentDurationTextView();
            totalDuration = songList.get(0).getSongDurationMs();
            songTotalDurationTextViewListMusic.setText(TimeUtils.milliSecondsToTimer(totalDuration));
        } else {
            songCoverImageViewListMusic.setImageURI(Uri.parse(songList.get(id-1).getSongCoverUri()));
            songTitleTextViewListMusic.setText(songList.get(id-1).getSongTitle());
            songArtistTextViewListMusic.setText(songList.get(id-1).getArtist());
            updateCurrentDurationTextView();
            totalDuration = songList.get(id-1).getSongDurationMs();
            songTotalDurationTextViewListMusic.setText(TimeUtils.milliSecondsToTimer(totalDuration));
        }
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

                long currentDurationGlobal = (currentDuration > 0 && currentDuration < totalDuration) ? currentDuration : 0;
                // Displaying time completed playing
                songProgressDurationTextViewListMusic.setText(TimeUtils.milliSecondsToTimer(currentDurationGlobal));

                Log.d(LOG_TAG, currentDurationGlobal + ", " + totalDuration);
                if (currentDurationGlobal >= totalDuration) {
                    Log.d(LOG_TAG, "Song finished..");
                    MediaPlayerManager.stepForward(ListMusicActivity.this);
                }

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SharedPrefsKeys.ID_SONG_KEY)) {
            updateCurrentSongUI();

            int id = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY);
            idCurrentSong = songList.get(id-1).getId();

            Uri uri = Uri.parse(songList.get(id-1).getSongUri());

            try {
                if (idLastSong != idCurrentSong) {
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
//                mediaPlayerManager.togglePlayPause(this);

                idLastSong = SharedPrefsManager.getInstance(this).readInt(SharedPrefsKeys.ID_SONG_KEY);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}