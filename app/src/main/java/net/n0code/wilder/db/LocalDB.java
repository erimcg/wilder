package net.n0code.wilder.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import net.n0code.wilder.obj.Excursion;
import net.n0code.wilder.obj.POI;
import net.n0code.wilder.obj.Trip;
import net.n0code.wilder.obj.User;

public class LocalDB {

    // ERROR LOGGING TAG
    private static final String TAG = "****LocalDB****";

    // DB INFO
    private static final String DATABASE_NAME = "Wilder_DB";
    private static final int DATABASE_VERSION = 1;

    // DB OBJECTS
    private static DatabaseHelper dBHelper = null;
    private static SQLiteDatabase db = null;

    // RETURN CODES FOR USER METHODS
    public static final int FAILURE = -1;
    public static final int SUCCESS = 0;
    public static final int EMAIL_ALREADY_EXISTS = 1;

    /************************************************************
     * DatabaseHelper
     ************************************************************/

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {
            _db.execSQL(Users_T.CREATE_TABLE);
            _db.execSQL(Trip_T.CREATE_TABLE);
            _db.execSQL(Excursions_T.CREATE_TABLE);
            _db.execSQL(GPS_T.CREATE_TABLE);
            _db.execSQL(POI_T.CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL(Users_T.DELETE_TABLE);
            _db.execSQL(Trip_T.DELETE_TABLE);
            _db.execSQL(Excursions_T.DELETE_TABLE);
            _db.execSQL(GPS_T.DELETE_TABLE);
            _db.execSQL(POI_T.DELETE_TABLE);


            // Recreate new database:
            onCreate(_db);
        }
    }

    /************************************************************
     * Open database
     ************************************************************/

    public static void openDB(Context context) {
        if (dBHelper == null) {
            dBHelper = new DatabaseHelper(context);
        }
        if (db == null) {
            db = dBHelper.getWritableDatabase();
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    /************************************************************
     * Public methods for Users_T
     ************************************************************/

    public static int addUser(User user) {
        if (emailExists(user.getEmail())) {
            return EMAIL_ALREADY_EXISTS;
        }

        ContentValues values = new ContentValues();

        values.put(Users_T.USERNAME, user.getUserName());
        values.put(Users_T.EMAIL, user.getEmail());
        values.put(Users_T.PASSWORD, user.getPassword());

        long rowID = db.insert(Users_T.TABLE_NAME, null, values);

        if (rowID == -1) {
            Log.d(TAG, "Error inserting user - row: " + rowID);
            return FAILURE;
        }

        // While we're at it, we'll update the User's id
        user.setUserID(getUserID(rowID));

        return SUCCESS;
    }

    public static long getUserID(long rowID) {
        String query = "SELECT "
                + Users_T.USER_ID + " "
                + "FROM " + Users_T.TABLE_NAME + " "
                + "WHERE _ROWID_ = '" + rowID + "' ";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            Log.d(TAG, "failed to get user id for new user");
            return -1;
        } else if (c.getCount() == 0) {
            c.close();
            Log.d(TAG, "failed (2) to get user id for new user");
            return -1;
        }

        c.moveToFirst();
        long uid = c.getLong(c.getColumnIndexOrThrow(Users_T.USER_ID));
        c.close();

        return uid;
    }

    public static int deleteUser(String email) {
        int result = db.delete(Users_T.TABLE_NAME, Users_T.EMAIL + " = '" + email + "'", null);

        if (result == 0)
            Log.d(TAG, "no rows deleted from users table");

        return result;
    }

    public static User getUser(String email) {
        String[] columns = {
                Users_T.USER_ID,
                Users_T.USERNAME,
                Users_T.PASSWORD,
                Users_T.EMAIL
        };

        String whereClause = Users_T.EMAIL + " = ?";
        String[] whereArgs = {email};

        Cursor c = db.query(
                Users_T.TABLE_NAME,
                columns,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );


        if (c == null) {
            return null;
        }
        if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        long id = c.getLong(c.getColumnIndex(Users_T.USER_ID));
        String username = c.getString(c.getColumnIndex(Users_T.USERNAME));
        String password = c.getString(c.getColumnIndex(Users_T.PASSWORD));
        c.close();

        return new User(id, username, password, email);
    }

    public static String getUserName(long uid) {
        String[] columns = {
                Users_T.USERNAME,
        };

        String whereClause = Users_T.USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(uid)};

        Cursor c = db.query(
                Users_T.TABLE_NAME,
                columns,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );


        if (c == null) {
            return null;
        }
        if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        String username = c.getString(c.getColumnIndex(Users_T.USERNAME));
        c.close();

        return username;
    }

