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
 * Created on 31/3/15 12:10 PM
 */
package com.odoo.addons.crm.models;

import android.content.Context;

import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class CrmCaseSection extends OModel {
    public static final String TAG = CrmCaseSection.class.getSimpleName();

    OColumn name = new OColumn("Sales Team", OVarchar.class);
    OColumn code = new OColumn("Code", OVarchar.class);
    OColumn invoiced_target = new OColumn("Invoice Target", OInteger.class).setDefaultValue(0);
    OColumn invoiced_forecast = new OColumn("Invoice Forecast", OInteger.class).setDefaultValue(0);
    OColumn user_id = new OColumn("Team Leader", ResUsers.class, OColumn.RelationType.ManyToOne);
    OColumn parent_id = new OColumn("Parent Team", CrmCaseSection.class,
            OColumn.RelationType.ManyToOne);
    OColumn stage_ids = new OColumn("Stages", CRMCaseStage.class, OColumn.RelationType.ManyToMany);
    OColumn member_ids = new OColumn("Team Memebers", ResUsers.class,
            OColumn.RelationType.ManyToMany);
    OColumn use_leads = new OColumn("Leads", OBoolean.class);
    OColumn use_opportunities = new OColumn("Opportunity", OBoolean.class);
    OColumn use_quotations = new OColumn("quotations", OBoolean.class);

    public CrmCaseSection(Context context, OUser user) {
        super(context, "crm.case.section", user);
    }

}
