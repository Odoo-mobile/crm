package com.odoo.addons.sale;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.addons.sale.providers.sale.SalesProvider;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class Sales extends BaseFragment implements OnPullListener,
		OnRowClickListener {

	public static final String TAG = Sales.class.getSimpleName();

	enum Keys {
		Quotation, Sale_order
	}

	View mView = null;
	OList mListControl = null;
	List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	Keys mCurrentKey = Keys.Quotation;
	Boolean mSyncDone = false;
	HashMap<String, String> mStates = new HashMap<String, String>();

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

	@Override
	public Object databaseHelper(Context context) {
		return new SaleOrder(context);
	}

	public void init() {
		mStates.put("draft", "Draft Quotation");
		mStates.put("sent", "Quotation Sent");
		mStates.put("cancel", "Cancelled");
		mStates.put("waiting_date", "Waiting Schedule");
		mStates.put("progress", "Sales Order");
		mStates.put("manual", "Sale to Invoice");
		mStates.put("shipping_except", "Shipping Exception");
		mStates.put("invoice_except", "Invoice Exception");
		mStates.put("done", "Done");
		checkArguments();
		mListControl = (OList) mView.findViewById(R.id.crm_listRecords);
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListControl, this);
		mListControl.setOnRowClickListener(this);
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
			count = new SaleOrder(context).count("state = ? or state = ?",
					new String[] { "draft", "cancel" });
			break;
		case Sale_order:
			count = new SaleOrder(context).count(
					"state = ? or state = ? or state = ?", new String[] {
							"manual", "progress", "done" });
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
					if (db().isEmptyTable() && !mSyncDone) {
						scope.main().requestSync(SalesProvider.AUTHORITY);
					}
					mListRecords.clear();

					switch (mCurrentKey) {
					case Quotation:
						mListRecords.addAll(db().select(
								"state = ? or state = ?",
								new String[] { "draft", "cancel" }));
						break;
					case Sale_order:
						mListRecords.addAll(db().select(
								"state = ? or state = ? or state = ?",
								new String[] { "manual", "progress", "done" }));
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
				mListControl.setCustomView(R.layout.sale_custom_layout);
				break;
			case Sale_order:
				mListControl.setCustomView(R.layout.sale_custom_layout);
				break;
			}
			mListControl.initListControl(mListRecords);
			OControls.setGone(mView, R.id.loadingProgress);
		}
	}

	@Override
	public void onPullStarted(View arg0) {
		scope.main().requestSync(SalesProvider.AUTHORITY);
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
			mSyncDone = true;
		}
	};

	private Fragment object(Keys value) {
		Sales sales = new Sales();
		Bundle args = new Bundle();
		args.putString("sales", value.toString());
		sales.setArguments(args);
		return sales;
	}

	@Override
	public void onRowItemClick(int position, View view, ODataRow row) {
		QuotationsDetail quDetail = new QuotationsDetail();
		Bundle bundle = new Bundle();
		bundle.putString("key", mCurrentKey.toString());
		bundle.putAll(row.getPrimaryBundleData());
		quDetail.setArguments(bundle);
		startFragment(quDetail, true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_sale, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_sale_create:
			QuotationsDetail quDetail = new QuotationsDetail();
			Bundle bundle = new Bundle();
			bundle.putString("key", mCurrentKey.toString());
			quDetail.setArguments(bundle);
			startFragment(quDetail, true);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
