package com.odoo.addons.sale.providers.sale;

import com.odoo.support.provider.OContentProvider;

public class SalesProvider extends OContentProvider{
	public static String CONTENTURI = "com.odoo.addons.sale.providers.sale.SalesProvider";
	public static String AUTHORITY = "com.odoo.addons.sale.providers.sale";
	
	@Override
	public String authority() {
		return AUTHORITY;
	}

	@Override
	public String contentUri() {
		// TODO Auto-generated method stub
		return CONTENTURI;
	}

}
