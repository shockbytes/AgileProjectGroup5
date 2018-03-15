package at.shockbytes.corey.running.ui.fragment


import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import at.shockbytes.corey.running.R
import at.shockbytes.corey.running.coaching.Coach
import at.shockbytes.corey.running.dagger.AppComponent
import at.shockbytes.corey.running.location.LocationManager
import at.shockbytes.corey.running.statistics.StatisticsManager
import at.shockbytes.corey.running.util.RunUtils
import at.shockbytes.corey.running.weather.model.Weather
import butterknife.OnClick
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotterknife.bindView
import kotterknife.bindViews
import javax.inject.Inject


class CoachFragment : BaseFragment(), OnCompleteListener<Location> {

    @Inject
    protected lateinit var coach: Coach

    @Inject
    protected lateinit var locationManager: LocationManager

    @Inject
    protected lateinit var statisticsManager: StatisticsManager

    private val editHeight: EditText by bindView(R.id.fragment_coach_body_edit_height)
    private val editWeight: EditText by bindView((R.id.fragment_coach_body_edit_weight))
    private val txtPlace: TextView by bindView(R.id.fragment_coach_weather_txt_place)
    private val txtStatsDistance: TextView by bindView(R.id.fragment_coach_stats_txt_distance)
    private val txtStatsLongestRun: TextView by bindView(R.id.fragment_coach_stats_txt_longest_run)
    private val txtStatsCalories: TextView by bindView(R.id.fragment_coach_stats_txt_calories)
    private val txtStatsAvgPace: TextView by bindView(R.id.fragment_coach_stats_txt_avg_pace)

    private val txtWeatherTemps: List<TextView> by bindViews(R.id.fragment_coach_weather_day_1_txt_temp,
            R.id.fragment_coach_weather_day_2_txt_temp,
            R.id.fragment_coach_weather_day_3_txt_temp,
            R.id.fragment_coach_weather_day_4_txt_temp,
            R.id.fragment_coach_weather_day_5_txt_temp)
    private val txtWeatherDays: List<TextView> by bindViews(R.id.fragment_coach_weather_day_1_txt_day,
            R.id.fragment_coach_weather_day_2_txt_day,
            R.id.fragment_coach_weather_day_3_txt_day,
            R.id.fragment_coach_weather_day_4_txt_day,
            R.id.fragment_coach_weather_day_5_txt_day)

    private val imgViewsWeather: List<ImageView> by bindViews(R.id.fragment_coach_weather_day_1_img,
            R.id.fragment_coach_weather_day_2_img,
            R.id.fragment_coach_weather_day_3_img,
            R.id.fragment_coach_weather_day_4_img,
            R.id.fragment_coach_weather_day_5_img)

    override val layoutId = R.layout.fragment_coach

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }

    @OnClick(R.id.fragment_coach_body_btn_update)
    protected fun onClickBodyUpdate() {

        val textWeight = editWeight.text.toString()
        val textHeight = editHeight.text.toString()

        if (textHeight.isEmpty() || textWeight.isEmpty()) {
            Snackbar.make(view!!, "Body information can't be empty", Snackbar.LENGTH_SHORT).show()
            return
        }
        coach.setUserBodyInformation(Integer.parseInt(textHeight), java.lang.Double.parseDouble(textWeight))

        Snackbar.make(view!!, "Body information updated!", Snackbar.LENGTH_SHORT).show()
    }

    @OnClick(R.id.fragment_coach_stats_btn_reset)
    protected fun onClickResetStatistics() {

        statisticsManager.resetStatistics()
        setupStatistics()
        showSnackbar("Statistics set back")
    }

    override fun setupViews() {
        editWeight.setText(coach.userWeight.toString())
        editHeight.setText(coach.userHeight.toString())

        setupStatistics()
        // Subscribe for the last known position and trigger the weather fetching upon receiving
        locationManager.subscribeForLastLocationCallback(this)
    }

    private fun setupWeatherView(weather: Weather, place: String) {

        txtPlace.text = place
        for (i in 0 until weather.forecastCount) {
            val wr = weather.getForecastFor(i)

            txtWeatherTemps[i].text = getString(R.string.weather_temperature, wr.temperatureAsInt)
            txtWeatherDays[i].text = RunUtils.getDayOfWeekAbbr(wr.timestamp)
            Picasso.with(context)
                    .load(getString(R.string.weather_icon_url, wr.weatherIconUrl))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imgViewsWeather[i])
        }

    }

    private fun setupStatistics() {

        val stats = statisticsManager.statistics

        txtStatsDistance.text = getString(R.string.coach_stats_distance, stats.coveredKilometers)
        txtStatsLongestRun.text = getString(R.string.coach_stats_longest_run, stats.longestRunFormatted)
        txtStatsCalories.text = getString(R.string.coach_stats_calories, stats.burnedCalories)
        txtStatsAvgPace.text = getString(R.string.coach_stats_avg_pace, stats.getFastestPace())
    }

    private fun loadWeather(place: String) {

        coach.getWeatherForecast(place).subscribe({ weather ->
            weather.compress()
            setupWeatherView(weather, place)
        }) { throwable ->
            throwable.printStackTrace()
            showSnackbar("Cannot load weather", "Reload", false, { loadWeather(place) })
        }
    }

    override fun onComplete(task: Task<Location>) {
        val location = task.result
        Observable.fromCallable {
            Geocoder(context)
                    .getFromLocation(location.latitude, location.longitude, 1)[0]
                    .locality
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ s ->
                    loadWeather(s)
                }) {
                    showSnackbar("Cannot acquire current position")
                }
    }

    companion object {

        fun newInstance(): CoachFragment {
            val fragment = CoachFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
