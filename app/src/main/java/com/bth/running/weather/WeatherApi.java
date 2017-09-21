package com.bth.running.weather;

import com.bth.running.weather.model.Weather;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author Martin Macheiner
 *         Date: 21.09.2017.
 */

public interface WeatherApi {

    String SERVICE_ENDPOINT = "https://api.openweathermap.org/data/2.5/";

    String API_KEY = "f1a5564dfc20059ace98a12dafc792d8";

    @GET("forecast")
    Observable<Weather> getWeatherForecast(@Query("q") String place,
                                           @Query("appid") String apiKey,
                                           @Query("units") String units);

}
