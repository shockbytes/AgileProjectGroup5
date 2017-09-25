package com.bth.running.util;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;


/**
 * @author Martin Macheiner
 *         Date: 10.09.2017.
 */

public class RunUtils {

    private static final double METS_5_30_PACE = 12.8;

    public static String calculatePace(long timeInMs, double distance) {

        if (distance <= 0) {
            return "-:--";
        }
        long kmMillis = (long) (timeInMs / distance);
        return formatPaceMillisToString(kmMillis);
    }

    public static int calculateCaloriesBurned(double runningTimeInMillis, double weightOfRunner) {
        //double burned = distance * weightOfRunner * 1.036;
        double runningTimeInHours = runningTimeInMillis / 3600000d;
        double burned = METS_5_30_PACE * weightOfRunner * runningTimeInHours;
        return (int) Math.floor(burned);
    }

    private static String formatPaceMillisToString(long kmMillis) {

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSeparator(":")
                .appendSeconds()
                .toFormatter();

        PeriodType minutesSeconds = PeriodType.time()
                .withMillisRemoved()
                .withHoursRemoved();

        Period kmPeriod = new Period(kmMillis, minutesSeconds);
        return formatter.print(kmPeriod);
    }

}
