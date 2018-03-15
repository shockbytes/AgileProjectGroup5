package at.shockbytes.corey.running.dagger

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Vibrator
import android.preference.PreferenceManager
import at.shockbytes.corey.running.coaching.Coach
import at.shockbytes.corey.running.coaching.DefaultCoach
import at.shockbytes.corey.running.location.GooglePlayLocationManager
import at.shockbytes.corey.running.location.LocationManager
import at.shockbytes.corey.running.running.DefaultRunningManager
import at.shockbytes.corey.running.running.RunningManager
import at.shockbytes.corey.running.statistics.DefaultStatisticsManager
import at.shockbytes.corey.running.statistics.StatisticsManager
import at.shockbytes.corey.running.storage.DummyStorageManager
import at.shockbytes.corey.running.storage.StorageManager
import at.shockbytes.corey.running.weather.WeatherApi
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * @author  Martin Macheiner
 * Date:    05.09.2017.
 */

@Module
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun provideSharedPreferences(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(app)
    }

    @Provides
    @Singleton
    fun provideRunningManager(coach: Coach): RunningManager {
        return DefaultRunningManager(coach)
    }

    @Provides
    @Singleton
    fun provideLocationManager(): LocationManager {
        return GooglePlayLocationManager(app.applicationContext)
    }

    @Provides
    @Singleton
    fun provideStorageManager(): StorageManager {
        return DummyStorageManager()
    }

    @Provides
    @Singleton
    fun provideCoach(preferences: SharedPreferences, api: WeatherApi): Coach {
        return DefaultCoach(app.applicationContext, preferences, api)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(): NotificationManager {
        return app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @Singleton
    fun provideVibrator(): Vibrator {
        return app.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    @Provides
    @Singleton
    fun provideStatisticsManager(): StatisticsManager {
        return DefaultStatisticsManager()
    }

}
