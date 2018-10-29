package net.n0code.wilder.ui.tripViewer;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.n0code.wilder.R;
import net.n0code.wilder.db.Excursions_T;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.db.Trip_T;
import net.n0code.wilder.obj.Trip;
import net.n0code.wilder.ui.TripEdit;

import java.text.SimpleDateFormat;

import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentTrip;

public class TripInfoFragment extends Fragment
{
    private String TAG = "***TRIP-INFO-VIEW***";

    public TripInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.trip_info, container, false);

        ImageButton button = (ImageButton) rootView.findViewById(R.id.tripEditButton);
        if (currentTrip.getExplorerID() != TripViewPager.currentUser.getUserID()) {
            button.setVisibility(View.GONE);
        }
        else {
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Intent i = new Intent(getActivity(), TripEdit.class);
                    i.putExtra("trip", currentTrip);
                    startActivity(i);
                }
            });
        }

        return rootView;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Log.d(TAG, "visible: " + isVisibleToUser);

        View rootView = getView();
        if (isVisibleToUser && rootView != null) {

            Trip trip = currentTrip;

            if (trip == null)
                return;

            TextView v = (TextView) rootView.findViewById(R.id.tripTitle);
            v.setText(trip.getTitle());

            v = (TextView) rootView.findViewById(R.id.tripDescription);
            v.setText(trip.getDescription());

            v = (TextView) rootView.findViewById(R.id.tripShareMode);
            v.setText(trip.getShareMode());

            v = (TextView) rootView.findViewById(R.id.tripTravelMode);
            v.setText(trip.getTravelMode());

            v = (TextView) rootView.findViewById(R.id.tripCloneCount);
            v.setText(String.valueOf(trip.getCloneCount()));

            v = (TextView) rootView.findViewById(R.id.tripExplorer);
            v.setText(LocalDB.getUsername(trip.getExplorerID()));

            v = (TextView) rootView.findViewById(R.id.tripLastModifiedDate);
            long timeLastMod = trip.getTimeLastModified();
            String time = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a z").format(timeLastMod);
            v.setText(time);

            v = (TextView) rootView.findViewById(R.id.tripOriginalAuthor);
            Trip origTrip = LocalDB.getTrip(trip.getOriginalTripID());

            v.setText(LocalDB.getUsername(origTrip.getExplorerID()));

            v = (TextView) rootView.findViewById(R.id.tripCreationDate);
            long timeCreated = trip.getTimeCreated();
            time = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a z").format(timeCreated);
            v.setText(time);

            v = (TextView) rootView.findViewById(R.id.tripExcursions);
            v.setText("");
            Cursor c = LocalDB.getExcursionInfo(currentTrip.getTripID());

            if (c != null) {
                int count = c.getCount();

                for (int i = 0; i < count; i++) {
                    String excursionName = c.getString(c.getColumnIndexOrThrow(Excursions_T.TITLE));
                    if (excursionName != null) {
                        v.append(excursionName);
                        v.append(System.getProperty("line.separator"));
                    }
                    c.moveToNext();
                }

                Log.d(TAG, v.getText().toString());
            }
        }
    }

}
