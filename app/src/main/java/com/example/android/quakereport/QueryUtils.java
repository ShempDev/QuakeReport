package com.example.android.quakereport;

import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by shiemke on 12/19/16.
 */

public final class QueryUtils {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name com.example.android.quakereport.QueryUtils (and an object instance of com.example.android.quakereport.QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Return a list of {@link QuakeData} objects that has been built up from
     * parsing a JSON response.
     */
    public static List<QuakeData> extractQuakeDatas(String jsonData) {
        // Create an empty List that we can start adding earthquakes to from the json data
        List<QuakeData> earthquakes = new ArrayList<>();
        Date dateObject;
        String dateTimePattern;
        // Format the dates in the json data to date/time matching the user's locale
        if (Build.VERSION.SDK_INT >= 18) {
            dateTimePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), "MMMddyyyyhmmz");
        } else {
            dateTimePattern = "MMM dd, yyyy\nh:mm z";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateTimePattern, Locale.getDefault());

        // Try to parse the SAMPLE_JSON_RESPONSE. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            JSONObject jsonObject = new JSONObject(jsonData); //converst string to json object
            //get json metadata object
            JSONObject metadata = jsonObject.getJSONObject("metadata");
            //get the json features array
            JSONArray features = jsonObject.getJSONArray("features");
            //loop through all the feature earthquakes and get the data we want
            for(int i = 0; i < metadata.getInt("count"); i++) { //for each object
                JSONObject earthquake = features.getJSONObject(i);
                JSONObject properties = earthquake.getJSONObject("properties");
                double mag = properties.getDouble("mag"); //get the magnitude
                String place = properties.getString("place"); //get the location
                long time = properties.getLong("time"); //get the date/time
                    dateObject = new Date(time);
                    String date = dateFormat.format(dateObject);
                String url = properties.getString("url"); //get the quakes details url page
                earthquakes.add(new QuakeData(mag, place, date, url)); //add to the earthquakes List

            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("ERR", "Problem parsing the earthquake JSON results", e);
        }

        // Return the list of earthquakes
        return earthquakes;
    }


}
