package com.bth.running.coaching;

import com.bth.running.weather.model.Weather;

import rx.Observable;

/**
 * @author Martin Macheiner
 *         Date: 20.09.2017.
 */

public interface Coach {

    Observable<Weather> getWeatherForecast(String place);

    void setUserBodyInformation(int height, double weight);

    double getUserWeight();

    int getUserHeight();

}
