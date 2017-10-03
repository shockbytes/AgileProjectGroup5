package com.bth.running.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bth.running.R;
import com.bth.running.location.RealmLatLng;
import com.bth.running.running.Run;
import com.bth.running.util.ResourceManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class HistoryDetailFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_RUN = "arg_run";

    public static HistoryDetailFragment newInstance(Run run) {
        HistoryDetailFragment fragment = new HistoryDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_RUN, run);
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.fragment_detail_history_map_container)
    protected View mapsContainer;

    @Bind(R.id.fragment_history_detail_txt_date)
    protected TextView txtDate;

    @Bind(R.id.fragment_history_detail_txt_time)
    protected TextView txtTime;

    @Bind(R.id.fragment_history_detail_txt_distance)
    protected TextView txtDistance;

    @Bind(R.id.fragment_history_detail_txt_calories)
    protected TextView txtCalories;

    @Bind(R.id.fragment_history_detail_txt_avg_pace)
    protected TextView txtAvgPace;

    private Run run;

    public HistoryDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            run = getArguments().getParcelable(ARG_RUN);
            Log.wtf("Running", run.toString());
        }
        postponeEnterTransition();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history_detail, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startPostponedEnterTransition();
        setupMap();
        setupViews();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (run.getRealmLocations() != null && run.getRealmLocations().size() > 0) {

            PolylineOptions lineOptions = new PolylineOptions()
                    .width(15)
                    .color(Color.parseColor("#03A9F4"));

            LatLngBounds.Builder b = LatLngBounds.builder();
            for (RealmLatLng rll : run.getRealmLocations()) {
                LatLng tmp = new LatLng(rll.lat, rll.lng);
                b.include(tmp);
                lineOptions.add(tmp);
            }
            googleMap.addPolyline(lineOptions);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 100));

            BitmapDescriptor iconStart = getMarkerIconFromDrawable(
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_marker_start));
            MarkerOptions startMarker = new MarkerOptions()
                    .draggable(false)
                    .title("Start")
                    .icon(iconStart)
                    .flat(true)
                    .position(run.getStartLatLng());
            googleMap.addMarker(startMarker);

            BitmapDescriptor iconEnd = getMarkerIconFromDrawable(
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_marker_goal));
            MarkerOptions endMarker = new MarkerOptions()
                    .draggable(false)
                    .title("Finish")
                    .icon(iconEnd)
                    .flat(true)
                    .position(run.getLastLatLng());
            googleMap.addMarker(endMarker);
        }
    }

    @OnClick(R.id.fragment_detail_history_btn_close)
    protected void onClickClose() {
        getFragmentManager().popBackStackImmediate();
    }

    @OnClick(R.id.fragment_detail_history_btn_share)
    protected void onClickShare(){
        Intent sendIntent = new Intent();

        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I ran " +
                ResourceManager.roundDoubleWithDigits(run.getDistance(), 2) + " km in " +
                ResourceManager.getPeriodFormatter()
                .print(new Period(run.getTime(), PeriodType.time().withMillisRemoved())) + " sec. Look at me I'm mr meeseeks");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void setupMap() {

        SupportMapFragment mapFragment = new SupportMapFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_detail_history_map_container, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);
    }

    private void setupViews() {

        setTextViewDrawableColor(txtTime, R.color.run_detailed_icon_tint);
        setTextViewDrawableColor(txtDistance, R.color.run_detailed_icon_tint);
        setTextViewDrawableColor(txtCalories, R.color.run_detailed_icon_tint);
        setTextViewDrawableColor(txtAvgPace, R.color.run_detailed_icon_tint);

        txtDistance.setText(ResourceManager.roundDoubleWithDigits(run.getDistance(), 2) + " km");
        txtTime.setText(ResourceManager.getPeriodFormatter()
                .print(new Period(run.getTime(), PeriodType.time().withMillisRemoved())));
        txtDate.setText(DateTimeFormat.forPattern("dd. MMM yyyy - kk:mm")
                .print(run.getStartTime()));
        txtCalories.setText(run.getCalories() + " kcal");
        txtAvgPace.setText(run.getAveragePace() + " min/km");
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat
                        .getColor(getContext(), color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}