    public static boolean emailExists(String email) {
        String[] columns = {
                Users_T.EMAIL,
        };

        String whereClause = Users_T.EMAIL + " = ?";
        String[] whereArgs = {email};

        Cursor c = db.query(
                Users_T.TABLE_NAME,
                columns,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        if (c == null) {
            return false;
        }

        boolean exists = c.getCount() > 0;
        c.close();

        return exists;
    }

    public static String getUsername(long uid) {

        String query = "SELECT "
                + Users_T.USERNAME + " "
                + "FROM " + Users_T.TABLE_NAME + " "
                + "WHERE " + Users_T.USER_ID + " = '" + uid + "' ";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        String username = c.getString(c.getColumnIndex(Users_T.USERNAME));
        c.close();

        return username;
    }

    public static boolean usersExist() {
        String[] columns = {
                Users_T.USERNAME
        };

        Cursor c = db.query(
                Users_T.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null
        );

        int count = c.getCount();
        c.close();

        if (count > 0)
            return true;
        else
            return false;
    }

    /************************************************************
     * Public methods for Trip_T
     ************************************************************/

    public static int addTrip(Trip trip) {
        ContentValues values = new ContentValues();

        values.put(Trip_T.EXPLORER_ID, trip.getExplorerID());
        values.put(Trip_T.TITLE, trip.getTitle());
        values.put(Trip_T.DESCRIPTION, trip.getDescription());
        values.put(Trip_T.TRAVEL_MODE, trip.getTravelMode());
        values.put(Trip_T.SHARE_MODE, trip.getShareMode());
        values.put(Trip_T.CLONE_COUNT, trip.getCloneCount());
        values.put(Trip_T.TIME_LAST_MODIFIED, trip.getTimeLastModified());
        values.put(Trip_T.TIME_CREATED, trip.getTimeCreated());
        values.put(Trip_T.ORIGINAL_TRIP_ID, trip.getOriginalTripID());

        long rowID = db.insert(Trip_T.TABLE_NAME, null, values);
        Log.d(TAG, "Adding trip.  row: " + rowID);


        if (rowID == -1) {
            Log.d(TAG, "Error inserting trip");
            return FAILURE;
        }

        //Set tripID and originalTripID
        long tid = getTripID(rowID);

        trip.setTripID(tid);
        Log.d(TAG, "added trip with tid: " + tid);

        if (trip.getOriginalTripID() == -1) {
            Log.d(TAG, "setting original trip id to: " + tid);
            trip.setOriginalTripID(tid);
        }

        return updateTrip(trip);
    }

    public static long getTripID(long rowID) {
        String query = "SELECT "
                + Trip_T.TRIP_ID + " "
                + "FROM " + Trip_T.TABLE_NAME + " "
                + "WHERE _ROWID_ = '" + rowID + "' ";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            Log.d(TAG, "failed to get trip id for new trip");
            return -1;
        } else if (c.getCount() == 0) {
            c.close();
            Log.d(TAG, "failed (2) to get trip id for new trip");
            return -1;
        }

        c.moveToFirst();
        long tid = c.getLong(c.getColumnIndexOrThrow(Trip_T.TRIP_ID));
        c.close();

        return tid;
    }

    public static int deleteTrip(long tid) {

        String[] args = {String.valueOf(tid)};
        int result = db.delete(Trip_T.TABLE_NAME, Trip_T.TRIP_ID + " = ?", args);

        if (result == 0) {
            Log.d(TAG, "no rows deleted from trip table");
        }

        Log.d(TAG, "delete trip (" + tid + ") successful: " + result);
        return result;
    }

