package com.bth.running.fragments;


import android.Manifest;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bth.running.R;
import com.bth.running.core.RunningApp;
import com.bth.running.location.LocationManager;
import com.bth.running.running.RunningManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


public class RunningFragment extends Fragment
        implements OnMapReadyCallback, LocationManager.OnLocationUpdateListener {

    private static final int REQ_CODE_PERM_LOCATION = 0x1245;
    private static final int REQUEST_CHECK_SETTINGS = 0x9874;

    public static RunningFragment newInstance() {
        RunningFragment fragment = new RunningFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private GoogleMap map;

    @Inject
    protected LocationManager locationManager;

    @Inject
    protected RunningManager runningManager;

    @Bind(R.id.fragment_running_map_background)
    protected View mapBackgroundView;

    @Bind(R.id.fragment_running_btn_start)
    protected Button btnStart;

    @Bind(R.id.fragment_running_txt_time)
    protected TextView txtTime;

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
        showMapFragment();
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
        setupTextViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (locationManager.isLocationUpdateRequested()) {
            locationManager.stop();
        }
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

    @SuppressWarnings({"MissingPermission"})
    @AfterPermissionGranted(REQ_CODE_PERM_LOCATION)
    private void setupMapAndLocationServices() {

        if (EasyPermissions.hasPermissions(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            map.setMyLocationEnabled(true);
            locationManager.start(this);

        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.perm_location_rationale),
                    REQ_CODE_PERM_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void setupTextViews() {
        txtTime.setText("0:00");
        txtDistance.setText("0.0 km");
        txtCurrentPace.setText("0:00\nmin/km");
        txtCalories.setText("0");
        txtAvgPace.setText("0:00\nmin/km");
    }

    private void showMapFragment() {
        SupportMapFragment mapFragment = new SupportMapFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_running_map_container, mapFragment).commit();
        mapFragment.getMapAsync(this);
    }

    @OnClick(R.id.fragment_running_btn_start)
    protected void onClickButtonStart() {

        if (!runningManager.isRecording()) {
            runningManager.startRunRecording();

            // Animate button & transparent view with a fade out ;transition and hide it in the end
            btnStart.animate().alpha(0).setDuration(500).withEndAction(new Runnable() {
                @Override
                public void run() {

                }
            });
            mapBackgroundView.animate().alpha(0).setDuration(500).withEndAction(new Runnable() {
                @Override
                public void run() {
                    mapBackgroundView.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public void onConnected() {
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onError(Exception e) {


        int statusCode = ((ApiException) e).getStatusCode();
        switch (statusCode) {

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                try {

                    ResolvableApiException rae = (ResolvableApiException) e;
                    rae.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException sie) {
                    sie.printStackTrace();
                }

                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                Toast.makeText(getContext(), "Wrong location settings, fix in settings!",
                        Toast.LENGTH_LONG).show();
                break;

        }
    }

    @Override
    public void onLocationUpdate(Location location) {

        map.moveCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));

        if (runningManager.isRecording()) {
            runningManager.updateCurrentRun(location);

            // TODO Update track on map & update views
            txtDistance.setText(runningManager.getDistanceCovered() + " km");
        }

    }
}
