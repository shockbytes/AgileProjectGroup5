package at.shockbytes.corey.running.util

import android.os.Parcelable
import at.shockbytes.corey.running.running.CoreyLatLng
import at.shockbytes.corey.running.running.Run
import kotlinx.android.parcel.Parcelize


/**
 * @author  Martin Macheiner
 * Date:    02.10.2017.
 */

@Parcelize
class RunUpdate(var location: CoreyLatLng? = null,
                var distance: Double = 0.toDouble(),
                var currentPace: String? = null,
                var isRunInfoAvailable: Boolean = false) : Parcelable {


    /*
    constructor(location: Location, run: Run?) : this() {
        this.location = location

        isRunInfoAvailable = run != null
        if (run != null) {
            this.distance = run.distance
            currentPace = calcCurrentPace(run)
        }
    }
    */

    companion object {

        fun calcCurrentPace(run: Run): String {
            val timeInMs = run.currentPaceTime
            val distance = run.currentPaceDistance
            return RunUtils.calculatePace(timeInMs, distance)
        }

    }

}
