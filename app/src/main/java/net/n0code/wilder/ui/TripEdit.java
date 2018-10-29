package net.n0code.wilder.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import net.n0code.wilder.R;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.obj.Trip;
import net.n0code.wilder.obj.ShareMode;
import net.n0code.wilder.obj.TravelMode;
import net.n0code.wilder.obj.User;

import java.util.ArrayList;

import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentTrip;

public class TripEdit extends AppCompatActivity {

    private String TAG = "***TRIP-ADD/EDIT***";
    private Trip trip = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_edit);

        if (getIntent().hasExtra("trip")) {
            trip = getIntent().getParcelableExtra("trip");
        }

        Spinner shareMode = (Spinner) findViewById(R.id.tripShareModeSpinner);
        ArrayList<String> share_mode_list = new ArrayList<>();
        for (ShareMode mode : ShareMode.values()) {
            share_mode_list.add(mode.toString());
        }
        ArrayAdapter<String> shareModeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, share_mode_list);
        shareModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shareMode.setAdapter(shareModeAdapter);

        Spinner travelMode = (Spinner) findViewById(R.id.tripTravelModeSpinner);
        ArrayList<String> travel_mode_list = new ArrayList<>();
        for (TravelMode mode: TravelMode.values()) {
            travel_mode_list.add(mode.toString());
        }
        ArrayAdapter<String> travelModeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, travel_mode_list);
        travelModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        travelMode.setAdapter(travelModeAdapter);

        if (trip != null) {
            EditText view = (EditText) findViewById(R.id.tripTitle);
            view.setText(trip.getTitle());

            view = (EditText) findViewById(R.id.tripDescription);
            view.setText(trip.getDescription());

            shareMode.setSelection(shareModeAdapter.getPosition(trip.getShareMode()));
            travelMode.setSelection(travelModeAdapter.getPosition(trip.getTravelMode()));
        }
        else {
            TextView view = (TextView) findViewById(R.id.editTripTitle);
            view.setText(R.string.createTripTitle);
        }
    }

    public void saveChangesButtonHandler(View v) {
        String title = ((EditText) findViewById(R.id.tripTitle)).getText().toString();
        String desc = ((EditText) findViewById(R.id.tripDescription)).getText().toString();
        String sm = ((Spinner) findViewById(R.id.tripShareModeSpinner)).getSelectedItem().toString();
        String tm = ((Spinner) findViewById(R.id.tripTravelModeSpinner)).getSelectedItem().toString();

        if (trip == null) {
            Log.d(TAG, "adding new trip");

            SharedPreferences sharedPref = getSharedPreferences(
                    getString(R.string.wilderSharedPreferences), Context.MODE_PRIVATE);
            String email = sharedPref.getString(getString(R.string.prefCurrentUserEmailKey), "unknown");
            User currentUser = LocalDB.getUser(email);

            Trip newTrip = new Trip.Builder(this, title, currentUser.getUserID()).
                    description(desc).shareMode(sm).travelMode(tm).build();

            LocalDB.addTrip(newTrip);

            Cursor cursor = LocalDB.getTrips();
            TripList.adapter.changeCursor(cursor);
        }
        else {
            Log.d(TAG, "updating existing trip");
            trip.setTitle(title);
            trip.setDescription(desc);
            trip.setShareMode(sm);
            trip.setTravelMode(tm);
            trip.updateTimeLastModified();

            LocalDB.updateTrip(trip);
            currentTrip = trip;

            Cursor cursor = LocalDB.getTrips();
            TripList.adapter.changeCursor(cursor);
        }

        finish();
    }

    public void cancelChangesButtonHandler(View v) {
        finish();
    }
}
