package net.n0code.wilder.obj;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Excursion {

    // fields set when excursion is created - non modifiable
    private long tripID = -1;
    private long excursionID = -1;
    private long explorerID = -1;
    private long startTime = 0;
    private long endTime = 0;

    // updatable fields
    private String title = null;
    private String description = null;
    private double distance = 0;
    private double minAltitude = Double.MAX_VALUE;
    private double maxAltitude = 0;


    // Used when first creating an excursion
    public Excursion(long tid, long expid) {
        tripID = tid;
        explorerID = expid;
        Date now = new Date();
        startTime = now.getTime();
    }

    // Used to update the database
    public Excursion(long eid, String t, String d, double dist, double minAlt, double maxAlt, long time) {
        excursionID = eid;
        title = t;
        description = d;
        distance = dist;
        minAltitude = minAlt;
        maxAltitude = maxAlt;
        endTime = time;
    }

    // Used to create excursion from database information
    public Excursion(long tid, long eid, long expid, long start, long end, String t, String d, double dist, double minAlt, double maxAlt) {
        tripID = tid;
        excursionID = eid;
        explorerID = expid;
        startTime = start;
        endTime = end;
        title = t;
        description = d;
        distance = dist;
        minAltitude = minAlt;
        maxAltitude = maxAlt;
    }

    public void setExcursionID(long eid) { excursionID = eid; }
    public void setTitle(String t) {title = t; }
    public void setDescription(String d) {description = d; }
    public void setDistance(double d) {distance = d; }
    public void setMinAltitude(double min) {minAltitude = min; }
    public void setMaxAltitude(double max) {maxAltitude = max; }
    public void setEndTime(long time) {endTime = time; }

    public long getTripID() { return tripID; }
    public long getExcursionID() { return excursionID; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getExplorerID() { return explorerID; }
    public double getDistance() { return distance; }
    public double getMinAltitude() { return minAltitude; }
    public double getMaxAltitude() { return maxAltitude; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
}
