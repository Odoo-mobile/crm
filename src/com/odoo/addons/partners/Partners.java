package com.odoo.addons.partners;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.helper.OListViewUtil;
import android.content.Context;
import android.content.Intent;
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

import com.odoo.OSwipeListener.SwipeCallbacks;
import com.odoo.OTouchListener;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.providers.partners.PartnersProvider;
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

public class Partners extends BaseFragment implements OnRefreshListener,
		OnItemClickListener, LoaderCallbacks<Cursor>,
		SyncStatusObserverListener, OnRowViewClickListener, SwipeCallbacks {
	public static final String TAG = Partners.class.getSimpleName();
	public static final String KEY_DRAWER = "Sales";
	private View mView = null;
	private ListView mListControl = null;
	private OCursorListAdapter mAdapter = null;
	private Context mContext = null;
	private OTouchListener mTouch;
	private int last_swiped_pos = -1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		mContext = getActivity();
		mView = inflater
				.inflate(R.layout.common_list_control, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setHasSwipeRefreshView(view, R.id.swipe_container, this);
		setHasSyncStatusObserver(KEY_DRAWER, this, db());
		scope = new AppScope(mContext);
		mTouch = scope.main().getTouchAttacher();
		mListControl = (ListView) view.findViewById(R.id.listRecords);
		if (mTouch != null)
			mTouch.setSwipeableView(mListControl, this);
		mAdapter = new OCursorListAdapter(mContext, null,
				R.layout.partners_item_layout);
		// mAdapter.setOnViewCreateListener(this);
		mAdapter.allowCacheView(true);
		mListControl.setAdapter(mAdapter);
		mListControl.setOnItemClickListener(this);
		mListControl.setEmptyView(mView.findViewById(R.id.loadingProgress));
		mAdapter.setOnRowViewClickListener(R.id.user_location, this);
		mAdapter.setOnRowViewClickListener(R.id.mail_to_user, this);
		mAdapter.setOnRowViewClickListener(R.id.call_user, this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Object databaseHelper(Context context) {
		return new ResPartner(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		List<DrawerItem> menu = new ArrayList<DrawerItem>();
		menu.add(new DrawerItem(KEY_DRAWER, "Customers", count(context),
				R.drawable.ic_action_customers, object("customer")));
		return menu;
	}

	private int count(Context context) {
		int count = 0;
		count = new ResPartner(context).count();
		return count;
	}

	private Fragment object(String value) {
		Partners resPartners = new Partners();
		Bundle args = new Bundle();
		args.putString("resPartner", value);
		resPartners.setArguments(args);
		return resPartners;
	}

	@Override
	public void onRefresh() {
		if (app().inNetwork()) {
			scope.main().requestSync(PartnersProvider.AUTHORITY);
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
		if (cursor.getCount() == 0) {
			scope.main().requestSync(PartnersProvider.AUTHORITY);
		}
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
		case R.id.user_location:
			String address = ((ResPartner) db()).getAddress(cursor);
			OLog.log(address + " <<");
			String map = "google.navigation:q=" + address;
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
			startActivity(intent);
			break;
		case R.id.call_user:
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
		case R.id.mail_to_user:
			String email = cursor.getString(cursor.getColumnIndex("email"));
			if (!email.equals("false")) {
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				String[] recipients = new String[] { email, "", };
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
						recipients);
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
				emailIntent.setType("text/html");
				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			} else {
				Toast.makeText(getActivity(), "No email found !",
						Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean canSwipe(int pos) {
		if (last_swiped_pos == pos) {
			return false;
		}
		return true;
	}

	@Override
	public void onSwipe(View arg0, int[] ids) {
		hideLastSwipe();
		for (int position : ids) {
			View view = OListViewUtil.getViewFromListView(mListControl,
					position);
			OControls.setGone(view, R.id.partner_detail_layout);
			OControls.setVisible(view, R.id.partner_swipe_layout);
			last_swiped_pos = position;
		}
	}

	private void hideLastSwipe() {
		if (last_swiped_pos != -1) {
			View v = OListViewUtil.getViewFromListView(mListControl,
					last_swiped_pos);
			OControls.setGone(v, R.id.partner_swipe_layout);
			OControls.setVisible(v, R.id.partner_detail_layout);
			last_swiped_pos = -1;
		}
	}

	@Override
	public boolean onBackPressed() {
		if (last_swiped_pos != -1) {
			hideLastSwipe();
			return false;
		}
		return true;
	}
}