    public static Trip getTrip(long tid) {
        String[] columns = {
                Trip_T.TRIP_ID,
                Trip_T.EXPLORER_ID,
                Trip_T.TITLE,
                Trip_T.DESCRIPTION,
                Trip_T.TRAVEL_MODE,
                Trip_T.SHARE_MODE,
                Trip_T.CLONE_COUNT,
                Trip_T.TIME_LAST_MODIFIED,
                Trip_T.TIME_CREATED,
                Trip_T.ORIGINAL_TRIP_ID
        };

        Cursor c = db.query(
                Trip_T.TABLE_NAME,
                columns,
                Trip_T.TRIP_ID + "='" + tid + "'",
                null,
                null,
                null,
                null
        );

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        Trip e = new Trip(
                c.getString(c.getColumnIndexOrThrow(Trip_T.TITLE)),
                c.getString(c.getColumnIndexOrThrow(Trip_T.DESCRIPTION)),
                c.getString(c.getColumnIndexOrThrow(Trip_T.TRAVEL_MODE)),
                c.getString(c.getColumnIndexOrThrow(Trip_T.SHARE_MODE)),
                c.getInt(c.getColumnIndexOrThrow(Trip_T.CLONE_COUNT)),
                c.getLong(c.getColumnIndexOrThrow(Trip_T.TIME_LAST_MODIFIED)),
                c.getLong(c.getColumnIndexOrThrow(Trip_T.TRIP_ID)),
                c.getLong(c.getColumnIndexOrThrow(Trip_T.EXPLORER_ID)),
                c.getLong(c.getColumnIndexOrThrow(Trip_T.TIME_CREATED)),
                c.getLong(c.getColumnIndexOrThrow(Trip_T.ORIGINAL_TRIP_ID))
        );

        c.close();
        return e;
    }

    public static Cursor getTrips() {
        String query = "SELECT "
                + Trip_T.TABLE_NAME + "." + Trip_T._ID + ", "
                + Trip_T.TABLE_NAME + "." + Trip_T.TRIP_ID + ", "
                + Trip_T.TABLE_NAME + "." + Trip_T.TITLE + ", "
                + Trip_T.TABLE_NAME + "." + Trip_T.TIME_CREATED + ", "
                + Users_T.TABLE_NAME + "." + Users_T.USERNAME + " "
                + "FROM " + Trip_T.TABLE_NAME + " "
                + "INNER JOIN " + Users_T.TABLE_NAME + " "
                + "ON " + Trip_T.TABLE_NAME + "." + Trip_T.EXPLORER_ID + "=" + Users_T.TABLE_NAME + "." + Users_T.USER_ID;

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        return c;
    }

    public static int updateTrip(Trip trip) {
        ContentValues values = new ContentValues();

        values.put(Trip_T.TITLE, trip.getTitle());
        values.put(Trip_T.DESCRIPTION, trip.getDescription());
        values.put(Trip_T.TRAVEL_MODE, trip.getTravelMode());
        values.put(Trip_T.SHARE_MODE, trip.getShareMode());
        values.put(Trip_T.CLONE_COUNT, trip.getCloneCount());
        values.put(Trip_T.TIME_LAST_MODIFIED, trip.getTimeLastModified());
        values.put(Trip_T.ORIGINAL_TRIP_ID, trip.getOriginalTripID());

        int count = db.update(
                Trip_T.TABLE_NAME,
                values,
                Trip_T.TRIP_ID + "='" + trip.getTripID() + "'",
                null);

        if (count == 0) {
            Log.d(TAG, "Error updating trip");
            return FAILURE;
        }

        return SUCCESS;
    }

    /************************************************************
     * Public methods for Excursions_T
     ************************************************************/
    public static int addExcursion(Excursion e) {
        ContentValues values = new ContentValues();

        values.put(Excursions_T.TRIP_ID, e.getTripID());
        values.put(Excursions_T.EXPLORER_ID, e.getExplorerID());
        values.put(Trip_T.TIME_CREATED, e.getStartTime());

        long rowID = db.insert(Excursions_T.TABLE_NAME, null, values);

        if (rowID == -1) {
            Log.d(TAG, "Error adding new excursion");
            return FAILURE;
        }

        Log.d(TAG, "ADDING EXCURSION with eid: " + rowID);
        e.setExcursionID(rowID);

        return SUCCESS;
    }

