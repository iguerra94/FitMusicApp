package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.tecnologiasmoviles.iua.fitmusic.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SongUtils {

    private static final String LOG_TAG = SongUtils.class.getSimpleName();

    private static final String SONGS_COLLECTION_KEY = "songs";

    public static List<Song> getMusic(ContentResolver cResolver) {
        List<Song> songList = new ArrayList<>();

        ContentResolver contentResolver = cResolver;
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
                    String currentAlbumArt = getAlbumArtUri(currentAlbumId, contentResolver);
                    long currentSongDurationMs = songCursor.getLong(songDurationMs);

                    if (currentAlbumArt != null) {
                        Song s = new Song(++id, currentTitle, currentArtist, currentAlbumId, currentAlbumArt, currentLocation, currentSongDurationMs);
                        songList.add(s);
                    }
                }
            } while (songCursor.moveToNext());
        }

        return songList;
    }

    public static void getMusicFromFirebase(Context context, FinishedLoadingMusicCallback finishedLoadingMusicCallback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection(SONGS_COLLECTION_KEY)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Song> songList = new ArrayList<>();
                        int id = 0;

                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            String title = document.getString("title");
                            String artist = document.getString("artist");
                            String songUri = document.getString("songUri");
                            String songCoverUri = document.getString("songCoverUri");
                            Long songDurationMs = document.getLong("songDurationMs");

                            Song s = new Song(++id, title, artist, 0, songCoverUri, songUri, songDurationMs);
                            songList.add(s);
                        }
                        finishedLoadingMusicCallback.onFinishedLoadingMusicCallback(songList);
                    } else {
                        Log.w(LOG_TAG, "Error getting documents.", task.getException());
                    }
                });
    }

    public interface FinishedLoadingMusicCallback {
        void onFinishedLoadingMusicCallback(List<Song> songs);
    }

    private static String getAlbumArtUri(int albumId, ContentResolver cResolver) {
        Uri albumUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        Cursor albumCursor = cResolver.query(
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


}
