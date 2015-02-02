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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.addons.crm.models.CRMLead;
import com.odoo.addons.customers.Customers;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.IOnSearchViewChangeListener;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.IOnItemClickListener;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.StringUtils;
import com.odoo.core.utils.sys.IOnActivityResultListener;
import com.odoo.core.utils.sys.IOnBackPressListener;
import com.odoo.crm.R;
import com.odoo.widgets.bottomsheet.BottomSheet;
import com.odoo.widgets.bottomsheet.BottomSheetListeners;

import java.util.ArrayList;
import java.util.List;

public class CRM extends BaseFragment implements OCursorListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener,
        ISyncStatusObserverListener, OCursorListAdapter.BeforeBindUpdateData,
        IOnSearchViewChangeListener, View.OnClickListener, IOnItemClickListener,
        BottomSheetListeners.OnSheetItemClickListener, BottomSheetListeners.OnSheetActionClickListener,
        IOnBackPressListener, IOnActivityResultListener {
    public static final String TAG = CRM.class.getSimpleName();
    public static final String KEY_MENU = "key_menu_item";
    public static final int REQUEST_CONVERT_TO_OPPORTUNITY_WIZARD = 223;
    public static final int REQUEST_CONVERT_TO_QUOTATION_WIZARD = 224;
    private Type mType = Type.Leads;
    private View mView;
    private ListView mList;
    private OCursorListAdapter mAdapter;
    private BottomSheet mSheet = null;
    private String mFilter = null;
    private String wonLost = "won";
    private boolean syncRequested = false;
    // Customer's data filter
    private boolean filter_customer_data = false;
    private int customer_id = -1;
    private ODataRow convertRequestRecord = null;

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
        parent().setOnBackPressListener(this);
        parent().setOnActivityResultListener(this);
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
        mAdapter.handleItemClickListener(mList, this);
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
                    setHasSwipeRefreshView(mView, R.id.customer_no_items, CRM.this);
                    OControls.setImage(mView, R.id.icon, (mType == Type.Leads) ? R.drawable.ic_action_leads
                            : R.drawable.ic_action_opportunities);
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
    public void onItemDoubleClick(View view, int position) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        IntentUtils.startActivity(getActivity(), CRMDetail.class, row.getPrimaryBundleData());
    }

    @Override
    public void onItemClick(View view, int position) {
        showSheet((Cursor) mAdapter.getItem(position));
    }

    private void showSheet(Cursor data) {
        ODataRow row = OCursorUtils.toDatarow(data);
        BottomSheet.Builder builder = new BottomSheet.Builder(getActivity());
        builder.listener(this);
        builder.setIconColor(_c(R.color.theme_primary_dark));
        builder.setTextColor(Color.parseColor("#414141"));
        builder.setData(data);
        builder.actionListener(this);
        builder.setActionIcon(R.drawable.ic_action_edit);
        builder.title(data.getString(data.getColumnIndex("name")));
        if (row.getString("type").equals("lead"))
            builder.menu(R.menu.menu_lead_list_sheet);
        else
            builder.menu(R.menu.menu_opp_list_sheet);
        mSheet = builder.create();
        mSheet.show();
    }

    @Override
    public void onSheetActionClick(BottomSheet sheet, Object extras) {
        mSheet.dismiss();
        ODataRow row = OCursorUtils.toDatarow((Cursor) extras);
        IntentUtils.startActivity(getActivity(), CRMDetail.class, row.getPrimaryBundleData());
    }

    @Override
    public void onItemClick(BottomSheet sheet, MenuItem menu, Object extras) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) extras);
        mSheet.dismiss();
        convertRequestRecord = row;
        CRMLead crmLead = (CRMLead) db();
        ResPartner partner = new ResPartner(getActivity(), null);
        switch (menu.getItemId()) {
            case R.id.menu_lead_convert_to_opportunity:
                if (inNetwork()) {
                    if (row.getInt("id") == 0) {
                        OAlert.showWarning(getActivity(), "Need to sync before converting to Opportunity");
                    } else {
                        int count = crmLead.count("id != ? and partner_id = ? and " + OColumn.ROW_ID + " != ?"
                                , new String[]{
                                "0",
                                row.getInt("partner_id") + "",
                                row.getString(OColumn.ROW_ID)
                        });
                        if (count > 0) {
                            Intent intent = new Intent(getActivity(), ConvertToOpportunityWizard.class);
                            intent.putExtras(row.getPrimaryBundleData());
                            parent().startActivityForResult(intent, REQUEST_CONVERT_TO_OPPORTUNITY_WIZARD);
                        } else {
                            crmLead.convertToOpportunity(row, new ArrayList<Integer>(), convertDoneListener);
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_lead_convert_to_quotation:
                if (inNetwork()) {
                    Intent intent = new Intent(getActivity(), ConvertToQuotation.class);
                    intent.putExtras(row.getPrimaryBundleData());
                    parent().startActivityForResult(intent, REQUEST_CONVERT_TO_QUOTATION_WIZARD);
                } else {
                    Toast.makeText(getActivity(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_lead_call_customer:
                if (!row.getString("partner_id").equals("false")) {
                    String contact = partner.getContact(getActivity(), row.getInt(OColumn.ROW_ID));
                    if (!contact.equals("false")) {
                        IntentUtils.requestCall(getActivity(), contact);
                    } else {
                        Toast.makeText(getActivity(), "No contact found !", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "No partner found !", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_lead_customer_location:
                if (!row.getString("partner_id").equals("false")) {
                    String address = partner.getAddress(partner.browse(row.getInt("partner_id")));
                    if (!address.equals("false") && !TextUtils.isEmpty(address)) {
                        IntentUtils.redirectToMap(getActivity(), address);
                    } else {
                        Toast.makeText(getActivity(), "No location found !", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "No partner found !", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_lead_reschedule:
                IntentUtils.startActivity(getActivity(), CRMDetail.class, row.getPrimaryBundleData());
                break;
            case R.id.menu_lead_won:
                wonLost = "won";
                if (inNetwork()) {
                    crmLead.markWonLost(wonLost, row, markDoneListener);
                } else {
                    Toast.makeText(getActivity(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_lead_lost:
                wonLost = "lost";
                if (inNetwork()) {
                    crmLead.markWonLost(wonLost, row, markDoneListener);
                } else {
                    Toast.makeText(getActivity(), R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onOdooActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONVERT_TO_OPPORTUNITY_WIZARD && resultCode == Activity.RESULT_OK) {
            CRMLead crmLead = (CRMLead) db();
            List<Integer> ids = data.getIntegerArrayListExtra(ConvertToOpportunityWizard.KEY_LEADS_IDS);
            crmLead.convertToOpportunity(convertRequestRecord, ids, convertDoneListener);
        }
        if (requestCode == REQUEST_CONVERT_TO_QUOTATION_WIZARD && resultCode == Activity.RESULT_OK) {
            CRMLead crmLead = (CRMLead) db();
            crmLead.createQuotation(convertRequestRecord, data.getBooleanExtra("mark_won", false), createQuotationListener);
        }
    }

    CRMLead.OnOperationSuccessListener createQuotationListener = new CRMLead.OnOperationSuccessListener() {
        @Override
        public void OnSuccess() {
            Toast.makeText(getActivity(), R.string.label_quotation_created +
                    convertRequestRecord.getString("name"), Toast.LENGTH_LONG).show();
        }

        @Override
        public void OnCancelled() {

        }
    };
    CRMLead.OnOperationSuccessListener markDoneListener = new CRMLead.OnOperationSuccessListener() {
        @Override
        public void OnSuccess() {
            Toast.makeText(getActivity(), StringUtils.capitalizeString(convertRequestRecord.getString("type"))
                    + " marked " + wonLost, Toast.LENGTH_LONG).show();
        }

        @Override
        public void OnCancelled() {

        }
    };
    CRMLead.OnOperationSuccessListener convertDoneListener = new CRMLead.OnOperationSuccessListener() {
        @Override
        public void OnSuccess() {
            Toast.makeText(getActivity(), R.string.label_convert_to_opportunity, Toast.LENGTH_LONG).show();
        }

        @Override
        public void OnCancelled() {

        }
    };

    @Override
    public boolean onBackPressed() {
        if (mSheet != null && mSheet.isShowing()) {
            mSheet.dismiss();
            return false;
        }
        return true;
    }

}
