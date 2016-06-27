/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 13/1/15 10:15 AM
 */
package com.odoo.addons.crm.models;

import android.content.Context;

import com.odoo.base.addons.ir.IrModel;
import com.odoo.core.account.setup.utils.OdooSetup;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

@OdooSetup.Model
public class CRMCaseCateg extends OModel {
    public static final String TAG = CRMCaseCateg.class.getSimpleName();
    OColumn name = new OColumn("Name", OVarchar.class);

    OColumn object_id = new OColumn("Model", IrModel.class, OColumn.RelationType.ManyToOne);

    public CRMCaseCateg(Context context, OUser user) {
        super(context, "crm.case.categ", user);
        if (getOdooVersion() != null) {
            int version = getOdooVersion().getVersionNumber();
            String serie = getOdooVersion().getServerSerie();
            if (version >= 9 || serie.equals("8.saas~6")) {
                setModelName("crm.lead.tag");
            }
        }
    }
}
