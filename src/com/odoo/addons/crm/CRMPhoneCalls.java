package com.odoo.addons.crm;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OList;
import odoo.controls.OList.OnRowClickListener;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class CRMPhoneCalls extends BaseFragment implements OnPullListener,
		OnRowClickListener {

	public static final String TAG = CRMPhoneCalls.class.getSimpleName();

	enum PhoneKeys {
		SchduledLoggedcalls
	}

	View mView = null;
	OList mListControl = null;
	List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	PhoneKeys mCurrentKey = PhoneKeys.SchduledLoggedcalls;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(this);
		mView = inflater
				.inflate(R.layout.common_list_control, container, false);
		init();
		return mView;
	}

	public void init() {
		checkArguments();
		mListControl = (OList) mView.findViewById(R.id.crm_listRecords);
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListControl, this);
		mListControl.setOnRowClickListener(this);
		mDataLoader = new DataLoader();
		mDataLoader.execute();
	}

	private void checkArguments() {
		Bundle arg = getArguments();
		mCurrentKey = PhoneKeys.valueOf(arg.getString("crmphone"));
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
				PhoneKeys.SchduledLoggedcalls), 0,
				object(PhoneKeys.SchduledLoggedcalls)));
		return menu;
	}

	private int count(Context context, PhoneKeys key) {
		int count = 0;
		count = new CRMPhoneCall(context).count();
		return count;
	}

	private Fragment object(PhoneKeys value) {
		CRMPhoneCalls crmCalls = new CRMPhoneCalls();
		Bundle args = new Bundle();
		args.putString("crmphone", value.toString());
		crmCalls.setArguments(args);
		return crmCalls;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_crm, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_crm_detail_create) {
			CRMPhoneDetail crmPhoneDeatil = new CRMPhoneDetail();
			Bundle bundle = new Bundle();
			bundle.putString("key", mCurrentKey.toString());
			crmPhoneDeatil.setArguments(bundle);
			startFragment(crmPhoneDeatil, true);
		}
		return super.onOptionsItemSelected(item);
	}

	class DataLoader extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (db().isEmptyTable()) {
						Bundle args = new Bundle();
						args.putString("crmphone", "pull_callLog");
						scope.main().requestSync(CRMProvider.AUTHORITY, args);
					}
					mListRecords.clear();
					mListRecords.addAll(db().select());
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mListControl.setCustomView(R.layout.crm_phone_custom_layout);
			mListControl.initListControl(mListRecords);
			OControls.setGone(mView, R.id.loadingProgress);
		}

	}

	@Override
	public void onPullStarted(View arg0) {
		Bundle args = new Bundle();
		args.putString("crmphone", "pull_callLog");
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

	@Override
	public void onRowItemClick(int position, View view, ODataRow row) {
		CRMPhoneDetail crmPhoneDetail = new CRMPhoneDetail();
		Bundle bundle = new Bundle();
		bundle.putAll(row.getPrimaryBundleData());
		crmPhoneDetail.setArguments(bundle);
		startFragment(crmPhoneDetail, true);
	}

}
