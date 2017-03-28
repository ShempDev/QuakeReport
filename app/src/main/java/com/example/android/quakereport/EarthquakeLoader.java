package com.example.android.quakereport;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static com.example.android.quakereport.EarthquakeActivity.LOG_TAG;

/**
 * Created by jeremy on 3/21/17.
 * This Class creates a custom Loader to query the USGS earthquake website.
 * It uses the URL from the constructor to start the query parse the json data.
 * After the json data is parsed this is sent as a String to our QueryUtils class
 * to convert the json data to a List of QuakeData
 */

public class EarthquakeLoader extends AsyncTaskLoader <List<QuakeData>> {
    private String mUrl;
    private String mSearch;
    private List<QuakeData> mEarthQuakes;
    private Context mContext;

    public EarthquakeLoader(Context context, String url, String search) {
        super(context);
        mContext = context;
        mUrl = url;
        mSearch = search;
    }

    @Override
    protected void onStartLoading() {
        if (mEarthQuakes != null) { //we already have the data, just deliver.
            deliverResult(mEarthQuakes);
        }else {
            //Create a connection and check for Internet connectivity
            ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            // Make sure we have an Internet connection.
            if (networkInfo != null && networkInfo.isConnected()) {
                //OK we have internet and can call our loader.
                Toast.makeText(getContext(), R.string.getting_data, Toast.LENGTH_SHORT).show();
                forceLoad();
            } else {
                // We do not have Internet, let the user know.
                Toast.makeText(getContext(), R.string.no_internet, Toast.LENGTH_SHORT).show();
                deliverResult(null);
            }
        }
    }

    @Override
    public void deliverResult(List<QuakeData> data) {

        super.deliverResult(data);
    }

    @Override
    public List<QuakeData> loadInBackground() {
        //Make sure we have at least one parameter, exit if not.
        if (mUrl.isEmpty()) {
            return null;
        }
        // OK... We have a URL string, continue.
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(mUrl); // URL for our json data
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
                //Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
            }
            //Now convert the json data to our earthquake list
            //List<QuakeData> earthquakes = new ArrayList<QuakeData>();
            String result = buffer.toString();
            // extract the data we want from the json string
            if (result.isEmpty() || result.contains("\"count\":0")) { //check if no data returned
                //resultsCount = 0;
                //earthquakes.add(new QuakeData(0.0, getString(R.string.data_error), "N/A", "https://earthquake.usgs.gov"));
                return null;
            } else { //ok we have valid data, convert to List of QuakeData.
                mEarthQuakes = QueryUtils.extractQuakeDatas(result);
                // Let's put the search info in the data so we can display in the UI.
                if (mEarthQuakes != null) {
                    mEarthQuakes.add(new QuakeData(-1, mSearch, Integer.toString(mEarthQuakes.size()), mUrl));
                }
                return mEarthQuakes;
                //resultsCount = earthquakes.size();
            }

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
}
