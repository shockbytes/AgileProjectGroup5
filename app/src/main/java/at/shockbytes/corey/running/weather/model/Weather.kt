package at.shockbytes.corey.running.weather.model

import java.util.*

/**
 * @author Martin Macheiner
 * Date: 21.09.2017.
 */

class Weather {

    private var list: List<WeatherRecord>

    val forecastCount: Int
        get() = list.size

    init {
        list = ArrayList()
    }

    fun compress() {
        if (list.size > 5) {
            val compressed = ArrayList<WeatherRecord>()
            var i = 0
            while (i < list.size) {
                compressed.add(list[i])
                i += list.size / 5
            }
            list = ArrayList(compressed)
        }
    }

    fun getForecastFor(day: Int): WeatherRecord {

        if (day in forecastCount..-1) {
            throw IndexOutOfBoundsException("Day must be in range")
        }
        return list[day]
    }

    override fun toString(): String {
        var str = "Size: " + list.size + "\n"
        for (record in list) {
            str += record.toString() + "\n"
        }

        return str
    }
}
