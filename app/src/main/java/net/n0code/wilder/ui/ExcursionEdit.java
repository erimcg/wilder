package net.n0code.wilder.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import net.n0code.wilder.R;
import net.n0code.wilder.db.Excursions_T;
import net.n0code.wilder.db.LocalDB;
import net.n0code.wilder.obj.Excursion;
import net.n0code.wilder.ui.tripViewer.TripViewPager;

public class ExcursionEdit extends AppCompatActivity {

    private String TAG = "****E-EDIT****";
    private long eid = -1;

    double distance;
    double minAlt;
    double maxAlt;
    long endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.excursion_edit);

        EditText titleET = (EditText) findViewById(R.id.excursionEditTitle);
        EditText descET = (EditText) findViewById(R.id.excursionEditDescription);

        if (getIntent().hasExtra("eid")) {
            eid = getIntent().getLongExtra("eid", -1);

            Cursor cursor = LocalDB.getExcursion(eid);

            if (cursor == null) {
                Log.d(TAG, "excursionsCursor is null");
                finish();
                return;
            }

            String title = cursor.getString(cursor.getColumnIndexOrThrow(Excursions_T.TITLE));
            titleET.setText(title);

            String desc = cursor.getString(cursor.getColumnIndexOrThrow(Excursions_T.DESCRIPTION));
            descET.setText(desc);

            distance = cursor.getDouble(cursor.getColumnIndexOrThrow(Excursions_T.DISTANCE));
            minAlt = cursor.getDouble(cursor.getColumnIndexOrThrow(Excursions_T.MIN_ALTITUDE));
            maxAlt = cursor.getDouble(cursor.getColumnIndexOrThrow(Excursions_T.MAX_ALTITUDE));
            endTime = cursor.getLong(cursor.getColumnIndexOrThrow(Excursions_T.TIME_ENDED));
        }
    }

    public void saveChangesButtonHandler(View v) {

        Log.d(TAG, "saving excursion: " + eid);

        String title = ((EditText) findViewById(R.id.excursionEditTitle)).getText().toString();
        String desc = ((EditText) findViewById(R.id.excursionEditDescription)).getText().toString();

        LocalDB.updateExcursion(new Excursion(eid, title, desc, distance, minAlt, maxAlt, endTime));

        TripViewPager.pager.setCurrentItem(TripViewPager.LIST_EXCURSIONS);

        finish();
    }

    public void cancelChangesButtonHandler(View v) {
        finish();
    }

}
