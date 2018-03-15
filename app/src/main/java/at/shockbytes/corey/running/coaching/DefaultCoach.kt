package at.shockbytes.corey.running.coaching

import android.content.Context
import android.content.SharedPreferences
import at.shockbytes.corey.running.R
import at.shockbytes.corey.running.weather.WeatherApi
import at.shockbytes.corey.running.weather.model.Weather
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author  Martin Macheiner
 * Date:    20.09.2017.
 */

class DefaultCoach(private val context: Context,
                   private val preferences: SharedPreferences,
                   private val weatherApi: WeatherApi) : Coach {
    override val userWeight: Double
        get() = java.lang.Double.longBitsToDouble(
                preferences.getLong(context.getString(R.string.preferences_key_weight),
                        java.lang.Double.doubleToLongBits(
                                context.resources.getInteger(R.integer.preferences_def_weight).toDouble())))
    override val userHeight: Int
        get() = preferences.getInt(context.getString(R.string.preferences_key_height),
                context.resources.getInteger(R.integer.preferences_def_height))

    override fun getWeatherForecast(place: String): Observable<Weather> {
        return weatherApi.getWeatherForecast(place, WeatherApi.API_KEY, "metric")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
    }

    override fun setUserBodyInformation(height: Int, weight: Double) {
        preferences.edit()
                .putLong(context.getString(R.string.preferences_key_weight), java.lang.Double.doubleToRawLongBits(weight))
                .putInt(context.getString(R.string.preferences_key_height), height)
                .apply()
    }

}
