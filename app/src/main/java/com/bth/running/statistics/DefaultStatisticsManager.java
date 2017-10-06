package com.bth.running.statistics;

import android.support.annotation.NonNull;

import com.bth.running.running.Run;

import io.realm.Realm;

/**
 * @author Martin Macheiner
 *         Date: 06.10.2017.
 */

public class DefaultStatisticsManager implements StatisticsManager {

    private Realm realm;

    public DefaultStatisticsManager(Realm realm) {
        this.realm = realm;
    }

    @Override
    public Statistics getStatistics() {

        Statistics stats = realm.where(Statistics.class).findFirst();
        // Create a new stats object if it isn't available
        if (stats == null) {
            realm.beginTransaction();
            stats = new Statistics();
            realm.copyToRealm(stats);
            realm.commitTransaction();
        }
        return stats;
    }

    @Override
    public void resetStatistics() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                getStatistics().deleteFromRealm();
            }
        });
    }

    @Override
    public void updateStatistics(final Run run) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                getStatistics().update(run);
            }
        });
    }
}
