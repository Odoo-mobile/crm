package com.odoo.base.addons.ir;

import android.content.Context;

import com.odoo.base.addons.res.ResGroups;
import com.odoo.core.account.setup.utils.OdooSetup;
import com.odoo.core.account.setup.utils.Priority;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import java.util.ArrayList;
import java.util.List;

import odoo.helper.ODomain;

@OdooSetup.Model(Priority.LOW)
public class IrModelData extends OModel {

    OColumn name = new OColumn("Name", OVarchar.class).setSize(150);
    OColumn model = new OColumn("Model", OVarchar.class).setSize(100);
    OColumn res_id = new OColumn("Resource ID", OInteger.class);
    OColumn module = new OColumn("Module", OVarchar.class).setSize(100);

    public IrModelData(Context context, OUser user) {
        super(context, "ir.model.data", user);
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

        List<String> models = new ArrayList<>();
        List<Integer> serverIds = new ArrayList<>();

        // Adding group model and server ids
        ResGroups groups = new ResGroups(getContext(), getUser());
        models.add(groups.getModelName());
        serverIds.addAll(groups.getServerIds());


        // Passing models and server ids to domain
        domain.add("model", "in", models);
        domain.add("res_id", "in", serverIds);

        return domain;
    }

}