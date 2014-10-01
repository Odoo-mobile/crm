package com.odoo.addons.crm.services;

import odoo.ODomain;
import android.os.Bundle;

import com.odoo.addons.crm.model.CRMPhoneCall;
import com.odoo.support.OUser;
import com.odoo.support.service.OSyncAdapter;
import com.odoo.support.service.OSyncService;

public class CRMPhoneCallService extends OSyncService {

	public static final String TAG = CRMPhoneCallService.class.getSimpleName();

	@Override
	public OSyncAdapter getSyncAdapter() {
		return new OSyncAdapter(getApplicationContext(), new CRMPhoneCall(
				getApplicationContext()), this, true).syncDataLimit(10);
	}

	@Override
	public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {
		ODomain domain = new ODomain();
		domain.add("user_id","=",user.getUser_id());
		adapter.setDomain(domain);
	}

}
