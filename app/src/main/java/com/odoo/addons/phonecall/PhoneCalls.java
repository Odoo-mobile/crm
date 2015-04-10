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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.addons.phonecall.models.CRMPhoneCalls;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.addons.fragment.IOnSearchViewChangeListener;
import com.odoo.core.support.addons.fragment.ISyncStatusObserverListener;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.IOnItemClickListener;
import com.odoo.core.support.list.OCursorListAdapter;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OCursorUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.dialog.OChoiceDialog;
import com.odoo.core.utils.sys.IOnBackPressListener;
import com.odoo.widgets.bottomsheet.BottomSheet;
import com.odoo.widgets.bottomsheet.BottomSheetListeners;
import com.odoo.widgets.snackbar.SnackBar;
import com.odoo.widgets.snackbar.SnackbarBuilder;
import com.odoo.widgets.snackbar.listeners.EventListener;

import java.util.ArrayList;
import java.util.List;


public class PhoneCalls extends BaseFragment implements
        OCursorListAdapter.OnViewBindListener, LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener, IOnSearchViewChangeListener,
        View.OnClickListener, ISyncStatusObserverListener,
        BottomSheetListeners.OnSheetItemClickListener, BottomSheetListeners.
        OnSheetActionClickListener,
        BottomSheetListeners.OnSheetMenuCreateListener, IOnItemClickListener, IOnBackPressListener, EventListener {
    public static final String TAG = PhoneCalls.class.getSimpleName();

    private View mView;
    private ListView mList;
    private OCursorListAdapter mAdapter;
    private String mFilter = null;
    private boolean syncRequested = false;
    private BottomSheet mSheet = null;


    public enum Type {
        Logged, Scheduled
    }

    private Type mType = Type.Logged;

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
        mType = Type.valueOf(getArguments().getString("type"));
        setHasSwipeRefreshView(mView, R.id.swipe_container, this);
        initAdapter();
    }

    private void initAdapter() {
        mList = (ListView) mView.findViewById(R.id.listview);
        mAdapter = new OCursorListAdapter(getActivity(), null, R.layout.phonecall_item);
        mAdapter.setOnViewBindListener(this);
        mList.setAdapter(mAdapter);
        parent().setOnBackPressListener(this);
        setHasFloatingButton(mView, R.id.fabButton, mList, this);
        setHasSyncStatusObserver(TAG, this, db());
        mAdapter.handleItemClickListener(mList, this);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        OControls.setText(view, R.id.name, row.getString("name"));
        String date = ODateUtils.convertToDefault(row.getString("date"),
                ODateUtils.DEFAULT_FORMAT, "MMM, dd hh:mm a");
        OControls.setText(view, R.id.date, date);
        OControls.setText(view, R.id.state, db().getLabel("state", row.getString("state")));
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
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        String where;
        List<String> args = new ArrayList<>();
        if (mType == Type.Logged) {
            where = " state = ?";
            args.add("done");
        } else {
            where = " state != ?";
            args.add("done");
        }
        if (mFilter != null) {
            where += " and name like ? or lead_name like ? or customer_name like ?";
            args.add("%" + mFilter + "%");
            args.add("%" + mFilter + "%");
            args.add("%" + mFilter + "%");
        }
        return new CursorLoader(getActivity(), db().uri(), null, where,
                (args.size() > 0) ? args.toArray(new String[args.size()]) : null, "date");
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
                    setHasSwipeRefreshView(mView, R.id.customer_no_items, PhoneCalls.this);
                    OControls.setImage(mView, R.id.icon, R.drawable.ic_action_customers);
                    if (mType == Type.Logged) {
                        OControls.setText(mView, R.id.title, _s(R.string.label_no_logged_calls_found));
                        OControls.setImage(mView, R.id.icon,R.drawable.ic_action_call_logs);
                    } else {
                        OControls.setText(mView, R.id.title, _s(R.string.label_no_scheduled_calls_found));
                        OControls.setImage(mView, R.id.icon,R.drawable.ic_action_schedule_call);
                    }
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
                .setExtra(extra(Type.Logged))
                .setInstance(new PhoneCalls()));
        menu.add(new ODrawerItem(TAG)
                .setTitle(OResource.string(context, R.string.label_scheduled_calls))
                .setIcon(R.drawable.ic_action_schedule_call)
                .setExtra(extra(Type.Scheduled))
                .setInstance(new PhoneCalls()));
        return menu;
    }

    private Bundle extra(Type type) {
        Bundle extra = new Bundle();
        extra.putString("type", type.toString());
        return extra;
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

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
//        IntentUtils.startActivity(getActivity(), PhoneCallDetail.class, row.getPrimaryBundleData());
//    }


    @Override
    public void onItemClick(BottomSheet sheet, MenuItem menu, Object extras) {
        dismissSheet(sheet);
        actionEvent(menu, (Cursor) extras);

    }

    @Override
    public void onItemDoubleClick(View view, int position) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        IntentUtils.startActivity(getActivity(), PhoneCallDetail.class, row.getPrimaryBundleData());
    }

    @Override
    public void onItemClick(View view, int position) {
        Cursor cr = (Cursor) mAdapter.getItem(position);
        showSheet(cr);
    }

    private void showSheet(Cursor data) {
        if (mSheet != null) {
            mSheet.dismiss();
        }
        BottomSheet.Builder builder = new BottomSheet.Builder(getActivity());
        builder.listener(this);
        builder.setIconColor(_c(R.color.body_text_2));
        builder.setTextColor(_c(R.color.body_text_1));
        builder.setData(data);
        builder.actionListener(this);
        builder.setActionIcon(R.drawable.ic_action_edit);
        builder.title(data.getString(data.getColumnIndex("name")));
        builder.setOnSheetMenuCreateListener(this);
        builder.menu(R.menu.menu_dashboard_phonecalls);
        mSheet = builder.create();
        mSheet.show();
    }

    private void actionEvent(MenuItem menu, Cursor cr) {
        String is_done = cr.getString(cr.getColumnIndex("is_done"));
        final OValues values = new OValues();
        values.put("_is_dirty", "false"); // to ignore update on server
        final int row_id = cr.getInt(cr.getColumnIndex(OColumn.ROW_ID));
        values.put("is_done", (is_done.equals("0")) ? 1 : 0);
        String done_label = (is_done.equals("0")) ? "done" : "undone";
        final ODataRow row = OCursorUtils.toDatarow(cr);
        Bundle data = row.getPrimaryBundleData();
        switch (menu.getItemId()) {

            case R.id.menu_phonecall_call:
                int partner_id = cr.getInt(cr.getColumnIndex("partner_id"));
                if (partner_id != 0) {
                    String contact = ResPartner.getContact(getActivity(), partner_id);
                    if (contact != null && !contact.equals("false")) {
                        IntentUtils.requestCall(getActivity(), contact);
                    } else {
                        Toast.makeText(getActivity(), _s(R.string.label_no_contact_found),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), _s(R.string.label_no_contact_found),
                            Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.menu_phonecall_reschedule:
                List<String> choices = new ArrayList<>();
                choices = new ArrayList<>();
                choices.add("Re-Schedule call");
                choices.add("Schedule other call");
                OChoiceDialog.get(getActivity()).withOptions(choices, -1)
                        .show(new OChoiceDialog.OnChoiceSelectListener() {
                            @Override
                            public void choiceSelected(int position, String value) {
                                switch (position) {
                                    case 0: // Re-Schedule
                                        IntentUtils.startActivity(getActivity(), PhoneCallDetail.class,
                                                row.getPrimaryBundleData());

                                        break;
                                    case 1: // Schedule other call
                                        Bundle extra = row.getPrimaryBundleData();
                                        extra.putInt("call_id", row.getInt(OColumn.ROW_ID));
                                        IntentUtils.startActivity(getActivity(), PhoneCallDetail.class,
                                                extra);
                                        break;
                                }
                            }
                        });
                break;
            // All done menu
            case R.id.menu_phonecall_all_done:
                final CRMPhoneCalls phone_call = new CRMPhoneCalls(getActivity(), null);
                values.put("state", "done");
                phone_call.update(row_id, values);
                getLoaderManager().restartLoader(0, null, this);
                SnackBar.get(getActivity()).text(_s(R.string.toast_phone_call_marked_done))
                        .duration(SnackbarBuilder.SnackbarDuration.LENGTH_LONG)
                        .withEventListener(this).show();
                break;


        }
    }

    private void dismissSheet(final BottomSheet sheet) {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                sheet.dismiss();
            }
        }, 100);
    }

    @Override
    public void onShow(int i) {
        hideFab();
    }

    @Override
    public void onDismiss(int i) {
        showFab();
    }

    @Override
    public void onSheetActionClick(BottomSheet sheet, final Object extras) {
        sheet.dismiss();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Cursor cr = (Cursor) extras;
                String data_type = cr.getString(cr.getColumnIndex("data_type"));
                int record_id = cr.getInt(cr.getColumnIndex(OColumn.ROW_ID));
                Bundle extra = new Bundle();
                extra.putInt(OColumn.ROW_ID, record_id);
                IntentUtils.startActivity(getActivity(), PhoneCallDetail.class, extra);
            }
        }, 250);
    }

    @Override
    public void onSheetMenuCreate(Menu menu, Object o) {
        if (mType == Type.Logged)
            menu.findItem(R.id.menu_phonecall_all_done).setVisible(false);

    }

    @Override
    public boolean onBackPressed() {
        if (mSheet != null && mSheet.isShowing()) {
            mSheet.dismiss();
            return false;
        }
        return true;
    }
}
