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
 * Created on 13/1/15 4:59 PM
 */
package com.odoo.addons.phonecall.services;

import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.addons.phonecall.models.CRMPhoneCalls;
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

import odoo.ODomain;

public class PhoneCallSyncService extends OSyncService implements ISyncFinishListener {
    public static final String TAG = PhoneCallSyncService.class.getSimpleName();

    @Override
    public OSyncAdapter getSyncAdapter() {
        return new OSyncAdapter(getApplicationContext(), CRMPhoneCalls.class, this, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        if (adapter.getModel().getModelName().equals("crm.phonecall")) {
            ODomain domain = new ODomain();
            domain.add("user_id", "=", user.getUser_id());
            adapter.setDomain(domain).syncDataLimit(10);
            adapter.onSyncFinish(this);
        }
    }

    @Override
    public OSyncAdapter performNextSync(OUser user, SyncResult syncResult) {
        CRMPhoneCalls crmPhoneCalls = new CRMPhoneCalls(getApplicationContext(), user);
        List<ODataRow> rows = crmPhoneCalls.select(null, "has_reminder = ? ", new String[]{"false"});
        int count = 0;
        for (ODataRow row : rows) {
            Date start_date = ODateUtils.createDateObject(row.getString("date"),
                    ODateUtils.DEFAULT_FORMAT, false);
            Date now = new Date();

            if (now.compareTo(start_date) < 0) {
                Bundle extra = row.getPrimaryBundleData();
                extra.putString(ReminderUtils.KEY_REMINDER_TYPE, "phonecall");
                if (ReminderUtils.get(getApplicationContext()).setReminder(start_date, extra)) {
                    OValues values = new OValues();
                    values.put("_is_dirty", "false");
                    values.put("has_reminder", "true");
                    crmPhoneCalls.update(row.getInt(OColumn.ROW_ID), values);
                    count++;
                }
            }
        }
        Log.i(TAG, count + " reminder updated");
        return null;
    }
}
