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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class EarthquakeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();
    final private int MY_PERMISSIONS_REQUEST = 123;
    //Initialze the json query data
    String startTime = "2016-12-17";
    String endTime = "2016-12-21";
    String minMag = "3.0";
    String limit = "100";
    String sortBy = "time";
    static String jsonData = null; //no longer needed. May want to use as default/error data if no Internet data
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        defaultSearch(sortBy);
        /*
        initializeDate(); // initialize the startTime and endTime variables for the json search
        // set string used to retrieve json data from USGS API
        String usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                "&starttime="+startTime+"&endtime="+endTime+"&minmag="+minMag+"&limit="+limit;
        //check for Internet connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new JsonTask().execute(usgsurl);
        } else {
            displayResult("{\"type\":\"FeatureCollection\",\"metadata\":{\"generated\":1482176445000,\"url\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-01-31&minmag=6&limit=10\",\"title\":\"USGS Earthquakes\",\"status\":200,\"api\":\"1.5.2\",\"limit\":10,\"offset\":1,\"count\":10},\"features\":[{\"type\":\"Feature\",\"properties\":{\"mag\":0.0,\"place\":\"Network Error!\",\"time\":0,\"updated\":1478815834700,\"tz\":720,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us20004vvx\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us20004vvx&format=geojson\",\"felt\":2,\"cdi\":3.4,\"mmi\":5.82,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":798,\"net\":\"us\",\"code\":\"20004vvx\",\"ids\":\",gcmt20160130032510,at00o1qxho,pt16030050,us20004vvx,gcmt20160130032512,\",\"sources\":\",gcmt,at,pt,us,gcmt,\",\"types\":\",associate,cap,dyfi,finite-fault,general-link,general-text,geoserve,impact-link,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":0.958,\"rms\":1.19,\"gap\":17,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"network error: please try again\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0,0]},\"id\":\"error\"},\n");
        }
        */

    }


    public void preJsonTask(String usgsurl) {

        //check for Internet connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new JsonTask().execute(usgsurl);
        } else {
            displayResult("{\"type\":\"FeatureCollection\",\"metadata\":{\"generated\":1482176445000,\"url\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-01-31&minmag=6&limit=10\",\"title\":\"USGS Earthquakes\",\"status\":200,\"api\":\"1.5.2\",\"limit\":10,\"offset\":1,\"count\":10},\"features\":[{\"type\":\"Feature\",\"properties\":{\"mag\":0.0,\"place\":\"Network Error!\",\"time\":0,\"updated\":1478815834700,\"tz\":720,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us20004vvx\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us20004vvx&format=geojson\",\"felt\":2,\"cdi\":3.4,\"mmi\":5.82,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":798,\"net\":\"us\",\"code\":\"20004vvx\",\"ids\":\",gcmt20160130032510,at00o1qxho,pt16030050,us20004vvx,gcmt20160130032512,\",\"sources\":\",gcmt,at,pt,us,gcmt,\",\"types\":\",associate,cap,dyfi,finite-fault,general-link,general-text,geoserve,impact-link,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":0.958,\"rms\":1.19,\"gap\":17,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"network error: please try again\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0,0]},\"id\":\"error\"},\n");
        }

    }

    public class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "Getting earthquake data from Internet\nPlease Wait...", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]); // URL for our json data
                // setup http connection to given url
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                int response = connection.getResponseCode();
                // Log.d(LOG_TAG, "connection response is: " + response);

                // Setup an input stream
                InputStream stream = new BufferedInputStream(connection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line = "";

                //read in the stream until eof
                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    // Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }
                //return our json data as a string
                return buffer.toString();

            // catch any exceptions thrown by httpurlconnection
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {  //disconnect and close the http connection
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            displayResult(result); //send result to our listview adapter
        }

    }

    public void displayResult(String result) {
        ArrayList<QuakeData> earthquakes = new ArrayList<QuakeData>();
        // extract the data we want from the json string
        if (result != null) { //check for no data returned
           earthquakes = QueryUtils.extractQuakeDatas(result);
        } else {
            earthquakes.add(new QuakeData(0.0, "Error getting data.\nPlease retry.", "No Date", "https://www.google.com"));
        }
        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);
        // Create a new {@link ArrayAdapter} of earthquakes
        final QuakeArrayAdapter adapter = new QuakeArrayAdapter(this, earthquakes);
        // Set the adapter on the {@link ListView}
        // so the list can be populated in the userinterface
        earthquakeListView.setAdapter(adapter);
        // On item click listener to call browser intent for quake details page
        earthquakeListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QuakeData currentEarthQuake = (QuakeData) adapter.getItem(position);
                Toast.makeText(getApplicationContext(), currentEarthQuake.getUrl(), Toast.LENGTH_SHORT).show();
                //test call to open a browser intent
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentEarthQuake.getUrl()));
                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(browserIntent);
                }
            }
        });

    }

    // Default search: recent significant earthquakes sorted by date
    public void defaultSearch(String sort) {
        String usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                "&minmagnitude=5.0" + "&orderby=" + sort;
        preJsonTask(usgsurl);

    }

    /*
    ** Method to initialize and complete earthquake search nearby
     */
    public void preNearBySearch(String sort){
        sortBy = sort;

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
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

    }

    public void postNearbySearch(){

        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();
        String maxRadiusKm = "200";
        String usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                "&longitude="+longitude+"&latitude="+latitude+"&maxradiuskm="+maxRadiusKm+"&limit="+limit+
                "&starttime=1971-01-01&orderby="+sortBy;

        //check for Internet connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            preJsonTask(usgsurl);
        } else {
            displayResult("{\"type\":\"FeatureCollection\",\"metadata\":{\"generated\":1482176445000,\"url\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-01-31&minmag=6&limit=10\",\"title\":\"USGS Earthquakes\",\"status\":200,\"api\":\"1.5.2\",\"limit\":10,\"offset\":1,\"count\":10},\"features\":[{\"type\":\"Feature\",\"properties\":{\"mag\":0.0,\"place\":\"Network Error!\",\"time\":0,\"updated\":1478815834700,\"tz\":720,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us20004vvx\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us20004vvx&format=geojson\",\"felt\":2,\"cdi\":3.4,\"mmi\":5.82,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":798,\"net\":\"us\",\"code\":\"20004vvx\",\"ids\":\",gcmt20160130032510,at00o1qxho,pt16030050,us20004vvx,gcmt20160130032512,\",\"sources\":\",gcmt,at,pt,us,gcmt,\",\"types\":\",associate,cap,dyfi,finite-fault,general-link,general-text,geoserve,impact-link,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":0.958,\"rms\":1.19,\"gap\":17,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"network error: please try again\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0,0]},\"id\":\"error\"},\n");
        }

    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("info", "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            Toast.makeText(this, "Need to allow location access for this search method", Toast.LENGTH_SHORT).show();
        }
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            Toast.makeText(this, latitude + "  " + longitude, Toast.LENGTH_SHORT).show();
            postNearbySearch();

        } else {
            Toast.makeText(this, "Couldn't get the location. Make sure location is enabled in settings", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.recent_significant_D:
                defaultSearch("time");
                return true;
            case R.id.recent_significant_M:
                defaultSearch("magnitude");
                return true;
            case R.id.nearby_200_D:
                preNearBySearch("time");
                return true;
            case R.id.nearby_200_M:
                preNearBySearch("magnitude");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
}
