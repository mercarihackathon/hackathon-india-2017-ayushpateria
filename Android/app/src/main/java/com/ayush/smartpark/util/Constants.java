package com.ayush.smartpark.util;

import android.net.Uri;
import android.util.Log;

/**
 * Created by ayush on 07/10/17.
 */

public class Constants {

    public static final String CURRENT_LOC ="cur_loc";
    public static final String CURRENT_ST ="cur_st";

    public static final String PLACE_API_KEY = "XXXXXXXXXX";


    public static String addURLspot(){
        Uri.Builder builder = new Uri.Builder();
        String URL;
        builder.scheme("http").authority("35.193.69.203")
                .appendPath("spots");
        URL = builder.build().toString();
        Log.d("URL spots", URL);
        return URL;
    }


    public static String getURLspot(String loc){
        Uri.Builder builder = new Uri.Builder();
        String URL;
        builder.scheme("http").authority("35.193.69.203")
                .appendPath("spots").appendPath(loc);
        URL = builder.build().toString();
        Log.d("URL spots", URL);
        return URL;
    }
}
