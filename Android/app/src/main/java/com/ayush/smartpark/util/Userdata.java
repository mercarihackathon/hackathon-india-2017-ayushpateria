package com.ayush.smartpark.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ayush on 07/10/17.
 */

public class Userdata {

    public static String getStringPref(Context context, String key_val, String def_val) {
        SharedPreferences pref = context.getSharedPreferences("pref_"+key_val,MODE_PRIVATE);
        return pref.getString(key_val, def_val);
    }

    public static void setStringPref(Context context, String key_val, String val) {
        SharedPreferences pref = context.getSharedPreferences("pref_"+key_val,MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.clear();
        prefEditor.putString(key_val, val);
        prefEditor.commit();
    }


    public static String getLongLat(Context context, String def_val){
        return getStringPref(context, Constants.CURRENT_LOC, def_val);
    }

    public static void setLongLat(Context context, String val){
        setStringPref(context, Constants.CURRENT_LOC, val) ;
    }

    public static void setStreet(Context context, String val){
        setStringPref(context, Constants.CURRENT_ST, val) ;
    }
    public static String getStreet(Context context, String def_val){
        return getStringPref(context, Constants.CURRENT_ST, def_val);
    }

}
