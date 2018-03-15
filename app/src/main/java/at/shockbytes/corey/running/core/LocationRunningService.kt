package at.shockbytes.corey.running.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import at.shockbytes.corey.running.R
import at.shockbytes.corey.running.location.LocationManager
import at.shockbytes.corey.running.running.CoreyLatLng
import at.shockbytes.corey.running.running.Run
import at.shockbytes.corey.running.running.RunningManager
import at.shockbytes.corey.running.ui.activity.MainActivity
import at.shockbytes.corey.running.util.AppParams
import at.shockbytes.corey.running.util.RunUpdate
import at.shockbytes.util.AppUtils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationSettingsStatusCodes
import javax.inject.Inject

class LocationRunningService : Service(), LocationManager.OnLocationUpdateListener {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val subject = intent.getIntExtra(AppParams.ServiceConnection.SUBJECT, -1)
            when (subject) {

                AppParams.ServiceConnection.SUBJECT_START -> {
                    val startMillis = intent
                            .getLongExtra(AppParams.ServiceConnection.EXTRA_START_MILLIS,
                                    SystemClock.elapsedRealtime())
                    startRun(startMillis)
                }

                AppParams.ServiceConnection.SUBJECT_STOP -> {
                    Log.wtf("Running", "STOP RUN IN SERVICE")
                    val run = stopRun()
                    LocalBroadcastManager.getInstance(applicationContext)
                            .sendBroadcast(sendFinishedRunIntent(run))
                }

                AppParams.ServiceConnection.SUBJECT_REQ_CUR1RENT -> LocalBroadcastManager.getInstance(applicationContext)
                        .sendBroadcast(sendCurrentRunIntent(runningManager.currentRun))
            }
        }
    }
    private val filter = IntentFilter(ACTION_LOCATION_SERVICE)

    private var notificationBuilder: NotificationCompat.Builder? = null

    private var startMillisVibration: Long = 0

    @Inject
    protected lateinit var notificationManager: NotificationManager

    @Inject
    protected lateinit var locationManager: LocationManager

    @Inject
    protected lateinit var runningManager: RunningManager

    @Inject
    protected lateinit var vibrator: Vibrator

    private val baseSendIntent: Intent
        get() = Intent(ACTION_LOCATION_SERVICE)

    override fun onCreate() {
        super.onCreate()
        (application as RunningApp).appComponent.inject(this)

        startMillisVibration = -1
        locationManager.start(this)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_NOT_STICKY
    }

    override fun stopService(name: Intent): Boolean {
        stopLocationUpdates()
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        stopLocationUpdates()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupNotificationChannelIfNecessary() {
        notificationManager.createNotificationChannel(
                NotificationChannel(AppParams.NOTIFICATION_CHANNEL_ID,
                        getString(R.string.notification_channel),
                        NotificationManager.IMPORTANCE_HIGH))
    }

    override fun onConnected() {
    }

    override fun onDisconnected() {
    }

    override fun onError(e: Exception) {

        val statusCode = (e as ApiException).statusCode
        when (statusCode) {

            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
            }

            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                Toast.makeText(this, "Wrong location settings, fix in settings!",
                        Toast.LENGTH_LONG).show()
        }
    }

    override fun onLocationUpdate(location: Location) {

        if (runningManager.isRecording) {
            val run = runningManager.updateCurrentRun(location)
            updateNotification(run)
            checkVibration()

            val update = RunUpdate(CoreyLatLng(location.latitude, location.longitude, location.time),
                    run.distance, RunUpdate.calcCurrentPace(run), true)
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(sendLocationUpdateIntent(update))
        }
    }

    private fun stopLocationUpdates() {
        if (locationManager.isLocationUpdateRequested) {
            locationManager.stop()
        }
        stopForeground(true)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    private fun sendFinishedRunIntent(run: Run?): Intent {
        return baseSendIntent
                .putExtra(AppParams.ServiceConnection.SERVICE_SUBJECT,
                        AppParams.ServiceConnection.SERVICE_SUBJECT_FINISHED_RUN)
                .putExtra(AppParams.ServiceConnection.EXTRA_FINISHED_RUN, run)
    }

    private fun sendLocationUpdateIntent(runUpdate: RunUpdate): Intent {
        return baseSendIntent
                .putExtra(AppParams.ServiceConnection.SERVICE_SUBJECT,
                        AppParams.ServiceConnection.SERVICE_SUBJECT_RUN_UPDATE)
                .putExtra(AppParams.ServiceConnection.EXTRA_RUN_UPDATE, runUpdate)
    }

    private fun sendCurrentRunIntent(run: Run): Intent {
        return baseSendIntent
                .putExtra(AppParams.ServiceConnection.SERVICE_SUBJECT,
                        AppParams.ServiceConnection.SERVICE_SUBJECT_CURRENT_RUN)
                .putExtra(AppParams.ServiceConnection.EXTRA_CURRENT_RUN, run)
    }

    private fun createServiceNotification(text: String): NotificationCompat.Builder {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannelIfNecessary()
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.action = AppParams.START_FROM_NOTIFICATION_ACTION
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val icon = BitmapFactory.decodeResource(resources,
                R.mipmap.ic_launcher)

        return NotificationCompat.Builder(this, AppParams.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Running")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setOngoing(true)
                .setUsesChronometer(true)
    }

    private fun updateNotification(run: Run?) {

        if (run != null) {
            val text = "${AppUtils.roundDouble(run.distance, 2)}km"

            if (notificationBuilder == null) {
                notificationBuilder = createServiceNotification(text)
            } else {
                notificationBuilder?.setContentText(text)
            }

            notificationManager.notify(AppParams.NOTIFICATION_ID, notificationBuilder?.build())
        }
    }

    private fun checkVibration() {

        if (startMillisVibration > -1 && SystemClock.elapsedRealtime() > startMillisVibration + VIBRATION_INTERVAL) {
            startMillisVibration = SystemClock.elapsedRealtime()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(300,
                        VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(300)
            }
        }
    }

    fun startRun(startMillis: Long) {

        if (!runningManager.isRecording) {
            startMillisVibration = startMillis
            runningManager.startRunRecording(startMillis)
            notificationBuilder = createServiceNotification("0.0km")
            startForeground(AppParams.NOTIFICATION_ID, notificationBuilder?.build())
        }
    }

    fun stopRun(): Run? {

        if (runningManager.isRecording) {
            startMillisVibration = -1
            runningManager.stopRunRecord()
            stopForeground(true)
            return runningManager.getFinishedRun(false)
        } else {
            Toast.makeText(this, "Run hasn't started!", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    companion object {

        var ACTION_LOCATION_SERVICE = "action_location_service"

        fun newIntent(context: Context): Intent {
            return Intent(context, LocationRunningService::class.java)
        }

        private val VIBRATION_INTERVAL = 600000L // 10 minutes
    }

}
