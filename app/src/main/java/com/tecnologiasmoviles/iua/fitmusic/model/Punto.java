package com.tecnologiasmoviles.iua.fitmusic.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.UUID;

public class Punto implements Serializable {
    private UUID id_punto;
    private double lat;
    private double lon;
    private boolean isStartingRacePoint;
    private boolean isLastRacePoint;

    public Punto() {}

    public Punto(UUID id_punto, double lat, double lon) {
        this.id_punto = id_punto;
        this.lat = lat;
        this.lon = lon;
        this.isStartingRacePoint = false;
        this.isLastRacePoint = false;
    }

    public Punto(UUID id_punto, double lat, double lon, boolean isStartingRacePoint, boolean isLastRacePoint) {
        this.id_punto = id_punto;
        this.lat = lat;
        this.lon = lon;
        this.isStartingRacePoint = isStartingRacePoint;
        this.isLastRacePoint = isLastRacePoint;
    }

    public Punto(double lat, double lon, boolean isStartingRacePoint, boolean isLastRacePoint) {
        this.lat = lat;
        this.lon = lon;
        this.isStartingRacePoint = isStartingRacePoint;
        this.isLastRacePoint = isLastRacePoint;
    }

    public UUID getIdPunto() {
        return id_punto;
    }

    public void setIdPunto(UUID id_punto) {
        this.id_punto = id_punto;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public boolean getIsStartingRacePoint() {
        return isStartingRacePoint;
    }

    public void setIsStartingRacePoint(boolean isStartingRacePoint) {
        this.isStartingRacePoint = isStartingRacePoint;
    }

    public boolean getIsLastRacePoint() {
        return isLastRacePoint;
    }

    public void setIsLastRacePoint(boolean isLastRacePoint) {
        this.isLastRacePoint = isLastRacePoint;
    }

    @Override
    public String toString() {
        return "(id_punto: " + id_punto +
                ", lat: " + lat +
                ", lon: " + lon +
                ", is_starting_race_point: " + isStartingRacePoint +
                ", is_last_race_point: " + isLastRacePoint + ")";
    }

    public static JSONObject toJSONObject(Punto punto) throws JSONException {
        JSONObject pointObject = new JSONObject();

        pointObject.put("id_punto", String.valueOf(punto.getIdPunto()));
        pointObject.put("lat", punto.getLat());
        pointObject.put("lon", punto.getLon());
        pointObject.put("is_starting_race_point", punto.getIsStartingRacePoint());
        pointObject.put("is_last_race_point", punto.getIsLastRacePoint());

        return pointObject;
    }

}