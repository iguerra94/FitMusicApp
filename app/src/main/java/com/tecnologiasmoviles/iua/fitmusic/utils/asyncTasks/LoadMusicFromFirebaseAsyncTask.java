package com.tecnologiasmoviles.iua.fitmusic.utils.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.tecnologiasmoviles.iua.fitmusic.model.Song;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.SongUtils;

import java.util.List;

import androidx.appcompat.app.AlertDialog;

/*
public class LoadMusicFromFirebaseAsyncTask extends AsyncTask<Void, String, List<Song>> implements SongUtils.FinishedLoadingMusicCallback {

    private Context context;

    public LoadMusicFromFirebaseAsyncTask(Context ctx) {
        this.context = ctx;
    }

    @Override
    protected void onPreExecute() {
        dialogLoadingMusicFromFirebase = (AlertDialog) createDialogLoadingMusicFromFirebase();
        dialogLoadingMusicFromFirebase.setCancelable(false);
        dialogLoadingMusicFromFirebase.show();
    }

    @Override
    protected List<Song> doInBackground(Void... voids) {
        SongUtils.getMusicFromFirebase(context, this);

        List<Song> songs;

        do {
            songs = SharedPrefsManager.getInstance(context).readListSongs(SharedPrefsKeys.LIST_SONGS_KEY);
        } while (songs == null);

        return songs;
    }

    @Override
    protected void onPostExecute(List<Song> songs) {
        new DownloadFilesAsyncTask().execute(songs);
    }

    @Override
    public void onFinishedLoadingMusicCallback(List<Song> songs) {
        SharedPrefsManager.getInstance(context).saveListSongs(SharedPrefsKeys.LIST_SONGS_KEY, songs);
    }

}
*/
