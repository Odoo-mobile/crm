package com.odoo.addons.sale.providers.sale;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class SalesOrderLineProvider extends OContentProvider {
	public static String AUTHORITY = "com.odoo.addons.sale.providers.sale";
	public static final String PATH = "sale_order";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new SaleOrder.SalesOrderLine(context);
	}

	@Override
	public String authority() {
		return SalesOrderLineProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return SalesOrderLineProvider.PATH;
	}

	@Override
	public Uri uri() {
		return SalesOrderLineProvider.CONTENT_URI;
	}
}
