package com.tecnologiasmoviles.iua.fitmusic.model;

import java.io.Serializable;

public class Punto implements Serializable {
    private int id_punto;
    private double lat;
    private double lon;

    public Punto() {}

    public Punto(int id_punto, double lat, double lon) {
        this.id_punto = id_punto;
        this.lat = lat;
        this.lon = lon;
    }

    public int getId_punto() {
        return id_punto;
    }

    public void setId_punto(int id_punto) {
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

    @Override
    public String toString() {
        return "id_punto: " + id_punto + ", lat: " + lat + ", lon: " + lon;
    }
}