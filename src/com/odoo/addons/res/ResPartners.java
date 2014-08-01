package com.odoo.addons.res;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OList;
import odoo.controls.OList.BeforeListRowCreateListener;
import odoo.controls.OList.OnListBottomReachedListener;
import odoo.controls.OList.OnListRowViewClickListener;
import odoo.controls.OList.OnRowClickListener;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.addons.res.providers.res.ResProvider;
import com.odoo.base.res.ResPartner;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.odoo.util.logger.OLog;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class ResPartners extends BaseFragment implements OnPullListener,
		OnRowClickListener, OnListBottomReachedListener,
		BeforeListRowCreateListener, OnListRowViewClickListener {

	public static final String TAG = ResPartners.class.getSimpleName();

	// enum Keys {
	// Customer
	// }

	View mView = null;
	OList mListControl = null;
	List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	// Keys mCurrentKey = Keys.Customer;
	Boolean mSyncDone = false;
	Integer mLastPosition = -1;
	Integer mLimit = 5;

	@Override
	public Object databaseHelper(Context context) {
		return new ResPartner(context);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		scope = new AppScope(getActivity());
		mView = inflater
				.inflate(R.layout.common_list_control, container, false);
		init();
		return mView;
	}

	public void init() {
		// checkArguments();
		mListControl = (OList) mView.findViewById(R.id.crm_listRecords);
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListControl, this);
		mListControl.setOnRowClickListener(this);
		mListControl.setOnListBottomReachedListener(this);
		mListControl.setRecordLimit(mLimit);
		mListControl.setBeforeListRowCreateListener(this);
		mListControl.setOnListRowViewClickListener(R.id.imgMail, this);
		mListControl.setOnListRowViewClickListener(R.id.imgCall, this);
		mListControl.setOnListRowViewClickListener(R.id.oCrmLeadCount, this);
		mListControl.setOnListRowViewClickListener(R.id.oSaleOrderCount, this);
		if (mLastPosition == -1) {
			mDataLoader = new DataLoader(0);
			mDataLoader.execute();
		}
	}

	// private void checkArguments() {
	// Bundle arg = getArguments();
	// mCurrentKey = Keys.valueOf(arg.getString("resPartner"));
	// }

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
					// mListRecords.clear();
					OModel model = db();
					if (mOffset == 0)
						mListRecords.clear();
					// switch (mCurrentKey) {
					// case Customer:
					// mListRecords.addAll(db().select());
					// break;
					// }
					List<ODataRow> list = model.setLimit(mLimit)
							.setOffset(mOffset).select();
					if (list.size() > 0)
						mListRecords.addAll(list);
					mListControl.setRecordOffset(model.getNextOffset());

					/*
					 * int leads = crmDB.count("partner_id = ? and type = ?",
					 * new String[] { row.getString("id"), "lead" }); int
					 * opportunity = crmDB.count("partner_id = ? and type = ?",
					 * new String[] { row.getString("id"), "opportunity" });
					 */
				}
			});
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// switch (mCurrentKey) {
			// case Customer:
			mListControl.setCustomView(R.layout.crm_custom_customer_layout);
			// break;
			// }
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
		// switch (key) {
		// case Customer:
		count = new ResPartner(context).count();
		// break;
		// default:
		// break;
		// }
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
		ResDetail note = new ResDetail();
		Bundle bundle = new Bundle();
		// bundle.putString("key", mCurrentKey.toString());
		bundle.putAll(row.getPrimaryBundleData());
		note.setArguments(bundle);
		startFragment(note, true);
	}

	@Override
	public void onBottomReached(Integer limit, Integer offset) {
		if (mDataLoader != null) {
			mDataLoader.cancel(true);
		}
		// if (mListRecords.size() == offset) {
		mDataLoader = new DataLoader(offset);
		mDataLoader.execute();
		// }
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
		switch (view.getId()) {
		case R.id.oSaleOrderCount:
			OLog.log("Sale");
			break;
		case R.id.oCrmLeadCount:
			OLog.log("Crm");
			break;
		case R.id.imgCall:
			if (!row.getString("phone").equals(false))
				OLog.log("Call");
			else
				OLog.log("Not Call");
			break;
		case R.id.imgMail:
			if (!row.getString("email").equals(false))
				OLog.log("Mail");
			else
				OLog.log("Note Mail");
			break;
		}
	}
}