    public static int deleteExcursion(long eid) {
        int result = db.delete(Excursions_T.TABLE_NAME, Excursions_T.EXCURSION_ID + " = '" + eid + "'", null);

        if (result == 0)
            Log.d(TAG, "no rows deleted from excursions table");

        return result;
    }

    public static Cursor getExcursionIDs(long tid) {
        String query = "SELECT "
                + Excursions_T.EXCURSION_ID + " "
                + "FROM " + Excursions_T.TABLE_NAME + " "
                + "WHERE " + Excursions_T.TRIP_ID + " = '" + tid + "' ";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        return c;
    }

    public static Cursor getExcursionInfo(long tid) {
        String query = "SELECT * "
                + "FROM " + Excursions_T.TABLE_NAME + " "
                + "WHERE " + Excursions_T.TRIP_ID + " = '" + tid + "' "
                + "ORDER BY _ROWID_ ASC";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        return c;
    }

    public static Cursor getExcursion(long eid) {
        String query = "SELECT * "
                + "FROM " + Excursions_T.TABLE_NAME + " "
                + "WHERE " + Excursions_T.EXCURSION_ID + " = '" + eid + "' ";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        return c;
    }

    public static String getExcursionTitle(long eid) {
        String query = "SELECT " + Excursions_T.TITLE + " "
                + "FROM " + Excursions_T.TABLE_NAME + " "
                + "WHERE " + Excursions_T.EXCURSION_ID + " = '" + eid + "' ";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        String excursionName = c.getString(c.getColumnIndexOrThrow(Excursions_T.TITLE));
        c.close();

        return excursionName;
    }

    public static LatLng getExcursionStartingLocation(long eid)
    {
        String query = "SELECT "
                + GPS_T.LATITUDE + ", "
                + GPS_T.LONGITUDE + " "
                + "FROM " + GPS_T.TABLE_NAME + " "
                + "WHERE " + GPS_T.EXCURSION_ID + " = '" + eid + "' "
                + "ORDER BY _ROWID_ ASC";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();

        double lat = c.getDouble(c.getColumnIndexOrThrow(GPS_T.LATITUDE));
        double lng = c.getDouble(c.getColumnIndexOrThrow(GPS_T.LONGITUDE));

        return new LatLng(lat, lng);
    }

    public static int updateExcursion(Excursion e) {

        ContentValues values = new ContentValues();

        values.put(Excursions_T.TITLE, e.getTitle());
        values.put(Excursions_T.DESCRIPTION, e.getDescription());
        values.put(Excursions_T.DISTANCE, e.getDistance());
        values.put(Excursions_T.MIN_ALTITUDE, e.getMinAltitude());
        values.put(Excursions_T.MAX_ALTITUDE, e.getMaxAltitude());
        values.put(Excursions_T.TIME_ENDED, e.getEndTime());

        int count = db.update(
                Excursions_T.TABLE_NAME,
                values,
                Excursions_T.EXCURSION_ID + "='" + e.getExcursionID() + "'",
                null);

        if(count == 0) {
            Log.d(TAG, "Error updating excursion");
            return FAILURE;
        }

        return SUCCESS;
    }

    /************************************************************
     * Public methods for GPS_T
     ************************************************************/
    public static int addLocation(long eid, LatLng location, double alt)
    {
        ContentValues values = new ContentValues();

        values.put(GPS_T.EXCURSION_ID, eid);
        values.put(GPS_T.LATITUDE, location.latitude);
        values.put(GPS_T.LONGITUDE, location.longitude);
        values.put(GPS_T.ALTITUDE, alt);

        long rowID = db.insert(GPS_T.TABLE_NAME, null, values);

        if (rowID == -1) {
            Log.d(TAG, "Error inserting location");
            return FAILURE;
        }

        return SUCCESS;
    }

