/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 9/1/15 11:35 AM
 */
package com.odoo.core.account;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.OResource;
import com.odoo.R;

import odoo.controls.DateTimePicker;

public class BaseSettings extends PreferenceFragment implements
        Preference.OnPreferenceClickListener, DateTimePicker.PickerCallBack {
    public static final String TAG = BaseSettings.class.getSimpleName();
    public static final String KEY_ADD_ACCOUNT = "add_account";
    private OPreferenceManager mPref;
    private Preference mTimePreference;

    // Keys
    public static final String KEY_LEAD_WORK_DAY_START_TIME = "lead_work_day_start_time";
    public static final String KEY_NOTIFICATION_RING_TONE = "phonecall_notification_ringtone";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.base_preference);
        mPref = new OPreferenceManager(getActivity());
        mTimePreference = findPreference(KEY_LEAD_WORK_DAY_START_TIME);
        if (mTimePreference != null)
            mTimePreference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(KEY_LEAD_WORK_DAY_START_TIME)) {
            showTimePicker();
            return true;
        }
        return false;
    }

    private void showTimePicker() {
        String defaultDayStartTime = OResource.string(getActivity(), R.string.default_day_start_time);
        String time = mPref.getString(KEY_LEAD_WORK_DAY_START_TIME, defaultDayStartTime);
        DateTimePicker.Builder builder = new DateTimePicker.Builder(getActivity());
        builder.setTime(ODateUtils.convertToUTC(time, ODateUtils.DEFAULT_TIME_FORMAT,
                ODateUtils.DEFAULT_TIME_FORMAT));
        builder.setType(DateTimePicker.Type.Time);
        builder.setCallBack(this);
        builder.build().show();
    }

    private void finish() {
        getActivity().finish();
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onDatePick(String date) {

    }

    @Override
    public void onTimePick(String time) {
        Log.i(TAG, "Working start day time : " + time);
        mPref.putString(KEY_LEAD_WORK_DAY_START_TIME, time);
    }

    public static Uri getNotificationRingTone(Context context) {
        OPreferenceManager mPref = new OPreferenceManager(context);
        String defaultUri = OResource.string(context, R.string.notification_default_ring_tone);
        return Uri.parse(mPref.getString(KEY_NOTIFICATION_RING_TONE, defaultUri));
    }

    public static String getDayStartTime(Context context) {
        String defaultDayStartTime = OResource.string(context, R.string.default_day_start_time);
        OPreferenceManager mPref = new OPreferenceManager(context);
        return mPref.getString(KEY_LEAD_WORK_DAY_START_TIME, defaultDayStartTime);
    }

}
