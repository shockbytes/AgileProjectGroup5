package com.bth.running.weather.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 21.09.2017.
 */

public class Weather {

    List<WeatherRecord> list;

    public Weather() {
        list = new ArrayList<>();
    }

    public void compress() {

        if (list.size() > 5) {
            List<WeatherRecord> compressed = new ArrayList<>();
            for (int i = 0; i < list.size(); i += (list.size()/5)) {
                compressed.add(list.get(i));
            }
            list = new ArrayList<>(compressed);
        }
    }

    public int getForecastCount() {
        return list.size();
    }

    public WeatherRecord getForecastFor(int day) {

        if (day < 0 && day >= getForecastCount()) {
            throw new IndexOutOfBoundsException("Day must be in range");
        }

        return list.get(day);
    }

    @Override
    public String toString() {
        String str =  "Size: " + list.size() + "\n";
        for (WeatherRecord record : list) {
            str += record.toString() + "\n";
        }

        return str;
    }
}
