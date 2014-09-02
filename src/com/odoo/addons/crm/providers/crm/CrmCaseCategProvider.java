package com.odoo.addons.crm.providers.crm;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.crm.model.CRMLead;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class CrmCaseCategProvider extends OContentProvider {
	public static String AUTHORITY = "com.odoo.addons.crm.providers.crm";
	public static final String PATH = "crm_case_categ";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new CRMLead.CRMCaseCateg(context);
	}

	@Override
	public String authority() {
		return CrmCaseCategProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return CrmCaseCategProvider.PATH;
	}

	@Override
	public Uri uri() {
		return CrmCaseCategProvider.CONTENT_URI;
	}

}
