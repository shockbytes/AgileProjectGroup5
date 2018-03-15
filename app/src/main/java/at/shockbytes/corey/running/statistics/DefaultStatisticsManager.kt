package at.shockbytes.corey.running.statistics

import at.shockbytes.corey.running.running.Run

/**
 * @author Martin Macheiner
 * Date: 06.10.2017.
 */

class DefaultStatisticsManager : StatisticsManager {

    override val statistics: Statistics
        get() {
            // TODO Do nothing now
            return Statistics()
        }

    override fun resetStatistics() {
        // TODO Do nothing now...
    }

    override fun updateStatistics(run: Run) {
        // TODO Do nothing now...
    }
}
