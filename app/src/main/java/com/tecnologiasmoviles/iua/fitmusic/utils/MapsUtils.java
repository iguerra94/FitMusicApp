package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.ui.IconGenerator;
import com.tecnologiasmoviles.iua.fitmusic.R;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.view.CustomInfoWindowGoogleMap;

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

    public static void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position, GoogleMap googleMap) {
        MarkerOptions markerOptions = new MarkerOptions().
                icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text))).
                position(position).
                anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        googleMap.setOnMarkerClickListener(marker -> {
            if (marker.getTitle() != null) {
                if (marker.getTitle().equalsIgnoreCase("Start")) {
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                    } else {
                        marker.showInfoWindow();
                    }
                    return true;
                }
                if (marker.getTitle().equalsIgnoreCase("Finish")) {
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                    } else {
                        marker.showInfoWindow();
                    }
                    return true;
                }
            }
            marker.hideInfoWindow();
            return true;
        });
        googleMap.setOnInfoWindowClickListener(marker -> {
            if (marker.getTitle() != null) {
                if (marker.getTitle().equalsIgnoreCase("Start")) {
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                    } else {
                        marker.showInfoWindow();
                    }
                }
                if (marker.getTitle().equalsIgnoreCase("Finish")) {
                    if (marker.isInfoWindowShown()) {
                        marker.hideInfoWindow();
                    } else {
                        marker.showInfoWindow();
                    }
                }
            } else {
                marker.hideInfoWindow();
            }
        });

        googleMap.addMarker(markerOptions);
    }

    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public static void addStartRacePointMarker(Context context, Punto startRacePoint, String text, GoogleMap googleMap) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(startRacePoint.getLat(), startRacePoint.getLon()))
                .icon(MapsUtils.bitmapDescriptorFromVector(context, R.drawable.ic_circle_solid))
                .anchor(0.5f, 0.5f)
                .title("Start");

        CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(context);
        googleMap.setInfoWindowAdapter(customInfoWindow);

        googleMap.addMarker(markerOptions);
    }

    public static void addLastSectionPointMarker(Context context, Punto lastSectionPoint, Punto firstPointNextSection, GoogleMap googleMap) {
        if (lastSectionPoint != null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(lastSectionPoint.getLat(), lastSectionPoint.getLon()))
                    .icon(MapsUtils.bitmapDescriptorFromVector(context, R.drawable.ic_dot_circle_solid))
                    .anchor(0.5f, 0.5f);

            googleMap.addMarker(markerOptions);
        }

        if (firstPointNextSection != null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(firstPointNextSection.getLat(), firstPointNextSection.getLon()))
                    .icon(MapsUtils.bitmapDescriptorFromVector(context, R.drawable.ic_dot_circle_solid))
                    .anchor(0.5f, 0.5f);

            googleMap.addMarker(markerOptions);
       }

    }

    public static void addLastRacePointMarker(Context context, Punto lastRacePoint, String text, GoogleMap googleMap) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(lastRacePoint.getLat(), lastRacePoint.getLon()))
                .icon(MapsUtils.bitmapDescriptorFromVector(context, R.drawable.ic_dot_circle_regular))
                .anchor(0.5f, 0.5f)
                .title("Finish");

        CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(context);
        googleMap.setInfoWindowAdapter(customInfoWindow);

        googleMap.addMarker(markerOptions);
    }

    public static void addDistanceIcon(Context context, Punto lastSectionPoint, long sectionDistance, GoogleMap googleMap) {
        float distanceToKms =  sectionDistance / 1000f;
        String distanceString = String.format("%.2f", distanceToKms) +  " km";

        IconGenerator iconFactory = new IconGenerator(context);
//        iconFactory.setRotation(270);
//        iconFactory.setContentRotation(-270);
        MapsUtils.addIcon(iconFactory, MapsUtils.makeCharSequence(distanceString),
                new LatLng(lastSectionPoint.getLat(), lastSectionPoint.getLon()),
                googleMap);
    }

    public static String createUrl(LatLng point) {
        return point.latitude + "," + point.longitude;
    }

    public static String createUrlWaypoints(List<LatLng> points, boolean optimize) {
        StringBuilder url = new StringBuilder();

        if (points.isEmpty()) {
            return "";
        }

        if (optimize) {
            int[] indexes;
            if (points.size() % 2 == 0) {
                indexes = new int[]{0,1,(points.size()/2)-1, points.size()/2,points.size()-2,points.size()-1};
            } else {
                indexes = new int[]{0,1,points.size()/2,points.size()-2,points.size()-1};
            }
            for (int index : indexes) {
                url.append("|").append(createUrl(points.get(index)));
            }
        } else {
            for (int i = 0; i < points.size()-1; i++) {
                url.append(createUrl(points.get(i))).append("|");
            }
            url.append(createUrl(points.get(points.size()-1)));
        }
        return url.toString();
    }

}