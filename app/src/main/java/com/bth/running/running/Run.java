package com.bth.running.running;

import android.location.Location;

import com.bth.running.location.RealmLatLng;
import com.bth.running.util.AppParams;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public class Run extends RealmObject {

    @Ignore
    private List<Location> locations;

    private RealmList<RealmLatLng> realmLocations;

    private double distance;
    private long time; // in ms
    private int calories;
    private String avgPace; // 00:00

    public Run() {
        locations = new ArrayList<>();
    }

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

    @Override
    public String toString() {

        return "Distance:\t" +
                distance +
                " km\nTime:\t" +
                time / 1000 +
                " seconds\nAvg. pace:\t" +
                avgPace +
                " km/h\nCalories:\t" +
                calories +
                " kcal\nLocation points:\t" +
                realmLocations.size() +
                "\n";
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
}