    public static Cursor getRoute(long eid)
    {
        String query = "SELECT "
                + GPS_T.LATITUDE + ", "
                + GPS_T.LONGITUDE + " "
                + "FROM " + GPS_T.TABLE_NAME + " "
                + "WHERE " + GPS_T.EXCURSION_ID + " = '" + eid + "' "
                + "ORDER BY _ROWID_ ASC";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();
        return c;
    }

    /************************************************************
     * Public methods for POI_T
     ************************************************************/

    public static int addPointOfInterest(POI poi)
    {
        Log.d(TAG, "Adding poi with excursion id: " + poi.getExcursionID());

        ContentValues values = new ContentValues();

        values.put(POI_T.EXCURSION_ID, poi.getExcursionID());
        values.put(POI_T.TYPE, poi.getType());
        values.put(POI_T.TITLE, poi.getTitle());
        values.put(POI_T.DESCRIPTION, poi.getDescription());
        values.put(POI_T.LATITUDE, poi.getLocation().latitude);
        values.put(POI_T.LONGITUDE, poi.getLocation().longitude);
        values.put(POI_T.EXPLORER_ID, poi.getExplorerID());
        values.put(POI_T.DATE_CREATED, poi.getDateCreated());
        values.put(POI_T.TIME_CREATED, poi.getTimeCreated());

        long rowID = db.insert(POI_T.TABLE_NAME, null, values);
        poi.setObservationID(rowID);

        if (rowID == -1) {
            Log.d(TAG, "Error inserting poi");
            return FAILURE;
        }

        return SUCCESS;
    }

    public static POI getPointOfInterest(long poi_id)
    {
        String query = "SELECT * "
                + "FROM " + POI_T.TABLE_NAME + " "
                + "WHERE " + POI_T.POI_ID + " = '" + poi_id + "'";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();

        long eid = c.getLong(c.getColumnIndexOrThrow(POI_T.EXCURSION_ID));
        String type = c.getString(c.getColumnIndexOrThrow(POI_T.TYPE));
        String title = c.getString(c.getColumnIndexOrThrow(POI_T.TITLE));
        String desc = c.getString(c.getColumnIndexOrThrow(POI_T.DESCRIPTION));
        double lat = c.getDouble(c.getColumnIndexOrThrow(POI_T.LATITUDE));
        double lng = c.getDouble(c.getColumnIndexOrThrow(POI_T.LONGITUDE));
        long exp_id = c.getLong(c.getColumnIndexOrThrow(POI_T.EXPLORER_ID));
        String date = c.getString(c.getColumnIndexOrThrow(POI_T.DATE_CREATED));
        String time = c.getString(c.getColumnIndexOrThrow(POI_T.TIME_CREATED));

        POI poi = new POI(type, eid, title, desc, lat, lng, exp_id, date, time);
        poi.setObservationID(poi_id);

        return poi;
    }

