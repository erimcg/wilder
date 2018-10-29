package net.n0code.wilder.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.n0code.wilder.R;
import net.n0code.wilder.db.Trip_T;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.db.Users_T;
import net.n0code.wilder.ui.lib.RecyclerViewCursorAdapter;
import net.n0code.wilder.ui.lib.RecyclerViewCursorViewHolder;
import net.n0code.wilder.obj.Trip;
import net.n0code.wilder.ui.tripViewer.TripViewPager;

import java.text.SimpleDateFormat;

public class TripListAdapter
        extends RecyclerViewCursorAdapter<TripListAdapter.TripViewHolder>{

    /**
     * Constructor.
     * @param context The Context the Adapter is displayed in.
     */
    public TripListAdapter(Context context, Cursor cursor) {
        super(context);

        setupCursorAdapter(cursor, 0, R.layout.trip_list_element, false);
    }

    /**
     * Returns the ViewHolder to use for this excursionListAdapter.
     */
    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TripViewHolder(mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent));
    }

    /**
     * Moves the Cursor of the CursorAdapter to the appropriate position and binds the view for
     * that item.
     */
    @Override
    public void onBindViewHolder(TripViewHolder holder, int position) {
        // Move excursionsCursor to this position
        mCursorAdapter.getCursor().moveToPosition(position);

        // Set the ViewHolder
        setViewHolder(holder);

        // Bind this view
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());
    }

    /**
     * ViewHolder used to display a movie name.
     */
    public class TripViewHolder extends RecyclerViewCursorViewHolder implements View.OnClickListener {
        public final String TAG = "***TRIP-VIEW_HOLDER***";
        public final TextView tripTitle;
        public final TextView tripExplorer;
        public final TextView tripCreationDate;

        public TripViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            tripTitle = (TextView) view.findViewById(R.id.tripTitle);
            tripExplorer = (TextView) view.findViewById(R.id.tripExplorer);
            tripCreationDate = (TextView) view.findViewById(R.id.tripCreationDate);
        }

        @Override
        public void bindCursor(Cursor cursor) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(Trip_T.TITLE));
            tripTitle.setText(title);

            long id = cursor.getLong(cursor.getColumnIndexOrThrow(Trip_T.TRIP_ID));
            tripTitle.setTag(id);  // hides the trip_id in the title view

            String username = cursor.getString(cursor.getColumnIndexOrThrow(Users_T.USERNAME));
            tripExplorer.setText("Created by " + username);

            long timeCreated = cursor.getLong(cursor.getColumnIndexOrThrow(Trip_T.TIME_CREATED));
            String time = new SimpleDateFormat("'On' MMM dd, yyyy 'at' hh:mm a z").format(timeCreated);
            tripCreationDate.setText(time);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "ON CLICK!");
            TextView tv = (TextView) v.findViewById(R.id.tripTitle);
            long tid = (long) tv.getTag();

            Log.d(TAG, "Opening trip: " + tid);

            Trip trip = LocalDB.getTrip(tid);

            if (trip == null) {
                Log.d(TAG, "trip is null");
            }

            Intent i = new Intent(v.getContext(), TripViewPager.class);
            i.putExtra("trip", trip);
            v.getContext().startActivity(i);
        }
    }
}