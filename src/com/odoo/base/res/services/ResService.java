package com.odoo.base.res.services;

import android.os.Bundle;

import com.odoo.base.res.ResPartner;
import com.odoo.support.OUser;
import com.odoo.support.service.OSyncAdapter;
import com.odoo.support.service.OSyncService;

public class ResService extends OSyncService {

	public static final String TAG = ResService.class.getSimpleName();

	@Override
	public OSyncAdapter getSyncAdapter() {
		return new OSyncAdapter(getApplicationContext(), new ResPartner(
				getApplicationContext()), this, true).syncDataLimit(10);
	}

	@Override
	public void performDataSync(OSyncAdapter adapter, Bundle extras, OUser user) {

	}

	// @Override
	// public Service getService() {
	// return this;
	// }
	//
	// @Override
	// public void performSync(Context context, OUser user, Account account,
	// Bundle extras, String authority, ContentProviderClient provider,
	// SyncResult syncResult) {
	// Log.v(TAG, "ResPartnerService:performSync()");
	// try {
	// OSyncHelper sync = null;
	// Intent intent = new Intent();
	// intent.setAction(SyncFinishReceiver.SYNC_FINISH);
	// ResPartner db = new ResPartner(context);
	// sync = db.getSyncHelper();
	// if (sync.syncWithServer())
	// context.sendBroadcast(intent);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

}
