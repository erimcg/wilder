package net.n0code.wilder.db;

import android.provider.BaseColumns;

public class Trip_T implements BaseColumns {

    private Trip_T() {}  // Makes class non-instantiable

    public static final String TABLE_NAME = "trips_t";

    /* Column names */
    // The BaseColumn interface provides a field named _ID
    public static final String TRIP_ID = "tid";
    public static final String EXPLORER_ID = "uid";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String TRAVEL_MODE = "travel_mode";
    public static final String SHARE_MODE = "share_mode";
    public static final String CLONE_COUNT = "clone_count";
    public static final String TIME_CREATED = "time_created";
    public static final String TIME_LAST_MODIFIED = "time_last_modified";
    public static final String ORIGINAL_TRIP_ID = "original_tid";

    /* SQL Statements */
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER, "
            + TRIP_ID + " INTEGER PRIMARY KEY, "
            + EXPLORER_ID + " INTEGER NOT NULL, "
            + TITLE + " TEXT NOT NULL, "
            + DESCRIPTION + " TEXT, "
            + TRAVEL_MODE + " TEXT NOT NULL, "
            + SHARE_MODE + " TEXT NOT NULL, "
            + CLONE_COUNT + " INTEGER NOT NULL, "
            + TIME_CREATED + " INTEGER NOT NULL, "
            + TIME_LAST_MODIFIED + " INTEGER NOT NULL, "
            + ORIGINAL_TRIP_ID + " INTEGER, "
            + "FOREIGN KEY (" + EXPLORER_ID + ") REFERENCES " + Users_T.TABLE_NAME + "(" + Users_T.USER_ID + ") "
            + "ON DELETE CASCADE)";

    public static final String DELETE_TABLE =
            "DELETE TABLE IF EXISTS " + TABLE_NAME;


}
