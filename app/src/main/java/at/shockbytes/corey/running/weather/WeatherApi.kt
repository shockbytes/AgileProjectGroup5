package at.shockbytes.corey.running.weather

import at.shockbytes.corey.running.weather.model.Weather
import io.reactivex.Observable

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @author Martin Macheiner
 * Date: 21.09.2017.
 */

interface WeatherApi {

    @GET("forecast")
    fun getWeatherForecast(@Query("q") place: String,
                           @Query("appid") apiKey: String,
                           @Query("units") units: String): Observable<Weather>

    companion object {

        const val SERVICE_ENDPOINT = "https://api.openweathermap.org/data/2.5/"

        const val API_KEY = "f1a5564dfc20059ace98a12dafc792d8"
    }

}
