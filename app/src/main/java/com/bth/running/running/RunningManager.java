package com.bth.running.running;

import android.location.Location;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public interface RunningManager {

    void startRunRecording();

    void stopRunRecord(long timeInMs);

    Run updateCurrentRun(Location location);

    String getCurrentPace();

    Run getFinishedRun();

    boolean isRecording();

}
