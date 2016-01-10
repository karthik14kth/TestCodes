package com.example.conevo.weatherappsearch;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by Conevo on 1/6/2016.
 */
public class CityPreferences {
    SharedPreferences prefs;

    public CityPreferences(Activity activity){
        prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    // If the user has not chosen a city yet, return
    // Sydney as the default city
    String getCity(){
        return prefs.getString("city", "Sweden,SE");
    }

    void setCity(String city){
        prefs.edit().putString("city", city).commit();
    }
}
