package net.n0code.wilder.ui.tripViewer;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.n0code.wilder.R;
import net.n0code.wilder.db.Excursions_T;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.obj.Excursion;
import net.n0code.wilder.ui.lib.RecyclerViewCursorAdapter;
import net.n0code.wilder.ui.lib.RecyclerViewCursorViewHolder;

import static net.n0code.wilder.ui.tripViewer.TripViewPager.currentExcursion;
import static net.n0code.wilder.ui.tripViewer.ExcursionListFragment.currentExcursionPosition;
import static net.n0code.wilder.ui.tripViewer.ExcursionListFragment.excursionsCursor;

public class ExcursionListAdapter
        extends RecyclerViewCursorAdapter<ExcursionListAdapter.ExcursionViewHolder>{

    Context context;

    public ExcursionListAdapter(Context context, Cursor cursor) {
        super(context);
        this.context = context;

        setupCursorAdapter(cursor, 0, R.layout.excursion_list_element, false);
    }

    @Override
    public ExcursionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ExcursionViewHolder(mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent));
    }

    @Override
    public void onBindViewHolder(ExcursionViewHolder holder, int position) {

        mCursorAdapter.getCursor().moveToPosition(position);
        setViewHolder(holder);
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());
    }

    /**
     * ViewHolder used to display an excursion summary.
     */
    public class ExcursionViewHolder extends RecyclerViewCursorViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        public final String TAG = "***EXC-VIEW_HOLDER***";
        public final TextView line1;
        public final TextView line2;
        public final TextView line3;
        public final TextView line4;

        private int position;
        private long eid;

        public ExcursionViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            line1 = (TextView) view.findViewById(R.id.excursionListLine1);
            line2 = (TextView) view.findViewById(R.id.excursionListLine2);
            line3 = (TextView) view.findViewById(R.id.excursionListLine3);
            line4 = (TextView) view.findViewById(R.id.excursionListLine4);
        }

        @Override
        public void bindCursor(Cursor cursor)
        {
            position = cursor.getPosition();

            String excursionName = cursor.getString(cursor.getColumnIndex(Excursions_T.TITLE));
            line1.setText(excursionName);
            eid = cursor.getLong(cursor.getColumnIndexOrThrow(Excursions_T.EXCURSION_ID));
            line1.setTag(eid);  // hides the excursion_id in the title view

            double distance = cursor.getDouble(cursor.getColumnIndex(Excursions_T.DISTANCE)) * 0.00062137;
            String d = String.format(context.getString(R.string.excursionDistance) + ": %.1f miles", distance);
            line2.setText(d);

            double minAlt = excursionsCursor.getDouble(cursor.getColumnIndexOrThrow(Excursions_T.MIN_ALTITUDE));
            minAlt *= 3.2808399;
            double maxAlt = excursionsCursor.getDouble(cursor.getColumnIndexOrThrow(Excursions_T.MAX_ALTITUDE));
            maxAlt *= 3.2808399;
            double gain = maxAlt - minAlt;
            String g = String.format(context.getString(R.string.excursionElevationChange) + ": %.0f feet", gain);
            line3.setText(g);

            long timeCreated = excursionsCursor.getLong(excursionsCursor.getColumnIndexOrThrow(Excursions_T.TIME_CREATED));
            long timeEnded = excursionsCursor.getLong(excursionsCursor.getColumnIndexOrThrow(Excursions_T.TIME_ENDED));
            long timeSpan = timeEnded - timeCreated;
            timeSpan = (timeSpan > 0) ? timeSpan / 60000 : 0;
            String duration = String.format(context.getString(R.string.excursionDuration) + ": %d min", timeSpan);
            line4.setText(duration);

            Log.d(TAG, "excursion: " + excursionName);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick...");

            currentExcursionPosition = position;
            TripViewPager.pager.setCurrentItem(TripViewPager.EXCURSION_INFO);
        }

        @Override
        public boolean onLongClick(View v) {
            currentExcursionPosition = position;

            Cursor cursor = LocalDB.getExcursion(eid);

            if (cursor == null) {
                Log.d(TAG, "getExcursion cursor is null");
                return false;
            }

            long tid = cursor.getLong(cursor.getColumnIndexOrThrow(Excursions_T.TRIP_ID));
            long expid = cursor.getLong(cursor.getColumnIndexOrThrow(Excursions_T.EXPLORER_ID));
            long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(Excursions_T.TIME_CREATED));
            long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(Excursions_T.TIME_ENDED));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(Excursions_T.TITLE));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(Excursions_T.DESCRIPTION));
            double dist = cursor.getDouble(cursor.getColumnIndexOrThrow(Excursions_T.DISTANCE));
            double minAlt = cursor.getDouble(cursor.getColumnIndexOrThrow(Excursions_T.MIN_ALTITUDE));
            double maxAlt = cursor.getDouble(cursor.getColumnIndexOrThrow(Excursions_T.MAX_ALTITUDE));

            currentExcursion = new Excursion(tid, eid, expid, startTime, endTime, title, desc, dist, minAlt, maxAlt);

            GoogleMapFragment.toggleLocationTrackingButton();
            TripViewPager.pager.setCurrentItem(TripViewPager.GOOGLE_MAP);
            return true;
        }
    }
}