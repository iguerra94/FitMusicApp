package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.tecnologiasmoviles.iua.fitmusic.model.Carrera;

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
        double distancia = 0d;
        Date duracion = null;
        Date ritmo = null;
        Date fechaCarrera = null;

        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.nextName().equals("id_carrera")) {
                id_carrera = UUID.fromString(reader.nextString());
            }
            if (reader.nextName().equals("descripcion")) {
                descripcion = reader.nextString();
            }
            if (reader.nextName().equals("distancia")) {
                distancia = reader.nextDouble();
            }
            if (reader.nextName().equals("duracion")) {
                duracion = getTimeDataObject(reader);
            }
            if (reader.nextName().equals("ritmo")) {
                ritmo = getTimeDataObject(reader);
            }
            if (reader.nextName().equals("fecha_carrera")) {
                SimpleDateFormat formatter1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

                try {
                    fechaCarrera = formatter1.parse(reader.nextString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Carrera(id_carrera, descripcion, distancia, duracion, ritmo, fechaCarrera, null);
    }

    private static Date getTimeDataObject(JsonReader reader) throws IOException {
        int hour = -1;
        int min = -1;
        int sec = -1;

        reader.beginObject();
        while (reader.hasNext()) {
            if (reader.nextName().equals("horas")) {
                hour = reader.nextInt();
            }
            if (reader.nextName().equals("minutos")) {
                min = reader.nextInt();
            }
            if (reader.nextName().equals("segundos")) {
                sec = reader.nextInt();
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return DateUtils.setTimeOfDate(hour, min, sec);
    }

    public static void saveRaceData(Context context, File file, Carrera carrera) throws IOException{
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        UUID id_carrera = UUID.randomUUID();

        JSONObject raceObject = new JSONObject();

        Date date = new Date();

        try {
            raceObject.put("id_carrera", String.valueOf(id_carrera));
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
            raceObject.put("fecha_carrera", formatter.format(date));

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
                writer.close();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}