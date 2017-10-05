package com.bth.running.location;

import com.bth.running.util.RunUpdate;

/**
 * @author Martin Macheiner
 *         Date: 03.10.2017.
 */

public interface RunningBroker {

    void startRun(long startMillis);

    void stopRun();

    interface RunningBrokerClient {

        void onRunUpdates(RunUpdate update);

        void onRunFinished();
    }

}
