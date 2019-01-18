package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;
import com.tecnologiasmoviles.iua.fitmusic.view.DetailRaceActivity;

import java.text.SimpleDateFormat;
import java.util.List;

public class RacesAdapter extends BaseAdapter {

    private static final String LOG_TAG = "RacesAdapter";

    private Context mContext;
    private List<Carrera> racesList;

    public RacesAdapter(Context context, List<Carrera> racesList) {
        this.mContext = context;
        this.racesList = racesList;
    }

    @Override
    public int getCount() {
        return racesList.size();
    }

    @Override
    public Object getItem(int position) {
        return racesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.race_list_cell, parent, false);
        }

        final Carrera carrera = racesList.get(position);
        Log.d(LOG_TAG, carrera.toString());

        // Race Distance
        TextView raceDistanceTextView = convertView.findViewById(R.id.raceDistanceTextView);

        float raceDistanceToKms = carrera.getDistancia() / 1000f;
        raceDistanceTextView.setText(String.format("%.2f", raceDistanceToKms));

        // Race Duration
        TextView raceDurationTextView = convertView.findViewById(R.id.raceDurationTextView);
        raceDurationTextView.setText(TimeUtils.milliSecondsToTimer(carrera.getDuracion()));

        // Race Date
        TextView raceDateTextView = convertView.findViewById(R.id.raceDateTextView);
        raceDateTextView.setText(new SimpleDateFormat("dd/MM/yyyy").format(carrera.getFechaCarrera()));

        // Navigation to Player Activity
        convertView.setOnClickListener(v -> goToRaceDetailActivity(carrera));

        return convertView;
    }

    private void goToRaceDetailActivity(Carrera carrera) {
        Intent detailActivityIntent = new Intent(mContext, DetailRaceActivity.class);
        detailActivityIntent.putExtra("raceData", carrera);
        mContext.startActivity(detailActivityIntent);
    }
}