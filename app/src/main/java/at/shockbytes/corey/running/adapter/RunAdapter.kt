package at.shockbytes.corey.running.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import at.shockbytes.corey.running.R
import at.shockbytes.corey.running.running.Run
import at.shockbytes.corey.running.util.RunUtils
import at.shockbytes.util.AppUtils
import at.shockbytes.util.adapter.BaseAdapter
import butterknife.BindView
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.format.DateTimeFormat


/**
 * @author  Martin Macheiner
 * Date:    22.04.2017.
 */

class RunAdapter(cxt: Context, data: List<Run>) : BaseAdapter<Run>(cxt, data.toMutableList()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseAdapter<Run>.ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.item_run, parent, false))
    }

    inner class ViewHolder(itemView: View) : BaseAdapter<Run>.ViewHolder(itemView) {

        @BindView(R.id.item_run_date_time)
        lateinit var txtDatetime: TextView

        @BindView(R.id.item_run_distance)
        lateinit var txtDistance: TextView

        @BindView(R.id.item_run_duration)
        lateinit var txtDuration: TextView

        override fun bind(t: Run) {
            content = t

            txtDuration.text = RunUtils.periodFormatter
                    .print(Period(t.time, PeriodType.time().withMillisRemoved()))
            txtDatetime.text = DateTimeFormat.forPattern("dd. MMM yyyy - kk:mm")
                    .print(t.startTimeSinceEpoch)
            txtDistance.text = AppUtils.roundDouble(t.distance, 2).toString() + " km"
        }

    }


}
