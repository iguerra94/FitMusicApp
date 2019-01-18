package com.tecnologiasmoviles.iua.fitmusic.model;

import android.os.Build;

import com.tecnologiasmoviles.iua.fitmusic.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
    private Tramo tramoMasRapido;
    private List<Tramo> tramos;
    private List<Punto> puntos;

    public Carrera() {}

    public Carrera(UUID id_carrera, String descripcion, long distancia, long duracion, long ritmo, Date fechaCarrera) {
        this.id_carrera = id_carrera;
        this.descripcion = descripcion;
        this.distancia = distancia;
        this.duracion = duracion;
        this.ritmo = ritmo;
        this.fechaCarrera = fechaCarrera;
    }

    public Carrera(UUID id_carrera, String descripcion, long distancia, long duracion, long ritmo, Date fechaCarrera, Tramo tramoMasRapido, List<Tramo> tramos, List<Punto> puntos) {
        this.id_carrera = id_carrera;
        this.descripcion = descripcion;
        this.distancia = distancia;
        this.duracion = duracion;
        this.ritmo = ritmo;
        this.fechaCarrera = fechaCarrera;
        this.tramoMasRapido = tramoMasRapido;
        this.tramos = tramos;
        this.puntos = puntos;
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

    public Tramo getTramoMasRapido() {
        return tramoMasRapido;
    }

    public void setTramoMasRapido(Tramo tramoMasRapido) {
        this.tramoMasRapido = tramoMasRapido;
    }

    public List<Tramo> getTramos() {
        return tramos;
    }

    public void setTramos(List<Tramo> tramos) {
        this.tramos = tramos;
    }

    public List<Punto> getPuntos() {
        return puntos;
    }

    public Punto getPuntoPorId(UUID idPunto) {
        List<Punto> puntos = getPuntos();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Optional<Punto> pointFound = puntos.stream()
                               .filter(punto -> punto.getIdPunto().equals(idPunto))
                               .findFirst();
            return pointFound.get();
        } else {
            for (Punto punto : puntos) {
                if (punto.getIdPunto().equals(idPunto)) {
                    return punto;
                }
            }
        }
        return null;
    }

    public int getPointIndexByUUID(UUID idPunto) {
        List<Punto> puntos = getPuntos();
        for (int i = 0; i < puntos.size(); i++) {
            if (puntos.get(i).getIdPunto().equals(idPunto)) {
                return i;
            }
        }
        return -1;
    }

    public void setPuntos(List<Punto> puntos) {
        this.puntos = puntos;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        float distanceToKms = distancia/1000f;

        return "id_carrera: " + id_carrera + ", descripcion: " + descripcion +
                ", distancia: " + String.format("%.2f", distanceToKms) + " km" +
                ", duracion: " + TimeUtils.milliSecondsToTimer(duracion) +
                ", ritmo: " + TimeUtils.milliSecondsToTimer(ritmo) + ", fechaCarrera: " + formatter.format(fechaCarrera) +
                ", tramo_mas_rapido: " + tramoMasRapido +
                ", tramos: " + tramos + ", puntos: " + puntos;
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

        return raceObject;
    }
}