package at.shockbytes.corey.running.location


import at.shockbytes.corey.running.util.RunUpdate

/**
 * @author  Martin Macheiner
 * Date:    03.10.2017.
 */

interface RunningBroker {

    fun startRun(startMillis: Long)

    fun stopRun()

    interface RunningBrokerClient {

        fun onRunUpdates(update: RunUpdate)

        fun onRunFinished()
    }

}
