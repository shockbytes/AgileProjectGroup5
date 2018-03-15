package at.shockbytes.corey.running.running

import android.location.Location

/**
 * @author  Martin Macheiner
 * Date:    05.09.2017.
 */

interface RunningManager {

    val currentRun: Run

    val isRecording: Boolean

    fun startRunRecording(startTimeInMillis: Long)

    fun stopRunRecord()

    fun updateCurrentRun(location: Location): Run

    fun getFinishedRun(resetRun: Boolean): Run

}
