package com.tecnologiasmoviles.iua.fitmusic.model;

import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class Tramo {
    private UUID id_tramo;
    // distancia en metros
    private long distancia_tramo;
    // ritmo en ms
    private long ritmo_tramo;
    private boolean is_fastest_section;
    private String section_polyline;
    private List<Punto> puntos_tramo;

    public Tramo() {}

    public Tramo(UUID id_tramo, long distancia_tramo, long ritmo_tramo, boolean is_fastest_section, String section_polyline, List<Punto> puntos_tramo) {
        this.id_tramo = id_tramo;
        this.distancia_tramo = distancia_tramo;
        this.ritmo_tramo = ritmo_tramo;
        this.is_fastest_section = is_fastest_section;
        this.section_polyline = section_polyline;
        this.puntos_tramo = puntos_tramo;
    }

    public Tramo(UUID id_tramo, long distancia_tramo, long ritmo_tramo, List<Punto> puntos_tramo) {
        this.id_tramo = id_tramo;
        this.distancia_tramo = distancia_tramo;
        this.ritmo_tramo = ritmo_tramo;
        this.is_fastest_section = false;
        this.section_polyline = "";
        this.puntos_tramo = puntos_tramo;
    }

    public Tramo(long distancia_tramo, long ritmo_tramo, List<Punto> puntos_tramo) {
        this.distancia_tramo = distancia_tramo;
        this.ritmo_tramo = ritmo_tramo;
        this.is_fastest_section = false;
        this.section_polyline = "";
        this.puntos_tramo = puntos_tramo;
    }

    public UUID getIdTramo() {
        return id_tramo;
    }

    public void setIdTramo(UUID id_tramo) {
        this.id_tramo = id_tramo;
    }

    public long getDistanciaTramo() {
        return distancia_tramo;
    }

    public void setDistanciaTramo(long distancia_tramo) {
        this.distancia_tramo = distancia_tramo;
    }

    public long getRitmoTramo() {
        return ritmo_tramo;
    }

    public void setRitmoTramo(long ritmo_tramo) {
        this.ritmo_tramo = ritmo_tramo;
    }

    public boolean getIsFastestSection() {
        return is_fastest_section;
    }

    public void setIsFastestSection(boolean is_fastest_section) {
        this.is_fastest_section = is_fastest_section;
    }

    public String getSectionPolyline() {
        return section_polyline;
    }

    public void setSectionPolyline(String section_polyline) {
        this.section_polyline = section_polyline;
    }

    public List<Punto> getPuntosTramo() {
        return puntos_tramo;
    }

    public void setPuntosTramo(List<Punto> puntos_tramo) {
        this.puntos_tramo = puntos_tramo;
    }

    @Override
    public String toString() {
        float distanceToKms = distancia_tramo/1000f;

        return "id_tramo: " + id_tramo +
                ", distancia_tramo: " + String.format("%.2f", distanceToKms) + " km" +
                ", ritmo_tramo: " + TimeUtils.milliSecondsToTimer(ritmo_tramo) +
                ", is_fastest_section: " + is_fastest_section +
                ", section_polyline: " + section_polyline +
                ", puntos_tramo: " + puntos_tramo;
    }

    public static JSONObject toJSONObject(Tramo tramo) throws JSONException {
        JSONObject sectionObject = new JSONObject();

        sectionObject.put("id_tramo", String.valueOf(tramo.getIdTramo()));
        sectionObject.put("distancia_tramo", tramo.getDistanciaTramo());
        sectionObject.put("ritmo_tramo", tramo.getRitmoTramo());
        sectionObject.put("is_fastest_section", tramo.getIsFastestSection());
        sectionObject.put("section_polyline", tramo.getSectionPolyline());

        JSONArray sectionPointsArray = new JSONArray();

        for (Punto p: tramo.getPuntosTramo()) {
            sectionPointsArray.put(Punto.toJSONObject(p));
        }

        sectionObject.put("puntos_tramo", sectionPointsArray);

        return sectionObject;
    }

}