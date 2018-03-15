package at.shockbytes.corey.running.dagger


import at.shockbytes.corey.running.core.LocationRunningService
import at.shockbytes.corey.running.ui.fragment.CoachFragment
import at.shockbytes.corey.running.ui.fragment.HistoryFragment
import at.shockbytes.corey.running.ui.fragment.RunningFragment
import dagger.Component
import javax.inject.Singleton

/**
 * @author  Martin Macheiner
 * Date:    05.09.2017.
 */

@Singleton
@Component(modules = [(AppModule::class), (NetworkModule::class)])
interface AppComponent {

    fun inject(fragment: RunningFragment)

    fun inject(fragment: HistoryFragment)

    fun inject(fragment: CoachFragment)

    fun inject(service: LocationRunningService)

}
