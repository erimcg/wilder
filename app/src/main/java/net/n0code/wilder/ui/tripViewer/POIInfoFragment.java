package net.n0code.wilder.ui.tripViewer;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import com.google.android.gms.maps.model.LatLng;

import net.n0code.wilder.R;
import net.n0code.wilder.db.Excursions_T;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.db.POI_T;
import net.n0code.wilder.ui.POIEdit;

import static net.n0code.wilder.ui.tripViewer.ExcursionListFragment.currentExcursionPosition;
import static net.n0code.wilder.ui.tripViewer.ExcursionListFragment.excursionsCursor;
import static net.n0code.wilder.ui.tripViewer.POIListFragment.poiCursor;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentTrip;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.pager;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.setQuickScroller;

public class POIInfoFragment extends Fragment implements View.OnClickListener {

    private String TAG = "***POI-INFO***";
    private View rootView = null;
    public static int currentPOIPosition = 0;
    private LatLng mLocation;

    public POIInfoFragment() {} // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.poi_info, container, false);

        ImageButton button = (ImageButton) rootView.findViewById(R.id.editPointOfInterestButton);
        button.setOnClickListener(this);

        button = (ImageButton) rootView.findViewById(R.id.navigateToPointOfInterestButton);
        button.setOnClickListener(this);

        button = (ImageButton) rootView.findViewById(R.id.prevPointOfInterestButton);
        button.setOnClickListener(this);

        button = (ImageButton) rootView.findViewById(R.id.nextPointOfInterestButton);
        button.setOnClickListener(this);

