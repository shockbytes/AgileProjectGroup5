package com.bth.running.dagger;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.bth.running.coaching.Coach;
import com.bth.running.coaching.DefaultCoach;
import com.bth.running.location.GooglePlayLocationManager;
import com.bth.running.location.LocationManager;
import com.bth.running.running.DefaultRunningManager;
import com.bth.running.running.RunningManager;
import com.bth.running.statistics.DefaultStatisticsManager;
import com.bth.running.statistics.StatisticsManager;
import com.bth.running.storage.RealmRunningMigration;
import com.bth.running.storage.RealmStorageManager;
import com.bth.running.storage.StorageManager;
import com.bth.running.util.AppParams;
import com.bth.running.weather.WeatherApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

@Module
public class AppModule {

    private Application app;

    public AppModule(Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    @Singleton
    public RunningManager provideRunningManager(Coach coach) {
        return new DefaultRunningManager(coach);
    }

    @Provides
    @Singleton
    public LocationManager provideLocationManager() {
        return new GooglePlayLocationManager(app.getApplicationContext());
    }

    @Provides
    @Singleton
    public Realm provideRealm() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(AppParams.REALM_SCHEMA_VERSION)
                .migration(new RealmRunningMigration())
                .build();
         return Realm.getInstance(config);
    }

    @Provides
    @Singleton
    public StorageManager provideStorageManager(Realm realm) {
        return new RealmStorageManager(realm);
    }

    @Provides
    @Singleton
    public Coach provideCoach(SharedPreferences preferences, WeatherApi api) {
        return new DefaultCoach(app.getApplicationContext(), preferences, api);
    }

    @Provides
    @Singleton
    public NotificationManager provideNotificationManager() {
        return (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Provides
    @Singleton
    public Vibrator provideVibrator() {
        return (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Provides
    @Singleton
    public StatisticsManager provideStatisticsManager(Realm realm) {
        return new DefaultStatisticsManager(realm);
    }

}
