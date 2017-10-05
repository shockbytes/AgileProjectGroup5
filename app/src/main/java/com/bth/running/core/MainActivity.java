package com.bth.running.core;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bth.running.R;
import com.bth.running.fragments.CoachFragment;
import com.bth.running.fragments.HistoryFragment;
import com.bth.running.fragments.RunningFragment;
import com.bth.running.fragments.dialog.CloseDialogFragment;
import com.bth.running.location.RunningBroker;
import com.bth.running.util.AppParams;
import com.bth.running.util.ResourceManager;
import com.bth.running.util.RunUpdate;

import butterknife.Bind;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.bth.running.util.AppParams.REQ_CODE_PERM_LOCATION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RunningBroker {

    private static final int REQ_CODE_PERM_CONTACTS = 0x2224;

    @Bind(R.id.main_toolbar)
    protected Toolbar toolbar;

    @Bind(R.id.main_drawer_layout)
    protected DrawerLayout drawerLayout;

    @Bind(R.id.main_navigation_view)
    protected NavigationView navigationView;

    private ActionBarDrawerToggle drawerToggle;

    private RunningBroker.RunningBrokerClient runningClient;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int subject = intent.getIntExtra(AppParams.ServiceConnection.SERVICE_SUBJECT, -1);
            if (subject == AppParams.ServiceConnection.SERVICE_SUBJECT_RUN_UPDATE) {
                RunUpdate update = intent.getParcelableExtra(AppParams.ServiceConnection.EXTRA_RUN_UPDATE);
                if (runningClient != null) {
                    runningClient.onRunUpdates(update);
                }
            } else if (subject == AppParams.ServiceConnection.SERVICE_SUBJECT_FINISHED_RUN) {
                if (runningClient != null) {
                    runningClient.onRunFinished();
                }
            }

        }
    };
    private IntentFilter filter = new IntentFilter(LocationRunningService.ACTION_LOCATION_SERVICE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupActionBar();
        setupNavigationDrawer();
        startLocationServiceInBackground();
        showFragment(RunningFragment.newInstance());

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        ButterKnife.unbind(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // Do not support click on already selected item
        if (item.isChecked()) {
            return false;
        }

        switch (item.getItemId()) {

            case R.id.menu_navigation_coach:

                toolbar.setTitle(R.string.title_coach);
                showFragment(CoachFragment.newInstance());
                break;

            case R.id.menu_navigation_run:

                toolbar.setTitle(R.string.title_running);
                showFragment(RunningFragment.newInstance());
                break;

            case R.id.menu_navigation_history:

                toolbar.setTitle(R.string.title_history);
                showFragment(HistoryFragment.newInstance());
                break;

        }

        drawerLayout.closeDrawer(Gravity.START);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {

        // Only trigger the question to quit although running when there
        // are no back-stack entries and the broker client is active
        if (getSupportFragmentManager().getBackStackEntryCount() == 0
                && runningClient != null) {

            CloseDialogFragment.newInstance()
                    .setOnCloseItemClickedListener(new CloseDialogFragment.OnCloseItemClickedListener() {
                        @Override
                        public void onContinueRunClicked() {
                            MainActivity.super.onBackPressed();
                        }

                        @Override
                        public void onStopRunClicked() {
                            stopService(LocationRunningService.newIntent(getApplicationContext()));
                            MainActivity.super.onBackPressed();
                        }
                    }).show(getSupportFragmentManager(), "close-fragment");
        } else {
            super.onBackPressed();
        }
    }

    private void setupActionBar() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_running);
        }
    }

    private void setupNavigationDrawer() {

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getHeaderView(0)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        initializePersonalizedDrawer();
                    }
                });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.contentdesc_drawer_open, R.string.contentdesc_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.getMenu().getItem(1).setChecked(true);

        initializePersonalizedDrawer();
    }

    private void showFragment(Fragment fragment) {

        // Register qualified fragments for running updates
        if (fragment instanceof RunningBrokerClient) {
            runningClient = (RunningBrokerClient) fragment;
        } else {
            runningClient = null;
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_content, fragment)
                .commit();
    }

    public void animateToolbar() {

        int primary = ContextCompat.getColor(this, R.color.colorPrimary);
        int bg = ContextCompat.getColor(this, R.color.help_background);

        // Color animation
        ObjectAnimator toolbarAnimatorIn = ObjectAnimator.ofObject(toolbar, "backgroundColor",
                new ArgbEvaluator(), primary, bg)
                .setDuration(400);
        ObjectAnimator toolbarAnimatorOut = ObjectAnimator.ofObject(toolbar, "backgroundColor",
                new ArgbEvaluator(), bg, primary)
                .setDuration(400);
        toolbarAnimatorOut.setStartDelay(AppParams.HELP_SHOW_DELAY);

        AnimatorSet set = new AnimatorSet();
        set.playSequentially(toolbarAnimatorIn, toolbarAnimatorOut);
        set.start();
    }

    public void lockNavigationDrawer(boolean lock) {
        int lockMode = lock
                ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                : DrawerLayout.LOCK_MODE_UNLOCKED;
        drawerLayout.setDrawerLockMode(lockMode);
    }

    @AfterPermissionGranted(REQ_CODE_PERM_CONTACTS)
    private void initializePersonalizedDrawer() {

        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_CONTACTS)) {

            TextView navigationHeaderText = navigationView.getHeaderView(0)
                    .findViewById(R.id.navigation_header_text);
            ImageView navigationHeaderIcon = navigationView.getHeaderView(0)
                    .findViewById(R.id.navigation_header_imgview);

            String name = ResourceManager.getProfileName(this);
            if (!name.isEmpty() && navigationHeaderText != null) {
                navigationHeaderText.setText(ResourceManager.getProfileName(this));
            }

            Uri imageUri = ResourceManager.getProfileImage(this);
            if (imageUri != null) {
                navigationHeaderIcon.setImageDrawable(ResourceManager.createRoundedBitmap(this, imageUri));
            } else if (!name.isEmpty()) {
                navigationHeaderIcon.setImageDrawable(ResourceManager.createRoundedBitmap(this,
                        ResourceManager.createStringBitmap(ResourceManager.convertDpInPixel(96, this),
                                ContextCompat.getColor(this, R.color.colorPrimaryDark), String.valueOf(name.charAt(0)))));
            }

        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.perm_contacts_rationale),
                    REQ_CODE_PERM_CONTACTS, Manifest.permission.READ_CONTACTS);
        }
    }

    @SuppressWarnings({"MissingPermission"})
    @AfterPermissionGranted(REQ_CODE_PERM_LOCATION)
    private void startLocationServiceInBackground() {

        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            startService(LocationRunningService.newIntent(this));
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.perm_location_rationale),
                    REQ_CODE_PERM_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void startRun(long startMillis) {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(LocationRunningService.ACTION_LOCATION_SERVICE)
                        .putExtra(AppParams.ServiceConnection.SUBJECT,
                                AppParams.ServiceConnection.SUBJECT_START)
                        .putExtra(AppParams.ServiceConnection.EXTRA_START_MILLIS, startMillis));
    }

    @Override
    public void stopRun() {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(LocationRunningService.ACTION_LOCATION_SERVICE)
                        .putExtra(AppParams.ServiceConnection.SUBJECT,
                                AppParams.ServiceConnection.SUBJECT_STOP));
    }

}
