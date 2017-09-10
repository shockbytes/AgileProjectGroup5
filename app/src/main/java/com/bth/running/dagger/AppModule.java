package com.bth.running.dagger;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bth.running.location.GooglePlayLocationManager;
import com.bth.running.location.LocationManager;
import com.bth.running.running.DefaultRunningManager;
import com.bth.running.running.RunningManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
    public RunningManager provideRunningManager() {
        return new DefaultRunningManager(app.getApplicationContext());
    }

    @Provides
    @Singleton
    public LocationManager provideLocationManager() {
        return new GooglePlayLocationManager(app.getApplicationContext());
    }

}
