package com.bth.running.core;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.bth.running.R;
import com.bth.running.location.LocationManager;
import com.bth.running.running.Run;
import com.bth.running.running.RunningManager;
import com.bth.running.util.AppParams;
import com.bth.running.util.ResourceManager;
import com.bth.running.util.RunUpdate;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import javax.inject.Inject;

public class LocationRunningService extends Service
        implements LocationManager.OnLocationUpdateListener {

    public static String ACTION_LOCATION_SERVICE = "action_location_service";

    public static Intent newIntent(Context context) {
        return new Intent(context, LocationRunningService.class);
    }

    private static final long VIBRATION_INTERVAL = 600000L; // 10 minutes

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int subject = intent.getIntExtra(AppParams.ServiceConnection.SUBJECT, -1);
            switch (subject) {

                case AppParams.ServiceConnection.SUBJECT_START:
                    long startMillis = intent
                            .getLongExtra(AppParams.ServiceConnection.EXTRA_START_MILLIS,
                                    SystemClock.elapsedRealtime());
                    startRun(startMillis);
                    break;

                case AppParams.ServiceConnection.SUBJECT_STOP:
                    Log.wtf("Running", "STOP RUN IN SERVICE");
                    Run run = stopRun();
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(sendFinishedRunIntent(run));
                    break;

                case AppParams.ServiceConnection.SUBJECT_REQ_CUR1RENT:
                    LocalBroadcastManager.getInstance(getApplicationContext())
                            .sendBroadcast(sendCurrentRunIntent(runningManager.getCurrentRun()));
                    break;
            }
        }
    };
    private IntentFilter filter = new IntentFilter(ACTION_LOCATION_SERVICE);

    private NotificationCompat.Builder notificationBuilder;

    private long startMillisVibration;

    @Inject
    protected NotificationManager notificationManager;

    @Inject
    protected LocationManager locationManager;

    @Inject
    protected RunningManager runningManager;

    @Inject
    protected Vibrator vibrator;

    public LocationRunningService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((RunningApp) getApplication()).getAppComponent().inject(this);

        startMillisVibration = -1;
        locationManager.start(this);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public boolean stopService(Intent name) {
        stopLocationUpdates();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        stopLocationUpdates();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupNotificationChannelIfNecessary() {
        notificationManager.createNotificationChannel(
                new NotificationChannel(AppParams.NOTIFICATION_CHANNEL_ID,
                        getString(R.string.notification_channel),
                        NotificationManager.IMPORTANCE_HIGH));
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

                /*
                try {

                    // Propagate to Activity
                    ResolvableApiException rae = (ResolvableApiException) e;
                    rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException sie) {
                    sie.printStackTrace();
                } */

                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                Toast.makeText(this, "Wrong location settings, fix in settings!",
                        Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onLocationUpdate(Location location) {

        Run run = null;
        if (runningManager.isRecording()) {
            run = runningManager.updateCurrentRun(location);
            updateNotification(run);
            checkVibration();
        }
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(sendLocationUpdateIntent(new RunUpdate(location, run)));
    }

    private void stopLocationUpdates() {
        if (locationManager.isLocationUpdateRequested()) {
            locationManager.stop();
        }
        stopForeground(true);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private Intent sendFinishedRunIntent(Run run) {
        return getBaseSendIntent()
                .putExtra(AppParams.ServiceConnection.SERVICE_SUBJECT,
                        AppParams.ServiceConnection.SERVICE_SUBJECT_FINISHED_RUN)
                .putExtra(AppParams.ServiceConnection.EXTRA_FINISHED_RUN, run);
    }

    private Intent sendLocationUpdateIntent(RunUpdate runUpdate) {
        return getBaseSendIntent()
                .putExtra(AppParams.ServiceConnection.SERVICE_SUBJECT,
                        AppParams.ServiceConnection.SERVICE_SUBJECT_RUN_UPDATE)
                .putExtra(AppParams.ServiceConnection.EXTRA_RUN_UPDATE, runUpdate);
    }

    private Intent sendCurrentRunIntent(Run run) {
        return getBaseSendIntent()
                .putExtra(AppParams.ServiceConnection.SERVICE_SUBJECT,
                        AppParams.ServiceConnection.SERVICE_SUBJECT_CURRENT_RUN)
                .putExtra(AppParams.ServiceConnection.EXTRA_CURRENT_RUN, run);
    }

    private Intent getBaseSendIntent() {
        return new Intent(ACTION_LOCATION_SERVICE);
    }

    private NotificationCompat.Builder createServiceNotification(String text) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannelIfNecessary();
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(AppParams.START_FROM_NOTIFICATION_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);

        return new NotificationCompat.Builder(this, AppParams.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Running")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setOngoing(true)
                .setUsesChronometer(true);
    }

    private void updateNotification(Run run) {

        if (run != null) {
            String text = String.valueOf(ResourceManager.roundDoubleWithDigits(run.getDistance(), 2))
                    + "km";

            if (notificationBuilder == null) {
                notificationBuilder = createServiceNotification(text);
            } else {
                notificationBuilder.setContentText(text);
            }

            notificationManager.notify(AppParams.NOTIFICATION_ID,
                    notificationBuilder.build());
        }
    }

    private void checkVibration() {

        if (startMillisVibration > -1
                && SystemClock.elapsedRealtime() > startMillisVibration + VIBRATION_INTERVAL) {
            startMillisVibration = SystemClock.elapsedRealtime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(300,
                        VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //noinspection deprecation
                vibrator.vibrate(300);
            }
        }
    }

    public void startRun(long startMillis) {

        if (!runningManager.isRecording()) {
            startMillisVibration = startMillis;
            runningManager.startRunRecording(startMillis);
            notificationBuilder = createServiceNotification("0.0km");
            startForeground(AppParams.NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    public Run stopRun() {

        if (runningManager.isRecording()) {
            startMillisVibration = -1;
            runningManager.stopRunRecord();
            stopForeground(true);
            return runningManager.getFinishedRun(false);
        } else {
            Toast.makeText(this, "Run hasn't started!", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}
