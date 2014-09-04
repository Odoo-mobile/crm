package com.odoo.addons.res;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
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

import com.odoo.addons.res.providers.res.ResProvider;
import com.odoo.base.res.ResPartner;
import com.odoo.crm.R;
import com.odoo.orm.OColumn;
import com.odoo.support.AppScope;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.support.fragment.SyncStatusObserverListener;
import com.odoo.support.listview.OCursorListAdapter;
import com.odoo.support.listview.OCursorListAdapter.OnRowViewClickListener;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;
import com.odoo.util.logger.OLog;

public class ResPartners extends BaseFragment implements OnRefreshListener,
		OnItemClickListener, LoaderCallbacks<Cursor>,
		SyncStatusObserverListener, OnRowViewClickListener {
	public static final String TAG = ResPartners.class.getSimpleName();
	public static final String KEY_DRAWER = "Sales";
	View mView = null;
	ListView mListControl = null;
	private OCursorListAdapter mAdapter = null;
	Context mContext = null;

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
		setHasSyncStatusObserver(KEY_DRAWER, this, db());
		mListControl = (ListView) view.findViewById(R.id.listRecords);
		mAdapter = new OCursorListAdapter(mContext, null,
				R.layout.res_custom_layout);
		// mAdapter.setOnViewCreateListener(this);
		mListControl.setAdapter(mAdapter);
		mListControl.setOnItemClickListener(this);
		mListControl.setEmptyView(mView.findViewById(R.id.loadingProgress));
		mAdapter.setOnRowViewClickListener(R.id.imgLocation, this);
		mAdapter.setOnRowViewClickListener(R.id.imgMail, this);
		mAdapter.setOnRowViewClickListener(R.id.imgCall, this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Object databaseHelper(Context context) {
		return new ResPartner(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(KEY_DRAWER, "Customer", count(context), 0,
				object("customer")));
		return menu;
	}

	private int count(Context context) {
		int count = 0;
		count = new ResPartner(context).count();
		return count;
	}

	private Fragment object(String value) {
		ResPartners resPartners = new ResPartners();
		Bundle args = new Bundle();
		args.putString("resPartner", value);
		resPartners.setArguments(args);
		return resPartners;
	}

	@Override
	public void onRefresh() {
		if (app().inNetwork()) {
			scope.main().requestSync(ResProvider.AUTHORITY);
		} else {
			hideRefreshingProgress();
			Toast.makeText(getActivity(), _s(R.string.no_connection),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Cursor cr = (Cursor) mAdapter.getItem(position);
		int _id = cr.getInt(cr.getColumnIndex(OColumn.ROW_ID));
		int record_id = cr.getInt(cr.getColumnIndex("id"));
		ResDetail resDetail = new ResDetail();
		Bundle bundle = new Bundle();
		bundle.putInt(OColumn.ROW_ID, _id);
		bundle.putInt("id", record_id);
		resDetail.setArguments(bundle);
		startFragment(resDetail, true);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(mContext, db().uri(), db().projection(), null,
				null, null);
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
	public void onRowViewClick(int position, Cursor cursor, View view,
			View parent) {
		switch (view.getId()) {
		case R.id.imgLocation:
			String address = null;
			address = cursor.getString(cursor.getColumnIndex("street"));
			address += "+" + cursor.getString(cursor.getColumnIndex("street2"));
			address += "+" + cursor.getString(cursor.getColumnIndex("city"));
			address += "+" + cursor.getString(cursor.getColumnIndex("zip"));

			// address = row.getString("street");
			// address += "+" + row.getString("street2");
			// address += "+" + row.getString("city");
			// address += "+" + row.getString("zip");
			final Intent locationIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("google.navigation:q=" + address));
			startActivity(locationIntent);
			break;
		case R.id.imgMail:
			String phone = cursor.getString(cursor.getColumnIndex("phone"));
			if (phone.equals("false"))
				Toast.makeText(mContext, "No valid number", Toast.LENGTH_SHORT)
						.show();
			else {
				String phoneNo = (phone.replace(" ", "").replace("+", ""));
				Intent callIntent = new Intent(Intent.ACTION_CALL);
				callIntent.setData(Uri.parse("tel:" + phoneNo));
				startActivity(callIntent);
			}
			break;
		case R.id.imgCall:
			String email = cursor.getString(cursor.getColumnIndex("email"));
			if (email.equals("false"))
				OLog.log("Note Mail");
			else {
				boolean installed = appInstalledOrNot("com.openerp");
				OLog.log("Mail Installed messaging :" + installed);
				if (installed) {
					Intent LaunchIntent = getActivity().getPackageManager()
							.getLaunchIntentForPackage("com.openerp");
					startActivity(LaunchIntent);
				} else {
					Intent emailIntent = new Intent(
							android.content.Intent.ACTION_SEND);
					String[] recipients = new String[] { email, "", };
					emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
							recipients);
					emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
							"Test Email");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
							"This is email's Partner");
					emailIntent.setType("text/html");
					startActivity(Intent.createChooser(emailIntent,
							"Send mail..."));
				}
			}
			break;
		default:
			break;
		}
	}

	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = getActivity().getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}
}
