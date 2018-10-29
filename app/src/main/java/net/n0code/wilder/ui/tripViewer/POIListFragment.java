package net.n0code.wilder.ui.tripViewer;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.n0code.wilder.R;
import net.n0code.wilder.db.Excursions_T;
import net.n0code.wilder.db.LocalDB;

import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentTrip;
import static net.n0code.wilder.ui.tripViewer.ExcursionListFragment.excursionsCursor;
import static net.n0code.wilder.ui.tripViewer.ExcursionListFragment.currentExcursionPosition;

public class POIListFragment extends Fragment {

    private String TAG = "***POI-List***";
    private RecyclerView mRecyclerView;

    public static POIListAdapter poiListAdapter;
    public static Cursor poiCursor = null;


    public POIListFragment() { }  // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.poi_list, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        poiListAdapter = new POIListAdapter(getActivity(), poiCursor);
        mRecyclerView.setAdapter(poiListAdapter);
        mRecyclerView.setHasFixedSize(false);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d(TAG, "is visible");
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

            POIListFragment.poiListAdapter.changeCursor(poiCursor);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setUserVisibleHint(true);
    }

}
