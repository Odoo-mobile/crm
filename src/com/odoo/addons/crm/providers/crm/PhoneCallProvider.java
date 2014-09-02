package com.odoo.addons.crm.providers.crm;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.crm.model.CRMPhoneCall;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class PhoneCallProvider extends OContentProvider {
	public static String AUTHORITY = "com.odoo.addons.crm.providers.crm";
	public static final String PATH = "crm_phonecall";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new CRMPhoneCall(context);
	}

	@Override
	public String authority() {
		return PhoneCallProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return PhoneCallProvider.PATH;
	}

	@Override
	public Uri uri() {
		return PhoneCallProvider.CONTENT_URI;
	}

}
