package com.bth.running.fragments;


import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bth.running.R;
import com.bth.running.coaching.Coach;
import com.bth.running.core.RunningApp;
import com.bth.running.location.LocationManager;
import com.bth.running.statistics.Statistics;
import com.bth.running.statistics.StatisticsManager;
import com.bth.running.util.ResourceManager;
import com.bth.running.weather.model.Weather;
import com.bth.running.weather.model.WeatherRecord;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class CoachFragment extends Fragment implements OnCompleteListener<Location> {

    public static CoachFragment newInstance() {
        CoachFragment fragment = new CoachFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Inject
    protected Coach coach;

    @Inject
    protected LocationManager locationManager;

    @Inject
    protected StatisticsManager statisticsManager;

    @Bind(R.id.fragment_coach_body_edit_height)
    protected EditText editHeight;

    @Bind(R.id.fragment_coach_body_edit_weight)
    protected EditText editWeight;

    @Bind(R.id.fragment_coach_weather_txt_place)
    protected TextView txtPlace;

    @Bind(R.id.fragment_coach_stats_txt_distance)
    protected TextView txtStatsDistance;

    @Bind(R.id.fragment_coach_stats_txt_longest_run)
    protected TextView txtStatsLongestRun;

    @Bind(R.id.fragment_coach_stats_txt_calories)
    protected TextView txtStatsCalories;

    @Bind(R.id.fragment_coach_stats_txt_avg_pace)
    protected TextView txtStatsAvgPace;

    @Bind({R.id.fragment_coach_weather_day_1_txt_temp,
            R.id.fragment_coach_weather_day_2_txt_temp,
            R.id.fragment_coach_weather_day_3_txt_temp,
            R.id.fragment_coach_weather_day_4_txt_temp,
            R.id.fragment_coach_weather_day_5_txt_temp})
    protected List<TextView> txtWeatherTemps;

    @Bind({R.id.fragment_coach_weather_day_1_txt_day,
            R.id.fragment_coach_weather_day_2_txt_day,
            R.id.fragment_coach_weather_day_3_txt_day,
            R.id.fragment_coach_weather_day_4_txt_day,
            R.id.fragment_coach_weather_day_5_txt_day})
    protected List<TextView> txtWeatherDays;

    @Bind({R.id.fragment_coach_weather_day_1_img,
            R.id.fragment_coach_weather_day_2_img,
            R.id.fragment_coach_weather_day_3_img,
            R.id.fragment_coach_weather_day_4_img,
            R.id.fragment_coach_weather_day_5_img})
    protected List<ImageView> imgViewsWeather;

    public CoachFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RunningApp) getActivity().getApplication()).getAppComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_coach, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViews();
        setupStatistics();
        // Subscribe for the last known position and trigger the weather fetching upon receiving
        locationManager.subscribeForLastLocationCallback(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.fragment_coach_body_btn_update)
    protected void onClickBodyUpdate() {

        String textWeight = editWeight.getText().toString();
        String textHeight = editHeight.getText().toString();

        if (textHeight.isEmpty() || textWeight.isEmpty()) {
            Snackbar.make(getView(), "Body information can't be empty", Snackbar.LENGTH_SHORT).show();
            return;
        }
        coach.setUserBodyInformation(Integer.parseInt(textHeight), Double.parseDouble(textWeight));

        Snackbar.make(getView(), "Body information updated!", Snackbar.LENGTH_SHORT).show();
    }

    @OnClick(R.id.fragment_coach_stats_btn_reset)
    protected void onClickResetStatistics() {

        statisticsManager.resetStatistics();
        setupStatistics();
        Snackbar.make(getView(), "Statistics set back", Snackbar.LENGTH_SHORT).show();
    }

    private void setupViews() {
        editWeight.setText(String.valueOf(coach.getUserWeight()));
        editHeight.setText(String.valueOf(coach.getUserHeight()));
    }

    private void setupWeatherView(Weather weather, String place) {

        txtPlace.setText(place);
        for (int i = 0; i < weather.getForecastCount(); i++) {
            WeatherRecord wr = weather.getForecastFor(i);

            txtWeatherTemps.get(i)
                    .setText(getString(R.string.weather_temperature, wr.getTemperatureAsInt()));
            txtWeatherDays.get(i)
                    .setText(ResourceManager.getDayOfWeekAbbr(wr.getTimestamp()));
            Picasso.with(getContext())
                    .load(getString(R.string.weather_icon_url, wr.getWeatherIconUrl()))
                    .placeholder(R.drawable.ic_image_placeholder)
                    .into(imgViewsWeather.get(i));
        }

    }

    private void setupStatistics() {

        Statistics stats = statisticsManager.getStatistics();

        txtStatsDistance.setText(
                getString(R.string.coach_stats_distance, stats.getCoveredKilometers()));
        txtStatsLongestRun.setText(
                getString(R.string.coach_stats_longest_run, stats.getLongestRunFormatted()));
        txtStatsCalories.setText(
                getString(R.string.coach_stats_calories, stats.getBurnedCalories()));
        txtStatsAvgPace.setText(
                getString(R.string.coach_stats_avg_pace, stats.getFastestPace()));
    }

    private void loadWeather(@NonNull final String place) {

        coach.getWeatherForecast(place).subscribe(new Action1<Weather>() {
            @Override
            public void call(Weather weather) {
                weather.compress();
                setupWeatherView(weather, place);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

                throwable.printStackTrace();
                Snackbar.make(getView(), "Cannot load weather", Snackbar.LENGTH_LONG).
                        setAction("Reload", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                loadWeather(place);
                            }
                        }).show();
            }
        });
    }

    @Override
    public void onComplete(@NonNull Task<Location> task) {

        final Location location = task.getResult();
        Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {

                try {

                    return Observable.just(new Geocoder(getContext())
                            .getFromLocation(location.getLatitude(), location.getLongitude(), 1)
                            .get(0).getLocality());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                loadWeather(s);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Snackbar.make(getView(), "Cannot acquire current position", Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
