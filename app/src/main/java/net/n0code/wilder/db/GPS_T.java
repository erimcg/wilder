package net.n0code.wilder.db;

import android.provider.BaseColumns;

public final class GPS_T implements BaseColumns
{
    private GPS_T() {}  // Makes class non-instantiable

    public static final String TABLE_NAME = "gps_t";

    /* Columns */
    // The BaseColumn interface provides a field named _ID
    public static final String EXCURSION_ID = "eid";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";
    public static final String ALTITUDE = "alt";

    /* SQL Statements */
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + _ID + " INTEGER, "
                    + EXCURSION_ID + " INTEGER NOT NULL, "
                    + LATITUDE + " REAL NOT NULL, "
                    + LONGITUDE + " REAL NOT NULL, "
                    + ALTITUDE + " REAL NOT NULL, "
                    + "FOREIGN KEY (" + EXCURSION_ID + ") REFERENCES " + Excursions_T.TABLE_NAME + "(" + Excursions_T.EXCURSION_ID + ") "
                    + "ON DELETE CASCADE, "
                    + "PRIMARY KEY (" + EXCURSION_ID + ", " + LATITUDE + ", " + LONGITUDE + "))";

    public static final String DELETE_TABLE =
            "DELETE TABLE IF EXISTS " + TABLE_NAME;

}
