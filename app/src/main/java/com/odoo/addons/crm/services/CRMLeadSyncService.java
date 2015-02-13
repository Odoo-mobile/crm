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
 * Created on 13/1/15 11:31 AM
 */
package com.odoo.addons.crm.services;

import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.addons.crm.CRMLeads;
import com.odoo.addons.crm.models.CRMCaseStage;
import com.odoo.addons.crm.models.CRMLead;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.service.ISyncFinishListener;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

import odoo.ODomain;

public class CRMLeadSyncService extends OSyncService implements ISyncFinishListener {
    public static final String TAG = CRMLeadSyncService.class.getSimpleName();
    private Context mContext;
    private OSyncService service;

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        mContext = context;
        this.service = service;
        return new OSyncAdapter(context, CRMLead.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        if (adapter.getModel().getModelName().equals("crm.lead")) {
            ODomain domain = new ODomain();
            if (extras.containsKey(CRMLeads.KEY_IS_LEAD)) {
                domain.add("type", "=",
                        (extras.getBoolean(CRMLeads.KEY_IS_LEAD)) ? "lead" : "opportunity");
                adapter.setDomain(domain);
                Log.d(TAG, "Setting lead filter type");
            }
            adapter.onSyncFinish(this).syncDataLimit(50);
        }
    }

    @Override
    public OSyncAdapter performNextSync(OUser user, SyncResult syncResult) {
        CRMLead crmLead = new CRMLead(mContext, user);
        for (ODataRow row : crmLead.select(new String[]{})) {
            crmLead.setReminder(row.getInt(OColumn.ROW_ID));
        }
        return new OSyncAdapter(mContext, CRMCaseStage.class, service, true);
    }
}
