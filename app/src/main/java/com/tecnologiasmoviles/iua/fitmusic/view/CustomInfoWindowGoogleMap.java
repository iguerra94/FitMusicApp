package com.tecnologiasmoviles.iua.fitmusic.view;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.tecnologiasmoviles.iua.fitmusic.R;

public class CustomInfoWindowGoogleMap implements GoogleMap.InfoWindowAdapter {

    private Context context;

    public CustomInfoWindowGoogleMap(Context ctx){
        context = ctx;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.info_window_view, null);

        TextView infoWindowContent = view.findViewById(R.id.infoWindowContent);

        infoWindowContent.setText(marker.getTitle());

        return view;
    }
}
 