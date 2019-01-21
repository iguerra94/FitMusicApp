package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.ui.IconGenerator;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;

import java.util.Arrays;
import java.util.List;

import androidx.core.content.ContextCompat;

import static android.graphics.Typeface.BOLD;
import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

public class MapsUtils {

    private static final int COLOR_NORMAL_SECTION_ARGB = 0xff03A9F4;
    private static final int COLOR_FASTEST_SECTION_ARGB = 0xff9C27B0;

    private static final int POLYLINE_STROKE_WIDTH_PX = 16;
    private static final int PATTERN_GAP_LENGTH_PX = 20;

    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);

    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    /**
     * Styles the polyline, based on type.
     * @param polyline The polyline object that needs styling.
     */
    public static void stylePolyline(Polyline polyline) {
        String type = "";
        // Get the data object stored with the polyline.
        if (polyline.getTag() != null) {
            type = polyline.getTag().toString();
        }

        switch (type) {
            // If no type is given, allow the API to use the default.
            case "fastestSection":
                // Use a custom bitmap as the cap at the start of the line.
                polyline.setColor(COLOR_FASTEST_SECTION_ARGB);
                break;
            case "section":
                polyline.setColor(COLOR_NORMAL_SECTION_ARGB);
                polyline.setPattern(PATTERN_POLYLINE_DOTTED);
                // Use a round cap at the start of the line.
                break;
        }

        polyline.setStartCap(new RoundCap());
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setJointType(JointType.ROUND);
    }

    public static CharSequence makeCharSequence(String text) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        ssb.setSpan(new StyleSpan(BOLD), 0, text.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    public static void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position, GoogleMap map) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        map.addMarker(markerOptions);
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static void addStartRacePointMarker(Context context, Punto startRacePoint, GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(startRacePoint.getLat(), startRacePoint.getLon()))
                .icon(MapsUtils.bitmapDescriptorFromVector(context, R.drawable.ic_map_marker_alt_solid))
                .title("START"));
    }

    public static void addLastRacePointMarker(Context context, Punto lastRacePoint, GoogleMap googleMap) {
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lastRacePoint.getLat(), lastRacePoint.getLon()))
                .icon(MapsUtils.bitmapDescriptorFromVector(context, R.drawable.ic_flag_checkered_solid))
                .title("FINISH"));
    }

    public static void addDistanceIcon(Context context, Punto lastSectionPoint, long sectionDistance, GoogleMap googleMap) {
        float distanceToKms =  sectionDistance / 1000f;
        String distanceString = String.format("%.2f", distanceToKms) +  " km";

        IconGenerator iconFactory = new IconGenerator(context);
        MapsUtils.addIcon(iconFactory, MapsUtils.makeCharSequence(distanceString),
                new LatLng(lastSectionPoint.getLat(), lastSectionPoint.getLon()),
                googleMap);
    }

    public static String createUrl(LatLng point) {
        return point.latitude + "," + point.longitude;
    }

    public static String createUrlWaypoints(List<LatLng> points) {
        StringBuilder url = new StringBuilder();
        for (LatLng point: points) {
            url.append("|").append(point.latitude).append(",").append(point.longitude);
        }
        return url.toString();
    }

}