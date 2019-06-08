package com.tecnologiasmoviles.iua.fitmusic.utils.asyncTasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.tecnologiasmoviles.iua.fitmusic.model.Song;
import com.tecnologiasmoviles.iua.fitmusic.utils.ImageUtils;
import com.tecnologiasmoviles.iua.fitmusic.utils.MediaPlayerManager;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsKeys;
import com.tecnologiasmoviles.iua.fitmusic.utils.SharedPrefsManager;

import java.util.List;

public class DownloadFilesAsyncTask extends AsyncTask<List<Song>, Void, List<Song>> {

    private Context context;

    public DownloadFilesAsyncTask(Context context) {
        this.context=context;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected List<Song> doInBackground(List<Song>... songs) {
        for (int i = 0; i < songs[0].size(); i++) {
            String songCoverUri = songs[0].get(i).getSongCoverUri();
            String songUri = songs[0].get(i).getSongUri();

            String name = Uri.parse(songCoverUri).getLastPathSegment().split("/")[2];
            Bitmap coverBitmap = ImageUtils.getBitmapFromURL(songCoverUri);

            String coverPath = ImageUtils.saveImage(context, name, coverBitmap);
            String songPath = FileUtils.saveFile(songUri);

            songs[0].get(i).setSongCoverUri(coverPath);
            songs[0].get(i).setSongUri(songPath);
        }
        return songs[0];
    }

    @Override
    protected void onPostExecute(List<Song> songs) {
        SharedPrefsManager.getInstance(context).saveListSongs(SharedPrefsKeys.LIST_SONGS_KEY, songs);
        MediaPlayerManager.initPlayer(context);
        //dialogLoadingMusicFromFirebase.dismiss();
        Toast.makeText(context, "Las canciones se cargaron correctamente!", Toast.LENGTH_SHORT).show();
    }

}
