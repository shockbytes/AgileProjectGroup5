package com.bth.running.location;

import android.location.Location;
import android.support.annotation.NonNull;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public interface LocationManager {

    public interface OnLocationUpdateListener {

        void onConnected();

        void onDisconnected();

        void onError(Exception e);

        void onLocationUpdate(Location location);

    }


    void start(@NonNull LocationManager.OnLocationUpdateListener listener);

    void stop();

    boolean isLocationUpdateRequested();

}