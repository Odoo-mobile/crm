package com.odoo.addons.crm.services;

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
	}
	
	// @Override
	// public android.app.Service getService() {
	// return this;
	// }
	//
	// @Override
	// public void performSync(Context context, OUser user, Account account,
	// Bundle extras, String authority, ContentProviderClient provider,
	// SyncResult syncResult) {
	// Log.v(TAG, "CRMService:performSync()");
	// try {
	// OSyncHelper sync = null;
	// Intent intent = new Intent();
	// intent.setAction(SyncFinishReceiver.SYNC_FINISH);
	// if (extras != null) {
	// if (extras.containsKey("crmcall")) {
	// CRMPhoneCall db = new CRMPhoneCall(context);
	// sync = db.getSyncHelper();
	// } else {
	// CRMLead db = new CRMLead(context);
	// sync = db.getSyncHelper();
	// }
	// }
	// if (sync.syncWithServer())
	// context.sendBroadcast(intent);
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
}
