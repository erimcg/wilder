package net.n0code.wilder.ui.tripViewer;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.n0code.wilder.R;
import net.n0code.wilder.db.LocalDB;

import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentTrip;
import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentExcursion;

public class ExcursionListFragment extends Fragment {

    private String TAG = "***Excursion-List***";
    private RecyclerView mRecyclerView;

    public static ExcursionListAdapter excursionListAdapter;
    public static Cursor excursionsCursor = null;
    public static int currentExcursionPosition = -1;

    public ExcursionListFragment() { }  // Required empty public constructor

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.excursion_list, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        excursionListAdapter = new ExcursionListAdapter(getActivity(), excursionsCursor);

        mRecyclerView.setAdapter(excursionListAdapter);
        mRecyclerView.setHasFixedSize(false);

        setUpItemTouchHelper();

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            currentExcursionPosition = -1;
            excursionsCursor = LocalDB.getExcursionInfo(currentTrip.getTripID());
            excursionListAdapter.changeCursor(excursionsCursor);
        }
    }

    private void setUpItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                final int position = viewHolder.getAdapterPosition();
                View v = mRecyclerView.getLayoutManager().findViewByPosition(position);
                TextView line1 = (TextView) v.findViewById(R.id.excursionListLine1);
                final long eid = (long) line1.getTag();

                /* You can not delete an excursion that has tracking on */
                if (currentExcursion != null && currentExcursion.getExcursionID() == eid) {
                    excursionListAdapter.notifyItemRemoved(position + 1);
                    excursionListAdapter.notifyItemRangeChanged(position, excursionListAdapter.getItemCount());
                    return;
                }

                if (direction == ItemTouchHelper.LEFT) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Are you sure to delete?");

                    builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LocalDB.deleteExcursion(eid);
                            excursionsCursor = LocalDB.getExcursionInfo(currentTrip.getTripID());;
                            excursionListAdapter.changeCursor(excursionsCursor);
                            return;

                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            excursionListAdapter.notifyItemRemoved(position + 1);
                            excursionListAdapter.notifyItemRangeChanged(position, excursionListAdapter.getItemCount());
                            return;

                        }
                    }).show();  //show alert dialog
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView); //set swipe to recylcerview

    }

}

