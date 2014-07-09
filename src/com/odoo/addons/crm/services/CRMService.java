package com.odoo.addons.crm.services;

import android.accounts.Account;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.addons.crm.model.CRMPhoneCall;
import com.odoo.addons.crm.model.CRMLead;
import com.odoo.orm.OSyncHelper;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.service.OService;

public class CRMService extends OService {

	public static final String TAG = CRMService.class.getSimpleName();

	@Override
	public android.app.Service getService() {
		return this;
	}

	@Override
	public void performSync(Context context, Account account, Bundle extras,
			String authority, ContentProviderClient provider,
			SyncResult syncResult) {
		Log.v(TAG, "CRMService:performSync()");
		try {
			OSyncHelper sync = null;
			Intent intent = new Intent();
			intent.setAction(SyncFinishReceiver.SYNC_FINISH);
			if (extras != null) {
				if (extras.containsKey("crmcall")) {
					CRMPhoneCall db = new CRMPhoneCall(context);
					sync = db.getSyncHelper();
				} else {
					CRMLead db = new CRMLead(context);
					sync = db.getSyncHelper();
				}
			}
			if (sync.syncWithServer())
				context.sendBroadcast(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
