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
 * Created on 13/1/15 11:06 AM
 */
package com.odoo.addons.sale;

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

import com.odoo.addons.sale.models.SaleOrder;
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
import java.util.Arrays;
import java.util.List;

public class Sales extends BaseFragment implements
        OCursorListAdapter.OnViewBindListener, LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, IOnSearchViewChangeListener, View.OnClickListener,
        ISyncStatusObserverListener, AdapterView.OnItemClickListener {
    public static final String TAG = Sales.class.getSimpleName();
    public static final String KEY_MENU = "key_sales_menu";

    private View mView;
    private ListView mList;
    private OCursorListAdapter mAdapter;
    private String mFilter = null;

    private Type mType = Type.Quotation;


    public enum Type {
        Quotation,
        SaleOrder
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mType = Type.valueOf(getArguments().getString(KEY_MENU));
        return inflater.inflate(R.layout.common_listview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        initAdapter();
    }

    private void initAdapter() {
        mList = (ListView) mView.findViewById(R.id.listview);
        mAdapter = new OCursorListAdapter(getActivity(), null, R.layout.sale_order_item);
        mAdapter.setOnViewBindListener(this);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        setHasFloatingButton(mView, R.id.fabButton, mList, this);
        setHasSyncStatusObserver(TAG, this, db());
        setHasSwipeRefreshView(mView, R.id.swipe_container, this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        OControls.setText(view, R.id.name, row.getString("name"));
        String date = ODateUtils.convertToDefault(row.getString("date_order"),
                ODateUtils.DEFAULT_FORMAT, "MMMM, dd");
        OControls.setText(view, R.id.date_order, date);
        OControls.setText(view, R.id.state, row.getString("state_title"));
        if (row.getString("partner_name").equals("false")) {
            OControls.setGone(view, (R.id.partner_name));
        } else {
            OControls.setVisible(view, R.id.partner_name);
            OControls.setText(view, R.id.partner_name, row.getString("partner_name"));
        }
        OControls.setText(view, R.id.amount_total, row.getString("amount_total"));
        if (row.getString("currency_symbol").equals("false")) {
            OControls.setGone(view, (R.id.currency_symbol));
        } else {
            OControls.setVisible(view, R.id.currency_symbol);
            OControls.setText(view, R.id.currency_symbol, row.getString("currency_symbol"));
        }
        OControls.setText(view, R.id.order_lines, row.getString("order_line_count"));
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add(new ODrawerItem(TAG).setTitle(OResource.string(context, R.string.label_quotation))
                .setIcon(R.drawable.ic_action_quotation)
                .setInstance(new Sales())
                .setExtra(data(Type.Quotation)));
        menu.add(new ODrawerItem(TAG).setTitle(OResource.string(context, R.string.label_sale_orders))
                .setIcon(R.drawable.ic_action_sale_order)
                .setInstance(new Sales())
                .setExtra(data(Type.SaleOrder)));
        return menu;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        String where = null;
        String[] whereArgs = null;
        List<String> args = new ArrayList<>();
        switch (mType) {
            case Quotation:
                where = " (state = ? or state = ?)";
                args.addAll(Arrays.asList(new String[]{"draft", "cancel"}));
                break;
            case SaleOrder:
                where = "(state = ? or state = ? or state = ?)";
                args.addAll(Arrays.asList(new String[]{"manual", "progress",
                        "done"}));
                break;
        }
        if (mFilter != null) {
            where += " and (name like ? or partner_name like ? or state_title like ?)";
            args.add("%" + mFilter + "%");
            args.add("%" + mFilter + "%");
            args.add("%" + mFilter + "%");
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
                    setHasSwipeRefreshView(mView, R.id.swipe_container, Sales.this);
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
                    setHasSwipeRefreshView(mView, R.id.customer_no_items, Sales.this);
                    OControls.setImage(mView, R.id.icon,
                            (mType == Type.Quotation) ? R.drawable.ic_action_quotation : R.drawable.ic_action_sale_order);
                    OControls.setText(mView, R.id.title, "No " + mType + " Found");
                    OControls.setText(mView, R.id.subTitle, "");
                }
            }, 500);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    private Bundle data(Type type) {
        Bundle extra = new Bundle();
        extra.putString(KEY_MENU, type.toString());
        return extra;
    }

    @Override
    public Class<SaleOrder> database() {
        return SaleOrder.class;
    }


    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(SaleOrder.AUTHORITY);
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
        inflater.inflate(R.menu.menu_sales_order, menu);
        setHasSearchView(this, menu, R.id.menu_sales_search);

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
                IntentUtils.startActivity(getActivity(), SalesDetail.class, null);
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
        Bundle data = row.getPrimaryBundleData();
        data.putString("type", mType.toString());
        IntentUtils.startActivity(getActivity(), SalesDetail.class, data);
    }

}
