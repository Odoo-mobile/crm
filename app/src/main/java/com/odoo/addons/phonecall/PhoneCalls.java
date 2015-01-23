/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 13/1/15 11:19 AM
 */
package com.odoo.addons.phonecall;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.addons.phonecall.models.CRMPhoneCalls;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.IOnSearchViewChangeListener;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OResource;
import com.odoo.crm.R;

import java.util.ArrayList;
import java.util.List;


public class PhoneCalls extends BaseFragment implements
        OCursorListAdapter.OnViewBindListener, LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, IOnSearchViewChangeListener,
        View.OnClickListener, ISyncStatusObserverListener, AdapterView.OnItemClickListener {
    public static final String TAG = PhoneCalls.class.getSimpleName();

    private View mView;
    private ListView mList;
    private OCursorListAdapter mAdapter;
    private String mFilter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.common_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        setHasSwipeRefreshView(mView, R.id.swipe_container, this);
        initAdapter();
    }

    private void initAdapter() {
        mList = (ListView) mView.findViewById(R.id.listview);
        mAdapter = new OCursorListAdapter(getActivity(), null, R.layout.phonecall_item);
        mAdapter.setOnViewBindListener(this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        setHasFloatingButton(mView, R.id.fabButton, mList, this);
        setHasSyncStatusObserver(TAG, this, db());
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        OControls.setText(view, R.id.name, row.getString("name"));
        String date = ODateUtils.convertToDefault(row.getString("date"),
                ODateUtils.DEFAULT_FORMAT, "MMM, dd hh:mm a");
        OControls.setText(view, R.id.date, date);
        OControls.setText(view, R.id.state, row.getString("state"));
        if (!row.getString("description").equals("false")) {
            OControls.setVisible(view, R.id.description);
            OControls.setText(view, R.id.description, row.getString("description"));
        } else {
            OControls.setGone(view, R.id.description);
        }
        if (!row.getString("customer_name").equals("")) {
            OControls.setVisible(view, R.id.customer_name);
            OControls.setText(view, R.id.customer_name, row.getString("customer_name"));
        } else {
            OControls.setGone(view, R.id.customer_name);
        }
        if (row.getString("call_type").equals("false")) {
            OControls.setGone(view, R.id.call_type_icon);
        } else {
            OControls.setVisible(view, R.id.call_type_icon);
            if (row.getString("call_type").equals("Inbound")) {
                OControls.setImage(view, R.id.call_type_icon, R.drawable.ic_action_call_inbound);
            } else {
                OControls.setImage(view, R.id.call_type_icon, R.drawable.ic_action_call_outbound);
            }
        }

        if (row.getString("lead_name").equals("")) {
            OControls.setGone(view, R.id.lead_name);
        } else {
            OControls.setVisible(view, R.id.lead_name);
            OControls.setText(view, R.id.lead_name, row.getString("lead_name"));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where = null;
        String[] whereArgs = null;
        if (mFilter != null) {
            where = " name like ? or lead_name like ? or customer_name like ?";
            whereArgs = new String[]{"%" + mFilter + "%", "%" + mFilter + "%", "%" + mFilter + "%"};
        }
        return new CursorLoader(getActivity(), db().uri(), null, where, whereArgs, "date");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.changeCursor(data);
        if (data.getCount() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setVisible(mView, R.id.swipe_container);
                    OControls.setGone(mView, R.id.customer_no_items);
                    setHasSwipeRefreshView(mView, R.id.swipe_container, PhoneCalls.this);
                }
            }, 500);
        } else {
            if (db().isEmptyTable()) {
                onRefresh();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setGone(mView, R.id.swipe_container);
                    OControls.setVisible(mView, R.id.customer_no_items);
                    setHasSwipeRefreshView(mView, R.id.customer_no_items, PhoneCalls.this);
                    OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
                    OControls.setText(mView, R.id.title, "No Logged calls Found");
                    OControls.setText(mView, R.id.subTitle, "");
                }
            }, 500);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add(new ODrawerItem(TAG)
                .setTitle(OResource.string(context, R.string.label_phone_calls))
                .setGroupTitle());
        menu.add(new ODrawerItem(TAG)
                .setTitle(OResource.string(context, R.string.label_logged_calls))
                .setIcon(R.drawable.ic_action_call_logs)
                .setInstance(new PhoneCalls()));
        return menu;
    }

    @Override
    public Class<CRMPhoneCalls> database() {
        return CRMPhoneCalls.class;
    }


    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(CRMPhoneCalls.AUTHORITY);
            setSwipeRefreshing(true);
        } else {
            hideRefreshingProgress();
            Toast.makeText(getActivity(), _s(R.string.toast_network_required), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_phonecalls, menu);
        setHasSearchView(this, menu, R.id.menu_phonecall_search);

    }

    @Override
    public boolean onSearchViewTextChange(String newFilter) {
        mFilter = newFilter;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public void onSearchViewClose() {
        //Nothing to do
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabButton:
                IntentUtils.startActivity(getActivity(), PhoneCallDetail.class, null);
                break;
        }
    }

    @Override
    public void onStatusChange(Boolean refreshing) {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        IntentUtils.startActivity(getActivity(), PhoneCallDetail.class, row.getPrimaryBundleData());
    }
}
