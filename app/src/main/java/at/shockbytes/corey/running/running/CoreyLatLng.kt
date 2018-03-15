package at.shockbytes.corey.running.running

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

/**
 * @author Martin Macheiner
 * Date: 14.03.2018.
 */

@Parcelize
data class CoreyLatLng(val latitude: Double, val longitude: Double, val time: Long) : Parcelable {

    fun toLatLng(): LatLng = LatLng(latitude, longitude)

}