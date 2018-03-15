package at.shockbytes.corey.running.location

import android.location.Location

import com.google.android.gms.tasks.OnCompleteListener

/**
 * @author  Martin Macheiner
 * Date:    05.09.2017.
 */

interface LocationManager {

    val isLocationUpdateRequested: Boolean

    interface OnLocationUpdateListener {

        fun onConnected()

        fun onDisconnected()

        fun onError(e: Exception)

        fun onLocationUpdate(location: Location)

    }

    fun start(listener: OnLocationUpdateListener)

    fun stop()

    fun subscribeForLastLocationCallback(listener: OnCompleteListener<Location>)

    companion object {

        // 1 seconds
        const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000

        // Half of normal update time
        const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

}