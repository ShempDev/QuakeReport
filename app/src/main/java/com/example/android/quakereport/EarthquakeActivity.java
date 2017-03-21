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
import android.content.DialogInterface;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class EarthquakeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);
        searchMethod = getString(R.string.recent_sig_date);

        defaultSearch(sortBy);

    }

    private void preJsonTask(String usgsurl) {

        //check for Internet connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new JsonTask().execute(usgsurl);
        } else {
            displayResult("{\"type\":\"FeatureCollection\",\"metadata\":{\"generated\":1482176445000,\"url\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-01-31&minmag=6&limit=10\",\"title\":\"USGS Earthquakes\",\"status\":200,\"api\":\"1.5.2\",\"limit\":10,\"offset\":1,\"count\":10},\"features\":[{\"type\":\"Feature\",\"properties\":{\"mag\":0.0,\"place\":\"Network Error!\",\"time\":0,\"updated\":1478815834700,\"tz\":720,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us20004vvx\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us20004vvx&format=geojson\",\"felt\":2,\"cdi\":3.4,\"mmi\":5.82,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":798,\"net\":\"us\",\"code\":\"20004vvx\",\"ids\":\",gcmt20160130032510,at00o1qxho,pt16030050,us20004vvx,gcmt20160130032512,\",\"sources\":\",gcmt,at,pt,us,gcmt,\",\"types\":\",associate,cap,dyfi,finite-fault,general-link,general-text,geoserve,impact-link,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":0.958,\"rms\":1.19,\"gap\":17,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"network error: please try again\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[0.0,0.0,0]},\"id\":\"error\"},\n");
        }

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), R.string.getting_data, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            //Make sure we have at least one parameter, exit if not.
            if (params.length < 1 || params[0] == null) {
                return null;
            }
            // OK... We have a URL string, continue.
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]); // URL for our json data
                // setup http connection to given url
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                Log.d(LOG_TAG, "connection response is: " + connection.getResponseCode());

                // Setup an input stream and read into a StringBuilder
                InputStream stream = new BufferedInputStream(connection.getInputStream());
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;

                //read in the stream until eof
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
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
                try {   //close the StingBuilder
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
            //super.onPostExecute(result);
            displayResult(result); //send result to our listview adapter
        }

    }

    private void displayResult(String result) {
        List<QuakeData> earthquakes = new ArrayList<QuakeData>();
        // extract the data we want from the json string
        if (result == null || result.contains("\"count\":0")) { //check if no data returned
            resultsCount = 0;
            earthquakes.add(new QuakeData(0.0, getString(R.string.data_error), "N/A", "https://earthquake.usgs.gov"));
        } else { //ok we have valid data
            earthquakes = QueryUtils.extractQuakeDatas(result);
            resultsCount = earthquakes.size();
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
                QuakeData currentEarthQuake = adapter.getItem(position);
                //Toast.makeText(getApplicationContext(), currentEarthQuake.getUrl(), Toast.LENGTH_SHORT).show();
                // call to open a browser intent
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentEarthQuake.getUrl()));
                if (browserIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(browserIntent);
                }
            }
        });

    }

    // Default search: recent significant earthquakes sorted by date
    private void defaultSearch(String sort) {
        usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                "&minmagnitude=5.0" + "&orderby=" + sort;
        preJsonTask(usgsurl);

    }

    private void oneDaySearch(String sort) {
        usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                "&starttime=NOW-24hours&minmag=2.5&orderby=" + sort;
        preJsonTask(usgsurl);
    }


    /*
    ** Method to initialize and complete earthquake search nearby
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

    // Called after location api connects
    private void postNearbySearch(){
        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();
        usgsurl = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
                "&longitude="+longitude+"&latitude="+latitude+"&maxradiuskm="+maxRadiusKm
                + "&starttime=" + WAY_BACK_DATE + "&orderby=" + sortBy;
        preJsonTask(usgsurl);
    }

    private void aboutDialog() {
        String aboutMessage = "";
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        myDialog.setTitle(searchMethod);
        myDialog.setCancelable(true);
        final TextView msg = new TextView(this);
        // Lets pull the search details out of our usgs url query
        String[] usgsItems = usgsurl.split("&");
        for (int i=1; i<usgsItems.length; i++){
            aboutMessage += usgsItems[i] + "\n";
        }
        aboutMessage += getString(R.string.count) + " " + resultsCount;
        //msg.setText(R.string.app_info);
        msg.setText(aboutMessage);
        msg.setPadding(16, 16, 16, 8);
        msg.setTextSize(14);
        myDialog.setView(msg);
        //myDialog.setMessage(R.string.app_info);
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
        myDialog.setTitle(R.string.app_info);
        myDialog.setCancelable(true);
        final TextView msg = new TextView(this);
        msg.setText(R.string.app_info);
        msg.setPadding(16, 16, 16, 8);
        msg.setTextSize(14);
        myDialog.setView(msg);
        myDialog.setNegativeButton(R.string.cancel, null);
        AlertDialog myAlert = myDialog.create();
        myAlert.show();
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
            postNearbySearch(); //complete the search
        } else {
            Toast.makeText(this, R.string.location_permissions, Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_RESULT) {
            if (resultCode == RESULT_OK) {
                usgsurl = data.getData().toString();
                preJsonTask(usgsurl);
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
