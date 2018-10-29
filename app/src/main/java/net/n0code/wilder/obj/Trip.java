package net.n0code.wilder.obj;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import net.n0code.wilder.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Trip implements Parcelable
{
    /* Builder and user modifiable fields */
    private String title;
    private String description;
    private String travelMode;
    private String shareMode;

    /* System modifiable fields */
    private int cloneCount;
    private long timeLastModified;

    /* Non-modifiable fields */
    private long tripID;
    private long explorerID;
    private long timeCreated;
    private long originalTripID;

    // For Excursions created in ExcursionsEdit ONLY
    public static class Builder
    {
        private Context ctx;
        private String title = null;
        private String description = null;
        private String travelMode = null;
        private String shareMode = null;
        private long explorerID = -1;

        public Builder(Context ctx, String title, long explorerID) {
            this.ctx = ctx;
            this.title = title;
            this.explorerID = explorerID;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder travelMode(String travelMode) {
            this.travelMode = travelMode;
            return this;
        }

        public Builder shareMode(String shareMode) {
            this.shareMode = shareMode;
            return this;
        }

        public Trip build() {
            return new Trip(ctx, this);
        }
    }

    private Trip(Context ctx, Builder b)
    {
        if (b != null) {
            this.title = b.title;
            this.description = b.description;
            this.travelMode = b.travelMode;
            this.shareMode = b.shareMode;
            this.explorerID = b.explorerID;
        }

        Resources res = ctx.getResources();

        if (title == null) {
            this.title = res.getString(R.string.defaultTripName);
        }
        if (description == null) {
            this.description = "";
        }
        if (this.travelMode == null) {
            this.travelMode = TravelMode.Foot.toString();
        }
        if (this.shareMode == null) {
            this.shareMode = ShareMode.Public.toString();
        }

        /* System defined fields */
        this.cloneCount = 0;

        Date now = new Date();
        this.timeCreated = now.getTime();
        this.timeLastModified = this.timeCreated;

        // These are added by the database
        this.tripID = -1;
        this.originalTripID = -1;
    }

    // For building Excursions using DB
    public Trip(String t, String d, String tm, String sm, int cc, long tlm,
                long tid, long eid, long tc, long otid)
    {
        this.title = t;
        this.description = d;
        this.travelMode = tm;
        this.shareMode = sm;
        this.cloneCount = cc;
        this.timeLastModified = tlm;
        this.tripID = tid;
        this.explorerID = eid;
        this.timeCreated = tc;
        this.originalTripID = otid;
    }

    private Trip() {}

    // For cloning existing Excursions already in the LocalDB
    public Trip clone(long explorerID)
    {
        Trip temp = new Trip();
        temp.title = this.title;
        temp.description = this.description;
        temp.travelMode = this.travelMode;
        temp.shareMode = this.shareMode;

        temp.originalTripID = this.tripID;
        temp.tripID = -1;          // This is set when added to the database

        temp.explorerID = explorerID;
        temp.cloneCount = 0;
        incrementCount();   // TODO: update databases with new count

        Date now = new Date();
        this.timeCreated = now.getTime();
        this.timeLastModified = this.timeCreated;

        return temp;
    }


    public String getTitle() {return title;}
    public String getDescription() {return description;}
    public String getTravelMode() { return travelMode;}
    public String getShareMode() { return shareMode;}
    public long getTimeCreated() { return timeCreated;}
    public long getOriginalTripID() { return originalTripID;}
    public long getExplorerID() { return explorerID;}
    public int getCloneCount() { return cloneCount;}
    public long getTimeLastModified() { return timeLastModified;}
    public long getTripID() { return tripID;}


    public void setTitle (String title) {
        this.title = title;
    }
    public void setDescription (String description) {
        this.description = description;
    }
    public void setTravelMode(String travelMode) {
        this.travelMode = travelMode;
    }
    public void setShareMode(String shareMode) {
        this.shareMode = shareMode;
    }

    //TODO: call updateTimeLastModified when editing Trip
    public void updateTimeLastModified() {
        Date now = new Date();
        this.timeLastModified = now.getTime();
    }

    public void incrementCount() {
        this.cloneCount++;
    }

    public void setTripID(long eid) {
        if (this.tripID == -1) {
            this.tripID = eid;
        }
    }

    public void setOriginalTripID(long eid) {
        if (this.originalTripID == -1) {
            this.originalTripID = eid;
        }
    }

    /* Code for the Parcelable interface */

    private Trip(Parcel in)
    {
        title = in.readString();
        description = in.readString();
        travelMode = in.readString();
        shareMode = in.readString();
        cloneCount = in.readInt();
        timeLastModified = in.readLong();
        tripID = in.readLong();
        explorerID = in.readLong();
        timeCreated = in.readLong();
        originalTripID = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(travelMode);
        dest.writeString(shareMode);
        dest.writeInt(cloneCount);
        dest.writeLong(timeLastModified);
        dest.writeLong(tripID);
        dest.writeLong(explorerID);
        dest.writeLong(timeCreated);
        dest.writeLong(originalTripID);
    }

    public static final Parcelable.Creator<Trip> CREATOR = new
            Parcelable.Creator<Trip>() {
                public Trip createFromParcel(Parcel in) {
                    return new Trip(in);
                }
                public Trip[] newArray(int size) {
                    return new Trip[size];
                }
            };

}

