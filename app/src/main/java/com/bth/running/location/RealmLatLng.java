package com.bth.running.location;

import io.realm.RealmObject;

/**
 * @author Martin Macheiner
 *         Date: 11.09.2017.
 */

public class RealmLatLng extends RealmObject {

    public double lat;
    public double lng;

    public RealmLatLng() {
        this(0, 0);
    }

    public RealmLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
