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
 * Created on 13/1/15 10:12 AM
 */
package com.odoo.addons.crm.models;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class CRMCaseStage extends OModel {
    public static final String TAG = CRMCaseStage.class.getSimpleName();
    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn sequence = new OColumn("Sequence", OInteger.class);
    OColumn probability = new OColumn("Probability (%)", OFloat.class).setSize(20);
    OColumn case_default = new OColumn("Default to New Sales Team",
            OBoolean.class);
    OColumn type = new OColumn("Type", OVarchar.class);

    public CRMCaseStage(Context context, OUser user) {
        super(context, "crm.case.stage", user);
        if (getOdooVersion() != null) {
            int version = getOdooVersion().getVersion_number();
            String serieVersion = getOdooVersion().getServer_serie();
            if (serieVersion.equals("8.saas~6") || version >= 9) {
                setModelName("crm.stage");
            }
        }
    }

}
