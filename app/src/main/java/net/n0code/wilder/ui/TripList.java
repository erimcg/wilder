package net.n0code.wilder.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.n0code.wilder.R;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.obj.Trip;
import net.n0code.wilder.obj.User;
import net.n0code.wilder.ui.settings.Settings;

public class TripList extends AppCompatActivity {

    public static String TAG = "****TRIP-LIST****";

    public static TripListAdapter adapter = null;
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_list);

        LocalDB.openDB(this);
        Cursor cursor = LocalDB.getTrips();

        if (cursor == null || cursor.getCount() == 0) {
            cursor = LocalDB.getTrips();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TripListAdapter(this, cursor);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(false);

        setUpItemTouchHelper();
    }

    public void createTripButtonHandler(View v) {
        Intent i = new Intent(this, TripEdit.class);
        startActivity(i);
    }

    public void settingsButtonHandler(View v) {
        Intent i = new Intent(this, Settings.class);
        startActivity(i);
    }

    public void onDestroy() {
        super.onDestroy();
        //LocationServer.close();

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
                TextView title = (TextView) v.findViewById(R.id.tripTitle);
                final long tid = (long) title.getTag();

                if (direction == ItemTouchHelper.LEFT) {    //if swipe left

                    AlertDialog.Builder builder = new AlertDialog.Builder(TripList.this);
                    builder.setMessage("Are you sure to delete?");    //set message

                    builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LocalDB.deleteTrip(tid);
                            Cursor cursor = LocalDB.getTrips();
                            adapter.changeCursor(cursor);
                            return;

                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in excursionListAdapter has been removed at a particular position.
                            adapter.notifyItemRangeChanged(position, adapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in excursionListAdapter has been changed from position(removed element index to end of list), please update it.
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

