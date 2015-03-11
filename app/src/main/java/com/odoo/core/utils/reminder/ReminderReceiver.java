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
 * Created on 9/1/15 6:15 PM
 */
package com.odoo.core.utils.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.odoo.addons.calendar.EventDetail;
import com.odoo.addons.calendar.models.CalendarEvent;
import com.odoo.addons.crm.CRMDetail;
import com.odoo.addons.crm.models.CRMLead;
import com.odoo.addons.phonecall.PhoneCallDetail;
import com.odoo.addons.phonecall.models.CRMPhoneCalls;
import com.odoo.base.addons.res.ResCurrency;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.notification.ONotificationBuilder;
import com.odoo.R;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String TAG = ReminderReceiver.class.getSimpleName();
    public static final int REQUEST_EVENT_REMINDER = 12345;
    public static final int REQUEST_PHONE_CALL_REMINDER = 12346;
    public static final String ACTION_EVENT_REMINDER_DONE = "action_event_reminder_done";
    public static final String ACTION_EVENT_REMINDER_RE_SCHEDULE = "action_event_reminder_re_schedule";
    public static final String ACTION_PHONE_CALL_REMINDER_CALLBACK = "action_phone_call_reminder_callback";
    public static final String ACTION_PHONE_CALL_REMINDER_DONE = "action_phone_call_reminder_done";
    public static final String ACTION_PHONE_CALL_REMINDER_RE_SCHEDULE = "action_phone_call_reminder_re_schedule";

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra(ReminderUtils.KEY_REMINDER_TYPE);
        showNotification(context, type, intent.getExtras());
    }

    private void showNotification(Context context, String type, Bundle data) {
        ONotificationBuilder builder = new ONotificationBuilder(context, data.getInt(OColumn.ROW_ID));
        Class<?> resultClass = null;
        int icon = R.drawable.ic_action_event;
        ODataRow record = null;
        data.putString("type", type);
        if (type.equals("event")) {
            resultClass = EventDetail.class;
            CalendarEvent event = new CalendarEvent(context, null);
            int row_id = data.getInt(OColumn.ROW_ID);
            record = event.browse(new String[]{"name", "description", "location"}, row_id);
            if (record != null) {
                if (record.getString("description").equals("false")) {
                    record.put("description", record.getString("name"));
                }
            }
            ONotificationBuilder.NotificationAction actionDone =
                    new ONotificationBuilder.NotificationAction(
                            R.drawable.ic_action_action_done_all,
                            OResource.string(context, R.string.label_mark_done),
                            REQUEST_EVENT_REMINDER,
                            ACTION_EVENT_REMINDER_DONE,
                            EventDetail.class,
                            data
                    );
            builder.addAction(actionDone);
            Bundle reScheduleData = data;
            reScheduleData.putBoolean(EventDetail.KEY_RESCHEDULE, true);
            ONotificationBuilder.NotificationAction actionReSchedule =
                    new ONotificationBuilder.NotificationAction(
                            R.drawable.ic_action_time_clock,
                            OResource.string(context, R.string.label_re_schedule),
                            REQUEST_EVENT_REMINDER,
                            ACTION_EVENT_REMINDER_RE_SCHEDULE,
                            EventDetail.class,
                            reScheduleData
                    );
            builder.addAction(actionReSchedule);

        }
        if (type.equals("phonecall")) {
            icon = R.drawable.ic_action_call_logs;
            resultClass = PhoneCallDetail.class;
            CRMPhoneCalls phoneCalls = new CRMPhoneCalls(context, null);
            int row_id = data.getInt(OColumn.ROW_ID);
            record = phoneCalls.browse(new String[]{"name", "description", "partner_id"}, row_id);
            if (record != null) {
                if (record.getString("description").equals("false")) {
                    record.put("description", record.getString("name"));
                }
                ResPartner partner = new ResPartner(context, null);
                data.putString("contact", partner.getContact(context, record.getInt("partner_id")));
            }
            ONotificationBuilder.NotificationAction actionCallBack =
                    new ONotificationBuilder.NotificationAction(
                            R.drawable.ic_action_phone,
                            "Call back",
                            REQUEST_PHONE_CALL_REMINDER,
                            ACTION_PHONE_CALL_REMINDER_CALLBACK,
                            PhoneCallDetail.class,
                            data
                    );
            builder.addAction(actionCallBack);
            ONotificationBuilder.NotificationAction actionDone =
                    new ONotificationBuilder.NotificationAction(
                            R.drawable.ic_action_action_done_all,
                            OResource.string(context, R.string.label_mark_done),
                            REQUEST_PHONE_CALL_REMINDER,
                            ACTION_PHONE_CALL_REMINDER_DONE,
                            PhoneCallDetail.class,
                            data
                    );
            builder.addAction(actionDone);
            ONotificationBuilder.NotificationAction actionReSchedule =
                    new ONotificationBuilder.NotificationAction(
                            R.drawable.ic_action_time_clock,
                            OResource.string(context, R.string.label_re_schedule),
                            REQUEST_PHONE_CALL_REMINDER,
                            ACTION_PHONE_CALL_REMINDER_RE_SCHEDULE,
                            PhoneCallDetail.class,
                            data
                    );
            builder.addAction(actionReSchedule);

        }
        if (type.equals("opportunity")) {
            boolean reminderOnExpiryDate = data.getBoolean("expiry_date");
            icon = R.drawable.ic_action_opportunities;
            resultClass = CRMDetail.class;
            CRMLead lead = new CRMLead(context, null);
            int row_id = data.getInt(OColumn.ROW_ID);
            record = lead.browse(row_id);
            String desc = record.getString("planned_revenue") + " "
                    + ResCurrency.getSymbol(context, record.getInt("company_currency")) +
                    " at " + record.getString("probability") + " %";
            if (!record.getString("title_action").equals("false")) {
                desc += "\n" + record.getString("title_action");
            }
            record.put("description", desc);
            //FIXME: Add reminder actions
        }
        if (record != null) {
            builder.setAutoCancel(true);
            builder.setIcon(icon);
            builder.setTitle(record.getString("name"));
            builder.setBigText(record.getString("description"));

            Intent resultIntent = new Intent(context, resultClass);
            resultIntent.putExtras(data);
            builder.setResultIntent(resultIntent);
            builder.build().show();
        }
    }

}
