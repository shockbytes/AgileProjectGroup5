package at.shockbytes.corey.running.ui.fragment


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.view.GestureDetectorCompat
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import at.shockbytes.corey.running.R
import at.shockbytes.corey.running.coaching.Coach
import at.shockbytes.corey.running.dagger.AppComponent
import at.shockbytes.corey.running.location.RunningBroker
import at.shockbytes.corey.running.running.CoreyLatLng
import at.shockbytes.corey.running.running.Run
import at.shockbytes.corey.running.running.RunningManager
import at.shockbytes.corey.running.statistics.StatisticsManager
import at.shockbytes.corey.running.storage.StorageManager
import at.shockbytes.corey.running.util.AppParams
import at.shockbytes.corey.running.util.AppParams.REQ_CODE_PERM_LOCATION
import at.shockbytes.corey.running.util.RunUpdate
import at.shockbytes.corey.running.util.RunUtils
import at.shockbytes.corey.running.util.ViewManager
import at.shockbytes.util.AppUtils
import butterknife.BindView
import butterknife.OnClick
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


class RunningFragment : BaseFragment(), OnMapReadyCallback,
        GestureDetector.OnDoubleTapListener, RunningBroker.RunningBrokerClient {

    private enum class HeaderState {
        NORMAL, TIME_UPFRONT, DISTANCE_UPFRONT
    }

    private enum class SwipeDirection {
        LEFT, RIGHT
    }

    private var map: GoogleMap? = null
    private var trackLine: Polyline? = null
    private var isFirstLocation: Boolean = false

    private var headerState: HeaderState = HeaderState.NORMAL

    private var runningBroker: RunningBroker? = null

    private lateinit var gestureDetector: GestureDetectorCompat

    @Inject
    protected lateinit var coach: Coach

    @Inject
    protected lateinit var runningManager: RunningManager

    @Inject
    protected lateinit var storageManager: StorageManager

    @Inject
    protected lateinit var statisticsManager: StatisticsManager

    @BindView(R.id.fragment_running_header)
    protected lateinit var headerView: View

    @BindView(R.id.fragment_running_stop_help_view)
    protected lateinit var stopHelpView: View

    @BindView(R.id.fragment_running_stop_help_imgview)
    protected lateinit var stopHelpImageView: View

    @BindView(R.id.fragment_running_data_view)
    protected lateinit var headerDataView: View

    @BindView(R.id.fragment_running_map_background)
    protected lateinit var mapBackgroundView: View

    @BindView(R.id.fragment_running_btn_start)
    protected lateinit var btnStart: Button

    @BindView(R.id.fragment_running_txt_time)
    protected lateinit var chronometer: Chronometer

    @BindView(R.id.fragment_running_txt_distance)
    protected lateinit var txtDistance: TextView

    @BindView(R.id.fragment_running_txt_current_pace)
    protected lateinit var txtCurrentPace: TextView

    @BindView(R.id.fragment_running_txt_calories)
    protected lateinit var txtCalories: TextView

    @BindView(R.id.fragment_running_txt_avg_pace)
    protected lateinit var txtAvgPace: TextView

    override val layoutId = R.layout.fragment_running

    override fun setupViews() {
        clearViews()
        setupHeaderGestureRecognizer()
    }

    override fun injectToGraph(appComponent: AppComponent) {
        appComponent.inject(this)
    }


    override fun onResume() {
        super.onResume()
        showMapFragment()

        if (runningManager.isRecording) {
            setResetRunningViews(true)
            chronometer.base = runningManager.currentRun.startTime
            chronometer.start()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap
        setupMapAndLocationServices()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        runningBroker = context as? RunningBroker
    }

    @OnClick(R.id.fragment_running_btn_start)
    protected fun onClickButtonStart() {
        startRun()
    }

    override fun onSingleTapConfirmed(motionEvent: MotionEvent): Boolean {
        return true
    }

    override fun onDoubleTap(motionEvent: MotionEvent): Boolean {
        stopRun()
        return true
    }

    override fun onDoubleTapEvent(motionEvent: MotionEvent): Boolean {
        return true
    }

    override fun onRunUpdates(update: RunUpdate) {
        updateRun(update)
    }

    override fun onRunFinished() {
        showFinishedRun()
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQ_CODE_PERM_LOCATION)
    private fun setupMapAndLocationServices() {

        if (EasyPermissions.hasPermissions(activity, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            map?.isMyLocationEnabled = true
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.perm_location_rationale),
                    REQ_CODE_PERM_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // -------------------------- Setup --------------------------

    private fun setupHeaderGestureRecognizer() {

        gestureDetector = GestureDetectorCompat(context,
                object : GestureDetector.OnGestureListener {
                    override fun onDown(motionEvent: MotionEvent): Boolean {
                        return true
                    }

                    override fun onShowPress(motionEvent: MotionEvent) {

                    }

                    override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
                        return true
                    }

                    override fun onScroll(motionEvent: MotionEvent, motionEvent1: MotionEvent, v: Float, v1: Float): Boolean {
                        return false
                    }

                    override fun onLongPress(motionEvent: MotionEvent) {
                        showHelpHeader()
                    }

                    override fun onFling(downEvent: MotionEvent, upEvent: MotionEvent, vx: Float, vy: Float): Boolean {

                        val directionX = downEvent.x - upEvent.x
                        if (Math.abs(directionX) > 200) {
                            val direction = if (directionX > 0)
                                SwipeDirection.LEFT
                            else
                                SwipeDirection.RIGHT
                            animateTimeDistanceHeader(direction)
                        }
                        return true
                    }
                })
        gestureDetector.setOnDoubleTapListener(this)

        headerView.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
    }

    // -----------------------------------------------------------

    // ------------------ Show views / fragments -----------------

    private fun showMapFragment() {
        isFirstLocation = true

        val mapFragment = SupportMapFragment()
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_running_map_container, mapFragment)
                .commit()
        mapFragment.getMapAsync(this)
    }

    private fun showHelpHeader() {

        stopHelpView.animate()
                .alpha(1f)
                .setStartDelay(0)
                .setDuration(400)
                .withEndAction {
                    stopHelpView.animate()
                            .alpha(0f)
                            .setStartDelay(AppParams.HELP_SHOW_DELAY)
                            .start()
                }.start()
        headerDataView.animate()
                .alpha(0.1f)
                .scaleY(0.8f)
                .scaleX(0.8f)
                .setStartDelay(0)
                .setDuration(400)
                .withEndAction {
                    headerDataView.animate()
                            .alpha(1f)
                            .scaleY(1f)
                            .scaleX(1f)
                            .setStartDelay(AppParams.HELP_SHOW_DELAY)
                            .start()
                }.start()

        ViewManager.animateDoubleTap(stopHelpImageView)
    }

    private fun showFinishedRun() {

        val run = runningManager.getFinishedRun(true)
        chronometer.stop()
        setResetRunningViews(false)

        storageManager.storeRun(run)
        statisticsManager.updateStatistics(run)
        showDetailFragment(run)
    }

    private fun showDetailFragment(run: Run) {
        fragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.main_content, HistoryDetailFragment.newInstance(run))
                .addToBackStack(null)
                .commit()
    }

    // -----------------------------------------------------------

    // ------------------------ Animation ------------------------

    private fun animateStartingViews(animateOut: Boolean) {

        val alpha = if (animateOut) 0 else 1
        // Animate button & transparent view with a fade out ;transition and hide it in the end
        btnStart.animate().alpha(alpha.toFloat()).duration = 500
        mapBackgroundView.animate().alpha(alpha.toFloat()).duration = 500
    }

    private fun animateTimeDistanceHeader(direction: SwipeDirection) {

        val x = (headerDataView.width / 2
                - txtDistance.width / 2
                - chronometer.x.toInt())

        when (headerState) {

            RunningFragment.HeaderState.NORMAL ->

                if (direction == SwipeDirection.LEFT) {
                    txtDistance.animate().translationX((-x).toFloat()).start()
                    chronometer.animate().scaleX(0.5f).scaleY(0.5f).alpha(0.5f).start()
                    headerState = HeaderState.DISTANCE_UPFRONT
                } else {
                    chronometer.animate().translationX(x.toFloat()).start()
                    txtDistance.animate().scaleX(0.5f).scaleY(0.5f).alpha(0.5f).start()
                    headerState = HeaderState.TIME_UPFRONT
                }

            RunningFragment.HeaderState.TIME_UPFRONT ->

                if (direction == SwipeDirection.LEFT) {
                    chronometer.animate().translationX(0f).start()
                    txtDistance.animate().scaleX(1f).scaleY(1f).alpha(1f).start()
                    headerState = HeaderState.NORMAL
                }

            RunningFragment.HeaderState.DISTANCE_UPFRONT ->

                if (direction == SwipeDirection.RIGHT) {
                    txtDistance.animate().translationX(0f).start()
                    chronometer.animate().scaleX(1f).scaleY(1f).alpha(1f).start()
                    headerState = HeaderState.NORMAL
                }
        }

    }

    // -----------------------------------------------------------

    // ---------------- Start / Stop / Update run ----------------

    private fun startRun() {

        val startMillis = SystemClock.elapsedRealtime()
        runningBroker?.startRun(startMillis)
        chronometer.base = startMillis
        chronometer.start()

        setResetRunningViews(true)
    }

    private fun stopRun() {
        runningBroker?.stopRun()
    }

    private fun updateRun(runUpdate: RunUpdate) {

        val location = runUpdate.location
        if (location != null) {
            if (isFirstLocation) {
                isFirstLocation = false
                map?.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(LatLng(location.latitude, location.longitude), 16f))
            } else {
                map?.animateCamera(CameraUpdateFactory
                        .newLatLng(LatLng(location.latitude, location.longitude)))
            }
        }

        val run = runningManager.currentRun
        if (runUpdate.isRunInfoAvailable) {
            updateViews(runUpdate)
            setTrackOnMap(run.locations)
        }
    }

    private fun updateViews(update: RunUpdate) {

        val timeInMs = SystemClock.elapsedRealtime() - chronometer.base
        val averagePace = RunUtils.calculatePace(timeInMs, update.distance)
        val calories = RunUtils.calculateCaloriesBurned(timeInMs.toDouble(), coach.userWeight)
        txtDistance.text = "${AppUtils.roundDouble(update.distance, 2)}km"
        txtAvgPace.text = averagePace + "\nmin/km"
        txtCurrentPace.text = update.currentPace + "\nmin/km"
        txtCalories.text = "$calories\nkcal"
    }

    private fun setTrackOnMap(runPoints: List<CoreyLatLng>) {

        if (trackLine == null) {
            val lineOptions = PolylineOptions()
                    .width(15f)
                    .color(Color.parseColor("#03A9F4"))
            trackLine = map?.addPolyline(lineOptions)
        }
        trackLine?.points = runPoints.map { LatLng(it.latitude, it.longitude) }
    }

    // -----------------------------------------------------------

    // -------------------- Convenience helper -------------------

    private fun clearViews() {

        // Clear the text views
        chronometer.text = "00:00"
        txtDistance.text = "0.0 km"
        txtCurrentPace.text = "0:00\nmin/km"
        txtCalories.text = "0\nkcal"
        txtAvgPace.text = "0:00\nmin/km"

        // Clear map
        if (trackLine != null) {
            trackLine?.remove()
            trackLine = null
        }
    }

    private fun setResetRunningViews(isSetup: Boolean) {
        clearViews()
        animateStartingViews(isSetup)
    }

    companion object {

        fun newInstance(): RunningFragment {
            val fragment = RunningFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    // -----------------------------------------------------------

}
