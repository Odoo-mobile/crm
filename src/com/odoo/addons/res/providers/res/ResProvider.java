package com.odoo.addons.res.providers.res;

import com.odoo.support.provider.OContentProvider;

public class ResProvider extends OContentProvider{

	public static final String CONTENTURI = "com.odoo.addons.res.providers.ResPartnerProvider";
	public static final String AUTHORITY = "com.odoo.addons.res.providers";
	
	@Override
	public String authority() {
		return AUTHORITY;
	}

	@Override
	public String contentUri() {
		return CONTENTURI;
	}

}
