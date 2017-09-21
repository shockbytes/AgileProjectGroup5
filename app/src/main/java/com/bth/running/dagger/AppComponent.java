package com.bth.running.dagger;

import com.bth.running.fragments.CoachFragment;
import com.bth.running.fragments.HistoryFragment;
import com.bth.running.fragments.RunningFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author Martin Macheiner
 *         Date: 05.09.2017.
 */

@Singleton
@Component(modules = {AppModule.class, NetworkModule.class})
public interface AppComponent {

    void inject(RunningFragment fragment);

    void inject(HistoryFragment fragment);

    void inject(CoachFragment fragment);

}
