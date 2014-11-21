package com.odoo.addons.crm;

import java.util.ArrayList;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.addons.crm.model.CRMPhoneCall;
import com.odoo.addons.crm.providers.crm.CRMProvider;
import com.odoo.addons.crm.providers.crm.PhoneCallProvider;
import com.odoo.crm.R;
import com.odoo.orm.OColumn;
import com.odoo.support.AppScope;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.support.fragment.SyncStatusObserverListener;
import com.odoo.support.listview.OCursorListAdapter;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class CRMPhoneCalls extends BaseFragment implements
		LoaderCallbacks<Cursor>, OnItemClickListener, OnRefreshListener,
		SyncStatusObserverListener {
	public static final String TAG = CRMPhoneCalls.class.getSimpleName();

	enum Keys {
		SchduledLoggedcalls
	}

	View mView = null;
	ListView mListControl = null;
	Context mContext = null;
	Keys mCurrentKey = Keys.SchduledLoggedcalls;
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
		// setHasSwipeRefreshView(view, R.id.swipe_container, this);
		setHasSyncStatusObserver(TAG, this, db());
		checkArguments();
		mListControl = (ListView) view.findViewById(R.id.listRecords);
		mAdapter = new OCursorListAdapter(mContext, null,
				R.layout.crm_phone_custom_layout);
		// mAdapter.setOnViewCreateListener(this);
		mListControl.setAdapter(mAdapter);
		mListControl.setOnItemClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	private void checkArguments() {
		Bundle arg = getArguments();
		mCurrentKey = Keys.valueOf(arg.getString("crmcall"));
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
				Keys.SchduledLoggedcalls), R.drawable.ic_action_call_logs,
				object(Keys.SchduledLoggedcalls)));
		return menu;
	}

	private int count(Context context, Keys key) {
		int count = 0;
		switch (key) {
		case SchduledLoggedcalls:
			count = new CRMPhoneCall(context).count();
			break;
		default:
			break;
		}
		return count;
	}

	private Fragment object(Keys value) {
		CRMPhoneCalls crmCalls = new CRMPhoneCalls();
		Bundle args = new Bundle();
		args.putString("crmcall", value.toString());
		crmCalls.setArguments(args);
		return crmCalls;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		if (db().isEmptyTable()) {
			scope.main().requestSync(PhoneCallProvider.AUTHORITY);
			setSwipeRefreshing(true);
		}
		return new CursorLoader(mContext, db().uri(), db().projection(), null,
				null, null);
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
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cr = (Cursor) mAdapter.getItem(position);
		int _id = cr.getInt(cr.getColumnIndex(OColumn.ROW_ID));
		int record_id = cr.getInt(cr.getColumnIndex("id"));
		CRMPhoneDetail crmPhoneDetail = new CRMPhoneDetail();
		Bundle bundle = new Bundle();
		bundle.putInt(OColumn.ROW_ID, _id);
		bundle.putInt("id", record_id);
		crmPhoneDetail.setArguments(bundle);
		startFragment(crmPhoneDetail, true);
	}

	@Override
	public void onStatusChange(Boolean refreshing) {
		if (!refreshing)
			hideRefreshingProgress();
		else
			setSwipeRefreshing(true);
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
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}
}
