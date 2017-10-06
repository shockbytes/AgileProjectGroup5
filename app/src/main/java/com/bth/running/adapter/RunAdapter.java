package com.bth.running.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bth.running.R;
import com.bth.running.running.Run;
import com.bth.running.util.ResourceManager;

import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

import butterknife.Bind;


/**
 * @author Martin Macheiner
 *         Date: 22.04.2017.
 */

public class RunAdapter extends BaseAdapter<Run> {

    public RunAdapter(Context cxt, List<Run> data) {
        super(cxt, data);
    }

    @Override
    public BaseAdapter<Run>.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_run, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseAdapter.ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    class ViewHolder extends BaseAdapter<Run>.ViewHolder {

        @Bind(R.id.item_run_date_time)
        TextView txtDatetime;

        @Bind(R.id.item_run_distance)
        TextView txtDistance;

        @Bind(R.id.item_run_duration)
        TextView txtDuration;

        ViewHolder(final View itemView) {
            super(itemView);
        }

        @Override
        public void bind(Run run) {
            content = run;

            txtDuration.setText(ResourceManager.getPeriodFormatter()
                    .print(new Period(run.getTime(), PeriodType.time().withMillisRemoved())));
            txtDatetime.setText(DateTimeFormat.forPattern("dd. MMM yyyy - kk:mm")
                    .print(run.getStartTimeSinceEpoch()));
            txtDistance.setText(ResourceManager.roundDoubleWithDigits(run.getDistance(), 2) + " km");
        }

    }


}
