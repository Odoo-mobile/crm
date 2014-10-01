package com.odoo.addons.sale.providers.sale;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.res.ResCurrency;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class ResCurrencyProvider extends OContentProvider {
	public static String AUTHORITY = "com.odoo.addons.sale.providers.sale.rescurrency";
	public static final String PATH = "res_currency";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new ResCurrency(context);
	}

	@Override
	public String authority() {
		return ResCurrencyProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return ResCurrencyProvider.PATH;
	}

	@Override
	public Uri uri() {
		return ResCurrencyProvider.CONTENT_URI;
	}
}
