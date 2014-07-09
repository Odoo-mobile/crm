package com.odoo.base.res;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OVarchar;

public class ResCurrency extends OModel {

	OColumn name = new OColumn("Name", OVarchar.class, 64);
	OColumn symbol = new OColumn("Symbol", OVarchar.class, 10);

	public ResCurrency(Context context) {
		super(context, "res.currency");
	}

}
