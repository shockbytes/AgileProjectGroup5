package at.shockbytes.corey.running.statistics


import at.shockbytes.corey.running.running.Run

/**
 * @author  Martin Macheiner
 * Date:    06.10.2017.
 */

interface StatisticsManager {

    val statistics: Statistics

    fun resetStatistics()

    fun updateStatistics(run: Run)

}
