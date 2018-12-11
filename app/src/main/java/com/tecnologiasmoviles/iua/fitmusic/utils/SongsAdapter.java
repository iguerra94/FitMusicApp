package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tecnologiasmoviles.iua.fitmusic.BuildConfig;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Song;

import java.util.List;

public class SongsAdapter extends BaseAdapter {

    private Context mContext;
    private List<Song> songsList;

    public SongsAdapter(Context context, List<Song> songsList) {
        this.mContext = context;
        this.songsList = songsList;
    }

    @Override
    public int getCount() {
        return songsList.size();
    }

    @Override
    public Object getItem(int position) {
        return songsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.song_list_cell, parent, false);
        }

        final Song song = songsList.get(position);

        // Song Cover
        ImageView songCoverImageView = convertView.findViewById(R.id.songCoverImageView);
        songCoverImageView.setImageURI(Uri.parse(song.getSongCoverUri()));

        // Song Title
        TextView songTitleTextView = convertView.findViewById(R.id.songTitleTextView);
        songTitleTextView.setText(song.getSongTitle());

        // Song Artist
        TextView songArtistTextView = convertView.findViewById(R.id.songArtistTextView);
        songArtistTextView.setText(song.getArtist());

        // Song Duration
        TextView songDurationTextView = convertView.findViewById(R.id.songDurationTextView);
        songDurationTextView.setText(TimeUtils.milliSecondsToTimer(song.getSongDurationMs()));

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveIdSongToSharedPreferences(
                        mContext.getString(R.string.id_song_key),
                        song.getId());

//                int id = readIdSongFromSharedPreferences(mContext.getString(R.string.id_song_key));
//                Toast.makeText(mContext, "ID CHANGED: " + id, Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    private void saveIdSongToSharedPreferences(String key, int value) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(key, value);
        editor.apply();
    }

    private int readIdSongFromSharedPreferences(String key) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(
                BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        return sharedPref.getInt(
                key,
                mContext.getResources().getInteger(R.integer.id_song_default_value));
    }

}