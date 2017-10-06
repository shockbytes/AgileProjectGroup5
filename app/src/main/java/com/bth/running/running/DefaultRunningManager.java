package com.bth.running.running;

import android.location.Location;
import android.os.SystemClock;

import com.bth.running.coaching.Coach;
import com.bth.running.util.RunUtils;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public class DefaultRunningManager implements RunningManager {

    private boolean isRecording;
    private Run run;
    private Location prevLocation;
    private Coach coach;

    public DefaultRunningManager(Coach coach) {
        this.coach = coach;
    }

    @Override
    public void startRunRecording(long startMillis) {
        isRecording = true;
        run = new Run(startMillis, System.currentTimeMillis());
        prevLocation = null;
    }

    @Override
    public void stopRunRecord() {
        isRecording = false;

        long timeInMs = SystemClock.elapsedRealtime() - run.getStartTime();
        run.setTime(timeInMs);
        run.setAvgPace(RunUtils.calculatePace(timeInMs, run.getDistance()));
        double weight = coach.getUserWeight();
        run.setCalories(RunUtils.calculateCaloriesBurned(timeInMs, weight));
        run.convertLocationsToRealmList();
    }

    @Override
    public Run updateCurrentRun(Location location) {
        float distance = 0f;

        if (prevLocation != null) {
            distance = prevLocation.distanceTo(location) / 1000f;
        }

        run.setDistance((double) distance + run.getDistance());
        run.addLocation(location);
        prevLocation = location;

        return run;
    }

    @Override
    public Run getCurrentRun() {
        return run;
    }

    @Override
    public Run getFinishedRun(boolean resetRun) {

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
