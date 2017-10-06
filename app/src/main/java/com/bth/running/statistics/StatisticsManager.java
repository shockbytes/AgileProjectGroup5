package com.bth.running.statistics;

import com.bth.running.running.Run;

/**
 * @author Martin Macheiner
 *         Date: 06.10.2017.
 */

public interface StatisticsManager {

    Statistics getStatistics();

    void resetStatistics();

    void updateStatistics(Run run);

}
