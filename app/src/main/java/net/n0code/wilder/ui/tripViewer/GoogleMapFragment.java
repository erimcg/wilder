package net.n0code.wilder.ui.tripViewer;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.n0code.wilder.R;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.db.GPS_T;
import net.n0code.wilder.db.Excursions_T;
import net.n0code.wilder.db.POI_T;
import net.n0code.wilder.obj.Excursion;
import net.n0code.wilder.ui.POIEdit;
import net.n0code.wilder.ui.dialog.InputDialog;
import net.n0code.wilder.ui.dialog.OnInputStringListener;
//import net.n0code.wilder.util.LocationServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentUser;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentTrip;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentExcursion;

import static net.n0code.wilder.ui.tripViewer.TripViewPager.lastKnownLocation;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.locationService;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.pageIndicator;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.setSlowScroller;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.trackingOn;

import static net.n0code.wilder.ui.tripViewer.ExcursionListFragment.currentExcursionPosition;

public class GoogleMapFragment extends Fragment implements
        OnClickListener,
        OnMapReadyCallback {

    private static final String TAG = "***MAP***";
    private static Context context;
    private static GoogleMap mMap;
    private UiSettings mUiSettings;
    private View rootView = null;
    private BroadcastReceiver br;
    private IntentFilter filter;

    // TODO: detect travel speed and set Location gathering relative to travel speed
    public static final int WALKING_SPEED = 5000;

    private static final int DOT_WIDTH = 10;
    private static final int PATTERN_GAP_LENGTH_PX = DOT_WIDTH;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    private static final int DEFAULT_TILT = 0;
    private static final int DEFAULT_ZOOM = 18;

    public static LatLng cameraLocation = null;
    public static int tiltLevel = DEFAULT_TILT;
    public static int zoomLevel = DEFAULT_ZOOM;

    public GoogleMapFragment() { } // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.google_map, container, false);
        }

        ImageButton trackButton = (ImageButton) rootView.findViewById(R.id.locationTrackingButton);
        trackButton.setOnClickListener(this);

        ImageButton addPOIButton = (ImageButton) rootView.findViewById(R.id.addPointOfInterestButton);
        addPOIButton.setOnClickListener(this);
        addPOIButton.setVisibility(View.INVISIBLE);

        if (currentTrip.getExplorerID() != currentUser.getUserID()) {
            trackButton.setVisibility(View.INVISIBLE);
        }

        // Create BroadcastReceiver
        br = new LocationServerBroadcastReceiver();
        filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(getString(R.string.locationServerPermissionsGranted));
        filter.addAction(getString(R.string.locationServerTrackingOn));
        filter.addAction(getString(R.string.locationServerTrackingOff));
        filter.addAction(getString(R.string.locationServerLocationChanged));
        getActivity().registerReceiver(br, filter);

        String title = currentTrip.getTitle();
        TextView v = (TextView) rootView.findViewById(R.id.tripMapTitle);
        v.setText(title);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        //pager.setPageTransformer(false, new FlipPageViewTransformer());

        if (isVisibleToUser && rootView != null) {
            Log.d(TAG, "fragment is visible");
            String title = currentTrip.getTitle();
            TextView v = (TextView) rootView.findViewById(R.id.tripMapTitle);
            v.setText(title);

            moveCamera(cameraLocation, tiltLevel, zoomLevel);

            Handler handler = new Handler();
            final Runnable r = new Runnable() {
                public void run() {
                    setSlowScroller();
                }
            };
            handler.postDelayed(r, 250);

            if (mMap != null) {
                onMapReady(mMap);
            }
        }
        else {
            Log.d(TAG, "fragment is not visible");
            currentExcursionPosition = -1;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(br);
        cameraLocation = null;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        Log.d(TAG, "map is ready");
        // TODO: Add ability to change map type
        if (mMap != null) {
            mMap.clear();

            try {
                mMap.setMyLocationEnabled(true);
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

                mUiSettings = mMap.getUiSettings();
                mUiSettings.setZoomControlsEnabled(true);
                mUiSettings.setCompassEnabled(true);
                mUiSettings.setMyLocationButtonEnabled(true);
                mUiSettings.setMapToolbarEnabled(false);

                mUiSettings.setScrollGesturesEnabled(true);
                mUiSettings.setZoomGesturesEnabled(true);
                mUiSettings.setTiltGesturesEnabled(true);
                mUiSettings.setRotateGesturesEnabled(true);
            }
            catch (SecurityException e) {
                Log.d("MAP", "Can not show user location - permission denied");
                if (locationService != null) {
                    locationService.requestPermissions(getActivity());
                }
            }

            drawSavedRoutes();
            drawPOIMarkers();

            Log.d(TAG, "moving camera");
            moveCamera(cameraLocation, tiltLevel, zoomLevel);
        }
    }

    private class LocationServerBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "****BR****";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, action);

            if (action.equals(getString(R.string.locationServerPermissionsGranted))) {
                onMapReady(mMap);
            }
            else if (action.equals(getString(R.string.locationServerTrackingOn))) {
                trackingOn = true;
                animatePageIndicator();
                if (currentExcursion == null) {
                    createNewExcursion();
                }
                switchTrackingButtonDrawable();
            }
            else if (action.equals(getString(R.string.locationServerTrackingOff))) {
                pageIndicator.clearAnimation();
                trackingOn = false;
                lastKnownLocation = null;
                switchTrackingButtonDrawable();
                Date now = new Date();
                currentExcursion.setEndTime(now.getTime());
                LocalDB.updateExcursion(currentExcursion);
                currentExcursion = null;
            }
            else if (action.equals(getString(R.string.locationServerLocationChanged))) {
                Location location = intent.getParcelableExtra("location");
                onLocationChange(location);
            }
        }
    }

    private void animatePageIndicator() {
        final Animation animation = new AlphaAnimation(1.0f, 0.5f); // Change alpha from fully visible to invisible
        animation.setDuration(300); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

        pageIndicator.startAnimation(animation);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.locationTrackingButton) {
            toggleLocationTrackingButton();
        }
        else if (v.getId() == R.id.addPointOfInterestButton) {
            if (lastKnownLocation == null) {
                Log.d(TAG, "Unable to add new poi: lastKnownLocation is null");
                Toast.makeText(getContext(), "Waiting for GPS location ...", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(getActivity(), POIEdit.class);
            i.putExtra("mode", "new");
            i.putExtra("location", lastKnownLocation);
            Log.d(TAG, "lat: " + lastKnownLocation.getLatitude() + " lng: " + lastKnownLocation.getLongitude());

            startActivity(i);
        }
    }

    private void onLocationChange(Location location) {
        if (currentExcursion == null) {
            return;
        }

        double altitude = location.getAltitude();
        if (altitude > 0 && altitude < currentExcursion.getMinAltitude()) {
            currentExcursion.setMinAltitude(altitude);
        }
        if (altitude > currentExcursion.getMaxAltitude()) {
            currentExcursion.setMaxAltitude(altitude);
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (lastKnownLocation != null) {
            double distance = lastKnownLocation.distanceTo(location) + currentExcursion.getDistance();
            currentExcursion.setDistance(distance);
        }
        LocalDB.updateExcursion(currentExcursion);

        Log.d(TAG, "adding location for eid:" + currentExcursion.getExcursionID() + ", tid:" + currentExcursion.getTripID());
        LocalDB.addLocation(currentExcursion.getExcursionID(), latLng, location.getAltitude());

        drawSegment(location);
    }

    public static void toggleLocationTrackingButton()
    {
        if (trackingOn){
            Log.d(TAG, "Tracking button toggled off");
            locationService.requestTrackingOff();
        }
        else {
            Log.d(TAG, "Tracking button toggled on");
            // TODO: adjust speed based on velocity
            locationService.requestTrackingOn(WALKING_SPEED);
        }
    }

    private void switchTrackingButtonDrawable() {
        ImageButton btn = (ImageButton) getActivity().findViewById(R.id.locationTrackingButton);
        ImageButton addPOIButton = (ImageButton) rootView.findViewById(R.id.addPointOfInterestButton);

        if (trackingOn) {
            btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_black_24dp, null));
            addPOIButton.setVisibility(View.VISIBLE);
        } else {
            btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_record_red_24dp, null));
            addPOIButton.setVisibility(View.INVISIBLE);
        }
    }

    private void createNewExcursion() {
        currentExcursion = new Excursion(currentTrip.getTripID(), currentUser.getUserID());
        LocalDB.addExcursion(currentExcursion);
        setExcursionName();
    }

    private void setExcursionName()
    {
        InputDialog dialog = new InputDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setPositiveButton(new OnInputStringListener() {
            @Override
            public boolean onInputString(AlertDialog dialog, String value) {
                Log.d(TAG, "Adding excursion: " + value);
                String title = value;
                if (title == null || title.length() == 0) {
                    title = "My Excursion";
                }
                currentExcursion.setTitle(title);
                LocalDB.updateExcursion(currentExcursion);

                return false; //return if consume event
            }
        });
        dialog.setTitle("Creating New Excursion");
        dialog.setHint("Please enter excursion name");
        dialog.show();
    }

    private void drawSavedRoutes() {
        Cursor routesCursor = LocalDB.getExcursionIDs(currentTrip.getTripID());
        Log.d(TAG, "drawing routes for tid:" + currentTrip.getTripID());

        if (routesCursor == null) {
            return;
        }

        int count = routesCursor.getCount();

        for (int i = 0; i < count; i++) {

            int eid = routesCursor.getInt(routesCursor.getColumnIndexOrThrow(Excursions_T.EXCURSION_ID));
            Cursor eCursor = LocalDB.getExcursion(eid);
            eCursor.moveToFirst();

            Log.d(TAG, "Drawing route for excursion: " + eid);
            Cursor c = LocalDB.getRoute(eid);

            if (c == null) {
                Log.d(TAG, "no route exists");
                return;
            }

            LatLng startLocation = new LatLng(c.getFloat(c.getColumnIndexOrThrow(GPS_T.LATITUDE)),
                    c.getFloat(c.getColumnIndexOrThrow(GPS_T.LONGITUDE)));

            if (cameraLocation == null) {
                cameraLocation = startLocation;
            }

            ArrayList<LatLng> list = new ArrayList<>();
            LatLng next = null;
            int num_points = c.getCount();
            for (int point = 0; point < num_points; point++) {
                float lat = c.getFloat(c.getColumnIndexOrThrow(GPS_T.LATITUDE));
                float lng = c.getFloat(c.getColumnIndexOrThrow(GPS_T.LONGITUDE));
                next = new LatLng(lat, lng);
                list.add(next);
                c.moveToNext();
            }
            c.close();
            drawRoute(list);

            eCursor.close();

            lastKnownLocation = null;
            routesCursor.moveToNext();
        }
        routesCursor.close();
    }

    private void drawRoute(ArrayList<LatLng> points) {
        //PatternItem DOT = new Dot();

        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .addAll(points)
                .width(7)
                .color(Color.BLACK));

        polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        polyline.setGeodesic(true);
    }

    private void drawSegment(Location curLocation) {
        String mssg = "(" + curLocation.getLatitude() + "," + curLocation.getLongitude() + ")";
        Log.d(TAG, mssg);

        if (lastKnownLocation == null) {
            lastKnownLocation = curLocation;
            return;
        }
        LatLng start = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        LatLng end = new LatLng(curLocation.getLatitude(), curLocation.getLongitude());

        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .add(start, end)
                .width(7)
                .color(Color.BLACK));

        polyline.setPattern(PATTERN_POLYLINE_DOTTED);
        polyline.setGeodesic(true);

        //moveCamera(curLocation);

        lastKnownLocation = curLocation;
    }

    private void drawPOIMarkers() {
        Cursor c = LocalDB.getPointsOfInterestByTID(currentTrip.getTripID());

        if (c == null) {
            Log.d(TAG, "no points of interest to draw");
            return;
        }

        int count = c.getCount();
        for (int i = 0; i < count; i++) {
            double latitude = c.getDouble(c.getColumnIndexOrThrow(POI_T.LATITUDE));
            double longitude = c.getDouble(c.getColumnIndexOrThrow(POI_T.LONGITUDE));
            String type = c.getString(c.getColumnIndexOrThrow(POI_T.TYPE));
            String title = c.getString(c.getColumnIndexOrThrow(POI_T.TITLE));
            drawPOIMarker(new LatLng(latitude, longitude), type, title);
            c.moveToNext();
        }
        c.close();
    }

    public static void drawPOIMarker(LatLng location, String type, String label) {
        Log.d(TAG, "drawing location: " + label + " at (" + location.latitude + "," + location.longitude + ")");

        String uri = "@drawable/" + type.toLowerCase() + "_24dp";
        int id = context.getResources().getIdentifier(uri, null, context.getPackageName());

        mMap.addMarker(new MarkerOptions()
                .position(location)
                .zIndex(2)
                .icon(BitmapDescriptorFactory.fromResource(id))
                .title(label));
    }

    public static void moveCamera(LatLng location, int tilt, int zoom) {
        if (location == null || mMap == null) {
            return;
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)
                .zoom(zoom)
                .tilt(tilt)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}
