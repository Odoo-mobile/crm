package com.odoo.base.addons.ir;

import android.content.Context;

import com.odoo.config.BaseConfig;
import com.odoo.core.account.setup.utils.OdooSetup;
import com.odoo.core.account.setup.utils.Priority;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import java.util.Arrays;

import odoo.helper.ODomain;

@OdooSetup.Model(Priority.HIGH)
public class IrModule extends OModel {

    OColumn name = new OColumn("Technical Name", OVarchar.class).setSize(100);
    OColumn shortdesc = new OColumn("Module", OVarchar.class).setSize(100);
    OColumn state = new OColumn("Status", OSelection.class)
            .addSelection("uninstallable", "Not Installable")
            .addSelection("uninstalled", "Not Installed")
            .addSelection("installed", "Installed")
            .addSelection("to upgrade", "To be upgraded")
            .addSelection("to remove", "To be removed")
            .addSelection("to install", "To be installed");

    OColumn category_id = new OColumn("Category", IrModuleCategory.class,
            OColumn.RelationType.ManyToOne);

    public IrModule(Context context, OUser user) {
        super(context, "ir.module.module", user);
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

    @Override
    public ODomain defaultDomain() {
        ODomain domain = super.defaultDomain();
        domain.add("name", "in", Arrays.asList(BaseConfig.DEPENDS_ON_MODULES));
        return domain;
    }

    public boolean isInstalled(String module) {
        ODataRow row = browse(null, "name = ?", new String[]{module});
        return (row != null && row.getString("state").equals("installed"));
    }
}
