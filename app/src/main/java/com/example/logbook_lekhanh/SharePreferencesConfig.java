package com.example.logbook_lekhanh;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SharePreferencesConfig {
    private static final String URL_ARRAY = "url_array";

    public static void writeArrayinPref(Context context, ArrayList<String> imageUrlArray){
        Gson gson = new Gson();
        String arrayString = gson.toJson(imageUrlArray);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(URL_ARRAY, arrayString);
        editor.apply();
    }

    public static  ArrayList<String> readArrayfromPref(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String arrayString = sp.getString(URL_ARRAY, " ");

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() { }.getType();
        ArrayList<String> imageUrlArray = gson.fromJson(arrayString, type);
        return imageUrlArray;

    }
}
