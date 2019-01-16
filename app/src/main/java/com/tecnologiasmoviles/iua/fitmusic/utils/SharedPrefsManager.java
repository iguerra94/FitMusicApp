package com.tecnologiasmoviles.iua.fitmusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tecnologiasmoviles.iua.fitmusic.BuildConfig;
import com.tecnologiasmoviles.iua.fitmusic.model.Punto;

import java.util.List;

public class SharedPrefsManager {

    private static SharedPrefsManager instance;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private SharedPrefsManager() {}

    public static SharedPrefsManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefsManager();
        }
        sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        return instance;
    }

    public SharedPreferences getSharedPrefs() {
        return sharedPreferences;
    }

    public void saveString(String KEY, String VALUE) {
        editor.putString(KEY, VALUE);
        editor.apply();
    }

    public String readString(String KEY) {
        return sharedPreferences.getString(KEY, "");
    }

    public void saveBoolean(String KEY, boolean VALUE) {
        editor.putBoolean(KEY, VALUE);
        editor.apply();
    }

    public boolean readBoolean(String KEY) {
        return sharedPreferences.getBoolean(KEY, false);
    }

    public void saveInt(String KEY, int VALUE) {
        editor.putInt(KEY, VALUE);
        editor.apply();
    }

    public int readInt(String KEY) {
        return sharedPreferences.getInt(KEY, -1);
    }

    public void saveLong(String KEY, long VALUE) {
        editor.putLong(KEY, VALUE);
        editor.apply();
    }

    public long readLong(String KEY) {
        return sharedPreferences.getLong(KEY, 0);
    }

    public void saveListPoints(String KEY, List<Punto> pointsList) {
        Gson gson = new Gson();
        String json = gson.toJson(pointsList);

        editor.putString(KEY, json);
        editor.apply();
    }

    public List<Punto> readListPoints(String KEY) {
        Gson gson = new Gson();

        String response = sharedPreferences.getString(KEY, "");
        List<Punto> pointsList = gson.fromJson(response, new TypeToken<List<Punto>>() {}.getType());

        return pointsList;
    }
    
    public static void initRaceSharedPrefsKeys(Context context) {
        SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.IS_RUNNING_KEY, false);
        SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_GETTING_LAST_POINT_KEY, false);
        SharedPrefsManager.getInstance(context).saveBoolean(SharedPrefsKeys.RACE_SHOULD_MEASURE_RYTHMN_KEY, false);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.LAST_UPDATE_TIME_MS_KEY, 0);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.INITIAL_RACE_TIME_KEY, 0);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_DISTANCE_KEY, 0);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_TIME_KEY, 0);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_CURRENT_RYTHMN_KEY, 0);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_DISTANCE_KEY, 0);
        SharedPrefsManager.getInstance(context).saveLong(SharedPrefsKeys.RACE_LAST_UPDATED_RYTHMN_TIME_KEY, 0);
        SharedPrefsManager.getInstance(context).saveInt(SharedPrefsKeys.ID_SONG_KEY, -1);
        SharedPrefsManager.getInstance(context).saveString(SharedPrefsKeys.LAST_UPDATE_TIME_KEY, "");
        SharedPrefsManager.getInstance(context).saveString(SharedPrefsKeys.RACE_DATE_STRING_KEY, "");
        SharedPrefsManager.getInstance(context).saveString(SharedPrefsKeys.RACE_DESCRIPTION_KEY, "");
        SharedPrefsManager.getInstance(context).saveString(SharedPrefsKeys.RACE_CURRENT_FIREBASE_KEY, "");
        SharedPrefsManager.getInstance(context).saveString(SharedPrefsKeys.RACE_DURATION_KEY, "");
        SharedPrefsManager.getInstance(context).saveListPoints(SharedPrefsKeys.RACE_LOCATION_POINTS_KEY, null);
    }

}