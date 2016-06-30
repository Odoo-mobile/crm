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
 * Created on 13/1/15 10:16 AM
 */
package com.odoo.base.addons.res;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.odoo.core.account.setup.utils.OdooSetup;
import com.odoo.core.account.setup.utils.Priority;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import odoo.helper.ODomain;

@OdooSetup.Model(Priority.HIGH)
public class ResUsers extends OModel {
    public static final String TAG = ResUsers.class.getSimpleName();

    OColumn name = new OColumn("Name", OVarchar.class);
    OColumn login = new OColumn("User Login name", OVarchar.class);
    OColumn groups_id = new OColumn("Groups", ResGroups.class, OColumn.RelationType.ManyToMany)
            .setRelTableName("res_groups_users_rel")
            .setRelBaseColumn("uid")
            .setRelRelationColumn("gid");

    @Override
    public boolean checkForCreateDate() {
        return false;
    }

    @Override
    public boolean checkForWriteDate() {
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
    public boolean allowDeleteRecordInLocal() {
        return false;
    }

    public ResUsers(Context context, OUser user) {
        super(context, "res.users", user);
    }

    public static int myId(Context context) {
        ResUsers users = new ResUsers(context, null);
        return users.selectRowId(users.getUser().getUserId());
    }

    @Override
    public ODomain defaultDomain() {
        ODomain domain = super.defaultDomain();
        domain.add("id", "=", getUser().getUserId());
        return domain;
    }

    public boolean hasGroup(int user_server_id, String group_xml_id) {
        String sql = "SELECT count(*) as total FROM res_groups_users_rel WHERE uid = ? AND gid IN ";
        sql += "(SELECT _id FROM res_groups where id IN (SELECT res_id FROM ir_model_data WHERE module = ? AND name = ?))";
        String[] xml_ids = group_xml_id.split("\\.");
        Cursor cr = execute(sql, new String[]{selectRowId(user_server_id) + "", xml_ids[0], xml_ids[1]});
        cr.moveToFirst();
        int total = cr.getInt(0);
        cr.close();
        Log.v(TAG, "User group : " + group_xml_id + "=" + total);
        return total > 0;
    }
}
