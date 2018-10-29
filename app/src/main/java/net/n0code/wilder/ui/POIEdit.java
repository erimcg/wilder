package net.n0code.wilder.ui;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import net.n0code.wilder.R;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.obj.POI;
import net.n0code.wilder.ui.tripViewer.GoogleMapFragment;
import net.n0code.wilder.ui.tripViewer.TripViewPager;

import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentExcursion;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentUser;


public class POIEdit extends AppCompatActivity implements POITypePickerDialog.POITypePickerDialogListener {

    private String TAG = "****POI-ADD/EDIT****";
    private POI poi = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_edit);

        if (getIntent().getStringExtra("mode").equals("new")) {
            TextView tv = (TextView) findViewById(R.id.editPointOfInterestTitle);
            tv.setText(R.string.addPointOfInterestTitle);
        }

        ImageButton typeButton = (ImageButton) findViewById(R.id.pointOfInterestType);
        EditText titleET = (EditText) findViewById(R.id.pointOfInterestTitle);
        EditText descET = (EditText) findViewById(R.id.pointOfInterestDescription);
        EditText latET = (EditText) findViewById(R.id.pointOfInterestLatitude);
        EditText lngET = (EditText) findViewById(R.id.pointOfInterestLongitude);

        if (getIntent().hasExtra("location")) {
            Location location = getIntent().getParcelableExtra("location");

            latET.setText(String.valueOf(location.getLatitude()));
            lngET.setText(String.valueOf(location.getLongitude()));
        }
        else if (getIntent().hasExtra("poi_id")) {
            long poi_id = getIntent().getLongExtra("poi_id", -1);

            if (poi_id == -1) {
                Log.d(TAG, "Can not get poi_id from extra");
                return;
            }

            poi = LocalDB.getPointOfInterest(poi_id);

            String uri = "@drawable/" + poi.getType().toLowerCase() + "_24dp";
            int id = getResources().getIdentifier(uri, null, getPackageName());
            typeButton.setImageDrawable(getResources().getDrawable(id));

            titleET.setText(poi.getTitle());
            descET.setText(poi.getDescription());
            latET.setText(String.valueOf(poi.getLocation().latitude));
            lngET.setText(String.valueOf(poi.getLocation().longitude));
        }

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {

        }
    }

    public void editTypeButtonHandler(View v) {
        DialogFragment newFragment = new POITypePickerDialog();
        newFragment.show(getSupportFragmentManager(), "Edit Type");
    }

    public void saveChangesButtonHandler(View v) {

        long eid = 0;
        if (currentExcursion != null) {
            eid = currentExcursion.getExcursionID();
        }
        Log.d(TAG, "excursion id when saving poi : " + eid);

        String type = findViewById(R.id.pointOfInterestType).getContentDescription().toString();
        String title = ((EditText) findViewById(R.id.pointOfInterestTitle)).getText().toString();
        String desc = ((EditText) findViewById(R.id.pointOfInterestDescription)).getText().toString();
        double latitude = Double.valueOf(((EditText) findViewById(R.id.pointOfInterestLatitude)).getText().toString());
        double longitude = Double.valueOf(((EditText) findViewById(R.id.pointOfInterestLongitude)).getText().toString());
        long exp_id = currentUser.getUserID();

        if (poi == null) {
            Log.d(TAG, "adding new poi");
            poi = new POI(type, eid, title, desc, latitude, longitude, exp_id);
            LocalDB.addPointOfInterest(poi);
        }
        else {
            Log.d(TAG, "updating existing poi");
            poi.setType(type);
            poi.setTitle(title);
            poi.setDescription(desc);
            poi.setLocation(latitude, longitude);
            LocalDB.updatePointOfInterest(poi);
        }

        GoogleMapFragment.drawPOIMarker(new LatLng(latitude, longitude), type, title);

        finish();
    }

    public void cancelChangesButtonHandler(View v) {
        finish();
    }

    @Override
    public void onSelectClick(DialogFragment dialog, String type) {
        String uri = "@drawable/" + type.toLowerCase() + "_24dp";
        Log.d(TAG, "Selected POI image: " + uri);

        int id = getResources().getIdentifier(uri, null, getPackageName());
        ImageButton typeButton = (ImageButton) findViewById(R.id.pointOfInterestType);
        typeButton.setContentDescription(type);
        typeButton.setImageDrawable(getResources().getDrawable(id));
    }
}
