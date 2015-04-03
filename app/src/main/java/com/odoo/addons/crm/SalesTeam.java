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
 * Created on 31/3/15 5:17 PM
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.addons.crm.models.CRMLead;
import com.odoo.addons.crm.models.CrmCaseSection;
import com.odoo.addons.crm.models.SaleConfigSettings;
import com.odoo.addons.sale.Sales;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.IOnItemClickListener;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.sys.IOnBackPressListener;
import com.odoo.widgets.bottomsheet.BottomSheet;
import com.odoo.widgets.bottomsheet.BottomSheetListeners;

import java.util.ArrayList;
import java.util.List;

public class SalesTeam extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, OCursorListAdapter.OnViewBindListener,
        ISyncStatusObserverListener, IOnItemClickListener,
        BottomSheetListeners.OnSheetItemClickListener,
        BottomSheetListeners.OnSheetActionClickListener,
        IOnBackPressListener, BottomSheetListeners.OnSheetMenuCreateListener {
    public static final String TAG = SalesTeam.class.getSimpleName();
    public static final String KEY_FILTER_REQUEST = "key_filter_request";
    public static final String KEY_SECTION_ID = "section_id";
    private OCursorListAdapter mAdapter = null;
    private ListView mListview = null;
    private View mView = null;
    private BottomSheet mSheet = null;
    Boolean syncRequested = false;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.common_listview, container, false);
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add(new ODrawerItem(TAG).setTitle("Sales Team").
                setIcon(R.drawable.ic_action_company).setInstance(new SalesTeam()));
        boolean showMenu = SaleConfigSettings.showSaleTeams(context);
        if (showMenu)
            return menu;
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        mView.findViewById(R.id.fabButton).setVisibility(View.GONE);
        setHasSyncStatusObserver(TAG, this, db());
        setAdapter();
    }

    private void setAdapter() {
        mListview = (ListView) mView.findViewById(R.id.listview);
        mAdapter = new OCursorListAdapter(getActivity(), null, R.layout.crm_sales_team);
        mAdapter.setOnViewBindListener(this);
        mAdapter.handleItemClickListener(mListview, this);
        mListview.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Class<CrmCaseSection> database() {
        return CrmCaseSection.class;
    }


    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        OControls.setText(view, R.id.teamName, row.getString("name"));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), db().uri(), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.changeCursor(cursor);
        if (cursor.getCount() > 0) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setVisible(mView, R.id.swipe_container);
                    OControls.setGone(mView, R.id.customer_no_items);
                    setHasSwipeRefreshView(mView, R.id.swipe_container, SalesTeam.this);
                }
            }, 500);
        } else {
            if (db().isEmptyTable() && !syncRequested) {
                syncRequested = true;
                onRefresh();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OControls.setGone(mView, R.id.loadingProgress);
                    OControls.setGone(mView, R.id.swipe_container);
                    OControls.setVisible(mView, R.id.customer_no_items);
                    setHasSwipeRefreshView(mView, R.id.customer_no_items, SalesTeam.this);
                    OControls.setImage(mView, R.id.icon, R.drawable.ic_action_leads
                    );
                    OControls.setText(mView, R.id.title, "No Sales Team Found");
                    OControls.setText(mView, R.id.subTitle, "");
                }
            }, 500);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.changeCursor(null);
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
    public void onItemDoubleClick(View view, int position) {

    }

    @Override
    public void onItemClick(View view, int position) {
        showSheet((Cursor) mAdapter.getItem(position));
    }

    @Override
    public boolean onBackPressed() {
        if (mSheet != null && mSheet.isShowing()) {
            mSheet.dismiss();
            return false;
        }
        return true;
    }

    @Override
    public void onSheetActionClick(BottomSheet bottomSheet, Object o) {

    }


    @Override
    public void onItemClick(BottomSheet bottomSheet, MenuItem menuItem, Object o) {
        mSheet.dismiss();
        ODataRow row = OCursorUtils.toDatarow((Cursor) o);
        Bundle extra = new Bundle();
        extra.putInt(KEY_SECTION_ID, row.getInt(OColumn.ROW_ID));
        extra.putBoolean(KEY_FILTER_REQUEST, true);
        extra.putString("name", row.getString("name"));
        switch (menuItem.getItemId()) {
            case R.id.menu_use_lead:
                startFragment(new CRMLeads(), true, extra);
                break;
            case R.id.menu_use_opportunity:
                startFragment(new CRMOpportunities(), true, extra);
                break;
            case R.id.menu_use_quotation:
                extra.putString(Sales.KEY_MENU, Sales.Type.Quotation.toString());
                startFragment(new Sales(), true, extra);
                break;
        }
    }


    private void showSheet(Cursor data) {
        BottomSheet.Builder builder = new BottomSheet.Builder(getActivity());
        builder.listener(this);
        builder.setIconColor(_c(R.color.body_text_2));
        builder.setActionIcon(0);
        builder.setTextColor(_c(R.color.body_text_2));
        builder.setData(data);
        builder.actionListener(this);
        builder.title(data.getString(data.getColumnIndex("name")));
        builder.menu(R.menu.menu_sales_team);
        builder.setOnSheetMenuCreateListener(this);
        mSheet = builder.create();
        mSheet.show();
    }


    @Override
    public void onSheetMenuCreate(Menu menu, Object o) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) o);
        if (row.getString("use_leads").equals("false"))
            menu.findItem(R.id.menu_use_lead).setVisible(false);
        if (row.getString("use_opportunities").equals("false"))
            menu.findItem(R.id.menu_use_opportunity).setVisible(false);
        if (row.getString("use_quotations").equals("false")) {
            menu.findItem(R.id.menu_use_quotation).setVisible(false);
        }
    }
}
