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
 * Created on 13/1/15 11:40 AM
 */
package com.odoo.addons.sale.services;

import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.odoo.addons.sale.models.AccountPaymentTerm;
import com.odoo.addons.sale.models.SaleOrder;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.service.ISyncFinishListener;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.service.OSyncService;
import com.odoo.core.support.OUser;

import java.util.ArrayList;
import java.util.List;

import odoo.ODomain;

public class SaleOrderSyncService extends OSyncService implements ISyncFinishListener {
    public static final String TAG = SaleOrderSyncService.class.getSimpleName();
    public Boolean firstSync = false;

    @Override
    public OSyncAdapter getSyncAdapter(OSyncService service, Context context) {
        return new OSyncAdapter(context, SaleOrder.class, service, true);
    }

    @Override
    public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
        if (adapter.getModel().getModelName().equals("sale.order")) {
            ODomain domain = new ODomain();
            SaleOrder saleOrder = new SaleOrder(getApplicationContext(), user);
            List<Integer> newIds = new ArrayList<>();
            for (ODataRow row : saleOrder.select(new String[]{}, "name = ? and id != ?", new String[]{"/", "0"})) {
                newIds.add(row.getInt("id"));
            }
            if (newIds.size() > 0) {
                domain.add("id", "in", newIds);
            }
            if (!firstSync)
                adapter.onSyncFinish(this);
            domain.add("user_id", "=", user.getUser_id());
            adapter.setDomain(domain).syncDataLimit(50);
        }
        if (adapter.getModel().getModelName().equals("account.payment.term")) {
            adapter.onSyncFinish(syncFinishListener);
        }
    }

    @Override
    public OSyncAdapter performNextSync(OUser user, SyncResult syncResult) {
        return new OSyncAdapter(getApplicationContext(), AccountPaymentTerm.class, SaleOrderSyncService.this, true);
    }

    ISyncFinishListener syncFinishListener = new ISyncFinishListener() {
        @Override
        public OSyncAdapter performNextSync(OUser user, SyncResult syncResult) {
            firstSync = true;
            return new OSyncAdapter(getApplicationContext(), SaleOrder.class, SaleOrderSyncService.this, true);
        }
    };
}
