package com.odoo.addons.crm.providers.crm;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.crm.model.CRMLead;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class CrmPaymentModeProvider extends OContentProvider {
	public static String AUTHORITY = "com.odoo.addons.crm.providers.crm";
	public static final String PATH = "crm_payment_mode";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new CRMLead.CRMPaymentMode(context);
	}

	@Override
	public String authority() {
		return CrmPaymentModeProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return CrmPaymentModeProvider.PATH;
	}

	@Override
	public Uri uri() {
		return CrmPaymentModeProvider.CONTENT_URI;
	}

}
