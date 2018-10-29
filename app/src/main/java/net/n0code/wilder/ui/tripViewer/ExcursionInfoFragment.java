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
import net.n0code.wilder.ui.ExcursionEdit;

import java.text.SimpleDateFormat;
import java.util.Date;

import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentTrip;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.pager;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.setQuickScroller;

import static net.n0code.wilder.ui.tripViewer.ExcursionListFragment.excursionsCursor;
import static net.n0code.wilder.ui.tripViewer.ExcursionListFragment.currentExcursionPosition;

public class ExcursionInfoFragment extends Fragment implements View.OnClickListener {

    private String TAG = "*** Excursion Info ***";
    private View rootView = null;
    private LatLng startLocation;

    public static long excursionID = -1;

    public ExcursionInfoFragment() {} // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.excursion_info, container, false);

        ImageButton button = (ImageButton) rootView.findViewById(R.id.editExcursionButton);
        button.setOnClickListener(this);

        button = (ImageButton) rootView.findViewById(R.id.navigateToExcursionButton);
        button.setOnClickListener(this);

        button = (ImageButton) rootView.findViewById(R.id.prevExcursionButton);
        button.setOnClickListener(this);

        button = (ImageButton) rootView.findViewById(R.id.nextExcursionButton);
        button.setOnClickListener(this);

