package com.odoo.addons.crm;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OList;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.addons.crm.model.CRMPhoneCall;
import com.odoo.addons.crm.providers.crm.CRMProvider;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class CRMPhoneCalls extends BaseFragment implements  OnPullListener{

	public static final String TAG = CRMPhoneCalls.class.getSimpleName();

	enum Keys {
		SchduledLoggedcalls
	}

	View mView = null;
	OList mListControl = null;
	List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	Keys mCurrentKey = Keys.SchduledLoggedcalls;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		scope = new AppScope(this);
		mView = inflater.inflate(R.layout.crm_layout, container, false);
		init();
		return mView;
	}

	public void init() {
		checkArguments();
		mListControl = (OList) mView.findViewById(R.id.crm_listRecords);
		mTouchListener = scope.main().getTouchAttacher();
		 mTouchListener.setPullableView(mListControl, this);
		// mListControl.setOnRowClickListener(this);
		mDataLoader = new DataLoader();
		mDataLoader.execute();
	}

	private void checkArguments() {
		Bundle arg = getArguments();
		mCurrentKey = Keys.valueOf(arg.getString("crmcall"));
	}

	@Override
	public Object databaseHelper(Context context) {
		return new CRMPhoneCall(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();

		menu.add(new DrawerItem(TAG, "Phone Call", true));
		menu.add(new DrawerItem(TAG, "Logged Calls", count(context,
				Keys.SchduledLoggedcalls), 0, object(Keys.SchduledLoggedcalls)));
		return menu;
	}

	private int count(Context context, Keys key) {
		int count = 0;
		switch (key) {
		case SchduledLoggedcalls:
			count = new CRMPhoneCall(context).count();
			break;
		default:
			break;
		}
		return count;
	}

	private Fragment object(Keys value) {
		CRMPhoneCalls crmCalls = new CRMPhoneCalls();
		Bundle args = new Bundle();
		args.putString("crmcall", value.toString());
		crmCalls.setArguments(args);
		return crmCalls;
	}

	class DataLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (db().isEmptyTable()) {
						scope.main().requestSync(CRMProvider.AUTHORITY);
					}
					mListRecords.clear();
					switch (mCurrentKey) {
					case SchduledLoggedcalls:
						mListRecords.addAll(db().select());
						break;
					}
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			switch (mCurrentKey) {
			case SchduledLoggedcalls:
				mListControl.setCustomView(R.layout.crm_view);
				break;
			}
			mListControl.initListControl(mListRecords);
			OControls.setGone(mView, R.id.loadingProgress);
		}

	}

	@Override
	public void onPullStarted(View arg0) {
		Bundle args = new Bundle();
		args.putString("crmcall", "pull_callLog");
		scope.main().requestSync(CRMProvider.AUTHORITY, args);
	}

	@Override
	public void onResume() {
		super.onResume();
		scope.main().registerReceiver(mSyncFinishReceiver,
				new IntentFilter(SyncFinishReceiver.SYNC_FINISH));
	}

	@Override
	public void onPause() {
		super.onPause();
		scope.main().unregisterReceiver(mSyncFinishReceiver);
	}

	SyncFinishReceiver mSyncFinishReceiver = new SyncFinishReceiver() {
		@Override
		public void onReceive(Context context, android.content.Intent intent) {
			scope.main().refreshDrawer(TAG);
			mTouchListener.setPullComplete();
			if (mDataLoader != null) {
				mDataLoader.cancel(true);
			}
			mDataLoader = new DataLoader();
			mDataLoader.execute();
		}
	};


}
