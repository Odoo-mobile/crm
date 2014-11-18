package com.odoo.addons.crm;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.addons.crm.model.CRMLead;
import com.odoo.addons.crm.providers.crm.CRMProvider;
import com.odoo.addons.partners.Partners;
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
import com.odoo.util.logger.OLog;

public class CRM extends BaseFragment implements OnRefreshListener,
		SyncStatusObserverListener, OnItemClickListener,
		LoaderCallbacks<Cursor>, BeforeBindUpdateData,
		OnSearchViewChangeListener {

//	public static final String TAG = CRM.class.getSimpleName();
	public static final String KEY_CRM_LEAD_TYPE = "crm_lead_type";

	enum Keys {
		Leads, Opportunities
	}

	private View mView = null;
	private ListView mListControl = null;
	private Keys mCurrentKey = Keys.Leads;
	private Context mContext = null;
	private OCursorListAdapter mAdapter = null;
	private String mFilterText = null;

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
		if (mCurrentKey == Keys.Leads)
			mAdapter = new OCursorListAdapter(mContext, null,
					R.layout.crm_custom_lead_layout);
		else if (mCurrentKey == Keys.Opportunities)
			mAdapter = new OCursorListAdapter(mContext, null,
					R.layout.crm_custom_opportunties_layout);
		mAdapter.setBeforeBindUpdateData(this);
		mListControl.setAdapter(mAdapter);
		mListControl.setOnItemClickListener(this);
		mListControl.setEmptyView(mView.findViewById(R.id.loadingProgress));
		getLoaderManager().initLoader(0, null, this);

	}

	private void checkArguments() {
		Bundle arg = getArguments();
		if (arg.containsKey(KEY_CRM_LEAD_TYPE))
			mCurrentKey = Keys.valueOf(arg.getString(KEY_CRM_LEAD_TYPE));
	}

	@Override
	public Object databaseHelper(Context context) {
		return new CRMLead(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(Partners.KEY_DRAWER, "Leads", count(context,
				Keys.Leads), R.drawable.ic_action_leads, object(Keys.Leads)));
		menu.add(new DrawerItem(Partners.KEY_DRAWER, "Opportunities", count(
				context, Keys.Opportunities),
				R.drawable.ic_action_opportunities, object(Keys.Opportunities)));
		return menu;
	}

	private int count(Context context, Keys key) {
		int count = 0;
		switch (key) {
		case Leads:
			count = new CRMLead(context).count("type = ?",
					new String[] { "lead" });
			break;
		case Opportunities:
			count = new CRMLead(context).count("type = ?",
					new String[] { "opportunity" });
		default:
			break;
		}
		return count;
	}

	private Fragment object(Keys value) {
		CRM crm = new CRM();
		Bundle args = new Bundle();
		args.putString(KEY_CRM_LEAD_TYPE, value.toString());
		crm.setArguments(args);
		return crm;
	}

	@Override
	public void onRefresh() {
		if (app().inNetwork()) {
			scope.main().requestSync(CRMProvider.AUTHORITY);
		} else {
			hideRefreshingProgress();
			Toast.makeText(mContext, _s(R.string.no_connection),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onStatusChange(Boolean refreshing) {
		if (!refreshing)
			hideRefreshingProgress();
		else
			setSwipeRefreshing(true);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cr = (Cursor) mAdapter.getItem(position);
		int _id = cr.getInt(cr.getColumnIndex(OColumn.ROW_ID));
		CRMDetail crmDetail = new CRMDetail();
		Bundle bundle = new Bundle();
		bundle.putInt(OColumn.ROW_ID, _id);
		bundle.putString(KEY_CRM_LEAD_TYPE, mCurrentKey.toString());
		crmDetail.setArguments(bundle);
		startFragment(crmDetail, true);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (db().isEmptyTable()) {
			scope.main().requestSync(CRMProvider.AUTHORITY);
			setSwipeRefreshing(true);
		}
		String where = "type = ?";
		List<String> whereArgs = new ArrayList<String>();
		String[] projections;
		if (mCurrentKey == Keys.Leads) {
			whereArgs.add("lead");
			 projections = new String[] { "name", "type", "display_name",
			 "stage_id.name", "create_date", "assignee_name" };
		} else {
			whereArgs.add("opportunity");
			projections = new String[] { "name", "type", "display_name",
					"stage_id.name", "create_date", "assignee_name",
					"planned_revenue", "probability",
					"company_currency.symbol", "title_action", "date_action" };
		}
		if (mFilterText != null) {
			where += " and name like ?";
			whereArgs.add("%" + mFilterText + "%");
		}
		return new CursorLoader(mContext, db().uri(), projections, where,
				whereArgs.toArray(new String[whereArgs.size()]),
				"create_date DESC, assignee_name");
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
	public ODataRow updateDataRow(Cursor cr) {
		ODataRow row = new ODataRow();
		if (mCurrentKey == Keys.Opportunities) {
			row.put("sep_at", " at ");
			row.put("sep_percentage", "%");
			String date_action = cr.getString(cr.getColumnIndex("date_action"));
			if (!date_action.equals("") && !date_action.equals("false"))
				row.put("sep_action", " : ");
		}
		return row;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_crm, menu);
		if (mAdapter != null) {
			setHasSearchView(this, menu, R.id.menu_search);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_crm_detail_create:
			CRMDetail detail = new CRMDetail();
			Bundle bundle = new Bundle();
			bundle.putString(KEY_CRM_LEAD_TYPE, mCurrentKey.toString());
			detail.setArguments(bundle);
			startFragment(detail, true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onSearchViewTextChange(String newFilter) {
		mFilterText = newFilter;
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public void onSearchViewClose() {
		// Nothing to do..
	}

	@Override
	public boolean onBackPressed() {
		return false;
	}
}