        return rootView;
    }


    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Log.d(TAG, "visible: " + isVisibleToUser);

        if (isVisibleToUser && rootView != null) {
            Log.d(TAG, "refreshing view");

            if (excursionsCursor == null) {
                excursionsCursor = LocalDB.getExcursionInfo(currentTrip.getTripID());
            }

            if (excursionsCursor == null || excursionsCursor.getCount() == 0) {
                ConstraintLayout layout = (ConstraintLayout) rootView.findViewById(R.id.excursionInfoGroup);
                layout.setVisibility(View.INVISIBLE);

                ImageButton button = (ImageButton) rootView.findViewById(R.id.editExcursionButton);
                button.setVisibility(View.INVISIBLE);

                return;
            }
            else  {
                ConstraintLayout layout = (ConstraintLayout) rootView.findViewById(R.id.excursionInfoGroup);
                layout.setVisibility(View.VISIBLE);
            }

            if (excursionsCursor.getCount() < 2) {
                ImageButton button = (ImageButton) rootView.findViewById(R.id.prevExcursionButton);
                button.setVisibility(View.INVISIBLE);

                button = (ImageButton) rootView.findViewById(R.id.nextExcursionButton);
                button.setVisibility(View.INVISIBLE);
            }
            else {
                ImageButton button = (ImageButton) rootView.findViewById(R.id.prevExcursionButton);
                button.setVisibility(View.VISIBLE);

                button = (ImageButton) rootView.findViewById(R.id.nextExcursionButton);
                button.setVisibility(View.VISIBLE);
            }

            if (currentExcursionPosition == -1) {
                currentExcursionPosition = excursionsCursor.getCount() - 1;
            }

            excursionsCursor.moveToPosition(currentExcursionPosition);

            String title = excursionsCursor.getString(excursionsCursor.getColumnIndex(Excursions_T.TITLE));
            TextView titleTV = (TextView) rootView.findViewById(R.id.excursionInfoTitle);
            titleTV.setText(title);

            String description = excursionsCursor.getString(excursionsCursor.getColumnIndex(Excursions_T.DESCRIPTION));
            TextView descriptionTV = (TextView) rootView.findViewById(R.id.excursionInfoDescription);
            descriptionTV.setText(description);

            long explorerID = excursionsCursor.getLong(excursionsCursor.getColumnIndex(Excursions_T.EXPLORER_ID));
            String name = LocalDB.getUserName(explorerID);
            TextView explorerTV = (TextView) rootView.findViewById(R.id.excursionInfoCreator);
            explorerTV.setText(name);

            double distance = excursionsCursor.getDouble(excursionsCursor.getColumnIndexOrThrow(Excursions_T.DISTANCE));
            distance *= 0.00062137;
            TextView distanceTV = (TextView) rootView.findViewById(R.id.excursionInfoDistance);
            String d = String.format("%.1f miles", distance);
            distanceTV.setText(d);

            double minAlt = excursionsCursor.getDouble(excursionsCursor.getColumnIndexOrThrow(Excursions_T.MIN_ALTITUDE));
            minAlt *= 3.2808399;
            TextView minAltTV = (TextView) rootView.findViewById(R.id.excursionInfoMinAlt);
            String min = String.format("%.0f feet", minAlt);
            minAltTV.setText(min);

            double maxAlt = excursionsCursor.getDouble(excursionsCursor.getColumnIndexOrThrow(Excursions_T.MAX_ALTITUDE));
            maxAlt *= 3.2808399;
            TextView maxAltTV = (TextView) rootView.findViewById(R.id.excursionInfoMaxAlt);
            String max = String.format("%.0f feet", maxAlt);
            maxAltTV.setText(max);

            double gain = maxAlt - minAlt;
            TextView gainTV = (TextView) rootView.findViewById(R.id.excursionInfoElevationGain);
            String g = String.format("%.0f feet", gain);
            gainTV.setText(g);

            excursionID = excursionsCursor.getLong(excursionsCursor.getColumnIndexOrThrow(Excursions_T.EXCURSION_ID));
            startLocation = LocalDB.getExcursionStartingLocation(excursionID);
            if (startLocation != null) {
                TextView startLocTV = (TextView) rootView.findViewById(R.id.excursionInfoStartLocation);
                startLocTV.setText("(" + startLocation.latitude + "," + startLocation.longitude + ")");
            }

            long startTime = excursionsCursor.getLong(excursionsCursor.getColumnIndexOrThrow(Excursions_T.TIME_CREATED));
            String time = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a z").format(startTime);
            TextView dateTimeCreatedTV = (TextView) rootView.findViewById(R.id.excursionInfoStartTime);
            dateTimeCreatedTV.setText(time);

            long endTime = excursionsCursor.getLong(excursionsCursor.getColumnIndexOrThrow(Excursions_T.TIME_ENDED));
            if (endTime > 0) {
                time = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a z").format(endTime);
                TextView dateTimeEndedTV = (TextView) rootView.findViewById(R.id.excursionInfoEndTime);
                dateTimeEndedTV.setText(time);
            } else {
                TextView dateTimeEndedTV = (TextView) rootView.findViewById(R.id.excursionInfoEndTime);
                dateTimeEndedTV.setText("---");
            }

            long timeSpan = endTime - startTime;
            timeSpan = (timeSpan > 0) ? timeSpan / 60000 : 0;
            String duration = String.format("%d min", timeSpan);
            TextView dateTimeTV = (TextView) rootView.findViewById(R.id.excursionInfoDuration);
            dateTimeTV.setText(duration);

            if (explorerID != TripViewPager.currentUser.getUserID()) {
                ImageButton button = (ImageButton) rootView.findViewById(R.id.editExcursionButton);
                button.setVisibility(View.INVISIBLE);
            }
            else {
                ImageButton button = (ImageButton) rootView.findViewById(R.id.editExcursionButton);
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
        if (excursionsCursor == null) {
            Log.d(TAG, "onClick - excursionsCursor is null");
            return;
        }

        int count = excursionsCursor.getCount();
        switch (v.getId()) {
            case R.id.nextExcursionButton:
                if (currentExcursionPosition < count - 1) {
                    currentExcursionPosition++;
                }
                else if (currentExcursionPosition == count - 1) {
                    currentExcursionPosition = 0;
                }
                setUserVisibleHint(true);
                break;

            case R.id.prevExcursionButton:
                if (currentExcursionPosition > 0) {
                    currentExcursionPosition--;
                }
                else if (currentExcursionPosition == 0) {
                    currentExcursionPosition = count - 1;
                }
                setUserVisibleHint(true);
                break;

            case R.id.editExcursionButton:
                Intent i = new Intent(getActivity(), ExcursionEdit.class);
                i.putExtra("eid", excursionID);
                startActivity(i);
                break;
            case R.id.navigateToExcursionButton:
                setQuickScroller();

                GoogleMapFragment.cameraLocation = startLocation;
                GoogleMapFragment.zoomLevel = 19;
                GoogleMapFragment.tiltLevel = 0;
                pager.setCurrentItem(TripViewPager.GOOGLE_MAP, true);
                break;
        }

    }

}
