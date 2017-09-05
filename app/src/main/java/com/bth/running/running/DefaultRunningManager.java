package com.bth.running.running;

import android.location.Location;

import com.bth.running.util.ResourceManager;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public class DefaultRunningManager implements RunningManager {

    private boolean isRecording;
    private Run run;

    public DefaultRunningManager() {
    }

    @Override
    public void startRunRecording() {
        isRecording = true;
        run = new Run();
    }

    @Override
    public void stopRunRecord() {
        isRecording = false;
        // TODO
    }

    @Override
    public void updateCurrentRun(Location location) {
        // TODO
    }

    @Override
    public double getDistanceCovered() {
        return ResourceManager.roundDoubleWithDigits(run.getDistance(), 2);
    }

    @Override
    public Run getFinishedRun() {

        if (!isRecording) {
            return run;
        } else {
            throw new IllegalArgumentException("Cannot get run data while manager is recording");
        }
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }
}
