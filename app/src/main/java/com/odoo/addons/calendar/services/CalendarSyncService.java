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
 * Created on 9/1/15 3:57 PM
 */
package com.odoo.addons.calendar.services;

import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.addons.calendar.models.CalendarEvent;
import com.odoo.core.account.BaseSettings;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.service.ISyncFinishListener;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.reminder.ReminderUtils;

import java.util.Date;
import java.util.List;

public class CalendarSyncService extends OSyncService implements ISyncFinishListener {
    public static final String TAG = CalendarSyncService.class.getSimpleName();
    public static final int SYNC_SLEEP_DELAY = 2000;

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, CalendarEvent.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        if (adapter.getModel().getModelName().equals("calendar.event")) {
            adapter.onSyncFinish(this).syncDataLimit(50);
        }
    }


    @Override
    public OSyncAdapter performNextSync(OUser user, SyncResult syncResult) {
        // Setting reminders to events
        CalendarEvent event = new CalendarEvent(getApplicationContext(), null);
        List<ODataRow> rows = event.select();
        int count = 0;
        for (ODataRow row : rows) {
            if (row.getBoolean("allday")) {
                String defaultTime = BaseSettings.getDayStartTime(getApplicationContext());
                row.put("date_start", row.getString("date_start") + " " + defaultTime);
            }
            Date start_date = ODateUtils.createDateObject(row.getString("date_start"),
                    ODateUtils.DEFAULT_FORMAT, false);
            Date now = new Date();

            if (now.compareTo(start_date) < 0) {
                Bundle extra = row.getPrimaryBundleData();
                extra.putString(ReminderUtils.KEY_REMINDER_TYPE, "event");
                if (ReminderUtils.get(getApplicationContext()).resetReminder(start_date, extra)) {
                    OValues values = new OValues();
                    values.put("_is_dirty", "false");
                    values.put("has_reminder", "true");
                    event.update(row.getInt(OColumn.ROW_ID), values);
                    count++;
                }
            }
        }
        Log.i(TAG, count + " reminder updated");
        return null;
    }
}