        return rootView;
    }


    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Log.d(TAG, "visible: " + isVisibleToUser);

        if (isVisibleToUser && rootView != null) {
            Log.d(TAG, "refreshing view");

            poiCursor = null;

            if (excursionsCursor == null || excursionsCursor.getCount() == 0){
                excursionsCursor = LocalDB.getExcursionInfo(currentTrip.getTripID());
            }

            if (excursionsCursor != null && excursionsCursor.getCount() > 0) {
                if (currentExcursionPosition == -1) {
                    currentExcursionPosition = excursionsCursor.getCount() - 1;
                }
                excursionsCursor.moveToPosition(currentExcursionPosition);
                long excursionID = excursionsCursor.getLong(excursionsCursor.getColumnIndexOrThrow(Excursions_T.EXCURSION_ID));
                poiCursor = LocalDB.getPointsOfInterestByEID(excursionID);
            }

            if (poiCursor == null || poiCursor.getCount() == 0) {
                ConstraintLayout layout = (ConstraintLayout) rootView.findViewById(R.id.pointOfInterestInfoGroup);
                layout.setVisibility(View.INVISIBLE);

                ImageButton button = (ImageButton) rootView.findViewById(R.id.editPointOfInterestButton);
                button.setVisibility(View.INVISIBLE);

                return;
            }
            else {
                ConstraintLayout layout = (ConstraintLayout) rootView.findViewById(R.id.pointOfInterestInfoGroup);
                layout.setVisibility(View.VISIBLE);
            }

            if (poiCursor.getCount() < 2) {
                ImageButton button = (ImageButton) rootView.findViewById(R.id.prevPointOfInterestButton);
                button.setVisibility(View.INVISIBLE);

                button = (ImageButton) rootView.findViewById(R.id.nextPointOfInterestButton);
                button.setVisibility(View.INVISIBLE);
            }
            else {
                ImageButton button = (ImageButton) rootView.findViewById(R.id.prevPointOfInterestButton);
                button.setVisibility(View.VISIBLE);

                button = (ImageButton) rootView.findViewById(R.id.nextPointOfInterestButton);
                button.setVisibility(View.VISIBLE);
            }

            Log.d(TAG, "Cursor count: " + poiCursor.getCount());
            poiCursor.moveToPosition(currentPOIPosition);

            String type = poiCursor.getString(poiCursor.getColumnIndex(POI_T.TYPE));
            String uri = "@drawable/" + type.toLowerCase() + "_24dp";
            int id = getResources().getIdentifier(uri, null, getContext().getPackageName());

            String title = poiCursor.getString(poiCursor.getColumnIndex(POI_T.TITLE));
            String desc = poiCursor.getString(poiCursor.getColumnIndexOrThrow(POI_T.DESCRIPTION));
            long explorerID = poiCursor.getLong(poiCursor.getColumnIndexOrThrow(POI_T.EXPLORER_ID));
            String username = LocalDB.getUsername(explorerID);
            long excursionID = poiCursor.getLong(poiCursor.getColumnIndexOrThrow(POI_T.EXCURSION_ID));
            String excursionName = LocalDB.getExcursionTitle(excursionID);

            double mLat = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow(POI_T.LATITUDE));
            double mLng = poiCursor.getDouble(poiCursor.getColumnIndexOrThrow(POI_T.LONGITUDE));
            mLocation = new LatLng(mLat, mLng);

            String lat = String.valueOf(mLat);
            String lng = String.valueOf(mLng);
            String location = "(" + lat + "," + lng + ")";

            String date = poiCursor.getString(poiCursor.getColumnIndexOrThrow(POI_T.DATE_CREATED));
            String time = poiCursor.getString(poiCursor.getColumnIndexOrThrow(POI_T.TIME_CREATED));
            String dateTime = date + " at " + time;

            ImageButton typeButton = (ImageButton) rootView.findViewById(R.id.pointOfInterestType);
            typeButton.setImageDrawable(getResources().getDrawable(id));
            TextView titleTV = (TextView) rootView.findViewById(R.id.pointOfInterestTitle);
            titleTV.setText(title);
            TextView descTV = (TextView) rootView.findViewById(R.id.pointOfInterestDescription);
            descTV.setText(desc);
            TextView explorerTV = (TextView) rootView.findViewById(R.id.pointOfInterestCreator);
            explorerTV.setText(username);
            TextView excursionTV = (TextView) rootView.findViewById(R.id.pointOfInterestExcursion);
            excursionTV.setText(excursionName);
            TextView locationTV = (TextView) rootView.findViewById(R.id.pointOfInterestLocation);
            locationTV.setText(location);
            TextView dateTimeTV = (TextView) rootView.findViewById(R.id.pointOfInterestDateTime);
            dateTimeTV.setText(dateTime);

            if (explorerID != TripViewPager.currentUser.getUserID()) {
                ImageButton button = (ImageButton) rootView.findViewById(R.id.editPointOfInterestButton);
                button.setVisibility(View.INVISIBLE);
            }
            else {
                ImageButton button = (ImageButton) rootView.findViewById(R.id.editPointOfInterestButton);
                button.setVisibility(View.VISIBLE);
            }
        }
    }

    public void onResume() {
        super.onResume();
        setUserVisibleHint(true);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick");

        if (poiCursor == null || !(v instanceof ImageButton)) {
            return;
        }

        int count = poiCursor.getCount();
        switch (v.getId()) {
            case R.id.nextPointOfInterestButton:
                if (currentPOIPosition < count - 1) {
                    currentPOIPosition++;
                }
                else if (currentPOIPosition == count - 1) {
                    currentPOIPosition = 0;
                }
                setUserVisibleHint(true);
                break;

            case R.id.prevPointOfInterestButton:
                if (currentPOIPosition > 0) {
                    currentPOIPosition--;
                }
                else if (currentPOIPosition == 0) {
                    currentPOIPosition = count - 1;
                }
                setUserVisibleHint(true);
                break;

            case R.id.editPointOfInterestButton:
                Intent i = new Intent(getActivity(), POIEdit.class);

                i.putExtra("mode", "edit");
                long poi_id = poiCursor.getLong(poiCursor.getColumnIndexOrThrow(POI_T.POI_ID));
                i.putExtra("poi_id", poi_id);

                startActivity(i);
                break;

            case R.id.navigateToPointOfInterestButton:
                setQuickScroller();

                GoogleMapFragment.cameraLocation = mLocation;
                GoogleMapFragment.zoomLevel = 19;
                pager.setCurrentItem(TripViewPager.GOOGLE_MAP, true);
                break;
        }

    }

}
