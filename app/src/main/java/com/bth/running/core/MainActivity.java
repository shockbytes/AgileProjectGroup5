package com.bth.running.core;

import android.Manifest;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.bth.running.util.ResourceManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQ_CODE_PERM_CONTACTS = 0x2224;

    @Bind(R.id.main_toolbar)
    protected Toolbar toolbar;

    @Bind(R.id.main_drawer_layout)
    protected DrawerLayout drawerLayout;

    @Bind(R.id.main_navigation_view)
    protected NavigationView navigationView;

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupActionBar();
        setupNavigationDrawer();

        showFragment(RunningFragment.newInstance());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

            case R.id.menu_navigation_settings:

                /*
                startActivity(SettingsActivity.newIntent(this),
                        ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle()); */
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

    private void setupActionBar() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            //getSupportActionBar().setElevation(0);
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
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_content, fragment)
                .commit();
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
}
