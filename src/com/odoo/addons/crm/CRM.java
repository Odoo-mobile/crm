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
import android.view.View;
import android.view.ViewGroup;

import com.odoo.addons.crm.model.CRMdb;
import com.odoo.addons.crm.providers.crm.CRMProvider;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.receivers.SyncFinishReceiver;
import com.odoo.support.AppScope;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.openerp.OETouchListener;
import com.openerp.OETouchListener.OnPullListener;

public class CRM extends BaseFragment implements OnPullListener,
		OnRowClickListener {

	public static final String TAG = CRM.class.getSimpleName();

	enum Keys {
		Leads, Opportunties
	}

	View mView = null;
	OList mListControl = null;
	List<ODataRow> mListRecords = new ArrayList<ODataRow>();
	OETouchListener mTouchListener = null;
	DataLoader mDataLoader = null;
	Keys mCurrentKey = Keys.Leads;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		scope = new AppScope(this);
		mView = inflater.inflate(R.layout.crm_layout, container, false);
		init();
		return mView;
	}

	public void init() {
		checkArguments();
		mListControl = (OList) mView.findViewById(R.id.listRecords);
		mTouchListener = scope.main().getTouchAttacher();
		mTouchListener.setPullableView(mListControl, this);
		mListControl.setOnRowClickListener(this);
		mDataLoader = new DataLoader();
		mDataLoader.execute();
	}

	private void checkArguments() {
		Bundle arg = getArguments();
		mCurrentKey = Keys.valueOf(arg.getString("crm"));
	}

	@Override
	public Object databaseHelper(Context context) {
		return new CRMdb(getActivity());
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();

		menu.add(new DrawerItem(TAG, "CRM", true));
		menu.add(new DrawerItem(TAG, "Leads", count(context, Keys.Leads), 0,
				object(Keys.Leads)));
		menu.add(new DrawerItem(TAG, "Opportunties", count(context,
				Keys.Opportunties), 0, object(Keys.Opportunties)));
		return menu;
	}

	private int count(Context context, Keys key) {
		int count = 0;
		switch (key) {
		case Leads:
			count = new CRMdb(context).count("type = ?",
					new String[] { "lead" });
			break;
		case Opportunties:
			count = new CRMdb(context).count("type = ?",
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
					case Opportunties:
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
				mListControl.setCustomView(R.layout.crm_view);
				break;
			case Opportunties:
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
		args.putString("crm", value.toString());
		crm.setArguments(args);
		return crm;
	}

	@Override
	public void onRowItemClick(int position, View view, ODataRow row) {
		// TODO Auto-generated method stub

	}

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// if (item.getItemId() == R.id.menu_library_detail_create) {
	// LibraryDetail library = new LibraryDetail();
	// Bundle bundle = new Bundle();
	// bundle.putString("key", mCurrentKey.toString());
	// library.setArguments(bundle);
	// startFragment(library, true);
	// }
	// return super.onOptionsItemSelected(item);
	// }

	// @Override
	// public void onRowItemClick(int position, View view, ODataRow row) {
	// LibraryDetail library = new LibraryDetail();
	// Bundle bundle = new Bundle();
	// bundle.putString("key", mCurrentKey.toString());
	// bundle.putAll(row.getPrimaryBundleData());
	// library.setArguments(bundle);
	// startFragment(library, true);
	// }
}
