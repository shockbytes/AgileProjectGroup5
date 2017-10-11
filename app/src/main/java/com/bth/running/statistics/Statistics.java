package com.bth.running.statistics;

import android.support.annotation.NonNull;

import com.bth.running.running.Run;
import com.bth.running.util.ResourceManager;

import org.joda.time.Period;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @author Martin Macheiner
 *         Date: 06.10.2017.
 */

public class Statistics extends RealmObject {

    @PrimaryKey
    private long primaryKey = 1; // Just keep it here to update the object class

    private double kilometersCovered;
    private long longestRun; // In milliseconds
    private int caloriesBurned;
    private int fastestPace; // In seconds per kilometer

    public Statistics() {

    }

    public String getCoveredKilometers() {
        return String.valueOf(ResourceManager.roundDoubleWithDigits(kilometersCovered, 2));
    }

    public long getLongestRun() {
        return longestRun;
    }

    public int getBurnedCalories() {
        return caloriesBurned;
    }

    public String getFastestPace() {

        int secs = fastestPace;
        int mins = 0;
        while (secs >= 60) {
            secs-= 60;
            mins++;
        }
        return mins + ":" + secs;
    }

    public String getLongestRunFormatted() {
        return ResourceManager.getPeriodFormatter()
                .print(new Period(getLongestRun()));
    }

    public void update(@NonNull Run run) {

        caloriesBurned += run.getCalories();
        kilometersCovered += run.getDistance();
        if (run.getTime() > longestRun) {
            longestRun = run.getTime();
        }
        updateFastestPace(run.getAveragePace());
    }

    /**
     *
     * @param avgPace in format mm:ss
     */
    private void updateFastestPace(String avgPace) {

        if (avgPace.contains(":") && !avgPace.contains("-")) {

            int mins = Integer.parseInt(avgPace.substring(0, avgPace.indexOf(":")));
            int secs = Integer.parseInt(avgPace.substring(avgPace.indexOf(":")+1, avgPace.length()));
            int avgPaceSecs = (mins * 60) + secs;

            if (avgPaceSecs < fastestPace) {
                fastestPace = avgPaceSecs;
            }
        }
    }
}
