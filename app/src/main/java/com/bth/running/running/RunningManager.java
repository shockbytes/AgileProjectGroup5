package com.bth.running.running;

import android.location.Location;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public interface RunningManager {

    void startRunRecording(long startTimeInMillis);

    void stopRunRecord();

    Run updateCurrentRun(Location location);

    Run getCurrentRun();

    Run getFinishedRun(boolean resetRun);

    boolean isRecording();

}
