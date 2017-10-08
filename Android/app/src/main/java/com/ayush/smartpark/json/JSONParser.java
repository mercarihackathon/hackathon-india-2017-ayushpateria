package com.ayush.smartpark.json;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ayush on 07/10/17.
 */


public class JSONParser {

    // Constructor
    public JSONParser() {

    }

    public JSONArray makeHttpRequest(String url, String method, JSONObject postDataParams) {

        // Making HTTP request
        String json = "";
        JSONArray jObj = null;
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            URL urlObj = new URL(url.toString());
            // check for request method
            if(method == "POST") {

                urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setReadTimeout(15000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);


                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                String requestBody = buildPostParameters(postDataParams);
                writer.write(requestBody);

                writer.flush();
                writer.close();
                os.close();
                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    inputStream = urlConnection.getInputStream();
                }
                else {
                    return null;
                }
            }
            else if(method == "GET"){

                urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            inputStream.close();
            if (sb.length() == 0) {
                return null;
            }
            json = sb.toString();
        }
        catch (IOException e) {
            Log.e("JSONParser", "Error: ", e);
            json = null;
        }
        finally {
            if (urlConnection != null)
                urlConnection.disconnect();

            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("JSONParser", "Error closing stream", e);
                }
            }
        }

        try {
            jObj = new JSONArray(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }

    public static String buildPostParameters(Object content) {
        String output = null;
        if ((content instanceof String) ||
                (content instanceof JSONObject) ||
                (content instanceof JSONArray)) {
            output = content.toString();
        } else if (content instanceof Map) {
            Uri.Builder builder = new Uri.Builder();
            HashMap hashMap = (HashMap) content;
            if (hashMap != null) {
                Iterator entries = hashMap.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    builder.appendQueryParameter(entry.getKey().toString(), entry.getValue().toString());
                    entries.remove();
                }
                output = builder.build().getEncodedQuery();
            }
        }

        return output;
    }

}
