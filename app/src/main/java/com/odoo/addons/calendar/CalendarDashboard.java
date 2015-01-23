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
 * Created on 9/1/15 10:34 AM
 */
package com.odoo.addons.calendar;

import android.content.Context;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.odoo.addons.calendar.models.CalendarEvent;
import com.odoo.addons.calendar.utils.TodayIcon;
import com.odoo.addons.crm.CRM;
import com.odoo.addons.crm.CRMDetail;
import com.odoo.addons.customers.CustomerDetails;
import com.odoo.addons.phonecall.PhoneCallDetail;
import com.odoo.addons.phonecall.models.CRMPhoneCalls;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.calendar.SysCal;
import com.odoo.calendar.view.OdooCalendar;
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
import com.odoo.core.utils.sys.IOnBackPressListener;
import com.odoo.crm.R;
import com.odoo.widgets.bottomsheet.BottomSheet;
import com.odoo.widgets.bottomsheet.BottomSheetListeners;
import com.odoo.widgets.snackbar.SnackBar;
import com.odoo.widgets.snackbar.SnackbarBuilder;
import com.odoo.widgets.snackbar.listeners.ActionClickListener;
import com.odoo.widgets.snackbar.listeners.EventListener;

import java.util.ArrayList;
import java.util.List;

public class CalendarDashboard extends BaseFragment implements View.OnClickListener,
        BottomSheetListeners.OnSheetItemClickListener, IOnBackPressListener,
        OdooCalendar.OdooCalendarDateSelectListener, LoaderManager.LoaderCallbacks<Cursor>,
        OCursorListAdapter.OnViewBindListener, SwipeRefreshLayout.OnRefreshListener,
        ISyncStatusObserverListener, BottomSheetListeners.OnSheetActionClickListener,
        BottomSheetListeners.OnSheetMenuCreateListener, EventListener,
        IOnSearchViewChangeListener, IOnItemClickListener, OCursorListAdapter.OnViewCreateListener {
    public static final String TAG = CalendarDashboard.class.getSimpleName();
    public static final String KEY = "key_calendar_dashboard";
    public static final String KEY_DATE = "key_date";
    private BottomSheet mSheet = null;
    private OdooCalendar odooCalendar;
    private View calendarView = null;
    private ListView dashboardListView;
    private View mView;
    private SysCal.DateInfo mDateInfo = null;
    private String mFilterDate;
    private OCursorListAdapter mAdapter;
    private boolean syncRequested = false;
    private String mFilter = null;

    private enum SheetType {
        Event, PhoneCall, Opportunity
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.calendar_dashboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        setHasFloatingButton(view, R.id.fabButton, null, this);
        parent().setOnBackPressListener(this);
        odooCalendar = (OdooCalendar) view.findViewById(R.id.dashboard_calendar);
        odooCalendar.setOdooCalendarDateSelectListener(this);
    }

    @Override
    public View getEventsView(ViewGroup parent, SysCal.DateInfo date) {
        calendarView = LayoutInflater.from(getActivity()).inflate(
                R.layout.calendar_dashboard_items, parent, false);
        calendarView.findViewById(R.id.dashboard_no_item_view)
                .setOnClickListener(this);
        dashboardListView = (ListView) calendarView
                .findViewById(R.id.items_container);
        setHasFloatingButton(mView, R.id.fabButton, dashboardListView, this);
        mDateInfo = date;
        initAdapter();
        mFilterDate = ODateUtils.convertToUTC(date.getYear() + "-" +
                date.getMonth() + "-" + date.getDate() + " 00:00:00", ODateUtils.DEFAULT_FORMAT);
        getLoaderManager().restartLoader(0, null, this);
        return calendarView;
    }

    private void initAdapter() {
        mAdapter = new OCursorListAdapter(getActivity(), null,
                R.layout.calendar_dashboard_item_view);
        mAdapter.setOnViewBindListener(this);
        mAdapter.setOnViewCreateListener(this);
        dashboardListView.setAdapter(mAdapter);
        mAdapter.changeCursor(null);
        mAdapter.handleItemClickListener(dashboardListView, this);
        setHasSyncStatusObserver(KEY, this, db());
    }


    @Override
    public void onItemDoubleClick(View view, int position) {
        ODataRow row = OCursorUtils.toDatarow((Cursor) mAdapter.getItem(position));
        String type = row.getString("data_type");
        Class<?> cls = EventDetail.class;
        if (type.equals("phone_call")) {
            cls = PhoneCallDetail.class;
        }
        if (type.equals("opportunity")) {
            cls = CRMDetail.class;
        }
        IntentUtils.startActivity(getActivity(), cls, row.getPrimaryBundleData());
    }

    @Override
    public void onItemClick(View view, int position) {
        Cursor cr = (Cursor) mAdapter.getItem(position);
        String data_type = cr.getString(cr.getColumnIndex("data_type"));
        if (data_type.equals("event")) {
            showSheet(SheetType.Event, cr);
        }
        if (data_type.equals("phone_call")) {
            showSheet(SheetType.PhoneCall, cr);
        }
        if (data_type.equals("opportunity")) {
            showSheet(SheetType.Opportunity, cr);
        }
    }

    private void showSheet(SheetType type, Cursor data) {
        if (mSheet != null) {
            mSheet.dismiss();
        }
        BottomSheet.Builder builder = new BottomSheet.Builder(getActivity());
        builder.listener(this);
        builder.setIconColor(_c(R.color.theme_primary_dark));
        builder.setTextColor(Color.parseColor("#414141"));
        builder.setData(data);
        builder.actionListener(this);
        builder.setActionIcon(R.drawable.ic_action_edit);
        builder.title(data.getString(data.getColumnIndex("name")));
        builder.setOnSheetMenuCreateListener(this);
        switch (type) {
            case Event:
                builder.menu(R.menu.menu_dashboard_events);
                break;
            case PhoneCall:
                builder.menu(R.menu.menu_dashboard_phonecalls);
                break;
            case Opportunity:
                builder.menu(R.menu.menu_dashboard_opportunity);
                break;
        }
        mSheet = builder.create();
        mSheet.show();
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
                if (data_type.equals("phone_call")) {
                    Bundle extra = new Bundle();
                    extra.putInt(OColumn.ROW_ID, record_id);
                    IntentUtils.startActivity(getActivity(), PhoneCallDetail.class, extra);
                }
                if (data_type.equals("event")) {
                    Bundle extra = new Bundle();
                    extra.putInt(OColumn.ROW_ID, record_id);
                    IntentUtils.startActivity(getActivity(), EventDetail.class, extra);
                }

                if (data_type.equals("opportunity")) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(OColumn.ROW_ID, record_id);
                    IntentUtils.startActivity(getActivity(), CRMDetail.class, bundle);
                }
            }
        }, 250);

    }

    @Override
    public void onSheetMenuCreate(Menu menu, Object extras) {
        Cursor cr = (Cursor) extras;
        String type = cr.getString(cr.getColumnIndex("data_type"));
        String is_done = cr.getString(cr.getColumnIndex("is_done"));
        if (is_done.equals("0"))
            return;
        MenuItem mark_done = null;
        if (type.equals("event")) {
            mark_done = menu.findItem(R.id.menu_events_all_done);
        }
        if (type.equals("phone_call")) {
            mark_done = menu.findItem(R.id.menu_phonecall_all_done);
        }
        if (type.equals("opportunity")) {
        }
        if (mark_done != null) {
            mark_done.setTitle("Mark Undone");
            mark_done.setIcon(R.drawable.ic_action_mark_undone);
        }
    }

    @Override
    public View onViewCreated(Context context, ViewGroup view, Cursor cr, int position) {
        String data_type = cr.getString(cr.getColumnIndex("data_type"));
        if (data_type.equals("separator")) {
            return LayoutInflater.from(getActivity()).inflate(
                    R.layout.calendar_dashboard_item_separator, view, false);
        }
        return LayoutInflater.from(getActivity()).inflate(
                R.layout.calendar_dashboard_item_view, view, false);
    }

    @Override
    public void onViewBind(View view, Cursor cursor, ODataRow row) {
        String type = row.getString("data_type");
        int icon = -1;
        if (type.equals("separator")) {
            OControls.setText(view, R.id.list_separator, row.getString("name"));
        } else {
            String date = "false";
            if (row.getString("description").equals("false")) {
                row.put("description", "");
            }

            if (type.equals("event")) {
                icon = R.drawable.ic_action_event;
                if (row.getString("allday").equals("false")) {
                    date = row.getString("date_start");
                    view.findViewById(R.id.allDay).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.allDay).setVisibility(View.VISIBLE);
                }
            }

            if (type.equals("phone_call")) {
                icon = R.drawable.ic_action_phone;
                date = row.getString("date");
            }
            if (type.equals("opportunity")) {
                icon = R.drawable.ic_action_opportunities;
            }

            if (!date.equals("false")) {
                date = ODateUtils.convertToDefault(date, ODateUtils.DEFAULT_FORMAT, "hh:mm a");
                OControls.setText(view, R.id.event_time, date);
            }
            OControls.setText(view, R.id.event_description, row.getString("description"));
            Boolean is_done = row.getString("is_done").equals("1");
            OControls.setImage(view, R.id.event_icon, icon);
            if (is_done) {
                int title_color = (is_done) ? Color.LTGRAY : Color.parseColor("#414141");
                int time_color = (is_done) ? Color.LTGRAY : _c(R.color.theme_secondary_light);
                int desc_color = (is_done) ? Color.LTGRAY : Color.parseColor("#aaaaaa");
                int allDay_color = (is_done) ? Color.LTGRAY : _c(R.color.theme_secondary);
                view.findViewById(R.id.event_icon).setBackgroundResource(
                        R.drawable.circle_mask_gray);
                OControls.setTextColor(view, R.id.event_name, title_color);
                OControls.setTextColor(view, R.id.event_time, time_color);
                OControls.setTextColor(view, R.id.event_description, desc_color);
                OControls.setTextColor(view, R.id.allDay, allDay_color);
                OControls.setTextViewStrikeThrough(view, R.id.event_name);
                OControls.setTextViewStrikeThrough(view, R.id.event_time);
                OControls.setTextViewStrikeThrough(view, R.id.event_description);
                OControls.setTextViewStrikeThrough(view, R.id.allDay);
            }
            OControls.setText(view, R.id.event_name, row.getString("name"));
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle data) {
        List<String> args = new ArrayList<>();
        args.add(mFilterDate);
        if (mFilter != null) {
            args.add("%" + mFilter + "%");
        }
        CalendarEvent event = (CalendarEvent) db();
        return new CursorLoader(getActivity(), event.agendaUri(),
                null, "", args.toArray(new String[args.size()]), null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cr) {
        mAdapter.changeCursor(cr);
        OControls.setVisible(calendarView, R.id.dashboard_progress);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (cr.getCount() > 0) {
                    OControls.setGone(calendarView, R.id.dashboard_progress);
                    OControls.setVisible(calendarView, R.id.items_container);
                    OControls.setGone(calendarView, R.id.dashboard_no_items);
                    setHasSwipeRefreshView(calendarView, R.id.swipe_container,
                            CalendarDashboard.this);
                } else {
                    setHasSwipeRefreshView(calendarView,
                            R.id.dashboard_no_items, CalendarDashboard.this);
                    if (db().isEmptyTable() && !syncRequested) {
                        syncRequested = true;
                        parent().sync().requestSync(
                                CalendarEvent.AUTHORITY);
                        setSwipeRefreshing(true);
                    }
                    OControls.setGone(calendarView, R.id.dashboard_progress);
                    OControls.setVisible(calendarView, R.id.dashboard_no_items);
                }
            }
        }, 300);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        List<ODrawerItem> menu = new ArrayList<>();
        menu.add(new ODrawerItem(KEY).setTitle("Agenda")
                .setInstance(new CalendarDashboard())
                .setIcon(R.drawable.ic_action_dashboard));
        return menu;
    }

    @Override
    public Class<CalendarEvent> database() {
        return CalendarEvent.class;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabButton:
                onFabClick();
                break;
            case R.id.dashboard_no_item_view:
                createEvent();
                break;
        }
    }

    private void onFabClick() {
        BottomSheet.Builder builder = new BottomSheet.Builder(getActivity());
        builder.listener(this);
        builder.setIconColor(_c(R.color.theme_secondary_dark));
        builder.setTextColor(Color.parseColor("#414141"));
        ODataRow data = new ODataRow();
        data.put("fab", true);
        builder.setData(data);
        builder.title(_s(R.string.label_new));
        builder.menu(R.menu.menu_dashboard_fab);
        mSheet = builder.create();
        mSheet.show();
    }

    @Override
    public void onItemClick(BottomSheet sheet, MenuItem menu, Object extras) {
        dismissSheet(sheet);
        if (extras instanceof ODataRow) {
            onFabMenuClick(menu);
            return;
        }
        actionEvent(menu, (Cursor) extras);
    }

    private void actionEvent(MenuItem menu, Cursor cr) {
        String is_done = cr.getString(cr.getColumnIndex("is_done"));
        final OValues values = new OValues();
        values.put("_is_dirty", "false"); // to ignore update on server
        final int row_id = cr.getInt(cr.getColumnIndex(OColumn.ROW_ID));
        values.put("is_done", (is_done.equals("0")) ? 1 : 0);
        String done_label = (is_done.equals("0")) ? "done" : "undone";
        switch (menu.getItemId()) {
            // Event menus
            case R.id.menu_events_location:
                String location = cr.getString(cr.getColumnIndex("location"));
                if (location.equals("false")) {
                    Toast.makeText(getActivity(), "No location found !",
                            Toast.LENGTH_LONG).show();
                } else {
                    IntentUtils.redirectToMap(getActivity(), location);
                }
                break;
            case R.id.menu_events_reschedule:
                ODataRow row = OCursorUtils.toDatarow(cr);
                Bundle data = row.getPrimaryBundleData();
                data.putBoolean(EventDetail.KEY_RESCHEDULE, true);
                IntentUtils.startActivity(getActivity(), EventDetail.class, data);
                break;
            // Opportunity menus
            case R.id.menu_opp_customer_location:
                String address = cr.getString(cr.getColumnIndex("street")) + " ";
                address += cr.getString(cr.getColumnIndex("street2")) + " ";
                address += cr.getString(cr.getColumnIndex("city")) + " ";
                address += cr.getString(cr.getColumnIndex("zip"));
                address = address.replaceAll("false", "");
                if (TextUtils.isEmpty(address.trim())) {
                    Toast.makeText(getActivity(), "No location found !",
                            Toast.LENGTH_LONG).show();
                } else {
                    IntentUtils.redirectToMap(getActivity(), address);
                }
                break;
            case R.id.menu_opp_call_customer:
            case R.id.menu_phonecall_call:
                int partner_id = cr.getInt(cr.getColumnIndex("partner_id"));
                if (partner_id != 0) {
                    String contact = ResPartner.getContact(getActivity(), partner_id);
                    if (contact != null) {
                        IntentUtils.requestCall(getActivity(), contact);
                    } else {
                        Toast.makeText(getActivity(), "No contact found.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "No contact found.",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_opp_lost:
                //TODO: opportunity lost in Agenda
                break;
            case R.id.menu_opp_won:
                //TODO: opportunity won in Agenda
                break;
            case R.id.menu_opp_reschedule:
                //TODO: opportunity schedule in Agenda
                break;

            case R.id.menu_phonecall_reschedule:
                row = OCursorUtils.toDatarow(cr);
                IntentUtils.startActivity(getActivity(), PhoneCallDetail.class, row.getPrimaryBundleData());
                break;
            // All done menu
            case R.id.menu_phonecall_all_done:
                final CRMPhoneCalls phone_call = new CRMPhoneCalls(getActivity(), null);
                phone_call.update(row_id, values);
                getLoaderManager().restartLoader(0, null, this);
                SnackBar.get(getActivity()).text("Event marked " + done_label)
                        .actionColor(_c(R.color.theme_primary_light))
                        .duration(SnackbarBuilder.SnackbarDuration.LENGTH_LONG)
                        .withAction("undo", new ActionClickListener() {

                            @Override
                            public void onActionClicked(SnackbarBuilder snackbar) {
                                values.put("is_done", (values.getString("is_done")
                                        .equals("0")) ? 1 : 0);
                                phone_call.update(row_id, values);
                                getLoaderManager().restartLoader(0, null,
                                        CalendarDashboard.this);
                            }
                        }).withEventListener(this).show();
                break;
            case R.id.menu_events_all_done:
                db().update(row_id, values);
                getLoaderManager().restartLoader(0, null, this);
                SnackBar.get(getActivity()).text("Event marked " + done_label)
                        .actionColor(_c(R.color.theme_primary_light))
                        .duration(SnackbarBuilder.SnackbarDuration.LENGTH_LONG)
                        .withAction("undo", new ActionClickListener() {

                            @Override
                            public void onActionClicked(SnackbarBuilder snackbar) {
                                values.put("is_done", (values.getString("is_done")
                                        .equals("0")) ? 1 : 0);
                                db().update(row_id, values);
                                getLoaderManager().restartLoader(0, null,
                                        CalendarDashboard.this);
                            }
                        }).withEventListener(this).show();
                break;
        }
    }


    private void onFabMenuClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_fab_new_event:
                createEvent();
                break;
            case R.id.menu_fab_new_customer:
                IntentUtils.startActivity(getActivity(), CustomerDetails.class, null);
                break;
            case R.id.menu_fab_new_lead:
                Log.e(">>>", "hiiiiii");
                Bundle type = new Bundle();
                type.putString("type", CRM.Type.Leads.toString());
                IntentUtils.startActivity(getActivity(), CRMDetail.class, type);
                break;
            case R.id.menu_fab_new_call_log:
                IntentUtils.startActivity(getActivity(), PhoneCallDetail.class, null);
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
    public boolean onBackPressed() {
        if (mSheet != null && mSheet.isShowing()) {
            mSheet.dismiss();
            return false;
        }
        return true;
    }


    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(CalendarEvent.AUTHORITY);
            setSwipeRefreshing(true);
        } else {
            hideRefreshingProgress();
            Toast.makeText(getActivity(),
                    _s(R.string.toast_network_required), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStatusChange(Boolean refreshing) {
//        if (!refreshing) {
//            parent().sync().requestSync(CRMPhoneCalls.AUTHORITY);
//        }
    }

    @Override
    public void onShow(int height) {
        hideFab();
    }

    @Override
    public void onDismiss(int height) {
        showFab();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_calendar_dashboard, menu);
        if (getActivity() != null) {
            MenuItem today = menu.findItem(R.id.menu_dashboard_goto_today);
            today.setIcon(TodayIcon.get(getActivity()).getIcon());
        }
        setHasSearchView(this, menu, R.id.menu_search);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_dashboard_goto_today:
                odooCalendar.goToToday();
                break;
        }
        return super.onOptionsItemSelected(item);
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

    private void createEvent() {
        Bundle data = new Bundle();
        data.putString(KEY_DATE, mFilterDate);
        IntentUtils.startActivity(getActivity(), EventDetail.class, data);
    }
}
