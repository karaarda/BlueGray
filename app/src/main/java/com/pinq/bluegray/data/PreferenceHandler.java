package com.pinq.bluegray.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Arda on 28.12.2016.
 */

public class PreferenceHandler {

    static public State getLastState(Context pContext){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(pContext);
        String lastState = pref.getString("LastState", null);

        if(lastState == null)
            return null;

        return getState(pContext, lastState);
    }

    public static void setLastState(Context pContext, String stateName){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(pContext);
        SharedPreferences.Editor prefsEditor = pref.edit();
        prefsEditor.putString("LastState", stateName);
        prefsEditor.commit();
    }

    public static void saveState(Context pContext, State state) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(pContext);
        SharedPreferences.Editor prefsEditor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(state);
        prefsEditor.putString(state.mName, json);
        prefsEditor.commit();
    }

    private static void saveStateData(Context pContext, State state){
        File file = new File(pContext.getDir("data", MODE_PRIVATE), state.mName);
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.writeObject(state.mVariables);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void loadStateData(Context pContext, State state){
        File file = new File(pContext.getDir("data", MODE_PRIVATE), state.mName);
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            state.mVariables = (HashMap<String, String>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static State getState(Context pContext, String stateName) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(pContext);
        Gson gson = new Gson();

        String json = pref.getString(stateName, null);

        if(json != null) {
            if(stateName.startsWith("delay_"))
                return gson.fromJson(json, DelayState.class);

            return gson.fromJson(json, State.class);
        }
        return null;
    }

    public static String getLanguage(Context pContext) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(pContext);

        String language = pref.getString("Language", Locale.getDefault().getLanguage());

        if(!language.equals("en") && !language.equals("tr") && !language.equals("az") && !language.equals("de") ) {
            language = "en";
            setLanguage(pContext, language);
        }

        return language;
    }

    public static void setLanguage(Context pContext, String language) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(pContext);
        SharedPreferences.Editor prefsEditor = pref.edit();
        prefsEditor.putString("Language", language);
        prefsEditor.commit();

        Log.e("Language set to", language);
    }
}
