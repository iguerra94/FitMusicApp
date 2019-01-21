package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

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

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void create(Context context, int resId) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.getApplicationContext(), resId);
        }
    }

    public void setDataSource(Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
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

    public void play() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

}