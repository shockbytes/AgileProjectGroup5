package at.shockbytes.corey.running.running

import android.location.Location
import android.os.SystemClock
import at.shockbytes.corey.running.coaching.Coach
import at.shockbytes.corey.running.util.RunUtils

/**
 * @author Martin Macheiner
 * Date: 05.09.2017.
 */

class DefaultRunningManager(private val coach: Coach) : RunningManager {

    override var isRecording: Boolean = false
    override var currentRun: Run = Run()

    override fun startRunRecording(startTimeInMillis: Long) {
        isRecording = true
        currentRun = Run(startTimeInMillis, System.currentTimeMillis())
    }

    override fun stopRunRecord() {
        isRecording = false

        val timeInMs = SystemClock.elapsedRealtime() - currentRun.startTime
        currentRun.time = timeInMs
        currentRun.setAvgPace(RunUtils.calculatePace(timeInMs, currentRun.distance))
        val weight = coach.userWeight
        currentRun.calories = RunUtils.calculateCaloriesBurned(timeInMs.toDouble(), weight)
    }

    override fun updateCurrentRun(location: Location): Run {
        currentRun.update(CoreyLatLng(location.latitude, location.longitude, location.time))
        return currentRun
    }

    override fun getFinishedRun(resetRun: Boolean): Run {
        return if (!isRecording) {
            currentRun
        } else {
            throw IllegalArgumentException("Cannot get run data while manager is recording")
        }
    }
}