    public static Cursor getPointsOfInterestByTID(long tid)
    {
        String query = "SELECT "
                + POI_T.TABLE_NAME + "." + POI_T._ID + ", "
                + POI_T.TABLE_NAME + "." + POI_T.POI_ID + ", "
                + POI_T.TABLE_NAME + "." + POI_T.EXCURSION_ID + ", "
                + POI_T.TABLE_NAME + "." + POI_T.TYPE + ", "
                + POI_T.TABLE_NAME + "." + POI_T.TITLE + ", "
                + POI_T.TABLE_NAME + "." + POI_T.DESCRIPTION + ", "
                + POI_T.TABLE_NAME + "." + POI_T.LONGITUDE + ", "
                + POI_T.TABLE_NAME + "." + POI_T.LATITUDE + ", "
                + POI_T.TABLE_NAME + "." + POI_T.EXPLORER_ID + ", "
                + POI_T.TABLE_NAME + "." + POI_T.DATE_CREATED + ", "
                + POI_T.TABLE_NAME + "." + POI_T.TIME_CREATED + ", "
                + Excursions_T.TABLE_NAME + "." + Excursions_T.TRIP_ID + ", "
                + Excursions_T.TABLE_NAME + "." + Excursions_T.TITLE + ", "
                + Users_T.TABLE_NAME + "." + Users_T.USERNAME + " "
                + "FROM " + POI_T.TABLE_NAME + " "
                + "LEFT JOIN " + Excursions_T.TABLE_NAME + " "
                + "ON " + POI_T.TABLE_NAME + "." + POI_T.EXCURSION_ID + "="
                + Excursions_T.TABLE_NAME + "." + Excursions_T.EXCURSION_ID + " "
                + "LEFT JOIN " + Users_T.TABLE_NAME + " "
                + "ON " + POI_T.TABLE_NAME + "." + POI_T.EXPLORER_ID + "="
                + Users_T.TABLE_NAME + "." + Users_T.USER_ID + " "
                + "WHERE " + Excursions_T.TRIP_ID + " = '" + tid + "'";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }
        Log.d(TAG, "returning " + c.getCount() + " points of interest");
        c.moveToFirst();
        return c;
    }

    public static Cursor getPointsOfInterestByEID(long eid)
    {
        String query = "SELECT "
                + POI_T.TABLE_NAME + "." + POI_T._ID + ", "
                + POI_T.TABLE_NAME + "." + POI_T.POI_ID + ", "
                + POI_T.TABLE_NAME + "." + POI_T.EXCURSION_ID + ", "
                + POI_T.TABLE_NAME + "." + POI_T.TYPE + ", "
                + POI_T.TABLE_NAME + "." + POI_T.TITLE + ", "
                + POI_T.TABLE_NAME + "." + POI_T.DESCRIPTION + ", "
                + POI_T.TABLE_NAME + "." + POI_T.LONGITUDE + ", "
                + POI_T.TABLE_NAME + "." + POI_T.LATITUDE + ", "
                + POI_T.TABLE_NAME + "." + POI_T.EXPLORER_ID + ", "
                + POI_T.TABLE_NAME + "." + POI_T.DATE_CREATED + ", "
                + POI_T.TABLE_NAME + "." + POI_T.TIME_CREATED + ", "
                + Excursions_T.TABLE_NAME + "." + Excursions_T.TRIP_ID + ", "
                + Excursions_T.TABLE_NAME + "." + Excursions_T.TITLE + ", "
                + Users_T.TABLE_NAME + "." + Users_T.USERNAME + " "
                + "FROM " + POI_T.TABLE_NAME + " "
                + "LEFT JOIN " + Excursions_T.TABLE_NAME + " "
                + "ON " + POI_T.TABLE_NAME + "." + POI_T.EXCURSION_ID + "="
                + Excursions_T.TABLE_NAME + "." + Excursions_T.EXCURSION_ID + " "
                + "LEFT JOIN " + Users_T.TABLE_NAME + " "
                + "ON " + POI_T.TABLE_NAME + "." + POI_T.EXPLORER_ID + "="
                + Users_T.TABLE_NAME + "." + Users_T.USER_ID + " "
                + "WHERE " + POI_T.TABLE_NAME + "." + POI_T.EXCURSION_ID + " = '" + eid + "'";

        Cursor c = db.rawQuery(query, null);

        if (c == null) {
            return null;
        } else if (c.getCount() == 0) {
            c.close();
            return null;
        }
        Log.d(TAG, "returning " + c.getCount() + " points of interest");
        c.moveToFirst();
        return c;
    }

    public static int updatePointOfInterest(POI poi) {

        ContentValues values = new ContentValues();
        values.put(POI_T.EXCURSION_ID, poi.getExcursionID());
        values.put(POI_T.TYPE, poi.getType());
        values.put(POI_T.TITLE, poi.getTitle());
        values.put(POI_T.DESCRIPTION, poi.getDescription());
        values.put(POI_T.LATITUDE, poi.getLocation().latitude);
        values.put(POI_T.LONGITUDE, poi.getLocation().longitude);

        long oid = poi.getObservationID();

        int count = db.update(
                POI_T.TABLE_NAME,
                values,
                POI_T.POI_ID + " ='" + oid + "'",
                null);

        if(count == 0) {
            Log.d(TAG, "Error updating poi");
            return FAILURE;
        }

        return SUCCESS;
    }

}
