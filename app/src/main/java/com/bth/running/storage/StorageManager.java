package com.bth.running.storage;

import com.bth.running.running.Run;

import java.util.List;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public interface StorageManager {

    List<Run> getRuns();

    void storeRun(Run run);

    void deleteRun(Run run);

}
