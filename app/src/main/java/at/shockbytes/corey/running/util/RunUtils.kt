package at.shockbytes.corey.running.util

import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author Martin Macheiner
 * Date: 10.09.2017.
 */

object RunUtils {

    private const val METS_5_30_PACE = 12.8

    val periodFormatter: PeriodFormatter
        get() = PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .printZeroAlways()
                .appendHours()
                .appendSeparator(":")
                .appendMinutes()
                .appendSeparator(":")
                .appendSeconds()
                .toFormatter()

    fun getDayOfWeekAbbr(date: Long): String {
        val locale = Locale.US
        val format2 = SimpleDateFormat("EE", locale)
        return format2.format(Date(date)).toUpperCase(locale)
    }

    fun calculatePace(timeInMs: Long, distance: Double): String {

        if (distance <= 0) {
            return "-:--"
        }
        val kmMillis = (timeInMs / distance).toLong()
        return formatPaceMillisToString(kmMillis)
    }

    fun calculateCaloriesBurned(runningTimeInMillis: Double, weightOfRunner: Double): Int {
        //double burned = distance * weightOfRunner * 1.036;
        val runningTimeInHours = runningTimeInMillis / 3600000.0
        val burned = METS_5_30_PACE * weightOfRunner * runningTimeInHours
        return Math.floor(burned).toInt()
    }

    private fun formatPaceMillisToString(kmMillis: Long): String {

        val formatter = PeriodFormatterBuilder()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSeparator(":")
                .appendSeconds()
                .toFormatter()

        val minutesSeconds = PeriodType.time()
                .withMillisRemoved()
                .withHoursRemoved()

        val kmPeriod = Period(kmMillis, minutesSeconds)
        return formatter.print(kmPeriod)
    }

}
