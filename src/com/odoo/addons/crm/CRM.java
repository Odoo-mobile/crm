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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.widgets.SwipeRefreshLayout.OnRefreshListener;

import com.odoo.addons.crm.model.CRMLead;
import com.odoo.addons.crm.providers.crm.CRMProvider;
import com.odoo.addons.partners.Partners;
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

public class CRM extends BaseFragment implements OnRefreshListener,
		SyncStatusObserverListener, OnItemClickListener,
		LoaderCallbacks<Cursor>, BeforeBindUpdateData {

	public static final String TAG = CRM.class.getSimpleName();

	enum Keys {
		Leads, Opportunities
	}

	View mView = null;
	ListView mListControl = null;
	Keys mCurrentKey = Keys.Leads;
	int index = -1;
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
		if (arg.containsKey("crm"))
			mCurrentKey = Keys.valueOf(arg.getString("crm"));
		else if (arg.containsKey("remove_index")) {
			index = arg.getInt("remove_index");
		}
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
		args.putString("crm", value.toString());
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
		int record_id = cr.getInt(cr.getColumnIndex("id"));
		CrmDetail crmDetail = new CrmDetail();
		Bundle bundle = new Bundle();
		bundle.putInt(OColumn.ROW_ID, _id);
		bundle.putInt("id", record_id);
		bundle.putInt("index", position);
		bundle.putString("key", mCurrentKey.toString());
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
		String[] whereArgs = null;
		String[] projections;
		if (mCurrentKey == Keys.Leads) {
			whereArgs = new String[] { "lead" };
			projections = new String[] { "name", "type", "display_name",
					"stage_id.name", "create_date", "assignee_name" };
		} else {
			whereArgs = new String[] { "opportunity" };
			projections = new String[] { "name", "type", "display_name",
					"stage_id.name", "create_date", "assignee_name",
					"planned_revenue", "probability",
					"company_currency.symbol", "title_action", "date_action" };
		}

		return new CursorLoader(mContext, db().uri(), projections, where,
				whereArgs, "create_date DESC, assignee_name");
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

}
