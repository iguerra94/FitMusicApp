package com.tecnologiasmoviles.iua.fitmusic.model;

import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Carrera implements Serializable {
    private UUID id_carrera;
    private String descripcion;
    // distancia en metros
    private long distancia;
    // duracion en ms
    private long duracion;
    // ritmo en ms
    private long ritmo;
    private Date fechaCarrera;
    private List<Tramo> tramos;

    public Carrera() {}

    public Carrera(UUID id_carrera, String descripcion, long distancia, long duracion, long ritmo, Date fechaCarrera) {
        this.id_carrera = id_carrera;
        this.descripcion = descripcion;
        this.distancia = distancia;
        this.duracion = duracion;
        this.ritmo = ritmo;
        this.fechaCarrera = fechaCarrera;
    }

    public Carrera(UUID id_carrera, String descripcion, long distancia, long duracion, long ritmo, Date fechaCarrera, List<Tramo> tramos) {
        this.id_carrera = id_carrera;
        this.descripcion = descripcion;
        this.distancia = distancia;
        this.duracion = duracion;
        this.ritmo = ritmo;
        this.fechaCarrera = fechaCarrera;
        this.tramos = tramos;
    }

    public UUID getIdCarrera() {
        return id_carrera;
    }

    public void setIdCarrera(UUID id_carrera) {
        this.id_carrera = id_carrera;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public long getDistancia() {
        return distancia;
    }

    public void setDistancia(long distancia) {
        this.distancia = distancia;
    }

    public long getDuracion() {
        return duracion;
    }

    public void setDuracion(long duracion) {
        this.duracion = duracion;
    }

    public long getRitmo() {
        return ritmo;
    }

    public void setRitmo(long ritmo) {
        this.ritmo = ritmo;
    }

    public Date getFechaCarrera() {
        return fechaCarrera;
    }

    public void setFechaCarrera(Date fechaCarrera) {
        this.fechaCarrera = fechaCarrera;
    }

    public List<Tramo> getTramos() {
        return tramos;
    }

    public void setTramos(List<Tramo> tramos) {
        this.tramos = tramos;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        float distanceToKms = distancia/1000f;

        return "id_carrera: " + id_carrera + ", descripcion: " + descripcion +
                ", distancia: " + String.format("%.2f", distanceToKms) + " km" +
                ", duracion: " + TimeUtils.milliSecondsToTimer(duracion) +
                ", ritmo: " + TimeUtils.milliSecondsToTimer(ritmo) + ", fechaCarrera: " + formatter.format(fechaCarrera) +
                ", tramos: " + tramos;
    }

    public static JSONObject toJSONObject(Carrera carrera) throws JSONException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        JSONObject raceObject = new JSONObject();

        raceObject.put("id_carrera", String.valueOf(carrera.getIdCarrera()));
        raceObject.put("descripcion", carrera.getDescripcion());
        raceObject.put("distancia", carrera.getDistancia());
        raceObject.put("duracion", carrera.getDuracion());
        raceObject.put("ritmo", carrera.getRitmo());
        raceObject.put("fecha_carrera", formatter.format(carrera.getFechaCarrera()));

        JSONArray raceSectionsArray = new JSONArray();

        for (Tramo t: carrera.getTramos()) {
            raceSectionsArray.put(Tramo.toJSONObject(t));
        }

        raceObject.put("tramos", raceSectionsArray);

        return raceObject;
    }
}