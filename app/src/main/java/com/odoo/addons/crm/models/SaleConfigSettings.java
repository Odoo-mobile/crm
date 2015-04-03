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
 * Created on 30/3/15 4:00 PM
 */
package com.odoo.addons.crm.models;

import android.content.Context;

import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.support.OUser;

import java.util.List;

public class SaleConfigSettings extends OModel {
    public static final String TAG = SaleConfigSettings.class.getSimpleName();
    OColumn group_multi_salesteams = new OColumn("Manage Sales Teams", OBoolean.class);

    public SaleConfigSettings(Context context, OUser user) {
        super(context, "sale.config.settings", user);
    }

    public static Boolean showSaleTeams(Context context) {
        SaleConfigSettings settings = new SaleConfigSettings(context, null);
        List<ODataRow> records = settings.query("select max(id) as id,group_multi_salesteams from "
                + settings.getTableName());
        return records.get(0).getBoolean("group_multi_salesteams");
    }
}
