package com.odoo.addons.crm.providers.crm;

import com.odoo.support.provider.OContentProvider;

public class CRMProvider extends OContentProvider {
	public static String CONTENTURI = "com.odoo.addons.crm.providers.crm.CRMProvider";
	public static String AUTHORITY = "com.odoo.addons.crm.providers.crm";

	@Override
	public String contentUri() {
		return CONTENTURI;
	}

	@Override
	public String authority() {
		return AUTHORITY;
	}

}
