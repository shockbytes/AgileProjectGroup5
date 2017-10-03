package com.bth.running.util;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.bth.running.running.Run;

/**
 * @author Martin Macheiner
 *         Date: 02.10.2017.
 */

public class RunUpdate implements Parcelable {

    private Location location;
    private double distance;
    private String currentPace;

    private boolean isRunInfoAvailable;

    public RunUpdate(Location location, Run run) {
        this.location = location;

        isRunInfoAvailable = run != null;
        if (run != null) {
            this.distance = run.getDistance();
            currentPace = calcCurrentPace(run);
        }
    }

    protected RunUpdate(Parcel in) {
        location = in.readParcelable(Location.class.getClassLoader());
        distance = in.readDouble();
        currentPace = in.readString();
        isRunInfoAvailable = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(location, flags);
        dest.writeDouble(distance);
        dest.writeString(currentPace);
        dest.writeByte((byte) (isRunInfoAvailable ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RunUpdate> CREATOR = new Creator<RunUpdate>() {
        @Override
        public RunUpdate createFromParcel(Parcel in) {
            return new RunUpdate(in);
        }

        @Override
        public RunUpdate[] newArray(int size) {
            return new RunUpdate[size];
        }
    };

    private String calcCurrentPace(Run run) {
        long timeInMs = run.getCurrentPaceTime();
        double distance = run.getCurrentPaceDistance();
        return RunUtils.calculatePace(timeInMs, distance);
    }

    public Location getLocation() {
        return location;
    }

    public double getDistance() {
        return distance;
    }

    public String getCurrentPace() {
        return currentPace;
    }

    public boolean isRunInfoAvailable() {
        return isRunInfoAvailable;
    }
}
