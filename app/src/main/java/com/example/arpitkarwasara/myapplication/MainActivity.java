/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.arpitkarwasara.myapplication;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Getting Location Updates.
 *
 * Demonstrates how to use the Fused Location Provider API to get updates about a device's
 * location. The Fused Location Provider is part of the Google Play services location APIs.
 *
 * For a simpler example that shows the use of Google Play services to fetch the last known location
 * of a device, see
 * https://github.com/googlesamples/android-play-location/tree/master/BasicLocation.
 *
 * This sample uses Google Play services, but it does not require authentication. For a sample that
 * uses Google Play services for authentication, see
 * https://github.com/googlesamples/android-google-accounts/tree/master/QuickStart.
 */
public class MainActivity extends FragmentActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener ,OnMapReadyCallback {
    protected static final String TAG = "location-updates-sample";
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    protected LatLng current;
    protected GoogleMap googleMap;

    final ArrayList<MarkerOptions>  mkr = new ArrayList<MarkerOptions>();

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;
    /**
     * Represents a geographical location.
     */
    String device;
    protected Location mCurrentLocation;
    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected ImageButton btnReceive;
    protected EditText etext;
    protected CheckBox check;


    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;
    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
// Locate the UI widgets.]
        btnReceive = (ImageButton)findViewById(R.id.receive);
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        etext = (EditText)findViewById(R.id.edittext);
        check = (CheckBox)findViewById(R.id.check);
        mRequestingLocationUpdates = false;

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        Parse.initialize(this, "WNI941GTbOl4Sg9q1WocH4SuDF34FPkoHRt47BwO", "zQNT4LsTLZsAOFKO2sdh33EevxVft0rJtVkYmAHs");
        

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        device = telephonyManager.getDeviceId();

// Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);
// Kick off the process of building a GoogleApiClient and requesting the LocationServices
// API.
        buildGoogleApiClient();
        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* LAfda here
                    LatLng current is updated , point is not
                 */

                onMapReady(googleMap);


                 getTourGuides();
                getTourists();



            }

        });}
         /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
// Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
// the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }
// Update the value of mCurrentLocation from the Bundle and update the UI to show the
// correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
// Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
// is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
// Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }
    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }
    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
// Sets the desired interval for active location updates. This interval is
// inexact. You may not receive updates at all if no location sources are available, or
// you may receive them slower than requested. You may also receive updates faster than
// requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
// Sets the fastest rate for active location updates. This interval is exact, and your
// application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }
    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }
    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
// The final argument to {@code requestLocationUpdates()} is a LocationListener
// (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        if(mCurrentLocation==null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }


        String tableName = (check.isChecked())?"TourGuides":"Tourists";
        ParseObject testObject = new ParseObject(tableName);


        double latitude = mCurrentLocation.getLatitude();
        double longitude = mCurrentLocation.getLongitude();
        ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);
        testObject.put("code",device);
        testObject.put("location", point);
        testObject.put("username",etext.getText().toString());
        testObject.saveInBackground();
check.setEnabled(false);
    }
    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }
    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        if (mCurrentLocation != null) {

        }
    }
    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
// It is a good practice to remove location requests when the activity is in a paused or
// stopped state. Doing so helps battery performance and is especially
// recommended in applications that request frequent location updates.
// The final argument to {@code requestLocationUpdates()} is a LocationListener
// (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String tableName = (check.isChecked())?"TourGuides":"Tourists";
        ParseQuery<ParseObject> query = ParseQuery.getQuery(tableName);
        query.whereEqualTo("code",device);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {

                    ParseObject testObject = parseObjects.get(0);

                    testObject.deleteInBackground();

                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });
        check.setEnabled(true);

    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    public void onResume() {
        super.onResume();
// Within {@code onPause()}, we pause location updates, but leave the
// connection to GoogleApiClient intact. Here, we resume receiving
// location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {

        }
    }
    @Override
    protected void onPause() {
        super.onPause();
// Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {

        }
    }
    @Override
    protected void onStop() {
        super.onStop();
            if(mStartUpdatesButton.isEnabled()==false){
                String tableName = (check.isChecked())?"TourGuides":"Tourists";
                ParseQuery<ParseObject> query = ParseQuery.getQuery(tableName);
                query.whereEqualTo("code",device);
                query.findInBackground(new FindCallback<ParseObject>() {

                    @Override
                    public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                        if (e == null) {

                            ParseObject testObject = parseObjects.get(0);

                            testObject.deleteInBackground();

                        } else {
                            Log.d("score", "Error: " + e.getMessage());
                        }
                    }
                });

            }
        mGoogleApiClient.disconnect();
    }
    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
