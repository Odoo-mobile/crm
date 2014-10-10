package com.odoo.addons.crm.services;

import odoo.ODomain;
import android.os.Bundle;

import com.odoo.addons.crm.model.CRMLead;
import com.odoo.support.OUser;
import com.odoo.support.service.OSyncAdapter;
import com.odoo.support.service.OSyncService;

public class CRMService extends OSyncService {

	public static final String TAG = CRMService.class.getSimpleName();

	@Override
	public OSyncAdapter getSyncAdapter() {
		return new OSyncAdapter(getApplicationContext(), new CRMLead(
				getApplicationContext()), this, true);
	}

	@Override
	public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
		ODomain domain = new ODomain();
		domain.add("|");
		domain.add("user_id", "=", user.getUser_id());
		domain.add("user_id", "=", false);
		adapter.setDomain(domain);
	}
}
