package com.example.android.quakereport;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by shiemke on 12/17/16.
 * custom array adapter for the earthquake data class QuakeData
 * Returns the List Views for our array adapter.
 */

public class QuakeArrayAdapter extends ArrayAdapter<QuakeData>{
    private static final String LOCATION_SEPERATOR = " of ";

    public QuakeArrayAdapter (Activity context, List<QuakeData> earthquakes){
        super(context, 0, earthquakes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if(listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        //get the earthquake data object at this positionin the list
        final QuakeData currentQuakeData = getItem(position);

        //get the textview for the magnitude and set current data
        //convert (double) magnitude to string formatted as #.#
        DecimalFormat dFormatter = new DecimalFormat("0.0");
        String magString = dFormatter.format(currentQuakeData.getQuakeMag());
        TextView magnitudeTextView = (TextView) listItemView.findViewById(R.id.magnitudeView);
        magnitudeTextView.setText(magString);
        // Set the color of the background circle depending on earthquake magnitude.
        GradientDrawable magnitudeCircle = (GradientDrawable) magnitudeTextView.getBackground();
        int magnitudeColor = getMagnitudeColor((int)currentQuakeData.getQuakeMag());
        magnitudeCircle.setColor(magnitudeColor);

        //get the textview for the location, split the string and set the current data
        //puts the "NESW of" info on a seperate line
        String primaryLocation, offsetLocation;
        String location = currentQuakeData.getLocation();

        if (location.contains(LOCATION_SEPERATOR)) {
            String[] parts = location.split(LOCATION_SEPERATOR);
            offsetLocation = parts[0] + LOCATION_SEPERATOR;
            primaryLocation = parts[1];
        } else { //Loaction does not include "NESW of" so, use "Near the"
            offsetLocation = getContext().getString(R.string.near_the);
            primaryLocation = location;
        }
        // Set the Location text views with above string data.
        TextView locationTextView = (TextView) listItemView.findViewById(R.id.locationView);
        locationTextView.setText(offsetLocation);
        TextView primaryLocationView = (TextView) listItemView.findViewById(R.id.location2View);
        primaryLocationView.setText(primaryLocation);

        //get the textview for the date and set the current data
        TextView dateTextView = (TextView) listItemView.findViewById(R.id.dateView);
        dateTextView.setText(currentQuakeData.getDate());

        return listItemView;
    }

    private int getMagnitudeColor(int magnitude) {
        switch (magnitude) {
            case 0:
            case 1:
                return(ContextCompat.getColor(getContext(), R.color.magnitude1));
            case 2:
                return(ContextCompat.getColor(getContext(), R.color.magnitude2));
            case 3:
                return(ContextCompat.getColor(getContext(), R.color.magnitude3));
            case 4:
                return(ContextCompat.getColor(getContext(), R.color.magnitude4));
            case 5:
                return(ContextCompat.getColor(getContext(), R.color.magnitude5));
            case 6:
                return(ContextCompat.getColor(getContext(), R.color.magnitude6));
            case 7:
                return(ContextCompat.getColor(getContext(), R.color.magnitude7));
            case 8:
                return(ContextCompat.getColor(getContext(), R.color.magnitude8));
            case 9:
                return(ContextCompat.getColor(getContext(), R.color.magnitude9));
            case 10:
            default:
                return(ContextCompat.getColor(getContext(), R.color.magnitude10));
        }
    }
}
