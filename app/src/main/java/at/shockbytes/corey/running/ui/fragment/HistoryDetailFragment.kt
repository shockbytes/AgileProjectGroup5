package at.shockbytes.corey.running.ui.fragment


import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import at.shockbytes.corey.running.R
import at.shockbytes.corey.running.dagger.AppComponent
import at.shockbytes.corey.running.running.Run
import at.shockbytes.corey.running.util.RunUtils
import at.shockbytes.util.AppUtils
import butterknife.BindView
import butterknife.OnClick
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.format.DateTimeFormat


class HistoryDetailFragment : BaseFragment(), OnMapReadyCallback {

    @BindView(R.id.fragment_detail_history_map_container)
    protected lateinit var mapsContainer: View

    @BindView(R.id.fragment_history_detail_txt_date)
    protected lateinit var txtDate: TextView

    @BindView(R.id.fragment_history_detail_txt_time)
    protected lateinit var txtTime: TextView

    @BindView(R.id.fragment_history_detail_txt_distance)
    protected lateinit var txtDistance: TextView

    @BindView(R.id.fragment_history_detail_txt_calories)
    protected lateinit var txtCalories: TextView

    @BindView(R.id.fragment_history_detail_txt_avg_pace)
    protected lateinit var txtAvgPace: TextView

    private lateinit var run: Run

    override val layoutId = R.layout.fragment_history_detail

    override fun injectToGraph(appComponent: AppComponent) {
        // Do nothing...
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        run = arguments?.getParcelable(ARG_RUN) ?: Run()
        postponeEnterTransition()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.menu_details_share) {
            onClickShare()
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
    }


    override fun onMapReady(googleMap: GoogleMap) {

        if (run.locations.isNotEmpty()) {

            val lineOptions = PolylineOptions()
                    .width(15f)
                    .color(Color.parseColor("#03A9F4"))

            val b = LatLngBounds.builder()
            run.locations.forEach { cll ->
                val tmp = LatLng(cll.latitude, cll.longitude)
                b.include(tmp)
                lineOptions.add(tmp)
            }
            googleMap.addPolyline(lineOptions)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(b.build(), 100))

            val iconStart = getMarkerIconFromDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_marker_start))
            val startMarker = MarkerOptions()
                    .draggable(false)
                    .title("Start")
                    .icon(iconStart)
                    .flat(true)
                    .position(run.startLatLng?.toLatLng()!!)
            googleMap.addMarker(startMarker)

            val iconEnd = getMarkerIconFromDrawable(
                    ContextCompat.getDrawable(context, R.drawable.ic_marker_goal))
            val endMarker = MarkerOptions()
                    .draggable(false)
                    .title("Finish")
                    .icon(iconEnd)
                    .flat(true)
                    .position(run.lastLatLng?.toLatLng()!!)
            googleMap.addMarker(endMarker)
        }
    }

    @OnClick(R.id.fragment_detail_history_btn_close)
    protected fun onClickClose() {
        fragmentManager.popBackStackImmediate()
    }

    //@OnClick(R.id.fragment_detail_history_btn_share)
    protected fun onClickShare() {
        val sendIntent = Intent()

        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I ran " +
                AppUtils.roundDouble(run.distance, 2) + " km in " +
                RunUtils.periodFormatter
                        .print(Period(run.time, PeriodType.time().withMillisRemoved())) + " sec. It's nice to run that fast!")
        sendIntent.type = "text/plain"
        startActivity(Intent.createChooser(sendIntent, "Share your run"))
    }

    private fun setupMap() {
        val mapFragment = SupportMapFragment()
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_detail_history_map_container, mapFragment)
                .commit()
        mapFragment.getMapAsync(this)
    }

    override fun setupViews() {

        setupMap()

        setTextViewDrawableColor(txtTime, R.color.dark_gray)
        setTextViewDrawableColor(txtDistance, R.color.dark_gray)
        setTextViewDrawableColor(txtCalories, R.color.dark_gray)
        setTextViewDrawableColor(txtAvgPace, R.color.dark_gray)

        txtDistance.text = AppUtils.roundDouble(run.distance, 2).toString() + " km"
        txtTime.text = RunUtils.periodFormatter
                .print(Period(run.time, PeriodType.time().withMillisRemoved()))
        txtDate.text = DateTimeFormat.forPattern("dd. MMM yyyy - kk:mm")
                .print(run.startTimeSinceEpoch)
        txtCalories.text = run.calories.toString() + " kcal"
        txtAvgPace.text = run.averagePace + " min/km"
    }

    private fun setTextViewDrawableColor(textView: TextView, color: Int) {
        for (drawable in textView.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter = PorterDuffColorFilter(ContextCompat
                        .getColor(context, color), PorterDuff.Mode.SRC_IN)
            }
        }
    }

    private fun getMarkerIconFromDrawable(drawable: Drawable): BitmapDescriptor {
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    companion object {

        private const val ARG_RUN = "arg_run"

        fun newInstance(run: Run): HistoryDetailFragment {
            val fragment = HistoryDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_RUN, run)
            fragment.arguments = args
            return fragment
        }
    }

}// Required empty public constructor
