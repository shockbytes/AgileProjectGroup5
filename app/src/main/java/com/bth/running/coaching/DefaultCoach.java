package com.bth.running.coaching;

import android.content.Context;
import android.content.SharedPreferences;

import com.bth.running.R;

/**
 * @author Martin Macheiner
 *         Date: 20.09.2017.
 */

public class DefaultCoach implements Coach {

    private Context context;
    private SharedPreferences preferences;

    public DefaultCoach(Context context, SharedPreferences preferences) {
        this.context = context;
        this.preferences = preferences;
    }

    @Override
    public void setUserBodyInformation(int height, double weight) {
        preferences.edit()
                .putLong(context.getString(R.string.preferences_key_weight), Double.doubleToRawLongBits(weight))
                .putInt(context.getString(R.string.preferences_key_height), height)
                .apply();
    }

    @Override
    public double getUserWeight() {
        return Double.longBitsToDouble(
                preferences.getLong(context.getString(R.string.preferences_key_weight),
                        Double.doubleToLongBits(
                                context.getResources().getInteger(R.integer.preferences_def_weight))));
    }

    @Override
    public int getUserHeight() {
        return preferences.getInt(context.getString(R.string.preferences_key_height),
                context.getResources().getInteger(R.integer.preferences_def_height));
    }
}
