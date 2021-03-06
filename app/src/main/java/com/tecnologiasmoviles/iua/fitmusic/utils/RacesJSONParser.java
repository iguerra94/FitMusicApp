package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RacesJSONParser {
    private static final String LOG_TAG = RacesJSONParser.class.getSimpleName();

    public static List<Carrera> getRacesJSONStream(InputStream in) throws IOException{
        InputStreamReader isr = new InputStreamReader(in);
        JsonReader reader = new JsonReader(isr);
        return getRacesDataArray(reader);
    }

    private static List<Tramo> getRaceSectionsDataArray(JsonReader reader) throws IOException {
        List<Tramo> tramos = new ArrayList<>();

        reader.beginArray();
        while(reader.hasNext()){
            tramos.add(getRaceSectionData(reader));
        }
        reader.endArray();

        return tramos;
    }

    private static Tramo getRaceSectionData(JsonReader reader) throws IOException {
        UUID id_tramo = null;
        long distancia_tramo = 0;
        long ritmo_tramo = 0;
        boolean is_fastest_section = false;
        String section_polyline = "";
        List<Punto> puntos_tramo = null;

        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.nextName().equals("id_tramo")) {
                id_tramo = UUID.fromString(reader.nextString());
                Log.d(LOG_TAG, "id_tramo: " + id_tramo);
            }
            if (reader.nextName().equals("distancia_tramo")) {
                distancia_tramo = reader.nextLong();
                Log.d(LOG_TAG, "distancia_tramo: " + distancia_tramo);
            }
            if (reader.nextName().equals("ritmo_tramo")) {
                ritmo_tramo = reader.nextLong();
                Log.d(LOG_TAG, "ritmo_tramo: " + ritmo_tramo);
            }
            if (reader.nextName().equals("is_fastest_section")) {
                is_fastest_section = reader.nextBoolean();
                Log.d(LOG_TAG, "is_fastest_section: " + is_fastest_section);
            }
            if (reader.nextName().equals("section_polyline")) {
                section_polyline = reader.nextString();
                Log.d(LOG_TAG, "section_polyline: " + section_polyline);
            }
            if (reader.nextName().equals("puntos_tramo")) {
                puntos_tramo = getPointsDataArray(reader);
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Tramo(id_tramo, distancia_tramo, ritmo_tramo, is_fastest_section, section_polyline, puntos_tramo);
    }

    private static List<Punto> getPointsDataArray(JsonReader reader) throws IOException{
        List<Punto> puntos = new ArrayList<>();

        reader.beginArray();
        while(reader.hasNext()){
            puntos.add(getPointData(reader));
        }
        reader.endArray();

        return puntos;
    }

    private static Punto getPointData(JsonReader reader) throws IOException {
        UUID id_punto = null;
        double lat = 0;
        double lon = 0;
        boolean is_starting_race_point = false;
        boolean is_last_race_point = false;

        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.nextName().equals("id_punto")) {
                id_punto = UUID.fromString(reader.nextString());
            }
            if (reader.nextName().equals("lat")) {
                lat = reader.nextDouble();
            }
            if (reader.nextName().equals("lon")) {
                lon = reader.nextDouble();
            }
            if (reader.nextName().equals("is_starting_race_point")) {
                is_starting_race_point = reader.nextBoolean();
            }
            if (reader.nextName().equals("is_last_race_point")) {
                is_last_race_point = reader.nextBoolean();
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Punto(id_punto, lat, lon, is_starting_race_point, is_last_race_point);
    }

    private static List<Carrera> getRacesDataArray(JsonReader reader) throws IOException{
        List<Carrera> carreras = new ArrayList<>();

        reader.beginArray();
        while(reader.hasNext()){
            carreras.add(getRaceData(reader));
        }
        reader.endArray();

        return carreras;
    }

    private static Carrera getRaceData(JsonReader reader) throws IOException{
        UUID id_carrera = null;
        String descripcion = "";
        long distancia = 0;
        long duracion = 0;
        long ritmo = 0;
        Date fecha_carrera = null;
        List<Tramo> tramos = null;

        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.nextName().equals("id_carrera")) {
                id_carrera = UUID.fromString(reader.nextString());
            }
            if (reader.nextName().equals("descripcion")) {
                descripcion = reader.nextString();
            }
            if (reader.nextName().equals("distancia")) {
                distancia = reader.nextLong();
            }
            if (reader.nextName().equals("duracion")) {
                duracion = reader.nextLong();
            }
            if (reader.nextName().equals("ritmo")) {
                ritmo = reader.nextLong();
            }
            if (reader.nextName().equals("fecha_carrera")) {
                SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                try {
                    fecha_carrera = formatter1.parse(reader.nextString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (reader.nextName().equals("tramos")) {
                tramos = getRaceSectionsDataArray(reader);
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Carrera(id_carrera, descripcion, distancia, duracion, ritmo, fecha_carrera, tramos);
    }

    public static void saveRaceData(Context context, File file, Carrera carrera) throws IOException{
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        UUID id_carrera = UUID.randomUUID();

        JSONObject raceObject = new JSONObject();

        try {
            raceObject.put("id_carrera", String.valueOf(id_carrera));
            raceObject.put("descripcion", carrera.getDescripcion());
            raceObject.put("distancia", carrera.getDistancia());
            raceObject.put("duracion", carrera.getDuracion());
            raceObject.put("ritmo", carrera.getRitmo());
            raceObject.put("fecha_carrera", formatter.format(carrera.getFechaCarrera()));

            JSONArray sectionsArray = new JSONArray();

            for (Tramo t: carrera.getTramos()) {
                sectionsArray.put(Tramo.toJSONObject(t));
            }

            raceObject.put("tramos", sectionsArray);

            List<Carrera> racesList = getRacesJSONStream(new FileInputStream(file));

            JSONArray racesArray = new JSONArray();

            for (Carrera c: racesList) {
                racesArray.put(Carrera.toJSONObject(c));
            }

            racesArray.put(raceObject);

            new PrintWriter(file).close();

            try (FileWriter writer = new FileWriter(new File(context.getFilesDir(), "races_data.json"))) {
                writer.write(racesArray.toString());
                Log.d(LOG_TAG, "\n" + racesArray.toString(2));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}