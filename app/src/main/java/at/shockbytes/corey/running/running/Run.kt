package at.shockbytes.corey.running.running

import android.location.Location
import android.os.Parcelable
import android.os.SystemClock
import at.shockbytes.corey.running.util.AppParams
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * @author  Martin Macheiner
 * Date:    05.09.2017.
 */

@Parcelize
class Run(var startTime: Long = SystemClock.elapsedRealtime(),
          var startTimeSinceEpoch: Long = System.currentTimeMillis(),
          var distance: Double = 0.toDouble(),
          var time: Long = 0, // in ms
          var calories: Int = 0,
          var averagePace: String? = null,
          val locations: MutableList<CoreyLatLng> = mutableListOf()) : Parcelable {

    @IgnoredOnParcel
    private var previousLocation: CoreyLatLng? = null

    val currentPaceDistance: Double
        get() = if (locations.size < AppParams.LOCATIONS_FOR_CURRENT_PACE) {
            0.0
        } else distanceBetween(locations[locations.size - AppParams.LOCATIONS_FOR_CURRENT_PACE],
                locations[locations.size - 1]).toDouble()

    val currentPaceTime: Long
        get() = if (locations.size < AppParams.LOCATIONS_FOR_CURRENT_PACE) {
            0
        } else locations[locations.size - 1].time - locations[locations.size - AppParams.LOCATIONS_FOR_CURRENT_PACE].time

    val startLatLng: CoreyLatLng?
        get() = locations.firstOrNull()

    val lastLatLng: CoreyLatLng?
        get() = locations.lastOrNull()


    init {
        previousLocation = lastLatLng // If this is a parcelable recreation, initialize previous with last known location
    }

    fun setAvgPace(avgPace: String) {
        this.averagePace = avgPace
    }

    fun update(location: CoreyLatLng) {
        locations.add(location)

        // Update distance
        previousLocation?.let { prev ->
            val dist = distanceBetween(prev, location)
            distance += dist
        }
    }

    private fun distanceBetween(start: CoreyLatLng, end: CoreyLatLng): Float {
        val res = FloatArray(1)
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, res)
        return res[0].div(1000f)
    }


}
