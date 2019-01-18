package com.tecnologiasmoviles.iua.fitmusic.model;

import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Tramo {
    private UUID id_tramo;
    private UUID id_punto_inicio;
    private UUID id_punto_fin;
    // distancia en metros
    private long distancia_tramo;
    // ritmo en ms
    private long ritmo_tramo;

    public Tramo() {}

    public Tramo(UUID id_tramo, UUID id_punto_inicio, UUID id_punto_fin, long distancia_tramo, long ritmo_tramo) {
        this.id_tramo = id_tramo;
        this.id_punto_inicio = id_punto_inicio;
        this.id_punto_fin = id_punto_fin;
        this.distancia_tramo = distancia_tramo;
        this.ritmo_tramo = ritmo_tramo;
    }

    public Tramo(UUID id_punto_inicio, UUID id_punto_fin, long distancia_tramo, long ritmo_tramo) {
        this.id_punto_inicio = id_punto_inicio;
        this.id_punto_fin = id_punto_fin;
        this.distancia_tramo = distancia_tramo;
        this.ritmo_tramo = ritmo_tramo;
    }

    public UUID getIdTramo() {
        return id_tramo;
    }

    public void setIdTramo(UUID id_tramo) {
        this.id_tramo = id_tramo;
    }

    public UUID getIdPuntoInicio() {
        return id_punto_inicio;
    }

    public void setIdPuntoInicio(UUID id_punto_inicio) {
        this.id_punto_inicio = id_punto_inicio;
    }

    public UUID getIdPuntoFin() {
        return id_punto_fin;
    }

    public void setIdPuntoFin(UUID id_punto_fin) {
        this.id_punto_fin = id_punto_fin;
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

    @Override
    public String toString() {
        float distanceToKms = distancia_tramo/1000f;

        return "id_tramo: " + id_tramo +
                ", id_punto_inicio: " + id_punto_inicio +
                ", id_punto_fin: " + id_punto_fin +
                ", distancia_tramo: " + String.format("%.2f", distanceToKms) +
                ", ritmo_tramo: " + TimeUtils.milliSecondsToTimer(ritmo_tramo);
    }

    public static JSONObject toJSONObject(Tramo tramo) throws JSONException {
        JSONObject raceObject = new JSONObject();

        raceObject.put("id_tramo", String.valueOf(tramo.getIdTramo()));
        raceObject.put("id_punto_inicio", tramo.getIdPuntoInicio());
        raceObject.put("id_punto_fin", tramo.getIdPuntoFin());
        raceObject.put("distancia_tramo", tramo.getDistanciaTramo());
        raceObject.put("ritmo_tramo", tramo.getRitmoTramo());

        return raceObject;
    }

}