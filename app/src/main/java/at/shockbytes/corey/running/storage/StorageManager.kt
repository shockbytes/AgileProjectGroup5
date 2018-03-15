package at.shockbytes.corey.running.storage


import at.shockbytes.corey.running.running.Run

/**
 * @author Martin Macheiner
 * Date: 05.09.2017.
 */

interface StorageManager {

    val runs: List<Run>

    fun storeRun(run: Run)

    fun deleteRun(run: Run)

}
