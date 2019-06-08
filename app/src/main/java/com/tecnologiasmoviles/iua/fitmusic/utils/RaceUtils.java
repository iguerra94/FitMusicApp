package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.tecnologiasmoviles.iua.fitmusic.model.Punto;
import com.tecnologiasmoviles.iua.fitmusic.model.Tramo;
import com.tecnologiasmoviles.iua.fitmusic.utils.asyncTasks.EncodeListPointsAsyncTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RaceUtils {

    public static long measureCurrentRythmn(long deltaTime, float distanceInKms) {
        return (int) (deltaTime / distanceInKms);
    }

    public static long measureAverageRythmn(long rythmnAccumulated, long currentRythmn) {
        return (int) ((rythmnAccumulated + currentRythmn) / 2);
    }

    public static void updateRaceSharedPrefsOptions(Context context, long newRythmn, long currentRaceTime, long newDistance) {
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, newRythmn);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY, currentRaceTime);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, newDistance);
        SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, false);
    }

    public static void setSectionData(Context context, long newDistance, long currentRythmn, boolean isFirstSection) {
        Tramo tramo = SharedPrefsManager.getInstance(context).readSectionObject(SharedPrefsKeys.RACE_ACTUAL_SECTION_KEY);
        List<Punto> listPoints = SharedPrefsManager.getInstance(context).readListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY);

        tramo.setDistanciaTramo(newDistance);
        tramo.setRitmoTramo(currentRythmn);
        tramo.setPuntosTramo(listPoints);

        new EncodeListPointsAsyncTask(context).execute(listPoints);

        List<Tramo> tramos = SharedPrefsManager.getInstance(context).readListSections(SharedPrefsKeys.RACE_SECTIONS_KEY);

        if (tramos == null) {
            tramos = new ArrayList<>();
        }

        tramos.add(tramo);

        SharedPrefsManager.getInstance(context).saveListSections(SharedPrefsKeys.RACE_SECTIONS_KEY, tramos);

        if (isFirstSection) {
            SharedPrefsManager.getInstance(context).saveInt(SharedPrefsKeys.RACE_CURRENT_SECTION_INDEX_KEY, 0);
        } else {
            int raceCurrentSectionIndex = tramos.size()-1;
            SharedPrefsManager.getInstance(context).saveInt(SharedPrefsKeys.RACE_CURRENT_SECTION_INDEX_KEY, raceCurrentSectionIndex);
        }
    }

    public static void determineCurrentFastestSection(Context context, long currentRythmn, boolean isFirstSection) {
        if (isFirstSection) {
            SharedPrefsManager.getInstance(context).saveInt(SharedPrefsKeys.RACE_CURRENT_FASTEST_SECTION_INDEX_KEY, 0);
            SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_FASTEST_SECTION_RYTHMN_KEY, currentRythmn);
        } else {
            long currentFastestSectionRythmn = SharedPrefsManager.getInstance(context).readLong(SharedPrefsKeys.RACE_CURRENT_FASTEST_SECTION_RYTHMN_KEY);

            if (currentRythmn < currentFastestSectionRythmn) {
                int raceCurrentSectionIndex = SharedPrefsManager.getInstance(context).readInt(SharedPrefsKeys.RACE_CURRENT_SECTION_INDEX_KEY);

                SharedPrefsManager.getInstance(context).saveInt(SharedPrefsKeys.RACE_CURRENT_FASTEST_SECTION_INDEX_KEY, raceCurrentSectionIndex);
                SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_FASTEST_SECTION_RYTHMN_KEY, currentRythmn);
            }
        }
    }

    public static void initNewSectionData(Context context, List<Punto> listPoints) {
        Tramo newSection = new Tramo();
        List<Punto> newSectionPointsList = new ArrayList<>();

        newSection.setIdTramo(UUID.randomUUID());
        newSectionPointsList.add(listPoints.get(listPoints.size() -1));

        SharedPrefsManager.getInstance(context).saveSectionObject(SharedPrefsKeys.RACE_ACTUAL_SECTION_KEY, newSection);
        SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_ACTUAL_SECTION_POINTS_KEY, newSectionPointsList);
    }

    public static void setLastUpdateTimeData(Context context) {
        Date now = new Date();

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

        String dateFormatted = formatter.format(now) + " hs";
        SharedPrefsManager.getInstance(context).saveString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY, dateFormatted);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.LAST_UPDATE_TIME_MS_KEY, now.getTime());
    }
}
