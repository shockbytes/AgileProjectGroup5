package com.bth.running.weather.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 21.09.2017.
 */

public class WeatherRecord {

    private long dt;

    @SerializedName("main")
    private Temperature temperature;

    private List<WeatherInfo> weather;

    public WeatherRecord() {
        weather = new ArrayList<>();
    }

    public long getTimestamp() {
        return dt * 1000L;
    }

    public double getTemperature() {
        return temperature.temp;
    }

    public int getTemperatureAsInt() {
        return (int) temperature.temp;
    }

    public String getWeatherIconUrl() {
        if (weather.size() > 0) {
            return weather.get(0).icon;
        }
        return null;
    }


    @Override
    public String toString() {
        return "Time: " + new Date(getTimestamp()).toString() + " WeatherIcon: " + weather.get(0).icon + " / " + temperature.toString();
    }

    private static class WeatherInfo {

        public String icon;

        public WeatherInfo() {

        }

    }

    private static class Temperature {

        double temp;
        double humidity;

        public Temperature() {

        }

        @Override
        public String toString() {
            return "Temp: " + temp + " / Humidity: " + humidity;
        }
    }

}
