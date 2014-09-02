package com.odoo.addons.crm;

import java.util.ArrayList;
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
import android.widget.SearchView;

import com.odoo.addons.crm.model.CRMLead;
import com.odoo.addons.crm.providers.crm.CRMProvider;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class CRM extends BaseFragment implements OnPullListener,
		OnRowClickListener {

	public static final String TAG = CRM.class.getSimpleName();

	enum Keys {
		Leads, Opportunities
	}

	View mView = null;
	OList mListControl = null;
	List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	Keys mCurrentKey = Keys.Leads;
	int index = -1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(this);
		mView = inflater
				.inflate(R.layout.common_list_control, container, false);
		init();
		return mView;
	}

	public void init() {
		checkArguments();
		mListControl = (OList) mView.findViewById(R.id.crm_listRecords);
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListControl, this);
		mListControl.setOnRowClickListener(this);
		mDataLoader = new DataLoader();
		mDataLoader.execute();
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

		menu.add(new DrawerItem(TAG, "CRM", true));
		menu.add(new DrawerItem(TAG, "Leads", count(context, Keys.Leads), 0,
				object(Keys.Leads)));
		menu.add(new DrawerItem(TAG, "Opportunities", count(context,
				Keys.Opportunities), 0, object(Keys.Opportunities)));
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
					case Leads:
						mListRecords.addAll(db().select("type = ?",
								new String[] { "lead" }));
						break;
					case Opportunities:
						mListRecords.addAll(db().select("type = ?",
								new String[] { "opportunity" }));
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
			case Leads:
				mListControl.setCustomView(R.layout.crm_custom_layout);
				break;
			case Opportunities:
				mListControl.setCustomView(R.layout.crm_custom_layout);
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
		args.putString("crm", value.toString());
		crm.setArguments(args);
		return crm;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_crm_detail_create) {
			CrmDetail crmDetail = new CrmDetail();
			Bundle bundle = new Bundle();
			bundle.putString("key", mCurrentKey.toString());
			crmDetail.setArguments(bundle);
			startFragment(crmDetail, true);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_crm, menu);
		SearchView mSearchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();
		if (mListControl != null)
			mSearchView.setOnQueryTextListener(mListControl.getQueryListener());
	}

	@Override
	public void onRowItemClick(int position, View view, ODataRow row) {
		CrmDetail crmDetail = new CrmDetail();
		Bundle bundle = new Bundle();
		bundle.putString("key", mCurrentKey.toString());
		bundle.putInt("index", position);
		bundle.putAll(row.getPrimaryBundleData());
		crmDetail.setArguments(bundle);
		startFragment(crmDetail, true);
	}
}
