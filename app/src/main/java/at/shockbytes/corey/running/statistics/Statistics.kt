package at.shockbytes.corey.running.statistics

import at.shockbytes.corey.running.running.Run
import at.shockbytes.corey.running.util.RunUtils
import at.shockbytes.util.AppUtils
import org.joda.time.Period

/**
 * @author  Martin Macheiner
 * Date:    06.10.2017.
 */

class Statistics {

    private var kilometersCovered: Double = 0.toDouble()
    var longestRun: Long = 0
        private set // In milliseconds
    var burnedCalories: Int = 0
        private set
    private var fastestPace: Int = 0 // In seconds per kilometer

    val coveredKilometers: String
        get() = AppUtils.roundDouble(kilometersCovered, 2).toString()

    val longestRunFormatted: String
        get() = RunUtils.periodFormatter
                .print(Period(longestRun))

    fun getFastestPace(): String {

        var secs = fastestPace
        var mins = 0
        while (secs >= 60) {
            secs -= 60
            mins++
        }
        return mins.toString() + ":" + secs
    }

    fun update(run: Run) {

        burnedCalories += run.calories
        kilometersCovered += run.distance
        if (run.time > longestRun) {
            longestRun = run.time
        }
        updateFastestPace(run.averagePace ?: return)
    }

    /**
     *
     * @param avgPace in format mm:ss
     */
    private fun updateFastestPace(avgPace: String) {

        if (avgPace.contains(":") && !avgPace.contains("-")) {

            val mins = Integer.parseInt(avgPace.substring(0, avgPace.indexOf(":")))
            val secs = Integer.parseInt(avgPace.substring(avgPace.indexOf(":") + 1, avgPace.length))
            val avgPaceSecs = mins * 60 + secs

            if (avgPaceSecs < fastestPace) {
                fastestPace = avgPaceSecs
            }
        }
    }
}
