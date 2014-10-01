package com.odoo.addons.partners.services;

import odoo.ODomain;
import android.os.Bundle;

import com.odoo.base.res.ResPartner;
import com.odoo.support.OUser;
import com.odoo.support.service.OSyncAdapter;
import com.odoo.support.service.OSyncService;

public class PartnerService extends OSyncService {

	public static final String TAG = PartnerService.class.getSimpleName();

	@Override
	public OSyncAdapter getSyncAdapter() {
		return new OSyncAdapter(getApplicationContext(), new ResPartner(
				getApplicationContext()), this, true);
	}

	@Override
	public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
		/**
		 * Restricting all partner sync.
		 */
		ODomain domain = new ODomain();
		domain.add("|");
		domain.add("opportunity_ids.user_id", "=", user.getUser_id());
		domain.add("sale_order_ids.user_id", "=", user.getUser_id());
		adapter.setDomain(domain);
	}

}
