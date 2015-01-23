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
 * Created on 13/1/15 10:24 AM
 */
package com.odoo.addons.crm;

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

import com.odoo.addons.crm.models.CRMLead;
import com.odoo.addons.customers.Customers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
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

public class CRM extends BaseFragment implements OCursorListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener,
        ISyncStatusObserverListener, OCursorListAdapter.BeforeBindUpdateData,
        IOnSearchViewChangeListener, View.OnClickListener, AdapterView.OnItemClickListener {
    public static final String TAG = CRM.class.getSimpleName();
    public static final String KEY_MENU = "key_menu_item";
    private Type mType = Type.Leads;
    private View mView;
    private ListView mList;
    private OCursorListAdapter mAdapter;

    private String mFilter = null;

    // Customer's data filter
    private boolean filter_customer_data = false;
    private int customer_id = -1;

    public enum Type {
        Leads, Opportunities
    }

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
        Bundle extra = getArguments();
        mType = Type.valueOf(extra.getString(KEY_MENU));
        if (extra != null && extra.containsKey(Customers.KEY_FILTER_REQUEST)) {
            filter_customer_data = true;
            customer_id = extra.getInt(Customers.KEY_CUSTOMER_ID);
            mView.findViewById(R.id.customer_filterContainer).setVisibility(View.VISIBLE);
            OControls.setText(mView, R.id.customer_name, extra.getString("name"));
            mView.findViewById(R.id.cancel_filter).setOnClickListener(this);
        }
        setHasSyncStatusObserver(TAG, this, db());
        initAdapter();
    }

    private void initAdapter() {
        mList = (ListView) mView.findViewById(R.id.listview);
        mAdapter = new OCursorListAdapter(getActivity(), null, R.layout.crm_item);
        mAdapter.setOnViewBindListener(this);
        mList.setAdapter(mAdapter);
        setHasFloatingButton(mView, R.id.fabButton, mList, this);
        mList.setOnItemClickListener(this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cancel_filter:
                filter_customer_data = false;
                getLoaderManager().restartLoader(0, null, this);
                mView.findViewById(R.id.customer_filterContainer).setVisibility(View.GONE);
                break;
            case R.id.fabButton:
                Bundle type = new Bundle();
                type.putString("type", mType.toString());
                IntentUtils.startActivity(getActivity(), CRMDetail.class, type);
                break;
        }
    }

    @Override
    public ODataRow updateDataRow(Cursor cr) {
        return db().browse(new String[]{"stage_id"},
                cr.getInt(cr.getColumnIndex(OColumn.ROW_ID)));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        String where = " type = ?";
        String[] whereArgs;
        List<String> args = new ArrayList<>();
        switch (mType) {
            case Leads:
                args.add("lead");
                break;
            case Opportunities:
                args.add("opportunity");
                break;
        }
        if (mFilter != null) {
            where += " and (name like ? or description like ? or display_name like ? " +
                    "or stage_name like ? or title_action like ?)";
            args.add("%" + mFilter + "%");
            args.add("%" + mFilter + "%");
            args.add("%" + mFilter + "%");
            args.add("%" + mFilter + "%");
            args.add("%" + mFilter + "%");
        }
        if (filter_customer_data) {
            where += " and partner_id = ?";
            args.add(customer_id + "");
        }
        whereArgs = args.toArray(new String[args.size()]);
        return new CursorLoader(getActivity(), db().uri(), null, where, whereArgs, null);
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
                    setHasSwipeRefreshView(mView, R.id.swipe_container, CRM.this);
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
                    setHasSwipeRefreshView(mView, R.id.customer_no_items, CRM.this);
                    OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
                    OControls.setText(mView, R.id.title, "No " + mType.toString() + " Found");
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
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        OControls.setText(view, R.id.name, row.getString("name"));
        OControls.setText(view, R.id.stage, row.getString("stage_name"));
        OControls.setText(view, R.id.display_name, row.getString("display_name"));
        OControls.setText(view, R.id.assignee_name, row.getString("assignee_name"));
        String date = ODateUtils.convertToDefault(row.getString("create_date"),
                ODateUtils.DEFAULT_FORMAT, "MMMM, dd");
        OControls.setText(view, R.id.create_date, date);

        // Controls for opportunity
        if (mType == Type.Opportunities) {
            view.findViewById(R.id.opportunity_controls).setVisibility(View.VISIBLE);
            if (!row.getString("date_action").equals("false")) {
                OControls.setVisible(view, R.id.date_action);
                String date_action = ODateUtils.convertToDefault(row.getString("date_action")
                        , ODateUtils.DEFAULT_DATE_FORMAT, "MMMM, dd");
                OControls.setText(view, R.id.date_action, date_action + " : ");
            } else {
                OControls.setGone(view, R.id.date_action);
            }
            if (!row.getString("title_action").equals("false")) {
                OControls.setVisible(view, R.id.title_action);
                OControls.setText(view, R.id.title_action, row.getString("title_action"));
            } else {
                OControls.setGone(view, R.id.title_action);
            }
        } else {
            view.findViewById(R.id.opportunity_controls).setVisibility(View.GONE);
        }
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add(new ODrawerItem(TAG)
                .setTitle(OResource.string(context, R.string.label_leads))
                .setInstance(new CRM())
                .setIcon(R.drawable.ic_action_leads)
                .setExtra(data(Type.Leads)));
        menu.add(new ODrawerItem(TAG)
                .setTitle(OResource.string(context, R.string.label_opportunities))
                .setInstance(new CRM())
                .setIcon(R.drawable.ic_action_opportunities)
                .setExtra(data(Type.Opportunities)));
        return menu;
    }

    private Bundle data(Type type) {
        Bundle extra = new Bundle();
        extra.putString(KEY_MENU, type.toString());
        return extra;
    }

    @Override
    public Class<CRMLead> database() {
        return CRMLead.class;
    }

    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(CRMLead.AUTHORITY);
            setSwipeRefreshing(true);
        } else {
            hideRefreshingProgress();
            Toast.makeText(getActivity(), _s(R.string.toast_network_required), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onStatusChange(Boolean refreshing) {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.menu_leads, menu);
        setHasSearchView(this, menu, R.id.menu_lead_search);
    }

    @Override
    public boolean onSearchViewTextChange(String newFilter) {
        mFilter = newFilter;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public void onSearchViewClose() {
        // Nothing to do
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        IntentUtils.startActivity(getActivity(), CRMDetail.class, row.getPrimaryBundleData());
    }
}
