package net.n0code.wilder.obj;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class POI implements Serializable {

    private String TAG = "OBSERVATION";

    private long observation_id;
    private String type;
    private long excursion_id;
    private String title;
    private String description;
    private double latitude;
    private double longitude;
    private long explorer_id;
    private String dateCreated;
    private String timeCreated;

    public POI(String type, long eid, String title, String d, double lat, double lng, long exp_id) {
        this.type = type;
        excursion_id = eid;
        this.title = title;
        description = d;
        latitude = lat;
        longitude = lng;
        explorer_id = exp_id;

        Date now = new Date();
        dateCreated = new SimpleDateFormat("MMM dd, yyyy").format(now);
        timeCreated = new SimpleDateFormat("hh:mm a z").format(now);
    }

    public POI(String type, long eid, String title, String desc, double lat, double lng,
               long exp_id, String date, String time) {

        this.type = type;
        excursion_id = eid;
        this.title = title;
        description = desc;
        latitude = lat;
        longitude = lng;
        explorer_id = exp_id;
        dateCreated = date;
        timeCreated = time;
    }

    public void setObservationID(long oid) {observation_id = oid; }
    public void setType(String type) {this.type = type;}
    public void setTitle(String t) { title = t; }
    public void setDescription(String d) {description = d; }

    public void setLocation(LatLng loc) {
        setLocation(loc.latitude, loc.longitude);
    }
    public void setLocation(double lat, double lng) {
        latitude = lat;
        longitude = lng;
        Log.d(TAG, "lat: " + latitude + " lng: " + longitude);
    }

    public long getObservationID() { return observation_id; }
    public String getType() {return type;}
    public long getExcursionID() { return excursion_id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LatLng getLocation() { return new LatLng(latitude, longitude); }
    public long getExplorerID() { return explorer_id; }
    public String getDateCreated() { return dateCreated; }
    public String getTimeCreated() { return timeCreated; }


}
