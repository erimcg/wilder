package net.n0code.wilder.location;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import net.n0code.wilder.R;
import net.n0code.wilder.ui.tripViewer.TripViewPager;

import java.util.Random;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "***LOC SERVER***";

    public static final int M_REQUEST_LOCATION_ON_FOR_APP = 1;
    public static final int M_REQUEST_LOCATION_ON_FOR_DEVICE = 2;
    private Activity activity;
    private GoogleApiClient mGoogleClient;

    private boolean appHasLocationEnabled = false;
    private boolean deviceHasLocationEnabled = false;
    private boolean requestTrackingOn = false;
    private boolean trackingOn = false;
    private int interval;

    // Binder given to clients
    private final IBinder mBinder = new LocationServiceBinder();
    // Random number generator
    private final Random mGenerator = new Random();

    public class LocationServiceBinder extends Binder
    {
        public LocationService getService() {
            // Return this instance of LocalService so clients can call public methods
            Log.d(TAG, "creating service");
            return LocationService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "service is bound to activity");
        if (mGoogleClient == null) {
            mGoogleClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleClient.connect();

        return mBinder;
    }

    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /*******************************************************
     * Map Activity or Fragment Tracking methods
     *******************************************************/

    public void requestPermissions(Activity activity) {
        this.activity = activity;

        //Log.d(TAG, "Received permissions check request.");
        if (!deviceHasLocationEnabled) {
            requestDeviceLocationPermissions();
        }
        else if (!appHasLocationEnabled) {
            requestAppLocationPermissions();
        }
        else {
            Intent intent = new Intent();
            intent.setAction(getApplicationContext().getString(R.string.locationServerPermissionsGranted));
            getApplicationContext().sendBroadcast(intent);
        }
    }

    public void requestTrackingOn(int i) {
        //Log.d(TAG, "Received tracking-on request.");
        requestTrackingOn = true;
        interval = i;

        if (!deviceHasLocationEnabled) {
            requestDeviceLocationPermissions();
        }
        else if (!appHasLocationEnabled) {
            requestAppLocationPermissions();
        }
        else {
            try {
                //Log.d(TAG, "Requesting location updates");

                LocationRequest mLocationRequest = new LocationRequest();
                mLocationRequest.setInterval(interval);
                mLocationRequest.setFastestInterval(interval);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleClient, mLocationRequest, this);
            } catch (SecurityException e) {
                //Log.d(TAG, "Request for location updates failed");
                return;
            }

            Intent intent = new Intent();
            intent.setAction(getApplicationContext().getString(R.string.locationServerTrackingOn));
            getApplicationContext().sendBroadcast(intent);

            trackingOn = true;
        }
        return;
    }


    public void requestTrackingOff() {
        //Log.d(TAG, "Received tracking-off request.");
        requestTrackingOn = false;

        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleClient, this);
        } catch (SecurityException e) {
            //Log.d(TAG, "Request to stop location updates failed");
            return;
        }

        Intent intent = new Intent();
        intent.setAction(getApplicationContext().getString(R.string.locationServerTrackingOff));
        getApplicationContext().sendBroadcast(intent);

        trackingOn = false;
    }

    /*******************************************************
     * LocationListener Callback
     *******************************************************/

    @Override
    public void onLocationChanged(Location location) {
        //LatLng curLocation = new LatLng(location.getLatitude(), location.getLongitude());

        Intent intent = new Intent();
        intent.setAction(getApplicationContext().getString(R.string.locationServerLocationChanged));
        intent.putExtra("location", location);
        getApplicationContext().sendBroadcast(intent);
    }

    /*******************************************************
     * Check DEVICE Location Permissions
     *******************************************************/

    private void requestDeviceLocationPermissions() {
        // Check if device has proper Location Settings
        //activeActivity = activity;

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleClient,
                builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //Log.d(TAG, "Device already has permission");
                        deviceHasLocationEnabled = true;

                        if (!appHasLocationEnabled) {
                            requestAppLocationPermissions();
                        }
                        else {
                            Intent intent = new Intent();
                            intent.setAction(getApplicationContext().getString(R.string.locationServerPermissionsGranted));
                            getApplicationContext().sendBroadcast(intent);

                            if (requestTrackingOn && !trackingOn) {
                                requestTrackingOn(interval);
                            }
                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //Log.d(TAG, "Device does not have permission - requesting permission");
                        deviceHasLocationEnabled = false;

                        try {
                            // Request permission and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity,
                                    M_REQUEST_LOCATION_ON_FOR_DEVICE);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                }
            }
        });
    }

    // Activity forwards result back here
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        if (requestCode == M_REQUEST_LOCATION_ON_FOR_DEVICE) {
            if (resultCode == TripViewPager.RESULT_OK )  {
                //Log.d(TAG, "Device granted permissions");
                deviceHasLocationEnabled = true;

                if (!appHasLocationEnabled) {
                    requestAppLocationPermissions();
                }
                else {
                    Intent intent = new Intent();
                    intent.setAction(getApplicationContext().getString(R.string.locationServerPermissionsGranted));
                    getApplicationContext().sendBroadcast(intent);

                    if (requestTrackingOn && !trackingOn) {
                        requestTrackingOn(interval);
                    }
                }
            }
            else if (resultCode == TripViewPager.RESULT_CANCELED) {
                //Log.d(TAG, "Device not granted permissions");
                deviceHasLocationEnabled = false;
            }
        }
    }

    /*******************************************************
     * Check APP Location Permissions
     *******************************************************/

    private void requestAppLocationPermissions() {
        //activeActivity = activity;

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            appHasLocationEnabled = true;
            //Log.d(TAG, "App already has permission");
            if(!deviceHasLocationEnabled) {
                requestDeviceLocationPermissions();
            }
            else {
                Intent intent = new Intent();
                intent.setAction(getApplicationContext().getString(R.string.locationServerPermissionsGranted));
                getApplicationContext().sendBroadcast(intent);

                if (requestTrackingOn && !trackingOn) {
                    requestTrackingOn(interval);
                }
            }
        }
        else {
            appHasLocationEnabled = false;
            //Log.d(TAG, "App does not have permission - requesting permission");
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    M_REQUEST_LOCATION_ON_FOR_APP);
        }
    }

    // Activity forwards result back here
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == M_REQUEST_LOCATION_ON_FOR_APP) {
            //Log.d(TAG, "Received result for app location permission");
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                appHasLocationEnabled = true;
                //Log.d(TAG, "App granted permissions");

                if (!deviceHasLocationEnabled){
                    requestDeviceLocationPermissions();
                }
                else {
                    Intent intent = new Intent();
                    intent.setAction(getApplicationContext().getString(R.string.locationServerPermissionsGranted));
                    getApplicationContext().sendBroadcast(intent);

                    if (requestTrackingOn && !trackingOn) {
                        requestTrackingOn(interval);
                    }
                }
            }
            else {
                //Log.d(TAG, "App not granted permissions");
                appHasLocationEnabled = false;
            }
        }
    }
}
