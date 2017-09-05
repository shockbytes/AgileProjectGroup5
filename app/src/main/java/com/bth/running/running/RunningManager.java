package com.bth.running.running;

import android.location.Location;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public interface RunningManager {

    void startRunRecording();

    void stopRunRecord();

    void updateCurrentRun(Location location);

    double getDistanceCovered();

    Run getFinishedRun();

    boolean isRecording();

}
