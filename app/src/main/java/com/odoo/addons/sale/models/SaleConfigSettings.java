package com.odoo.addons.sale.models;

import android.content.Context;

import com.odoo.core.account.setup.utils.OdooSetup;
import com.odoo.core.account.setup.utils.Priority;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.support.OUser;

@OdooSetup.Model(Priority.CONFIGURATION)
public class SaleConfigSettings extends OModel {

    OColumn group_multi_salesteams = new OColumn("Manage Sales Teams", OBoolean.class)
            .setDefaultValue(false);

    public SaleConfigSettings(Context context, OUser user) {
        super(context, "sale.config.settings", user);
    }

}