// If the initial location was never previously requested, we use
// FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
// its value in the Bundle and check for it in onCreate(). We
// do not request it again unless the user specifically requests location updates by pressing
// the Start Updates button.
//
// Because we cache the value of the initial location in the Bundle, it means that if the
// user launches the activity,
// moves to a new location, and then changes the device orientation, the original location
// is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }
// If the user presses the Start Updates button before GoogleApiClient connects, we set
// mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
// the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                Toast.LENGTH_SHORT).show();
        String tableName = (check.isChecked())?"TourGuides":"Tourists";

        ParseQuery<ParseObject> query = ParseQuery.getQuery(tableName);
        query.whereEqualTo("code",device);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {

                    ParseObject testObject = parseObjects.get(0);
                    double latitude = mCurrentLocation.getLatitude();
                    double longitude = mCurrentLocation.getLongitude();
                    ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);

                    testObject.put("location", point);
                    testObject.saveInBackground();
                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });

    }
    @Override
    public void onConnectionSuspended(int cause) {
// The connection to Google Play services was lost for some reason. We call connect() to
// attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
// onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }
    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (googleMap == null) {
            Log.d("Google Map = ",googleMap+" :5");
           if(mCurrentLocation==null) {
               mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
           }
            googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.setMyLocationEnabled(true);

            current = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

            CameraPosition cameraPosition = new CameraPosition.Builder().target(current).zoom(13).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            Log.d("Google Map = ",googleMap+" :6");

        }
                else {googleMap.clear();}

    }



    public void plusMarker(MarkerOptions arrMarker[])
    {
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        googleMap.setMyLocationEnabled(true);

        for(int i = 0 ; i<arrMarker.length; i++)
        {
            googleMap.addMarker(arrMarker[i]);
        }

    }

    public void getTourists(){
        current = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        ParseGeoPoint point = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Tourists");
        query.whereNear("location", point);
        query.setLimit(100);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {

                    MarkerOptions[] arrMarker = new MarkerOptions[parseObjects.size()];

                    double lat,longi;
                    String displayName;

                    for (int i = 0; i < parseObjects.size(); i++) {

                        lat = parseObjects.get(i).getParseGeoPoint("location").getLatitude();
                        longi = parseObjects.get(i).getParseGeoPoint("location").getLongitude();
                        displayName = parseObjects.get(i).getString("username");

                        arrMarker[i] = new MarkerOptions().position(new LatLng(lat, longi)).title("Tourist: " + displayName);


                    }
                    plusMarker(arrMarker);
                } else {
                    Log.d("score", "Error: " + e.getMessage());

                }
            }
        });
    }
    public void getTourGuides(){
        current = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        ParseGeoPoint point = new ParseGeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        ParseQuery<ParseObject> query = ParseQuery.getQuery("TourGuides");
        query.whereNear("location", point);
        query.setLimit(100);
        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                if (e == null) {

                    MarkerOptions[] arrMarker = new MarkerOptions[parseObjects.size()];

                    double lat,longi;
                    String displayName;

                    for (int i = 0; i < parseObjects.size(); i++) {

                        lat = parseObjects.get(i).getParseGeoPoint("location").getLatitude();
                        longi = parseObjects.get(i).getParseGeoPoint("location").getLongitude();
                        displayName = parseObjects.get(i).getString("username");

                        arrMarker[i] = new MarkerOptions().position(new LatLng(lat, longi)).title("TourGuide: " + displayName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));


                    }
                    plusMarker(arrMarker);
                } else {
                    Log.d("score", "Error: " + e.getMessage());

                }

            }
        });
    }
}

