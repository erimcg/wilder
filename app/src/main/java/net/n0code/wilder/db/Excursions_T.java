package net.n0code.wilder.db;

import android.provider.BaseColumns;

public class Excursions_T implements BaseColumns {

    private Excursions_T() {}  // Makes class non-instantiable

    public static final String TABLE_NAME = "excursions_t";

    /* Columns */
    // The BaseColumn interface provides a field named _ID
    public static final String TRIP_ID = "tid";
    public static final String EXCURSION_ID = "eid";
    public static final String EXPLORER_ID = "exp_id";
    public static final String TIME_CREATED = "time_created";
    public static final String TIME_ENDED = "time_ended";

    public static final String TITLE = "e_name";
    public static final String DESCRIPTION = "desc";
    public static final String DISTANCE = "distance";
    public static final String MIN_ALTITUDE = "min_alt";
    public static final String MAX_ALTITUDE = "max_alt";


    /* SQL Statements */
    public static final String CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER, "
            + EXCURSION_ID + " INTEGER PRIMARY KEY, "
            + TITLE + " TEXT, "
            + DESCRIPTION + " TEXT, "
            + EXPLORER_ID + " INTEGER NOT NULL, "
            + DISTANCE + " REAL DEFAULT 0, "
            + MIN_ALTITUDE + " REAL DEFAULT 0, "
            + MAX_ALTITUDE + " REAL DEFAULT 0, "
            + TIME_CREATED + " INTEGER NOT NULL, "
            + TIME_ENDED + " INTEGER DEFAULT 0, "
            + TRIP_ID + " INTEGER NOT NULL, "
            + "FOREIGN KEY (" + TRIP_ID + ") REFERENCES " + Trip_T.TABLE_NAME + "(" + Trip_T.TRIP_ID + ") "
             + "ON DELETE CASCADE)";

    public static final String DELETE_TABLE =
            "DELETE TABLE IF EXISTS " + TABLE_NAME;


}
