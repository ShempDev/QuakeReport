package com.example.android.quakereport;

/**
 * Created by shiemke on 12/17/16.
 */

public class QuakeData {
    private double mMagnitude; //magnitude of the earthquake 0.0 - 10.0
    private String mLocation; //location of the earhtquake
    private String mDate;  //date of the earthquake
    private String mUrl;  //http url address for usgs details web page

    /*
    Create new quakeData object
    @param magnitude
    @param location
    @param date
    @param url
     */
    public QuakeData (double magnitude, String location, String date, String url){
        mMagnitude = magnitude;
        mLocation = location;
        mDate = date;
        mUrl = url;
    }

    //get the magnitude of the earthquake
    public double getQuakeMag (){
        return mMagnitude;
    }

    //get the location of the earthquake
    public String getLocation (){
        return mLocation;
    }

    //get the date of the earthquake
    public String getDate (){
        return mDate;
    }
    //get the earthquake details web page url as String
    public String getUrl() { return mUrl;}
}
