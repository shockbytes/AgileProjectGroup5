package com.bth.running.storage;

import android.support.annotation.NonNull;

import com.bth.running.running.Run;
import com.bth.running.statistics.Statistics;
import com.bth.running.util.AppParams;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * @author Martin Macheiner
 *         Date: 06.10.2017.
 */

public class RealmRunningMigration implements RealmMigration {

    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();
        if (oldVersion == AppParams.REALM_VERSION_STATS_UPDATE_VERSION) {
            addStatistics(schema);
            oldVersion++;
        }
        if (oldVersion == AppParams.REALM_VERSION_RUN_START_TIME_UPDATE) {
            addStartTimeSinceEpoch(schema);
            oldVersion++;
        }
        if (oldVersion == AppParams.REALM_VERSION_BUGFIX) {
            String name = Statistics.class.getSimpleName();
            RealmObjectSchema realmObject = schema.get(name);
            if (realmObject != null) {
                realmObject.addField("primaryKey", long.class);
                realmObject.addIndex("primaryKey");
            }
            oldVersion++;
        }
    }

    private void addStatistics(RealmSchema schema) {

        String name = Statistics.class.getSimpleName();
        schema.create(name);
        RealmObjectSchema realmObject = schema.get(name);
        if (realmObject != null) {
            realmObject.addField("kilometersCovered", double.class);
            realmObject.addField("longestRun", long.class);
            realmObject.addField("caloriesBurned", int.class);
            realmObject.addField("fastestPace", int.class);
        }
    }

    private void addStartTimeSinceEpoch(RealmSchema schema) {

        String name = Run.class.getSimpleName();
        RealmObjectSchema realmObject = schema.get(name);
        if (realmObject != null) {
            realmObject.addField("startTimeSinceEpoch", long.class);
        }
    }

}
