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
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.notification.ONotificationBuilder;
import com.odoo.crm.R;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String TAG = ReminderReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String type = intent.getStringExtra(ReminderUtils.KEY_REMINDER_TYPE);
        showNotification(context, type, intent.getExtras());
    }

    private void showNotification(Context context, String type, Bundle data) {
        if (type.equals("event")) {
            CalendarEvent event = new CalendarEvent(context, null);
            int row_id = data.getInt(OColumn.ROW_ID);
            ODataRow record = event.browse(new String[]{"name", "description", "location"}, row_id);
            if (record != null) {
                if (record.getString("description").equals("false")) {
                    record.put("description", record.getString("name"));
                }
                ONotificationBuilder builder = new ONotificationBuilder(context, 0);
                builder.setAutoCancel(true);
                builder.setIcon(R.drawable.ic_action_event);
                builder.setTitle(record.getString("name"));
                builder.setText(record.getString("description"));
                builder.setBigText(record.getString("description"));

                Intent resultIntent = new Intent(context, EventDetail.class);
                resultIntent.putExtras(data);
                builder.setResultIntent(resultIntent);
                builder.build().show();
            }
        }
    }

}
