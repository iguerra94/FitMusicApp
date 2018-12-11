package com.tecnologiasmoviles.iua.fitmusic.model;

import com.tecnologiasmoviles.iua.fitmusic.model.exception.RaceModelException;
import com.tecnologiasmoviles.iua.fitmusic.utils.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Carrera implements Serializable {
    private UUID id_carrera;
    private String descripcion = "";
    private double distancia = 0d;
    private Date duracion = null;
    private Date ritmo = null;
    private Date fechaCarrera = null;
    private List<Punto> puntos;

    public Carrera() {}

    public Carrera(UUID id_carrera, String descripcion, double distancia, Date duracion, Date ritmo, Date fechaCarrera) {
        this.id_carrera = id_carrera;
        this.descripcion = descripcion;
        this.distancia = distancia;
        this.duracion = duracion;
        this.ritmo = ritmo;
        this.fechaCarrera = fechaCarrera;
    }

    public Carrera(UUID id_carrera, String descripcion, double distancia, Date duracion, Date ritmo, Date fechaCarrera, List<Punto> puntos) {
        this.id_carrera = id_carrera;
        this.descripcion = descripcion;
        this.distancia = distancia;
        this.duracion = duracion;
        this.ritmo = ritmo;
        this.fechaCarrera = fechaCarrera;
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

    public double getDistancia() {
        return distancia;
    }

    public void setDistancia(double distancia) {
        this.distancia = distancia;
    }

    public Date getDuracion() {
        return duracion;
    }

    public void setDuracion(Date duracion) {
        this.duracion = duracion;
    }

    public Date getRitmo() {
        return ritmo;
    }

    public void setRitmo(Date ritmo) {
        this.ritmo = ritmo;
    }

    public Date getFechaCarrera() {
        return fechaCarrera;
    }

    public void setFechaCarrera(Date fechaCarrera) {
        this.fechaCarrera = fechaCarrera;
    }

    public List<Punto> getPuntos() {
        return puntos;
    }

    public void setPuntos(List<Punto> puntos) {
        this.puntos = puntos;
    }

    public static void verificarDatos(Carrera carrera) throws RaceModelException {
        if (carrera.getDescripcion().trim().length() == 0) {
            throw new RaceModelException("Debes ingresar una descripcion para la carrera");
        }
        if (String.valueOf(carrera.getDistancia()).trim().length() == 0 || carrera.getDistancia() <= 0) {
            throw new RaceModelException("Debes ingresar una distancia para la carrera");
        }
        if (DateUtils.getMinutesOfDate(carrera.getDuracion()) <= 0) {
            throw new RaceModelException("Debes ingresar una duracion para la carrera");
        }
        if (DateUtils.getMinutesOfDate(carrera.getRitmo()) <= 0) {
            throw new RaceModelException("Debes ingresar el ritmo de la carrera");
        }
    }

    @Override
    public String toString() {
        return "id_carrera: " + id_carrera + ", descripcion: " + descripcion +
                ", distancia: " + distancia + ", duracion: " + duracion +
                ", ritmo: " + ritmo + ", fechaCarrera: " + fechaCarrera +
                ", puntos: (" + puntos + ")";
    }

    public static JSONObject toJSONObject(Carrera carrera) throws JSONException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        JSONObject raceObject = new JSONObject();

        raceObject.put("id_carrera", String.valueOf(carrera.getIdCarrera()));
        raceObject.put("descripcion", carrera.getDescripcion());
        raceObject.put("distancia", carrera.getDistancia());

        JSONObject raceDurationObject = new JSONObject();
        raceDurationObject.put("horas", DateUtils.getHourOfDate(carrera.getDuracion()));
        raceDurationObject.put("minutos", DateUtils.getMinutesOfDate(carrera.getDuracion()));
        raceDurationObject.put("segundos", DateUtils.getSecondsOfDate(carrera.getDuracion()));

        raceObject.put("duracion", raceDurationObject);

        JSONObject raceRithmnObject = new JSONObject();
        raceRithmnObject.put("horas", DateUtils.getHourOfDate(carrera.getRitmo()));
        raceRithmnObject.put("minutos", DateUtils.getMinutesOfDate(carrera.getRitmo()));
        raceRithmnObject.put("segundos", DateUtils.getSecondsOfDate(carrera.getRitmo()));

        raceObject.put("ritmo", raceRithmnObject);
        raceObject.put("fecha_carrera", formatter.format(carrera.getFechaCarrera()));

        return raceObject;
    }
}