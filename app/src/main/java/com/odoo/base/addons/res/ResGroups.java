package com.odoo.base.addons.res;

import android.content.Context;

import com.odoo.base.addons.ir.IrModuleCategory;
import com.odoo.core.account.setup.utils.OdooSetup;
import com.odoo.core.account.setup.utils.Priority;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import odoo.helper.ODomain;

@OdooSetup.Model(Priority.MEDIUM)
public class ResGroups extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class).setSize(100);
    OColumn category_id = new OColumn("Application", IrModuleCategory.class, OColumn.RelationType.ManyToOne);

    OColumn users = new OColumn("Users", ResUsers.class, OColumn.RelationType.ManyToMany)
            .setRelTableName("res_groups_user_rel")
            .setRelBaseColumn("gid")
            .setRelRelationColumn("uid");

    public ResGroups(Context context, OUser user) {
        super(context, "res.groups", user);
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
    public boolean allowDeleteRecordOnServer() {
        return false;
    }

    @Override
    public ODomain defaultDomain() {
        IrModuleCategory category = new IrModuleCategory(getContext(), getUser());
        ODomain domain = super.defaultDomain();
        domain.add("users.id", "=", getUser().getUserId());
        domain.add("category_id", "in", category.getServerIds());
        return domain;
    }
}
