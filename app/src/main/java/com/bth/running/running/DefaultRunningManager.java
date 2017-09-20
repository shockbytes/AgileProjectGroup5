package com.bth.running.running;

import android.content.Context;
import android.location.Location;

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
    private Context context;
    private Coach coach;

    public DefaultRunningManager(Context context, Coach coach) {
        this.context = context;
        this.coach = coach;
    }

    @Override
    public void startRunRecording() {
        isRecording = true;
        run = new Run(System.currentTimeMillis());
        prevLocation = null;
    }

    @Override
    public void stopRunRecord(long timeInMs) {
        isRecording = false;

        run.setTime(timeInMs);
        run.setAvgPace(RunUtils.calculatePace(timeInMs, run.getDistance()));
        double weight = coach.getUserWeight();
        run.setCalories(RunUtils.calculateCaloriesBurned(run.getDistance(), weight));
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
    public String getCurrentPace() {


        double distance = run.getCurrentPaceDistance();
        long timeInMs = run.getCurrentPaceTime();

        return RunUtils.calculatePace(timeInMs, distance);
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
