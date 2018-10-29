package net.n0code.wilder.ui.tripViewer;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import net.n0code.wilder.R;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.ui.lib.CubeOutTransformer;
import net.n0code.wilder.obj.Excursion;
import net.n0code.wilder.obj.Trip;
import net.n0code.wilder.obj.User;
import net.n0code.wilder.ui.vpi.CirclePageIndicator;
import net.n0code.wilder.location.LocationService;

import java.lang.reflect.Field;

public class TripViewPager extends FragmentActivity {

    private static String TAG = "****Trip-ViewPager***";

    private static final int NUM_PAGES = 6;

    public static final int TRIP_INFO = 0;
    public static final int GOOGLE_MAP = 1;
    public static final int LIST_EXCURSIONS = 2;
    public static final int EXCURSION_INFO = 3;
    public static final int LIST_POI = 4;
    public static final int POI_INFO = 5;

    private TripInfoFragment tripInfoFragment = null;
    private GoogleMapFragment mapFragment = null;
    private ExcursionListFragment eListFragment = null;
    private ExcursionInfoFragment eInfoFragment = null;
    private POIListFragment poiListFragment = null;
    private POIInfoFragment poiInfoFragment = null;

    public static Trip currentTrip = null;
    public static User currentUser = null;
    public static Excursion currentExcursion = null;

    public static Location lastKnownLocation = null;
    public static boolean trackingOn = false;

    public static LocationService locationService;
    public static boolean mBound = false;
    public static ViewPager pager;
    public static CirclePageIndicator pageIndicator;

    private static Field mScrollerField;
    private static FixedSpeedScroller slowScroller;
    private static FixedSpeedScroller quickScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Creating trip view pager");

        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        setContentView(R.layout.trip_viewpager);

        currentTrip = null;

        if (getIntent().hasExtra("trip")) {
            currentTrip = getIntent().getParcelableExtra("trip");
            Log.d(TAG, "Loading trip: " + currentTrip.getTripID());
        }

        if (currentTrip == null) {
            Log.d(TAG, "Oops...current trip is null");
            finish();
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.wilderSharedPreferences), Context.MODE_PRIVATE);
        String email = sharedPref.getString(getString(R.string.prefCurrentUserEmailKey), "unknown");
        currentUser = LocalDB.getUser(email);

        if (currentUser == null) {
            Log.d(TAG, "Oops...current user is null");
            finish();
            return;
        }

        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        LocalDB.openDB(this);

        pager = (ViewPager) findViewById(R.id.tripViewerPager);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(5);

        pager.setPageTransformer(false, new CubeOutTransformer());

        try {
            mScrollerField = ViewPager.class.getDeclaredField("mScroller");
            mScrollerField.setAccessible(true);

            Interpolator sInterpolator = new LinearInterpolator();
            quickScroller = new FixedSpeedScroller(pager.getContext(), sInterpolator, 10);
            slowScroller = new FixedSpeedScroller(pager.getContext(), sInterpolator, 750);
            mScrollerField.set(pager, slowScroller);

        } catch (Exception e) {
            Log.d(TAG, "error setting scroller");
        }

        pageIndicator = (CirclePageIndicator)findViewById(R.id.pageIndicator);
        pageIndicator.setViewPager(pager);

        // Create fragments
        tripInfoFragment = new TripInfoFragment();
        mapFragment = new GoogleMapFragment();
        eListFragment = new ExcursionListFragment();
        eInfoFragment = new ExcursionInfoFragment();
        poiListFragment = new POIListFragment();
        poiInfoFragment = new POIInfoFragment();

        pager.setCurrentItem(GOOGLE_MAP);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "ONSTART");
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "service connected");
            LocationService.LocationServiceBinder binder = (LocationService.LocationServiceBinder) service;
            locationService = binder.getService();
            mBound = true;
            locationService.requestPermissions(TripViewPager.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    public static void setSlowScroller() {
        try {
            mScrollerField.set(pager, slowScroller);
        } catch (Exception e) {
            Log.d(TAG, "error setting default scroller");
        }
    }

    public static void setQuickScroller() {
        try {
            mScrollerField.set(pager, quickScroller);
        } catch (Exception e) {
            Log.d(TAG, "error setting quick scroller");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unbind from the service
        if (mBound) {
            Log.d(TAG, "unbinding service");
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onBackPressed() {
        if(trackingOn) {
            Log.d(TAG, "navigating home");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            builder.setTitle(getString(R.string.viewPagerDialogTitle));

            AlertDialog dialog = builder.create();
            dialog.show();
            return;
        }

        finish();
    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {
        private ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment nextFragment = mapFragment;

            switch(position) {
                case TRIP_INFO:
                    nextFragment = tripInfoFragment;
                    break;
                case GOOGLE_MAP:
                    nextFragment = mapFragment;
                    break;
                case LIST_EXCURSIONS:
                    nextFragment = eListFragment;
                    break;
                case EXCURSION_INFO:
                    nextFragment = eInfoFragment;
                    break;
                case LIST_POI:
                    nextFragment = poiListFragment;
                    break;
                case POI_INFO:
                    nextFragment = poiInfoFragment;
                    break;
                default:
                    nextFragment = mapFragment;
            }

            return nextFragment;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    public class FixedSpeedScroller extends Scroller {

        private int mDuration;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator, int duration) {
            super(context, interpolator);
            mDuration = duration;
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }

    public void navigateHomeButtonHandler(View v) {

    }

    /*
     * The activity that gets the instance of LocationServer will receive permission
     * results from the system.  This activity forwards the results back to the LocationServer
     */

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "Received permission results for app");
        if (requestCode == LocationService.M_REQUEST_LOCATION_ON_FOR_APP) {
            locationService.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Received permission results for device");
        if (requestCode == LocationService.M_REQUEST_LOCATION_ON_FOR_DEVICE) {
            locationService.onActivityResult(requestCode, resultCode, data);
        }
    }
}

