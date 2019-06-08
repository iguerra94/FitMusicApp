package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tecnologiasmoviles.iua.fitmusic.model.Song;
import com.tecnologiasmoviles.iua.fitmusic.view.NewRaceFragment;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MediaPlayerManager {

    private static MediaPlayerManager manager;
    private MediaPlayer mediaPlayer;
    private Uri dataSource;

    public static MediaPlayerManager getInstance() {
        if (manager == null) {
            manager = new MediaPlayerManager();
        }
        return manager;
    }

    public static void finishMediaPlayer() {
        if (manager.getMediaPlayer() != null) {
            manager.getMediaPlayer().stop();
            manager.getMediaPlayer().release();
            manager.setMediaPlayer(null);
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void setDataSource(Uri uri) throws IllegalArgumentException, SecurityException, IllegalStateException {
        this.dataSource = uri;
    }

    public Uri getDataSource() {
        return this.dataSource;
    }

    public void create() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
    }

    public static void play() {
        MediaPlayerManager.getInstance().getMediaPlayer().start();
    }

    public static void pause() {
        MediaPlayerManager.getInstance().getMediaPlayer().pause();
    }

    public static void initPlayer(Context context) {
        if (manager.getDataSource() == null) {
            Song firstSong = SharedPrefsManager.getInstance(context).readListSongs(SharedPrefsKeys.LIST_SONGS_KEY).get(0);

            SharedPrefsManager.getInstance(context).saveInt(SharedPrefsKeys.ID_SONG_KEY, 1);

            Uri uri = Uri.parse(firstSong.getSongUri());

            try {
                manager.getMediaPlayer().reset();
                manager.getMediaPlayer().setDataSource(Objects.requireNonNull(context), uri);
                manager.setDataSource(uri);
                manager.getMediaPlayer().prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void stepBackward(Context context) {
        List<Song> songList = SharedPrefsManager.getInstance(context).readListSongs(SharedPrefsKeys.LIST_SONGS_KEY);

        int id = SharedPrefsManager.getInstance(context).readInt(SharedPrefsKeys.ID_SONG_KEY) - 1;
        int idSongPrev = id > 0 ? songList.get(id - 1).getId() : songList.get(songList.size() - 1).getId();

        SharedPrefsManager.getInstance(context).saveInt(SharedPrefsKeys.ID_SONG_KEY, idSongPrev);

        if (manager.getMediaPlayer() != null) {
            manager.getMediaPlayer().stop();
            manager.getMediaPlayer().release();
            manager.setMediaPlayer(null);
        }

        Uri uri = Uri.parse(songList.get(idSongPrev - 1).getSongUri());

        try {
            manager.create();
            manager.getMediaPlayer().reset();
            manager.getMediaPlayer().setDataSource(context, uri);
            manager.setDataSource(uri);
            manager.getMediaPlayer().prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stepForward(Context context) {
        List<Song> songList = SharedPrefsManager.getInstance(context).readListSongs(SharedPrefsKeys.LIST_SONGS_KEY);

        int id = SharedPrefsManager.getInstance(context).readInt(SharedPrefsKeys.ID_SONG_KEY) + 1;

        // The user step forward with no songs played before
        if (id == 0) {
            id = 2;
        }

        if (id >= 0) {
            int idSongNext = id <= songList.size() ? songList.get(id - 1).getId() : 1;

            SharedPrefsManager.getInstance(context).saveInt(SharedPrefsKeys.ID_SONG_KEY, idSongNext);

            if (manager.getMediaPlayer() != null) {
                manager.getMediaPlayer().stop();
                manager.getMediaPlayer().release();
                manager.setMediaPlayer(null);
            }

            Uri uri = Uri.parse(songList.get(idSongNext - 1).getSongUri());

            try {
                manager.create();
                manager.getMediaPlayer().reset();
                manager.getMediaPlayer().setDataSource(context, uri);
                manager.setDataSource(uri);
                manager.getMediaPlayer().prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}