package com.odoo.base.addons.ir;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

public class IrModuleCategory extends OModel {

    OColumn name = new OColumn("Application ", OVarchar.class).setSize(100);

    public IrModuleCategory(Context context, OUser user) {
        super(context, "ir.module.category", user);
    }

    @Override
    public boolean allowDeleteRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowUpdateRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowCreateRecordOnServer() {
        return false;
    }

}
