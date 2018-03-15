package at.shockbytes.corey.running.storage

import at.shockbytes.corey.running.running.Run

/**
 * @author  Martin Macheiner
 * Date:    14.03.2018
 */

// TODO DELETE THIS CLASS LATER, WHEN THERE IS A FIREBASE SOLUTION
class DummyStorageManager : StorageManager {
    override val runs: List<Run>
        get() = listOf()

    override fun storeRun(run: Run) {
        // Do nothing...
    }

    override fun deleteRun(run: Run) {
        // Do nothing
    }

}