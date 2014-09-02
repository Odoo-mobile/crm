package com.odoo.addons.res.providers.res;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.res.ResPartner;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class ResProvider extends OContentProvider {

	public static final String AUTHORITY = "com.odoo.addons.res.providers";
	public static final String PATH = "res_partner";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new ResPartner(context);
	}

	@Override
	public String authority() {
		return ResProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return ResProvider.PATH;
	}

	@Override
	public Uri uri() {
		return ResProvider.CONTENT_URI;
	}
}
