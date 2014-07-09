package com.odoo.base.res;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OVarchar;

public class ResCountry extends OModel {

	OColumn name = new OColumn("Name", OVarchar.class, 100);
	OColumn code = new OColumn("Country Code", OVarchar.class, 10);

	public ResCountry(Context context) {
		super(context, "res.country");
	}

}
