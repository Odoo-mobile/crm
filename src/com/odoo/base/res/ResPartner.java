/*
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
 */

package com.odoo.base.res;

import android.content.Context;

import com.odoo.addons.crm.model.CRMLead;
import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.annotations.Odoo.Functional;
import com.odoo.orm.types.OBlob;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;

/**
 * The Class Res_PartnerDBHelper.
 */
public class ResPartner extends OModel {
	Context mContext = null;

	OColumn name = new OColumn("Name", OText.class);
	OColumn is_company = new OColumn("Is Company", OBoolean.class)
			.setDefault(false);
	OColumn image_small = new OColumn("Image", OBlob.class).setDefault(false);
	OColumn street = new OColumn("Street", OText.class);
	OColumn street2 = new OColumn("Street2", OText.class);
	OColumn city = new OColumn("City", OText.class);
	OColumn zip = new OColumn("Zip", OVarchar.class, 10);
	OColumn website = new OColumn("Website", OText.class);
	OColumn phone = new OColumn("Phone", OText.class);
	OColumn mobile = new OColumn("Mobile", OText.class);
	OColumn email = new OColumn("Email", OText.class);
	OColumn company_id = new OColumn("Company", ResCompany.class,
			RelationType.ManyToOne).addDomain("is_company", "=", true);

	@Functional(method = "validationPhone")
	OColumn resPhone = new OColumn("Phone no", OText.class);
	@Functional(method = "validationEmail")
	OColumn resEmail = new OColumn("Email Id", OText.class);
	@Functional(method = "getSaleOrdersCount")
	OColumn salesOrdersCount = new OColumn("Total Sale Orders", OVarchar.class);
	@Functional(method = "getcrmLeadCount")
	OColumn crmLeadCount = new OColumn("Total Opportunities", OVarchar.class);
	@Functional(method = "resAddressFull")
	OColumn resAddress = new OColumn("Address", OText.class);

	public ResPartner(Context context) {
		super(context, "res.partner");
		mContext = context;
	}

	public String validationPhone(ODataRow row) {
		if (row.getString("phone") != null
				|| !row.getString("phone").equals(false))
			return row.getString("phone");
		else
			return "";
	}

	public String validationEmail(ODataRow row) {
		if (row.getString("email") != null
				|| !row.getString("email").equals(false))
			return row.getString("email");
		else
			return "";
	}

	public String getSaleOrdersCount(ODataRow row) {
		SaleOrder sale = new SaleOrder(mContext);
		int count = sale.count("partner_id = ? ",
				new Object[] { row.getInt(OColumn.ROW_ID) });
		if (count > 0)
			return count + " Sales";
		else
			return "";
	}

	public String getcrmLeadCount(ODataRow row) {
		CRMLead sale = new CRMLead(mContext);
		int count = sale.count("partner_id = ? and type = ?", new Object[] {
				row.getInt(OColumn.ROW_ID), "opportunity" });
		if (count > 0)
			return count + " Opportunities";
		else
			return "";
	}

	public String resAddressFull(ODataRow row) {
		String add = "";
		if (!row.getBoolean("street").equals(false))
			add = row.getString("street");
		if (!row.getBoolean("street2").equals(false))
			add = add + "\n" + row.getString("street2");
		if (!row.getBoolean("city").equals(false))
			add = add + row.getString("city");
		if (!row.getBoolean("zip").equals(false))
			add = add + row.getString("zip");
		return add;
	}
}
