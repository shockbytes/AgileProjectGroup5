package com.bth.running.location;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

/**
 * @author Martin Macheiner
 *         Date: 11.09.2017.
 */

public class RealmLatLng extends RealmObject implements Parcelable {

    public double lat;
    public double lng;

    public RealmLatLng() {
        this(0, 0);
    }

    public RealmLatLng(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    protected RealmLatLng(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
    }

    public static final Creator<RealmLatLng> CREATOR = new Creator<RealmLatLng>() {
        @Override
        public RealmLatLng createFromParcel(Parcel in) {
            return new RealmLatLng(in);
        }

        @Override
        public RealmLatLng[] newArray(int size) {
            return new RealmLatLng[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(lat);
        parcel.writeDouble(lng);
    }
}
