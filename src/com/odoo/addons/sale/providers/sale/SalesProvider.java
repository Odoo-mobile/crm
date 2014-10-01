package com.odoo.addons.sale.providers.sale;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class SalesProvider extends OContentProvider {
	public static String AUTHORITY = "com.odoo.addons.sale.providers.sale";
	public static final String PATH = "sale_order";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new SaleOrder(context);
	}

	@Override
	public String authority() {
		return SalesProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return SalesProvider.PATH;
	}

	@Override
	public Uri uri() {
		return SalesProvider.CONTENT_URI;
	}
}
