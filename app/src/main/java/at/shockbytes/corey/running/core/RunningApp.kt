package at.shockbytes.corey.running.core

import android.app.Application
import at.shockbytes.corey.running.dagger.AppComponent
import at.shockbytes.corey.running.dagger.AppModule
import at.shockbytes.corey.running.dagger.DaggerAppComponent
import at.shockbytes.corey.running.dagger.NetworkModule
import net.danlew.android.joda.JodaTimeAndroid

/**
 * @author  Martin Macheiner
 * Date:    05.09.2017.
 */

class RunningApp : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()

        JodaTimeAndroid.init(this)

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .networkModule(NetworkModule(this))
                .build()
    }


}
