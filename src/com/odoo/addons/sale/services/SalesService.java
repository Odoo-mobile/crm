package com.odoo.addons.sale.services;

import android.accounts.Account;
import android.app.Service;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.orm.OSyncHelper;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.service.OService;
import com.odoo.support.service.OServiceListener;

public class SalesService extends OService implements OServiceListener {
	public static final String TAG = SalesService.class.getSimpleName();

	@Override
	public Service getService() {
		return this;
	}

	@Override
	public void performSync(Context context, Account account, Bundle extras,
			String authority, ContentProviderClient provider,
			SyncResult syncResult) {
		Log.v(TAG, "SalesService:performSync()");
		try {
			OSyncHelper sync = null;
			Intent intent = new Intent();
			intent.setAction(SyncFinishReceiver.SYNC_FINISH);
			SaleOrder db = new SaleOrder(context);
			sync = db.getSyncHelper();
			if (sync.syncWithServer())
				context.sendBroadcast(intent);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
