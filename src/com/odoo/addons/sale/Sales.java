package com.odoo.addons.sale;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.addons.customers.Customers;
import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.addons.sale.providers.sale.SalesProvider;
import com.odoo.crm.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.support.AppScope;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.support.fragment.OnSearchViewChangeListener;
import com.odoo.support.fragment.SyncStatusObserverListener;
import com.odoo.support.listview.OCursorListAdapter;
import com.odoo.support.listview.OCursorListAdapter.BeforeBindUpdateData;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class Sales extends BaseFragment implements OnRefreshListener,
		LoaderCallbacks<Cursor>, SyncStatusObserverListener,
		OnItemClickListener, BeforeBindUpdateData, OnSearchViewChangeListener {

	public static final String TAG = Sales.class.getSimpleName();

	enum Keys {
		Quotation, Sale_order
	}

	private Context mContext = null;
	private View mView = null;
	private ListView mListControl = null;
	private Keys mCurrentKey = Keys.Quotation;
	private OCursorListAdapter mAdapter = null;
	private String mSearchFilter = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mContext = getActivity();
		scope = new AppScope(mContext);
		mView = inflater
				.inflate(R.layout.common_list_control, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setHasSwipeRefreshView(view, R.id.swipe_container, this);
		setHasSyncStatusObserver(Customers.KEY_DRAWER, this, db());
		checkArguments();
		mListControl = (ListView) view.findViewById(R.id.listRecords);
		mAdapter = new OCursorListAdapter(mContext, null,
				R.layout.sale_custom_layout);
		mAdapter.setBeforeBindUpdateData(this);
		mListControl.setAdapter(mAdapter);
		mListControl.setOnItemClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	private void checkArguments() {
		Bundle arg = getArguments();
		mCurrentKey = Keys.valueOf(arg.getString("sales"));
	}

	@Override
	public Object databaseHelper(Context context) {
		return new SaleOrder(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(Customers.KEY_DRAWER, "Quotations", count(
				context, Keys.Quotation), R.drawable.ic_action_quotation,
				object(Keys.Quotation)));
		menu.add(new DrawerItem(Customers.KEY_DRAWER, "Sales Orders", count(
				context, Keys.Sale_order), R.drawable.ic_action_sale_order,
				object(Keys.Sale_order)));
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

	private Fragment object(Keys value) {
		Sales sales = new Sales();
		Bundle args = new Bundle();
		args.putString("sales", value.toString());
		sales.setArguments(args);
		return sales;
	}

	@Override
	public void onRefresh() {
		if (app().inNetwork()) {
			scope.main().requestSync(SalesProvider.AUTHORITY);
		} else {
			hideRefreshingProgress();
			Toast.makeText(mContext, _s(R.string.no_connection),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cr = (Cursor) mAdapter.getItem(position);
		int _id = cr.getInt(cr.getColumnIndex(OColumn.ROW_ID));
		int record_id = cr.getInt(cr.getColumnIndex("id"));
		QuotationsDetail quDetail = new QuotationsDetail();
		Bundle bundle = new Bundle();
		bundle.putInt(OColumn.ROW_ID, _id);
		bundle.putInt("id", record_id);
		bundle.putString("key", mCurrentKey.toString());
		quDetail.setArguments(bundle);
		startFragment(quDetail, true);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (db().isEmptyTable()) {
			scope.main().requestSync(SalesProvider.AUTHORITY);
			setSwipeRefreshing(true);
		}
		String where = "";
		List<String> args = new ArrayList<String>();
		if (mSearchFilter != null) {
			where = "name like ? and ";
			args.add("%" + mSearchFilter + "%");
		}
		if (mCurrentKey == Keys.Quotation) {
			where += "(state = ? or state = ?)";
			args.addAll(Arrays.asList(new String[] { "draft", "cancel" }));
		} else {
			if (getArguments().containsKey("id")) { // Res partner in sales
				where += "partner_id = ?";
				args.add(getArguments().getString("id"));
			} else {
				where += "(state = ? or state = ? or state = ?)";
				args.addAll(Arrays.asList(new String[] { "manual", "progress",
						"done" }));
			}
		}
		return new CursorLoader(mContext, db().uri(), new String[] { "name",
				"partner_id.id", "partner_id.name", "date_order", "state",
				"amount_total", "state_title", "currency_id.symbol",
				"order_line_count" }, where, args.toArray(new String[args
				.size()]), "date_order DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				OControls.setGone(mView, R.id.loadingProgress);
				OControls.setVisible(mView, R.id.swipe_container);
			}
		}, 700);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onStatusChange(Boolean refreshing) {
		if (!refreshing)
			hideRefreshingProgress();
		else
			setSwipeRefreshing(true);
	}

	@Override
	public ODataRow updateDataRow(Cursor cr) {
		ODataRow row = new ODataRow();
		String name = cr.getString(cr.getColumnIndex("name"));
		String type = (mCurrentKey == Keys.Quotation) ? "Quotation"
				: "Sales Order";
		row.put("name", type + " " + name);
		row.put("state_title", cr.getString(cr.getColumnIndex("state_title"))
				+ cr.getString(cr.getColumnIndex("order_line_count")));
		return row;
	}

	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_sale, menu);
		if (mAdapter != null) {
			setHasSearchView(this, menu, R.id.menu_sale_search);
		}
	}

	@Override
	public boolean onSearchViewTextChange(String newFilter) {
		mSearchFilter = newFilter;
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public void onSearchViewClose() {
		// Nothing to do
	}

}
