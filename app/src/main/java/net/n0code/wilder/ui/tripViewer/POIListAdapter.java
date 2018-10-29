package net.n0code.wilder.ui.tripViewer;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.n0code.wilder.R;
import net.n0code.wilder.db.Excursions_T;
import net.n0code.wilder.db.POI_T;
import net.n0code.wilder.db.Users_T;
import net.n0code.wilder.ui.lib.RecyclerViewCursorAdapter;
import net.n0code.wilder.ui.lib.RecyclerViewCursorViewHolder;

public class POIListAdapter
        extends RecyclerViewCursorAdapter<POIListAdapter.ObservationViewHolder>{

    Context context;

    public POIListAdapter(Context context, Cursor cursor) {
        super(context);
        this.context = context;

        setupCursorAdapter(cursor, 0, R.layout.poi_list_element, false);
    }

    @Override
    public ObservationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ObservationViewHolder(mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent));
    }


    @Override
    public void onBindViewHolder(ObservationViewHolder holder, int position) {

        mCursorAdapter.getCursor().moveToPosition(position);
        setViewHolder(holder);
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());
    }

    /**
     * ViewHolder used to display a movie name.
     */
    public class ObservationViewHolder extends RecyclerViewCursorViewHolder implements View.OnClickListener
    {
        public final String TAG = "***POI-List-Adapter***";
        public final TextView line1;
        public final TextView line2;
        public final ImageButton typeButton;

        private int position;

        public ObservationViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            line1 = (TextView) view.findViewById(R.id.pointOfInterestListLine1);
            line2 = (TextView) view.findViewById(R.id.pointOfInterestListLine2);
            typeButton = (ImageButton) view.findViewById(R.id.pointOfInterestType);
        }

        @Override
        public void bindCursor(Cursor cursor) {

            position = cursor.getPosition();

            String title = cursor.getString(cursor.getColumnIndexOrThrow(POI_T.TITLE));
            line1.setText(title);

            String description = cursor.getString(cursor.getColumnIndexOrThrow(POI_T.DESCRIPTION));
            line2.setText(description);

            String type = cursor.getString(cursor.getColumnIndex(POI_T.TYPE));
            String uri = "@drawable/" + type.toLowerCase() + "_24dp";
            int id = context.getResources().getIdentifier(uri, null, context.getPackageName());
            typeButton.setImageDrawable(context.getResources().getDrawable(id));

            Log.d(TAG, "displaying poi: " + title);

        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick...position: " + position);

            POIInfoFragment.currentPOIPosition = position;
            TripViewPager.pager.setCurrentItem(TripViewPager.POI_INFO);
        }
    }
}