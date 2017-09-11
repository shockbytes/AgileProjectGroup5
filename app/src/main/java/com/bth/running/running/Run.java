package com.bth.running.running;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.bth.running.location.RealmLatLng;
import com.bth.running.util.AppParams;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public class Run extends RealmObject implements Parcelable {

    @Ignore
    private List<Location> locations;

    @PrimaryKey
    private long id;

    private RealmList<RealmLatLng> realmLocations;

    private long startTime;
    private double distance;
    private long time; // in ms
    private int calories;
    private String avgPace; // 00:00

    public Run() {
        this(System.currentTimeMillis());
    }

    public Run(long startTime) {
        this.startTime = startTime;
        locations = new ArrayList<>();
    }

    protected Run(Parcel in) {
        //locations = in.createTypedArrayList(Location.CREATOR);
        id = in.readLong();
        realmLocations = new RealmList<>();
        realmLocations.addAll(in.createTypedArrayList(RealmLatLng.CREATOR));
        //realmLocations = in.createTypedArrayList(RealmLatLng.CREATOR);
        startTime = in.readLong();
        distance = in.readDouble();
        time = in.readLong();
        calories = in.readInt();
        avgPace = in.readString();
    }

    public static final Creator<Run> CREATOR = new Creator<Run>() {
        @Override
        public Run createFromParcel(Parcel in) {
            return new Run(in);
        }

        @Override
        public Run[] newArray(int size) {
            return new Run[size];
        }
    };

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public int getCalories() {
        return calories;
    }

    public void setAvgPace(String avgPace) {
        this.avgPace = avgPace;
    }

    public String getAveragePace() {
        return avgPace;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void convertLocationsToRealmList() {
        realmLocations = new RealmList<>();
        for (Location l : locations) {
            realmLocations.add(new RealmLatLng(l.getLatitude(), l.getLongitude()));
        }
    }

    public void addLocation(Location location) {
        locations.add(location);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {

        return "Start time:\t\t\t" +
                new Date(startTime).toString() +
                "\nDistance:\t\t\t\t" +
                distance +
                " km\nTime:\t\t\t\t\t" +
                time / 1000 +
                " seconds\nAvg. pace:\t\t\t" +
                avgPace +
                " km/h\nCalories:\t\t\t\t" +
                calories +
                " kcal\nLocation points:\t\t\t" +
                realmLocations.size() +
                "\n---------------------\n";
    }

    double getCurrentPaceDistance() {

        if (locations.size() < AppParams.LOCATIONS_FOR_CURRENT_PACE) {
            return 0;
        }
        return locations.get(locations.size() - AppParams.LOCATIONS_FOR_CURRENT_PACE)
                .distanceTo(locations.get(locations.size() - 1)) / 1000d;
    }

    long getCurrentPaceTime() {

        if (locations.size() < AppParams.LOCATIONS_FOR_CURRENT_PACE) {
            return 0;
        }
        return (locations.get(locations.size() - 1)).getTime() -
                locations.get(locations.size() - AppParams.LOCATIONS_FOR_CURRENT_PACE).getTime();
    }

    public LatLng getStartLatLng() {
        if (realmLocations != null && realmLocations.size() > 0) {
            return new LatLng(realmLocations.get(0).lat, realmLocations.get(0).lng);
        }
        return null;
    }

    public LatLng getLastLatLng() {
        if (realmLocations != null && realmLocations.size() > 0) {
            return new LatLng(realmLocations.get(realmLocations.size()-1).lat,
                    realmLocations.get(realmLocations.size()-1).lng);
        }
        return null;
    }

    public List<RealmLatLng> getRealmLocations() {
        return realmLocations;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        //parcel.writeTypedList(locations);
        parcel.writeLong(id);
        parcel.writeTypedList(realmLocations);
        parcel.writeLong(startTime);
        parcel.writeDouble(distance);
        parcel.writeLong(time);
        parcel.writeInt(calories);
        parcel.writeString(avgPace);
    }

}
