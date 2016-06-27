package com.odoo.base.addons.ir;

import android.content.Context;

import com.odoo.base.addons.res.ResGroups;
import com.odoo.core.account.setup.utils.OdooSetup;
import com.odoo.core.account.setup.utils.Priority;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import odoo.helper.ODomain;

@OdooSetup.Model(Priority.MEDIUM)
public class IrModelAccess extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class).setSize(150);
    OColumn perm_read = new OColumn("Perm Read", OBoolean.class);
    OColumn perm_unlink = new OColumn("Perm Unlink", OBoolean.class);
    OColumn perm_write = new OColumn("Perm Write", OBoolean.class);
    OColumn perm_create = new OColumn("Perm Create", OBoolean.class);
    OColumn model_id = new OColumn("Model", IrModel.class, OColumn.RelationType.ManyToOne);
    OColumn group_id = new OColumn("Group", ResGroups.class, OColumn.RelationType.ManyToOne);

    public IrModelAccess(Context context, OUser user) {
        super(context, "ir.model.access", user);
    }

    @Override
    public boolean allowDeleteRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowCreateRecordOnServer() {
        return false;
    }

    @Override
    public boolean allowUpdateRecordOnServer() {
        return false;
    }

    @Override
    public ODomain defaultDomain() {
        ODomain domain = super.defaultDomain();
        domain.add("model_id", "in", new IrModel(getContext(), getUser()).getServerIds());
        domain.add("group_id", "in", new ResGroups(getContext(), getUser()).getServerIds());
        return domain;
    }
}
