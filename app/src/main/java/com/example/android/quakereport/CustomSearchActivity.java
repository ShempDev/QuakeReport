package com.example.android.quakereport;

/*
* Created by Jeremy Shiemke 01/10/17
* This Class creates a new activity used to build a custom earthquake query
* Methods implemented:
 * Select dates using date picker dialog
 * Google Location GPS to get Current location as starting point
 * GeoCoder to convert long/lat to address and vice/versa
 * Spinners to select other search options
 * Once complete, send the query URL back to the main activity.
 */
import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class CustomSearchActivity extends AppCompatActivity implements OnClickListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

	//UI References
    private Button fromDateEtxt;
	private Button toDateEtxt;
    private String fromDate;
    private String toDate;
    private double latitude;
    private double longitude;
	private DatePickerDialog fromDatePickerDialog;
	private DatePickerDialog toDatePickerDialog;
	private SimpleDateFormat dateFormatter;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    public String currentAddress;
    private Button searchButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        final int MY_PERMISSIONS_REQUEST = 586;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_search_activity);
		dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            // If we do not have permissions we need to request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST);
            return;
        }
        // OK we have permissions and can continue to get location
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
		
		findViewsById();
		setDateTimeField();
        mGoogleApiClient.connect();
	}

	// This method set's up our views and initializes the data
	private void findViewsById() {
        Calendar cal = Calendar.getInstance(); //Initiate a calendar object
        Date today = cal.getTime(); //set today's date
        cal.add(Calendar.DAY_OF_YEAR, -30);
        Date thirtyDaysAgo = cal.getTime(); //set date for thirty days ago.
        // Get from date view and set to (default) 30 days ago
		fromDateEtxt = (Button) findViewById(R.id.etxt_fromdate);
        fromDateEtxt.setText(dateFormatter.format(thirtyDaysAgo));
        fromDateEtxt.setInputType(InputType.TYPE_NULL);
        fromDateEtxt.requestFocus();
		// Get to date view and set to today's date.
		toDateEtxt = (Button) findViewById(R.id.etxt_todate);
		toDateEtxt.setInputType(InputType.TYPE_NULL);
        toDateEtxt.setText(dateFormatter.format(today));
        toDate = toDateEtxt.getText().toString();
        fromDate = fromDateEtxt.getText().toString();
        findViewById(R.id.clearTextButton).setOnClickListener(clearTextListener);
        // Get SEARCH button and click listener to apply the search
        searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);

	}

	//method to create date picker listeners on our date fields
	private void setDateTimeField() {
		fromDateEtxt.setOnClickListener(this);
		toDateEtxt.setOnClickListener(this);
		Calendar newCalendar = Calendar.getInstance();

        //setup date pickers for user to set and change the search dates
		toDatePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {

	        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
	            Calendar newDate = Calendar.getInstance();
	            newDate.set(year, monthOfYear, dayOfMonth);
                toDate = dateFormatter.format(newDate.getTime());
	            toDateEtxt.setText(toDate);
	        }

	    },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        toDatePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        toDatePickerDialog.getDatePicker().setMinDate(0);

        newCalendar.add(Calendar.DAY_OF_YEAR, -30); //substract 30 days for from Date picker start
        fromDatePickerDialog = new DatePickerDialog(this, new OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                fromDate = dateFormatter.format(newDate.getTime());
                fromDateEtxt.setText(fromDate);
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.getDatePicker().setMaxDate(new Date().getTime());
        fromDatePickerDialog.getDatePicker().setMinDate(0);

    }

    //method to complete the location field using Geocoder to fill with first location found
    //from users input string. This is called once the location api connects/completes.
    public void getLocation(){
        final Geocoder coder = new Geocoder(this);
        final TextView locationView = (TextView) findViewById(R.id.locationEditText);
        //currentAddress should be long/lat from location service API
        locationView.setText(currentAddress);
        /*
        * This edit text listener will accept a location input and attempt to find an address.
        * If an address is found, we can get the long/lat data for our query.
        * As is, it only returns one address based on input. A nice feature to add
        * would be to pop up a selection dialog with multiple location options.
         */
        locationView.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if(id == EditorInfo.IME_ACTION_SEARCH | id == EditorInfo.IME_ACTION_NEXT) {
                    // hide virtual keyboard on search key pressed or tabbed to next item.
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(locationView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    String location = locationView.getText().toString();
                    try {    //just taking first location returned from Geocoder for now.
                        ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocationName(location, 1);
                        if(addresses.size() == 0){
                            locationView.setText(R.string.not_found);

                        } else {   //update the location field with geocoder address info.
                                longitude = addresses.get(0).getLongitude();
                                latitude = addresses.get(0).getLatitude();
                                currentAddress = addresses.get(0).getLocality() + ", "
                                        + addresses.get(0).getAdminArea() + ", "
                                        + addresses.get(0).getCountryCode();
                                locationView.setText(currentAddress);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return false;
            }
        });
        // Force the onEditorAction to complete our first geocoder instance on activity create
        locationView.onEditorAction(EditorInfo.IME_ACTION_SEARCH);

    }

    //Method to build the USGS API query string and return to the main activity.
    private void applySearch() {
        String usgsQueryString;
        String mSortBy;
        // Get values from the spinners
        // future feature - radio button to use search option?
        Spinner radiusSpinner = (Spinner) findViewById(R.id.radiusSpinner);
        String mRadius = radiusSpinner.getSelectedItem().toString();
        Spinner magnitudeSpinner = (Spinner) findViewById(R.id.magnitudeSpinner);
        String mMagnitude = magnitudeSpinner.getSelectedItem().toString();
        Spinner resultsSpinner = (Spinner) findViewById(R.id.resultsSpinner);
        String mResults = resultsSpinner.getSelectedItem().toString();
        //Get the value from the sort by radio buttons
        RadioButton timeRadioButton = (RadioButton) findViewById(R.id.timeRadioButton);
        if (timeRadioButton.isChecked()) {
            mSortBy = "time";
        } else {
            mSortBy = "magnitude";
        }
        // Build the USGS string from the above view selections
        usgsQueryString = "https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson";
        usgsQueryString += "&starttime=" + fromDate + "&endtime=" + toDate;
        if (!currentAddress.isEmpty()) { //ignore location if blank
            usgsQueryString += "&longitude=" + longitude + "&latitude=" + latitude + "&maxradiuskm=" + mRadius;
        }
        usgsQueryString += "&minmagnitude=" + mMagnitude;
        usgsQueryString += "&limit=" + mResults;
        usgsQueryString += "&orderby=" + mSortBy;

        //need to return the usgs query string back to the main activity
        Intent resultIntent = new Intent();
        resultIntent.setData(Uri.parse(usgsQueryString));
        setResult(Activity.RESULT_OK, resultIntent);
        finish(); //Activity is done, we can close and return to main activity.
    }

    // listener method to clear the editText when the delete button is clicked
    // * possibly hide button after clear and show after start typing?
    final OnClickListener clearTextListener = new OnClickListener() {
        public void onClick(final View view) {
            TextView locationView = (TextView) findViewById(R.id.locationEditText);
            locationView.setText("");
            currentAddress = "";
            locationView.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(locationView, InputMethodManager.SHOW_IMPLICIT);
        }
    };
/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
*/
	@Override
	public void onClick(View view) {
		if(view == fromDateEtxt) {
            fromDatePickerDialog.show();
		} else if(view == toDateEtxt) {
			toDatePickerDialog.show();
		} else if (view == searchButton) {
            applySearch();
        }
	}

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        // Once connected with google api, get the location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } else {
            Toast.makeText(this, R.string.location_permissions, Toast.LENGTH_SHORT).show();
        }
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            currentAddress = Double.toString(latitude) + "  ";
            currentAddress += Double.toString(longitude);
            getLocation();

        } else {
            Toast.makeText(this, R.string.location_permissions, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }
    /*
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }
    */
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

}
