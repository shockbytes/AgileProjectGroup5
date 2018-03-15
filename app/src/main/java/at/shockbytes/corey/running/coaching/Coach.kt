package at.shockbytes.corey.running.coaching


import at.shockbytes.corey.running.weather.model.Weather
import io.reactivex.Observable

/**
 * @author Martin Macheiner
 * Date: 20.09.2017.
 */

interface Coach {

    val userWeight: Double

    val userHeight: Int

    fun getWeatherForecast(place: String): Observable<Weather>

    fun setUserBodyInformation(height: Int, weight: Double)

}
