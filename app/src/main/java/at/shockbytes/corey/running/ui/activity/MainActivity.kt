package at.shockbytes.corey.running.ui.activity

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import at.shockbytes.corey.running.R
import at.shockbytes.corey.running.core.LocationRunningService
import at.shockbytes.corey.running.location.RunningBroker
import at.shockbytes.corey.running.ui.fragment.CoachFragment
import at.shockbytes.corey.running.ui.fragment.HistoryFragment
import at.shockbytes.corey.running.ui.fragment.RunningFragment
import at.shockbytes.corey.running.ui.fragment.dialog.CloseDialogFragment
import at.shockbytes.corey.running.util.AppParams
import at.shockbytes.corey.running.util.AppParams.REQ_CODE_PERM_LOCATION
import at.shockbytes.corey.running.util.RunUpdate
import at.shockbytes.util.AppUtils
import butterknife.ButterKnife
import butterknife.Unbinder
import kotterknife.bindView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity(),
        BottomNavigationView.OnNavigationItemSelectedListener, RunningBroker {

    private val bottomNavigationView: BottomNavigationView by bindView(R.id.main_bottom_navigation)

    private var runningClient: RunningBroker.RunningBrokerClient? = null

    private var unbinder: Unbinder? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val subject = intent.getIntExtra(AppParams.ServiceConnection.SERVICE_SUBJECT, -1)
            if (subject == AppParams.ServiceConnection.SERVICE_SUBJECT_RUN_UPDATE) {
                val update = intent.getParcelableExtra<RunUpdate>(AppParams.ServiceConnection.EXTRA_RUN_UPDATE)
                runningClient?.onRunUpdates(update)
            } else if (subject == AppParams.ServiceConnection.SERVICE_SUBJECT_FINISHED_RUN) {
                runningClient?.onRunFinished()
            }
        }
    }
    private val filter = IntentFilter(LocationRunningService.ACTION_LOCATION_SERVICE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        unbinder = ButterKnife.bind(this)

        setupBottomNavigation()
        startLocationServiceInBackground()

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        unbinder?.unbind()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_navigation_profile -> {
                setActionBarElevation(true)
                showFragment(CoachFragment.newInstance())
            }
            R.id.menu_navigation_run -> {
                setActionBarElevation(false)
                showFragment(RunningFragment.newInstance())
            }
            R.id.menu_navigation_history -> {
                setActionBarElevation(true)
                showFragment(HistoryFragment.newInstance())
            }
        }
        return true
    }

    private fun setActionBarElevation(set: Boolean) {
        val dp = if (set) 8 else 0
        supportActionBar?.elevation = AppUtils.convertDpInPixel(dp, this).toFloat()
    }

    override fun onBackPressed() {

        // Only trigger the question to quit although running when there
        // are no back-stack entries and the broker client is active
        if (supportFragmentManager.backStackEntryCount == 0 && runningClient != null) {
            CloseDialogFragment.newInstance()
                    .setOnCloseItemClickedListener { stop ->
                        if (stop) {
                            stopService(LocationRunningService.newIntent(applicationContext))
                            super@MainActivity.onBackPressed()
                        } else {
                            super@MainActivity.onBackPressed()
                        }
                    }
                    .show(supportFragmentManager, "close-fragment")
        } else {
            super.onBackPressed()
        }
    }

    @AfterPermissionGranted(REQ_CODE_PERM_LOCATION)
    private fun startLocationServiceInBackground() {

        if (EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            startService(LocationRunningService.newIntent(this))
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.perm_location_rationale),
                    REQ_CODE_PERM_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun startRun(startMillis: Long) {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(LocationRunningService.ACTION_LOCATION_SERVICE)
                        .putExtra(AppParams.ServiceConnection.SUBJECT,
                                AppParams.ServiceConnection.SUBJECT_START)
                        .putExtra(AppParams.ServiceConnection.EXTRA_START_MILLIS, startMillis))

        animateBottomNavigationView(true)
    }

    override fun stopRun() {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(LocationRunningService.ACTION_LOCATION_SERVICE)
                        .putExtra(AppParams.ServiceConnection.SUBJECT,
                                AppParams.ServiceConnection.SUBJECT_STOP))

        animateBottomNavigationView(false)
    }

    private fun animateBottomNavigationView(isStarted: Boolean) {
        val translationY = if (isStarted) bottomNavigationView.height.toFloat() else 0f
        bottomNavigationView.animate()
                .translationY(translationY)
                .start()
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this)
        bottomNavigationView.selectedItemId = R.id.menu_navigation_run
    }

    private fun showFragment(fragment: Fragment) {

        // Register qualified fragments for running updates
        runningClient = if (fragment is RunningBroker.RunningBrokerClient) {
            fragment
        } else {
            null
        }

        supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_content, fragment)
                .commit()
    }


}
