package net.n0code.wilder.db;

import android.provider.BaseColumns;

public final class Users_T implements BaseColumns
{
    private Users_T() {}  // Makes class non-instantiable

    public static final String TABLE_NAME = "users_t";

    /* Columns */
    // The BaseColumn interface provides a field named _ID
    public static final String USER_ID = "uid";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String EMAIL = "email";

    /* SQL Statements */
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + _ID + " INTEGER, "
            + USER_ID + " INTEGER PRIMARY KEY, "
            + USERNAME + " TEXT NOT NULL, "
            + PASSWORD + " TEXT NOT NULL, "
            + EMAIL + " TEXT NOT NULL UNIQUE)";

    public static final String DELETE_TABLE =
            "DELETE TABLE IF EXISTS " + TABLE_NAME;

}


