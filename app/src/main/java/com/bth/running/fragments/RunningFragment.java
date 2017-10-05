package com.bth.running.fragments;


import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import com.bth.running.R;
import com.bth.running.coaching.Coach;
import com.bth.running.core.MainActivity;
import com.bth.running.core.RunningApp;
import com.bth.running.location.RunningBroker;
import com.bth.running.running.Run;
import com.bth.running.running.RunningManager;
import com.bth.running.storage.StorageManager;
import com.bth.running.util.AppParams;
import com.bth.running.util.ResourceManager;
import com.bth.running.util.RunUpdate;
import com.bth.running.util.RunUtils;
import com.bth.running.util.ViewManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.bth.running.util.AppParams.REQ_CODE_PERM_LOCATION;


public class RunningFragment extends Fragment
        implements OnMapReadyCallback,
        GestureDetector.OnDoubleTapListener, RunningBroker.RunningBrokerClient {

    private enum HeaderState {
        NORMAL, TIME_UPFRONT, DISTANCE_UPFRONT
    }

    private enum SwipeDirection {
        LEFT, RIGHT
    }

    public static RunningFragment newInstance() {
        RunningFragment fragment = new RunningFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private GoogleMap map;
    private Polyline trackLine;
    private boolean isFirstLocation;

    private HeaderState headerState;

    private GestureDetectorCompat gestureDetector;

    private RunningBroker runningBroker;

    @Inject
    protected Coach coach;

    @Inject
    protected RunningManager runningManager;

    @Inject
    protected StorageManager storageManager;

    @Bind(R.id.fragment_running_header)
    protected View headerView;

    @Bind(R.id.fragment_running_stop_help_view)
    protected View stopHelpView;

    @Bind(R.id.fragment_running_stop_help_imgview)
    protected View stopHelpImageView;

    @Bind(R.id.fragment_running_data_view)
    protected View headerDataView;

    @Bind(R.id.fragment_running_map_background)
    protected View mapBackgroundView;

    @Bind(R.id.fragment_running_btn_start)
    protected Button btnStart;

    @Bind(R.id.fragment_running_txt_time)
    protected Chronometer chronometer;

    @Bind(R.id.fragment_running_txt_distance)
    protected TextView txtDistance;

    @Bind(R.id.fragment_running_txt_current_pace)
    protected TextView txtCurrentPace;

    @Bind(R.id.fragment_running_txt_calories)
    protected TextView txtCalories;

    @Bind(R.id.fragment_running_txt_avg_pace)
    protected TextView txtAvgPace;

    public RunningFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RunningApp) getActivity().getApplication()).getAppComponent().inject(this);
        headerState = HeaderState.NORMAL;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_running, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clearViews();
        setupHeaderGestureRecognizer();
    }

    @Override
    public void onResume() {
        super.onResume();
        showMapFragment();

        if (runningManager.isRecording()) {
            setResetRunningViews(true);
            chronometer.setBase(runningManager.getCurrentRun().getStartTime());
            chronometer.start();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        setupMapAndLocationServices();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            runningBroker = (RunningBroker) context;
        } catch (IllegalArgumentException e) {
            Log.wtf("Running", "Parent not implementing interface");
        }
    }

    @OnClick(R.id.fragment_running_btn_start)
    protected void onClickButtonStart() {
        startRun();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        stopRun();
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onRunUpdates(RunUpdate update) {
        updateRun(update);
    }

    @Override
    public void onRunFinished() {
        showFinishedRun();
    }

    @SuppressWarnings({"MissingPermission"})
    @AfterPermissionGranted(REQ_CODE_PERM_LOCATION)
    private void setupMapAndLocationServices() {

        if (EasyPermissions.hasPermissions(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            map.setMyLocationEnabled(true);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.perm_location_rationale),
                    REQ_CODE_PERM_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    // -------------------------- Setup --------------------------

    private void setupHeaderGestureRecognizer() {

        gestureDetector = new GestureDetectorCompat(getContext(),
                new GestureDetector.OnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent motionEvent) {
                        return true;
                    }

                    @Override
                    public void onShowPress(MotionEvent motionEvent) {

                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent motionEvent) {
                        return true;
                    }

                    @Override
                    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent motionEvent) {
                        showHelpHeader();
                    }

                    @Override
                    public boolean onFling(MotionEvent downEvent, MotionEvent upEvent, float vx, float vy) {

                        float directionX = downEvent.getX() - upEvent.getX();
                        if (Math.abs(directionX) > 200) {
                            SwipeDirection direction = (directionX > 0)
                                    ? SwipeDirection.LEFT
                                    : SwipeDirection.RIGHT;
                            animateTimeDistanceHeader(direction);
                        }
                        return true;
                    }
                });
        gestureDetector.setOnDoubleTapListener(this);

        headerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
    }

    // -----------------------------------------------------------

    // ------------------ Show views / fragments -----------------

    private void showMapFragment() {
        isFirstLocation = true;

        SupportMapFragment mapFragment = new SupportMapFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_running_map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    private void showHelpHeader() {

        ((MainActivity) getActivity()).animateToolbar();

        stopHelpView.animate()
                .alpha(1)
                .setStartDelay(0)
                .setDuration(400)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        stopHelpView.animate()
                                .alpha(0)
                                .setStartDelay(AppParams.HELP_SHOW_DELAY)
                                .start();
                    }
                }).start();
        headerDataView.animate()
                .alpha(0.1f)
                .scaleY(0.8f)
                .scaleX(0.8f)
                .setStartDelay(0)
                .setDuration(400)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        headerDataView.animate()
                                .alpha(1)
                                .scaleY(1)
                                .scaleX(1)
                                .setStartDelay(AppParams.HELP_SHOW_DELAY)
                                .start();
                    }
                }).start();

        ViewManager.animateDoubleTap(stopHelpImageView);
    }

    private void showFinishedRun() {

        Run run = runningManager.getFinishedRun(true);
        chronometer.stop();

        setResetRunningViews(false);

        storageManager.storeRun(run);
        showDetailFragment(run);
    }

    private void showDetailFragment(Run run) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_content, HistoryDetailFragment.newInstance(run))
                .addToBackStack(null)
                .commit();
    }

    // -----------------------------------------------------------

    // ------------------------ Animation ------------------------

    private void animateStartingViews(boolean animateOut) {

        int alpha = animateOut ? 0 : 1;
        // Animate button & transparent view with a fade out ;transition and hide it in the end
        btnStart.animate().alpha(alpha).setDuration(500);
        mapBackgroundView.animate().alpha(alpha).setDuration(500);
    }

    private void animateTimeDistanceHeader(SwipeDirection direction) {

        int x = (headerDataView.getWidth() / 2)
                - (txtDistance.getWidth() / 2)
                - (int) chronometer.getX();

        switch (headerState) {

            case NORMAL:

                if (direction == SwipeDirection.LEFT) {
                    txtDistance.animate().translationX(-x).start();
                    chronometer.animate().scaleX(0.5f).scaleY(0.5f).alpha(0.5f).start();
                    headerState = HeaderState.DISTANCE_UPFRONT;
                } else {
                    chronometer.animate().translationX(x).start();
                    txtDistance.animate().scaleX(0.5f).scaleY(0.5f).alpha(0.5f).start();
                    headerState = HeaderState.TIME_UPFRONT;
                }
                break;

            case TIME_UPFRONT:

                if (direction == SwipeDirection.LEFT) {
                    chronometer.animate().translationX(0).start();
                    txtDistance.animate().scaleX(1).scaleY(1).alpha(1).start();
                    headerState = HeaderState.NORMAL;
                }
                break;

            case DISTANCE_UPFRONT:

                if (direction == SwipeDirection.RIGHT) {
                    txtDistance.animate().translationX(0).start();
                    chronometer.animate().scaleX(1).scaleY(1).alpha(1).start();
                    headerState = HeaderState.NORMAL;
                }
                break;
        }

    }

    // -----------------------------------------------------------

    // ---------------- Start / Stop / Update run ----------------

    private void startRun() {

        long startMillis = SystemClock.elapsedRealtime();
        runningBroker.startRun(startMillis);
        chronometer.setBase(startMillis);
        chronometer.start();

        setResetRunningViews(true);
    }

    private void stopRun() {
        runningBroker.stopRun();
    }

    private void updateRun(RunUpdate runUpdate) {

        Location location = runUpdate.getLocation();
        if (isFirstLocation) {
            isFirstLocation = false;
            map.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
        } else {
            map.animateCamera(CameraUpdateFactory
                    .newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
        }

        Run run = runningManager.getCurrentRun();
        if (runUpdate.isRunInfoAvailable() && run != null) {
            updateViews(runUpdate);
            setTrackOnMap(run.getRuntimeLocationAsLatLng());
        }
    }

    private void updateViews(RunUpdate update) {

        long timeInMs = (SystemClock.elapsedRealtime() - chronometer.getBase());
        String averagePace = RunUtils.calculatePace(timeInMs, update.getDistance());
        int calories = RunUtils.calculateCaloriesBurned(timeInMs, coach.getUserWeight());
        txtDistance.setText(ResourceManager.roundDoubleWithDigits(update.getDistance(), 2) + " km");
        txtAvgPace.setText(averagePace + "\nmin/km");
        txtCurrentPace.setText(update.getCurrentPace() + "\nmin/km");
        txtCalories.setText(calories + "\nkcal");
    }

    private void setTrackOnMap(List<LatLng> runPoints) {

        if (trackLine == null) {
            PolylineOptions lineOptions = new PolylineOptions()
                    .width(15)
                    .color(Color.parseColor("#03A9F4"));
            trackLine = map.addPolyline(lineOptions);
        }
        trackLine.setPoints(runPoints);
    }

    // -----------------------------------------------------------

    // -------------------- Convenience helper -------------------

    private void clearViews() {

        // Clear the text views
        chronometer.setText("00:00");
        txtDistance.setText("0.0 km");
        txtCurrentPace.setText("0:00\nmin/km");
        txtCalories.setText("0\nkcal");
        txtAvgPace.setText("0:00\nmin/km");

        // Clear map
        if (trackLine != null) {
            trackLine.remove();
            trackLine = null;
        }
    }

    private void setResetRunningViews(boolean isSetup) {
        clearViews();
        animateStartingViews(isSetup);
        ((MainActivity) getActivity()).lockNavigationDrawer(isSetup);
    }

    // -----------------------------------------------------------

}
