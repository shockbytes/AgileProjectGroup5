package com.bth.running.core;

import android.app.Application;

import com.bth.running.dagger.AppComponent;
import com.bth.running.dagger.AppModule;
import com.bth.running.dagger.DaggerAppComponent;

import net.danlew.android.joda.JodaTimeAndroid;

import io.realm.Realm;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

public class RunningApp extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        JodaTimeAndroid.init(this);

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }


}
