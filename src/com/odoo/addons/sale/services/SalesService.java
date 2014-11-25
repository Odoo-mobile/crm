package com.odoo.addons.sale.services;

import odoo.ODomain;
import android.os.Bundle;

import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.support.OUser;
import com.odoo.support.service.OSyncAdapter;
import com.odoo.support.service.OSyncService;

public class SalesService extends OSyncService {
	public static final String TAG = SalesService.class.getSimpleName();

	@Override
	public OSyncAdapter getSyncAdapter() {
		return new OSyncAdapter(getApplicationContext(), new SaleOrder(
				getApplicationContext()), this, true);
	}

	@Override
	public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
		ODomain domain = new ODomain();
		domain.add("user_id", "=", user.getUser_id());
		adapter.setDomain(domain);
	}

}
