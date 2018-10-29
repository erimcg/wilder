package net.n0code.wilder.db;

import android.provider.BaseColumns;

public class POI_T implements BaseColumns {

    private POI_T() {}  // Makes class non-instantiable

    public static final String TABLE_NAME = "poi_t";

    /* Columns */
    // The BaseColumn interface provides a field named _ID
    public static final String POI_ID = "poi_id";
    public static final String EXCURSION_ID = "eid";
    public static final String TYPE = "type";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "desc";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";
    public static final String EXPLORER_ID = "explorer_id";
    public static final String DATE_CREATED = "date_created";
    public static final String TIME_CREATED = "time_created";

    /* SQL Statements */
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + _ID + " INTEGER, "
                    + POI_ID + " INTEGER PRIMARY KEY, "
                    + EXCURSION_ID + " INTEGER NOT NULL, "
                    + TYPE + " TEXT NOT NULL, "
                    + TITLE + " TEXT NOT NULL, "
                    + DESCRIPTION + " TEXT, "
                    + LATITUDE + " REAL NOT NULL, "
                    + LONGITUDE + " REAL NOT NULL, "
                    + EXPLORER_ID + " INTEGER NOT NULL, "
                    + DATE_CREATED + " TEXT NOT NULL, "
                    + TIME_CREATED + " TEXT NOT NULL, "
                    + "FOREIGN KEY (" + EXCURSION_ID + ") REFERENCES " + Excursions_T.TABLE_NAME + "(" + Excursions_T.EXCURSION_ID + ") "
                    + "ON DELETE CASCADE)";

    public static final String DELETE_TABLE =
            "DELETE TABLE IF EXISTS " + TABLE_NAME;


}