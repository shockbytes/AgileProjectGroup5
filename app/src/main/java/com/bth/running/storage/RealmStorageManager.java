package com.bth.running.storage;

import android.support.annotation.NonNull;

import com.bth.running.running.Run;

import java.util.List;

import io.realm.Realm;
import io.realm.Sort;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public class RealmStorageManager implements StorageManager {

    private Realm realm;

    public RealmStorageManager(Realm realm) {
        this.realm = realm;
    }

    @Override
    public List<Run> getRuns() {
        return realm.where(Run.class).findAllSorted("startTime", Sort.DESCENDING);
    }

    @Override
    public void storeRun(final Run run) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                run.setId(getNextId(realm));
                realm.insertOrUpdate(run);
            }
        });
    }

    @Override
    public void deleteRun(Run run) {

        realm.beginTransaction();
        run.deleteFromRealm();
        realm.commitTransaction();
    }

    /**
     * This method must always be called inside a transaction!
     *
     * @return auto increment of primary key for run
     */
    private long getNextId(Realm realm) {

        Number currentIdNum = realm.where(Run.class).max("id");
        long nextId;
        if (currentIdNum == null) {
            nextId = 1;
        } else {
            nextId = currentIdNum.longValue() + 1;
        }
        return nextId;
    }

}
