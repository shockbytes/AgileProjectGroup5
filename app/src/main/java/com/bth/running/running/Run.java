package com.bth.running.running;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public class Run {

    private List<LatLng> locations;
    private double distance;

    public Run() {
        locations = new ArrayList<>();
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

}
