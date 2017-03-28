/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.Manifest;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class EarthquakeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<List<QuakeData>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    final private int MY_RESULT = 586;
    private static final String WAY_BACK_DATE = "1971-01-01"; //starting date for our nearby searches

    //json query data values we need to keep for other methods
    private String usgsurl;
    private String sortBy = "time";
    private String maxRadiusKm;
    private String searchMethod;
    private int resultsCount = 0;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    // UI item definitions
    private QuakeArrayAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);
        searchMethod = getString(R.string.recent_sig_date);
        //Let's setup our UI
        ListView earthquakeListView = (ListView) findViewById(R.id.list);
        // Create a new {@link ArrayAdapter} of earthquakes
        List<QuakeData> earthquakes = new ArrayList<QuakeData>();
        // holder data so we can initialize our listview adapter, should never display
        earthquakes.add(new QuakeData(0.0, "NULL DATA", "2017-01-01", "https.earthquakes.usgs.gov"));
        adapter = new QuakeArrayAdapter(this, earthquakes);
        // Set the adapter on the {@link ListView}
        // so the list can be populated in the userinterface
        earthquakeListView.setAdapter(adapter);
        // On item click listener to call browser intent for quake details page
        earthquakeListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QuakeData currentEarthQuake = adapter.getItem(position);
                //Toast.makeText(getApplicationContext(), currentEarthQuake.getUrl(), Toast.LENGTH_SHORT).show();
                // call to open a browser intent
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentEarthQuake.getUrl()));
                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(browserIntent);
                }
            }
        });
        adapter.clear();
        //Set the USGS query url for our first search
        if (getLoaderManager().getLoader(1) == null) { // Don't reset search if we already have loader.
            usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                    "&minmagnitude=5.0" + "&orderby=" + sortBy;
        }
        getLoaderManager().initLoader(1, null, this);
    }

    // Default search: recent significant earthquakes sorted by date
    private void defaultSearch(String sort) {
        usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                "&minmagnitude=5.0" + "&orderby=" + sort;
        getLoaderManager().restartLoader(1, null, this);
    }

    private void oneDaySearch(String sort) {
        usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                "&starttime=NOW-24hours&minmag=2.5&orderby=" + sort;
        getLoaderManager().restartLoader(1, null, this);
    }

    /*
    ** Method to initialize and complete earthquake search nearby
    *  Uses Google location API service to get last known location.
     */
    private void preNearBySearch(String sort, String km){
        final int MY_PERMISSIONS_REQUEST = 123;
        sortBy = sort;
        maxRadiusKm = km;

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            // If we do not have permissions we need to request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
            return;
        }
        // OK we have permissions and can continue to get location
        // Create the location client to start receiving updates
        if (mGoogleApiClient == null) { //only create one new client
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
        }else { //Api client already exists
            mGoogleApiClient.reconnect(); //reconnect so location is called
        }
    }

    // Method called after preNearbySearch and location api connects
    private void postNearbySearch(){
        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();
        usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                "&longitude="+longitude+"&latitude="+latitude+"&maxradiuskm="+maxRadiusKm
                + "&starttime=" + WAY_BACK_DATE + "&orderby=" + sortBy;
        getLoaderManager().restartLoader(1, null, this);
    }

    // This method pops up a dialog to show details of the current search
    private void aboutDialog() {
        //make sure we are using the usgsurl from most recent search
        //Get the search information from the hidden adapter item
        usgsurl = adapter.getItem(resultsCount - 1).getUrl();
        searchMethod = adapter.getItem(resultsCount - 1).getLocation();
        String aboutMessage = "";
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle(searchMethod);
        myDialog.setCancelable(true);
        TextView msg = new TextView(this);
        // Lets pull the search details out of our usgs url query
        String[] usgsItems = usgsurl.split("&");
        for (int i=1; i<usgsItems.length; i++){
            aboutMessage += usgsItems[i] + "\n";
        }
        aboutMessage += getString(R.string.count) + " " + (resultsCount - 1);
        //msg.setText(R.string.app_info);
        msg.setText(aboutMessage);
        msg.setPadding(16, 16, 16, 8);
        msg.setTextSize(14);
        myDialog.setView(msg);
        myDialog.setNegativeButton(R.string.cancel, null);
        myDialog.setPositiveButton(R.string.info, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aboutApp();
            }
        });
        AlertDialog myAlert = myDialog.create();
        myAlert.show();
    }

    private void aboutApp() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setIcon(R.mipmap.ic_launcher);
        myDialog.setTitle(R.string.app_name);
        myDialog.setCancelable(true);
        TextView msg = new TextView(this);
        msg.setText(R.string.app_info);
        msg.setPadding(16, 16, 16, 8);
        msg.setTextSize(14);
        myDialog.setView(msg);
        myDialog.setNegativeButton(R.string.cancel, null);
        AlertDialog myAlert = myDialog.create();
        myAlert.show();
    }

    /**
     * Android LoaderManager callback overides
     */
    @Override //This constructs our Loader on the first initialize call.
    public Loader<List<QuakeData>> onCreateLoader(int id, Bundle args) {
        // create new EarthQuakeLoader with the USGS query url string
        // Also, send the searchMethod param so we keep the method used.
        return new EarthquakeLoader(this, usgsurl, searchMethod);
    }

    @Override //This method is where we get the Quake Data from the Loader.
    public void onLoadFinished(Loader<List<QuakeData>> loader, List<QuakeData> data) {
        if (data == null) {
            //if no data returned, populate with default quake
            data = new ArrayList<QuakeData>();
            data.add(new QuakeData(0.0, "NO DATA FOUND of TRY NEW SEARCH", "9999-99-99", "https.earthquakes.usgs.gov"));
        }
        resultsCount = data.size(); //get the count from our data list for later display
        adapter.clear(); //clear the adapter before adding the loader's data
        adapter.addAll(data);
    }

    @Override
    public void onLoaderReset(Loader<List<QuakeData>> loader) {
        adapter.clear(); //clear the adapter list
    }

    /**
     * Google location api callback methods
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i("info", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            postNearbySearch(); //complete the search
        } else {
            Toast.makeText(this, R.string.location_permissions, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    /*
    * These methods inflate our options menu and call the selected search methods.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.one_day_date:
                searchMethod = item.getTitle().toString();
                oneDaySearch("time");
                return true;
            case R.id.one_day_mag:
                searchMethod = item.getTitle().toString();
                oneDaySearch("magnitude");
                return true;
            case R.id.recent_significant_D:
                searchMethod = item.getTitle().toString();
                defaultSearch("time");
                return true;
            case R.id.recent_significant_M:
                searchMethod = item.getTitle().toString();
                defaultSearch("magnitude");
                return true;
            case R.id.nearby_100_D:
                searchMethod = item.getTitle().toString();
                preNearBySearch("time", "100");
                return true;
            case R.id.nearby_100_M:
                searchMethod = item.getTitle().toString();
                preNearBySearch("magnitude", "100");
                return true;
            case R.id.nearby_250_D:
                searchMethod = item.getTitle().toString();
                preNearBySearch("time", "250");
                return true;
            case R.id.nearby_250_M:
                searchMethod = item.getTitle().toString();
                preNearBySearch("magnitude", "250");
                return true;
            case R.id.custom_search:
                searchMethod = item.getTitle().toString();
                Intent searchIntent = new Intent(this, CustomSearchActivity.class);
                startActivityForResult(searchIntent, MY_RESULT);
                return true;
            case R.id.about:
                aboutDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override //This method gets the returned data from the Custom Search Activity.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Get the custom search query url data
        if (requestCode == MY_RESULT) {
            if (resultCode == RESULT_OK) {
                usgsurl = data.getData().toString();
                getLoaderManager().restartLoader(1, null, this);
            }
        }
    }

    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
}
