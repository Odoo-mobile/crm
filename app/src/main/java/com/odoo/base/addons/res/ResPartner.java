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
 * Created on 30/12/14 4:00 PM
 */
package com.odoo.base.addons.res;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.odoo.addons.sale.models.AccountPaymentTerm;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBlob;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.OText;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import org.json.JSONArray;

public class
        ResPartner extends OModel {
    public static final String AUTHORITY = "com.odoo.core.crm.provider.content.sync.res_partner";
    OColumn name = new OColumn("Name", OVarchar.class).setSize(100).setRequired();
    OColumn is_company = new OColumn("Is Company", OBoolean.class).setDefaultValue(false);
    OColumn image_small = new OColumn("Avatar", OBlob.class).setDefaultValue(false);
    OColumn street = new OColumn("Street", OVarchar.class).setSize(100);
    OColumn street2 = new OColumn("Street2", OVarchar.class).setSize(100);
    OColumn city = new OColumn("City", OVarchar.class);
    OColumn zip = new OColumn("Zip", OVarchar.class);
    OColumn website = new OColumn("Website", OVarchar.class).setSize(100);
    OColumn phone = new OColumn("Phone", OVarchar.class).setSize(15);
    OColumn mobile = new OColumn("Mobile", OVarchar.class).setSize(15);
    OColumn email = new OColumn("Email", OVarchar.class);
    OColumn company_id = new OColumn("Company", ResCompany.class, OColumn.RelationType.ManyToOne);
    OColumn parent_id = new OColumn("Related Company", ResPartner.class, OColumn.RelationType.ManyToOne)
            .addDomain("is_company", "=", true);
    OColumn country_id = new OColumn("Country", ResCountry.class, OColumn.RelationType.ManyToOne);
    OColumn customer = new OColumn("Customer", OBoolean.class).setDefaultValue("true");
    OColumn comment = new OColumn("Internal Note", OText.class);
    @Odoo.Functional(store = true, depends = {"parent_id"}, method = "storeCompanyName")
    OColumn company_name = new OColumn("Company Name", OVarchar.class).setSize(100)
            .setLocalColumn();
    OColumn large_image = new OColumn("Image", OBlob.class).setDefaultValue("false").setLocalColumn();

    OColumn partner_invoice_id = new OColumn("partner_invoice_id", OVarchar.class).setLocalColumn();
    OColumn partner_shipping_id = new OColumn("partner_shipping_id", OVarchar.class).setLocalColumn();
    OColumn pricelist_id = new OColumn("pricelist_id", OVarchar.class).setLocalColumn();
    OColumn fiscal_position = new OColumn("fiscal_position", OVarchar.class).setLocalColumn();
    OColumn payment_term = new OColumn("Payment Term", AccountPaymentTerm.class, OColumn.RelationType.ManyToOne).setLocalColumn();

    public ResPartner(Context context, OUser user) {
        super(context, "res.partner", user);
        setHasMailChatter(true);
    }

    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

    public String storeCompanyName(OValues value) {
        try {
            if (!value.getString("parent_id").equals("false")) {
                JSONArray parent_id = new JSONArray(value.getString("parent_id"));
                return parent_id.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getContact(Context context, int row_id) {
        ODataRow row = new ResPartner(context, null).browse(row_id);
        String contact;
        if (row.getString("mobile").equals("false")) {
            contact = row.getString("phone");
        } else {
            contact = row.getString("mobile");
        }
        return contact;
    }

    public String getAddress(ODataRow row) {
        String add = "";
        if (!row.getString("street").equals("false"))
            add += row.getString("street") + ", ";
        if (!row.getString("street2").equals("false"))
            add += "\n" + row.getString("street2") + ", ";
        if (!row.getString("city").equals("false"))
            add += row.getString("city");
        if (!row.getString("zip").equals("false"))
            add += " - " + row.getString("zip") + " ";
        return add;
    }

    public Uri liveSearchURI() {
        return uri().buildUpon().appendPath("live_searchable_customer").build();
    }
}
