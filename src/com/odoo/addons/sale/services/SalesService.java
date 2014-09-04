package com.odoo.addons.sale.services;

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
	// Log.v(TAG, "SalesService:performSync()");
	// try {
	// OSyncHelper sync = null;
	// Intent intent = new Intent();
	// intent.setAction(SyncFinishReceiver.SYNC_FINISH);
	// SaleOrder db = new SaleOrder(context);
	// sync = db.getSyncHelper();
	// if (sync.syncWithServer())
	// context.sendBroadcast(intent);
	// // ResCurrency dbRes = new ResCurrency(context);
	// // sync = dbRes.getSyncHelper();
	// // if (sync.syncWithServer())
	// // context.sendBroadcast(intent);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

}
