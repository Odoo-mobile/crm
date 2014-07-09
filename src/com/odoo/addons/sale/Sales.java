package com.odoo.addons.sale;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OList;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.odoo.addons.crm.CRM;
import com.odoo.addons.crm.providers.crm.CRMProvider;
import com.odoo.addons.sale.model.SalesDB;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class Sales extends BaseFragment implements OnPullListener {

	public static final String TAG = CRM.class.getSimpleName();

	enum Keys {
		Quotation, Sale_order
	}

	View mView = null;
	OList mListControl = null;
	List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	Keys mCurrentKey = Keys.Quotation;

	@Override
	public Object databaseHelper(Context context) {
		return new SalesDB(getActivity());
	}

	public void init() {
		checkArguments();
		mListControl = (OList) mView.findViewById(R.id.listRecords);
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListControl, this);
		// mListControl.setOnRowClickListener(this);
		mDataLoader = new DataLoader();
		mDataLoader.execute();
	}

	private void checkArguments() {
		Bundle arg = getArguments();
		mCurrentKey = Keys.valueOf(arg.getString("sales"));
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();

		menu.add(new DrawerItem(TAG, "Sales", true));
		menu.add(new DrawerItem(TAG, "Quotation",
				count(context, Keys.Quotation), 0, object(Keys.Quotation)));
		menu.add(new DrawerItem(TAG, "Sales Order", count(context,
				Keys.Sale_order), 0, object(Keys.Sale_order)));
		return menu;
	}

	private int count(Context context, Keys key) {
		int count = 0;
		switch (key) {
		case Quotation:
			count = new SalesDB(context).count();
			break;
		case Sale_order:
			count = new SalesDB(context).count();
			break;
		default:
			break;
		}
		return count;
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
					case Quotation:
						mListRecords.addAll(db().select());
						break;
					case Sale_order:
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
			case Quotation:
				mListControl.setCustomView(R.layout.crm_view);
				break;
			case Sale_order:
				mListControl.setCustomView(R.layout.crm_view);
				break;
			}
			mListControl.initListControl(mListRecords);
			OControls.setGone(mView, R.id.loadingProgress);
		}
	}

	@Override
	public void onPullStarted(View arg0) {
		scope.main().requestSync(CRMProvider.AUTHORITY);
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

	private Fragment object(Keys value) {
		CRM crm = new CRM();
		Bundle args = new Bundle();
		args.putString("sales", value.toString());
		crm.setArguments(args);
		return crm;
	}

}
