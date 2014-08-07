package com.odoo.addons.res;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OList;
import odoo.controls.OList.BeforeListRowCreateListener;
import odoo.controls.OList.OnListBottomReachedListener;
import odoo.controls.OList.OnListRowViewClickListener;
import odoo.controls.OList.OnRowClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.addons.crm.CRM;
import com.odoo.addons.res.providers.res.ResProvider;
import com.odoo.addons.sale.Sales;
import com.odoo.base.res.ResPartner;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.odoo.util.logger.OLog;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class ResPartners extends BaseFragment implements OnPullListener,
		OnRowClickListener, OnListBottomReachedListener,
		BeforeListRowCreateListener, OnListRowViewClickListener {

	public static final String TAG = ResPartners.class.getSimpleName();

	View mView = null;
	OList mListControl = null;
	List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	Boolean mSyncDone = false;
	Integer mLastPosition = -1;
	Integer mLimit = 10;

	@Override
	public Object databaseHelper(Context context) {
		return new ResPartner(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(getActivity());
		mView = inflater
				.inflate(R.layout.common_list_control, container, false);
		init();
		return mView;
	}

	public void init() {
		mListControl = (OList) mView.findViewById(R.id.crm_listRecords);
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListControl, this);
		mListControl.setOnRowClickListener(this);
		mListControl.setOnListBottomReachedListener(this);
		mListControl.setRecordLimit(mLimit);
		mListControl.setBeforeListRowCreateListener(this);
		mListControl.setOnListRowViewClickListener(R.id.imgLocation, this);
		mListControl.setOnListRowViewClickListener(R.id.imgMail, this);
		mListControl.setOnListRowViewClickListener(R.id.imgCall, this);
		mListControl.setOnListRowViewClickListener(R.id.oCrmLeadCount, this);
		mListControl.setOnListRowViewClickListener(R.id.oSaleOrderCount, this);
		if (mLastPosition == -1) {
			mDataLoader = new DataLoader(0);
			mDataLoader.execute();
		}
	}

	class DataLoader extends AsyncTask<Void, Void, Void> {
		Integer mOffset = 0;

		public DataLoader(Integer offset) {
			mOffset = offset;
		}

		@Override
		protected Void doInBackground(Void... params) {
			scope.main().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (db().isEmptyTable() && !mSyncDone) {
						scope.main().requestSync(ResProvider.AUTHORITY);
					}
					OModel model = db();
					if (mOffset == 0)
						mListRecords.clear();
					List<ODataRow> list = null;

					list = model.setLimit(mLimit).setOffset(mOffset).select();
					if (list.size() > 0)
						mListRecords.addAll(list);
					mListControl.setRecordOffset(model.getNextOffset());
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mListControl.setCustomView(R.layout.crm_custom_customer_layout);
			if (mListRecords.size() > 0)
				mListControl.initListControl(mListRecords);
			OControls.setGone(mView, R.id.loadingProgress);
		}
	}

	@Override
	public void onPullStarted(View arg0) {
		scope.main().requestSync(ResProvider.AUTHORITY);
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
			mDataLoader = new DataLoader(0);
			mDataLoader.execute();
			mSyncDone = true;
		}
	};

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(TAG, "Sales", true));
		menu.add(new DrawerItem(TAG, "Customer", count(context), 0,
				object("customer")));
		return menu;
	}

	private int count(Context context) {
		int count = 0;
		count = new ResPartner(context).count();
		return count;
	}

	private Fragment object(String value) {
		ResPartners resPartners = new ResPartners();
		Bundle args = new Bundle();
		args.putString("resPartner", value);
		resPartners.setArguments(args);
		return resPartners;
	}

	@Override
	public void onRowItemClick(int position, View view, ODataRow row) {
		ResDetail resDetail = new ResDetail();
		Bundle bundle = new Bundle();
		// bundle.putString("key", mCurrentKey.toString());
		bundle.putAll(row.getPrimaryBundleData());
		resDetail.setArguments(bundle);
		startFragment(resDetail, true);
	}

	@Override
	public void onBottomReached(Integer limit, Integer offset) {
		if (mDataLoader != null) {
			mDataLoader.cancel(true);
		}
		mDataLoader = new DataLoader(offset);
		mDataLoader.execute();
	}

	@Override
	public Boolean showLoader() {
		return false;
	}

	@Override
	public void beforeListRowCreate(int position, ODataRow row, View view) {
		OControls.toggleViewVisibility(view, R.id.oSaleOrderCount, !row
				.getString("salesOrdersCount").equals(""));
		OControls.toggleViewVisibility(view, R.id.oCrmLeadCount, !row
				.getString("crmLeadCount").equals(""));
	}

	@Override
	public void onRowViewClick(ViewGroup view_group, View view, int position,
			ODataRow row) {
		Bundle bundle = new Bundle();
		switch (view.getId()) {
		case R.id.oSaleOrderCount:
			OLog.log("Sale");
			Sales sale = new Sales();
			bundle.putString("sales", "Sale_order");
			bundle.putString("id", row.getString("local_id"));
			sale.setArguments(bundle);
			startFragment(sale, true);
			break;
		case R.id.oCrmLeadCount:
			OLog.log("Crm");
			CRM crm = new CRM();
			bundle.putString("crm", "Opportunities");
			bundle.putString("type", "opportunity");
			bundle.putString("id", row.getString("local_id"));
			crm.setArguments(bundle);
			startFragment(crm, true);
			break;
		case R.id.imgLocation:
			String address = null;
			address = row.getString("street");
			address += "+" + row.getString("street2");
			address += "+" + row.getString("city");
			address += "+" + row.getString("zip");
			OLog.log("Address" + address);
			final Intent locationIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("google.navigation:q=" + address));
			startActivity(locationIntent);
			break;
		case R.id.imgCall:

			if (row.getString("phone").equals("false"))
				OLog.log("Not Call");
			else
				OLog.log("Call");
			break;
		case R.id.imgMail:
			if (row.getString("email").equals("false"))
				OLog.log("Note Mail");
			else {
				boolean installed = appInstalledOrNot("com.openerp");
				OLog.log("Mail Installed messaging :"+installed);
				if (installed) {
					Intent LaunchIntent = getActivity().getPackageManager()
							.getLaunchIntentForPackage("com.openerp");
					startActivity(LaunchIntent);
				} else {
					Intent emailIntent = new Intent(
							android.content.Intent.ACTION_SEND);
					String[] recipients = new String[] {
							row.getString("email"), "", };
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
							recipients);
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
							"Test Email");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
							"This is email's Partner");
					emailIntent.setType("text/html");
					startActivity(Intent.createChooser(emailIntent,
							"Send mail..."));
				}
			}

			break;
		}
	}

	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = getActivity().getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}
}
