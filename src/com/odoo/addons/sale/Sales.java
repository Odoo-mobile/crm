package com.odoo.addons.sale;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widgets.SwipeRefreshLayout.OnRefreshListener;

import com.odoo.addons.partners.Partners;
import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.addons.sale.providers.sale.SalesProvider;
import com.odoo.crm.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.support.AppScope;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.support.fragment.SyncStatusObserverListener;
import com.odoo.support.listview.OCursorListAdapter;
import com.odoo.support.listview.OCursorListAdapter.BeforeBindUpdateData;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class Sales extends BaseFragment implements OnRefreshListener,
		LoaderCallbacks<Cursor>, SyncStatusObserverListener,
		OnItemClickListener, BeforeBindUpdateData {

	public static final String TAG = Sales.class.getSimpleName();

	enum Keys {
		Quotation, Sale_order
	}

	View mView = null;
	ListView mListControl = null;
	Keys mCurrentKey = Keys.Quotation;
	Context mContext = null;
	private OCursorListAdapter mAdapter = null;

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
		setHasSyncStatusObserver(Partners.KEY_DRAWER, this, db());
		checkArguments();
		mListControl = (ListView) view.findViewById(R.id.listRecords);
		mAdapter = new OCursorListAdapter(mContext, null,
				R.layout.sale_custom_layout);
		mAdapter.setBeforeBindUpdateData(this);
		mListControl.setAdapter(mAdapter);
		mListControl.setOnItemClickListener(this);
		mListControl.setEmptyView(mView.findViewById(R.id.loadingProgress));
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
		menu.add(new DrawerItem(Partners.KEY_DRAWER, "Quotations", count(
				context, Keys.Quotation), R.drawable.ic_action_quotation,
				object(Keys.Quotation)));
		menu.add(new DrawerItem(Partners.KEY_DRAWER, "Sales Orders", count(
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
		String args[] = {};
		if (mCurrentKey == Keys.Quotation) {
			where = "state = ? or state = ?";
			args = new String[] { "draft", "cancel" };
		} else {
			if (getArguments().containsKey("id")) { // Res partner in sales
				where = "partner_id = ?";
				args = new String[] { getArguments().getString("id") };
			} else {
				where = "state = ? or state = ? or state = ?";
				args = new String[] { "manual", "progress", "done" };
			}
		}
		return new CursorLoader(mContext, db().uri(), new String[] { "name",
				"partner_id.id", "partner_id.name", "date_order", "state",
				"amount_total", "state_title", "currency_id.symbol",
				"order_line_count" }, where, args, "date_order DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		OControls.setGone(mView, R.id.loadingProgress);
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
}
