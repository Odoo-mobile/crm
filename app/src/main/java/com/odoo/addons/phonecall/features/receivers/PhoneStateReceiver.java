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
 * Created on 13/1/15 6:12 PM
 */
package com.odoo.addons.phonecall.features.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.odoo.addons.phonecall.PhoneCallDetail;
import com.odoo.addons.phonecall.features.CallerWindow;
import com.odoo.addons.phonecall.features.CustomerFinder;
import com.odoo.addons.phonecall.features.IOnCustomerFindListener;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.notification.ONotificationBuilder;
import com.odoo.R;

import java.util.Date;

public class PhoneStateReceiver extends BroadcastReceiver implements IOnCustomerFindListener {
    public static final String TAG = PhoneStateReceiver.class.getSimpleName();
    public static final String ACTION_CALL_BACK = "action_call_back";
    public static final String ACTION_CALL_SCHEDULE = "action_call_schedule";
    public static final Integer REQUEST_CALL_BACK = 5567;
    public static final Integer REQUEST_CALL_SCHEDULE = 5568;
    public static final String KEY_RECEIVED = "phone_state_received";
    public static final String KEY_RINGING = "phone_state_ringing";
    public static final String KEY_OFFHOOK = "phone_state_offhook";
    public static final String KEY_DURATION_START = "key_duration_start";
    public static final String KEY_DURATION_END = "key_duration_end";
    public static final String KEY_ACTIVITY_STARTED = "key_activity_started";
    private String callerNumber = null;
    private TelephonyManager telephonyManager;
    private OPreferenceManager mPref;
    private static CallerWindow callerWindow;
    private CustomerFinder customerFinder;
    private Context mContext;
    private Bundle extra = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (OUser.current(mContext) != null) {
            Log.v(TAG, "Phone state received.");
            mPref = new OPreferenceManager(context);
            if (callerWindow == null)
                callerWindow = new CallerWindow(context);
            customerFinder = new CustomerFinder(context);
            customerFinder.setOnCustomerFindListener(this);
            if (mPref.getBoolean(KEY_RECEIVED, true) && !callerWindow.isShowing()) {
                mPref.setBoolean(KEY_RECEIVED, false);
                mPref.setBoolean(KEY_RINGING, false);
            }
            if (!mPref.getBoolean(KEY_RECEIVED, false)) {
                telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                Bundle bundle = intent.getExtras();
                callerNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (callerNumber != null) {
                    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                }
                mPref.setBoolean(KEY_RECEIVED, true);
            }
        }
    }

    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.i(TAG, callerNumber + " CALL_STATE_IDLE");

                    mPref.setBoolean(KEY_RECEIVED, false);
                    mPref.setBoolean(KEY_RINGING, false);
                    if (extra != null) {
                        extra.putBoolean("in_bound", mPref.getBoolean("in_bound", false));
                        extra.putString(KEY_DURATION_END, new Date().getTime() + "");
                    }
                    if (!mPref.getBoolean(KEY_OFFHOOK, false)) {
                        showMissCallNotification(extra);
                    } else {
                        startLogCallActivity(extra);
                    }
                    if (callerWindow != null)
                        callerWindow.dismiss();
                    callerWindow = null;
                    customerFinder = null;
                    extra = null;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i(TAG, callerNumber + " CALL_STATE_OFFHOOK");
                    mPref.setBoolean(KEY_OFFHOOK, true);
                    // Call Started (received or dialed)
                    callStarted();
                    if (!mPref.getBoolean(KEY_RINGING, false)) {
                        mPref.setBoolean("in_bound", false);
                        if (customerFinder != null)
                            customerFinder.findCustomer(true, callerNumber);
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.i(TAG, callerNumber + " CALL_STATE_RINGING");
                    mPref.setBoolean(KEY_RINGING, true);
                    mPref.setBoolean(KEY_OFFHOOK, false);
                    mPref.setBoolean("notified", false);
                    mPref.setBoolean("in_bound", true);
                    if (customerFinder != null)
                        customerFinder.findCustomer(false, callerNumber);
                    break;
            }
        }
    };

    private void showMissCallNotification(Bundle data) {
        mPref.setBoolean(KEY_OFFHOOK, false);
        if (data != null && !mPref.getBoolean("notified", false)) {
            mPref.setBoolean("notified", true);
            int notification_id = 55568;
            ONotificationBuilder builder = new ONotificationBuilder(mContext, notification_id);
            data.putInt("notification_id", notification_id);
            builder.setTitle(_s(R.string.label_missed_call_from_customer));
            builder.setIcon(R.drawable.ic_action_user);
            builder.setText(data.getString("name"));
            ONotificationBuilder.NotificationAction callBack =
                    new ONotificationBuilder.NotificationAction(R.drawable.ic_action_phone,
                            "Call back", REQUEST_CALL_BACK,
                            ACTION_CALL_BACK, PhoneCallDetail.class, data);
            builder.addAction(callBack);
            data.putBoolean(PhoneCallDetail.KEY_LOG_CALL_REQUEST, true);
            data.putString(PhoneCallDetail.KEY_PHONE_NUMBER, callerNumber);
            data.putInt(PhoneCallDetail.KEY_OPPORTUNITY_ID, data.getInt("opportunity_id"));
            ONotificationBuilder.NotificationAction scheduleCall =
                    new ONotificationBuilder.NotificationAction(R.drawable.ic_action_reschedule,
                            "Schedule", REQUEST_CALL_SCHEDULE,
                            ACTION_CALL_SCHEDULE, PhoneCallDetail.class, data);
            builder.addAction(scheduleCall);
            builder.allowVibrate(false);
            builder.build().show();
        }
    }

    public String _s(int res_id) {
        return OResource.string(mContext, res_id);
    }

    private void startLogCallActivity(Bundle data) {
        if (data != null && !mPref.getBoolean(KEY_ACTIVITY_STARTED, false)) {
            mPref.setBoolean(KEY_ACTIVITY_STARTED, true);
            Intent intent = new Intent(mContext, PhoneCallDetail.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            data.putBoolean(PhoneCallDetail.KEY_LOG_CALL_REQUEST, true);
            data.putString(PhoneCallDetail.KEY_PHONE_NUMBER, callerNumber);
            data.putInt(PhoneCallDetail.KEY_OPPORTUNITY_ID, data.getInt("opportunity_id"));
            intent.putExtras(data);
            mContext.startActivity(intent);
        }
    }

    private void callStarted() {
        if (extra != null && !extra.containsKey(KEY_DURATION_START)) {
            extra.putString(KEY_DURATION_START, new Date().getTime() + "");
        }
    }

    @Override
    public void onCustomerFind(Boolean dialed, ODataRow row) {
        if (row != null) {
            extra = new Bundle();
            extra = row.getPrimaryBundleData();
            callStarted();
            extra.putString("name", row.getString("name"));
            int row_id = (row.getString("opportunity_id").equals("false")) ? -1
                    : row.getInt("opportunity_id");
            extra.putInt("opportunity_id", row_id);
            row.put("caller_contact", callerNumber);
            if(callerWindow!=null) {
                callerWindow.show(dialed, row);
                mPref.setBoolean(KEY_ACTIVITY_STARTED, false);
            }
        }
    }
}
