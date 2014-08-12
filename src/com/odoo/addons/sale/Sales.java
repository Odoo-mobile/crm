package com.odoo.addons.sale;

import java.util.ArrayList;
import java.util.List;
import odoo.controls.OList;
import odoo.controls.OList.OnListBottomReachedListener;
import odoo.controls.OList.OnListRowViewClickListener;
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
import android.widget.SearchView;
import android.widget.Toast;
import com.odoo.addons.res.ResPartners;
import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.addons.sale.providers.sale.SalesProvider;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.OValues;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class Sales extends BaseFragment implements OnPullListener,
		OnRowClickListener, OnListBottomReachedListener,
		OnListRowViewClickListener {

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
	Integer mLastPosition = -1;
	Integer mLimit = 5;

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
		checkArguments();
		mListControl = (OList) mView.findViewById(R.id.crm_listRecords);
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListControl, this);
		mListControl.setOnRowClickListener(this);
		mListControl.setOnListBottomReachedListener(this);
		mListControl.setOnListRowViewClickListener(R.id.imgCancel, this);
		mListControl.setOnListRowViewClickListener(R.id.imgConfirmCreate, this);
		mListControl.setRecordLimit(mLimit);
		if (mLastPosition == -1) {
			mDataLoader = new DataLoader(0);
			mDataLoader.execute();
		}
	}

	private void checkArguments() {
		Bundle arg = getArguments();
		mCurrentKey = Keys.valueOf(arg.getString("sales"));
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(ResPartners.KEY_DRAWER, "Quotation", count(
				context, Keys.Quotation), 0, object(Keys.Quotation)));
		menu.add(new DrawerItem(ResPartners.KEY_DRAWER, "Sales Order", count(
				context, Keys.Sale_order), 0, object(Keys.Sale_order)));
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
						scope.main().requestSync(SalesProvider.AUTHORITY);
					}
					// mListRecords.clear();
					OModel model = db();
					if (mOffset == 0)
						mListRecords.clear();
					String where = null, args[] = null;
					switch (mCurrentKey) {
					case Quotation:
						where = "state = ? or state = ?";
						args = new String[] { "draft", "cancel" };
						break;
					case Sale_order:
						if (getArguments().containsKey("id")) {
							where = "partner_id = ?";
							args = new String[] { getArguments()
									.getString("id") };
						} else {
							where = "state = ? or state = ? or state = ?";
							args = new String[] { "manual", "progress", "done" };
						}
						break;
					}
					mListRecords.addAll(model.setLimit(mLimit)
							.setOffset(mOffset).select(where, args));
					mListControl.setRecordOffset(model.getNextOffset());
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
			if (mListRecords.size() > 0)
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
			scope.main().refreshDrawer(ResPartners.KEY_DRAWER);
			mTouchListener.setPullComplete();
			if (mDataLoader != null) {
				mDataLoader.cancel(true);
			}
			mDataLoader = new DataLoader(0);
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
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_sale, menu);
		SearchView mSearchView = (SearchView) menu.findItem(
				R.id.menu_sale_search).getActionView();
		if (mListControl != null)
			mSearchView.setOnQueryTextListener(mListControl.getQueryListener());
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

	@Override
	public void onBottomReached(Integer limit, Integer offset) {
		if (mDataLoader != null) {
			mDataLoader.cancel(true);
		}
		if (mListRecords.size() == offset) {
			mDataLoader = new DataLoader(offset);
			mDataLoader.execute();
		}
	}

	@Override
	public Boolean showLoader() {
		return false;
	}

	@Override
	public void onRowViewClick(ViewGroup view_group, View view, int position,
			ODataRow row) {
		OValues values = null;
		switch (view.getId()) {
		case R.id.imgCancel:
			if (row.getString("state").equals("cancel"))
				Toast.makeText(getActivity(), "new Copy of Quotations",
						Toast.LENGTH_SHORT).show();
			else {
				values = new OValues();
				values.put("state", "cancel");
				db().update(values, row.getInt("local_id"));
				Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.imgConfirmCreate:
			if (mCurrentKey.equals(Keys.Quotation)
					&& row.getString("state").equals("cancel"))
				Toast.makeText(getActivity(), "new Copy of Quotations",
						Toast.LENGTH_SHORT).show();
			else {
				if (Double.parseDouble(row.getString("amount_total")) > 0) {
					values = new OValues();
					values.put("state", "manual");
					new SaleOrder(getActivity()).update(values,
							row.getInt("local_id"));
					Toast.makeText(getActivity(), "Confirm to Sale",
							Toast.LENGTH_SHORT).show();
				} else
					Toast.makeText(getActivity(), "No Order Line",
							Toast.LENGTH_SHORT).show();
				break;
			}
		}
		scope.main().refreshDrawer(ResPartners.KEY_DRAWER);
	}
}
