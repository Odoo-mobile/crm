package com.odoo.addons.crm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import com.odoo.addons.calendar.EventDetail;
import com.odoo.addons.crm.models.CRMCaseStage;
import com.odoo.addons.crm.models.CRMLead;
import com.odoo.addons.customers.Customers;
import com.odoo.addons.phonecall.PhoneCallDetail;
import com.odoo.addons.sale.models.SaleOrder;
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
import com.odoo.core.utils.StringUtils;
import com.odoo.core.utils.dialog.OChoiceDialog;
import com.odoo.core.utils.sys.IOnActivityResultListener;
import com.odoo.core.utils.sys.IOnBackPressListener;
import com.odoo.R;
import com.odoo.widgets.bottomsheet.BottomSheet;
import com.odoo.widgets.bottomsheet.BottomSheetListeners;

import java.util.ArrayList;
import java.util.List;

public class CRMOpportunities extends BaseFragment implements OCursorListAdapter.OnViewBindListener,
        LoaderManager.LoaderCallbacks<Cursor>, SwipeRefreshLayout.OnRefreshListener,
        ISyncStatusObserverListener, OCursorListAdapter.BeforeBindUpdateData,
        IOnSearchViewChangeListener, View.OnClickListener, IOnItemClickListener,
        BottomSheetListeners.OnSheetItemClickListener,
        BottomSheetListeners.OnSheetActionClickListener,
        IOnBackPressListener, IOnActivityResultListener {
    public static final String TAG = CRMOpportunities.class.getSimpleName();
    public static final String KEY_MENU = "key_menu_item";
    public static final int REQUEST_CONVERT_TO_OPPORTUNITY_WIZARD = 223;
    public static final int REQUEST_CONVERT_TO_QUOTATION_WIZARD = 224;
    public static final String KEY_IS_LEAD = "key_is_lead";
    //    private Type mType = Type.Opportunities;
    private View mView;
    private ListView mList;
    private OCursorListAdapter mAdapter;
    private BottomSheet mSheet = null;
    private String mFilter = null;
    private String wonLost = "won";
    private int stage_id = -1;
    private boolean syncRequested = false;
    // Customer's data filter
    private boolean filter_customer_data = false;
    private int customer_id = -1;
    private ODataRow convertRequestRecord = null;
    private Bundle syncBundle = new Bundle();

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
        parent().setHasActionBarSpinner(true);
        parent().setOnActivityResultListener(this);
        Bundle extra = getArguments();
        if (extra != null) {
            if (extra.containsKey(CRMOpportunitiesPager.KEY_STAGE_ID)) {
                stage_id = extra.getInt(CRMOpportunitiesPager.KEY_STAGE_ID);
            }
            if (extra.containsKey(Customers.KEY_FILTER_REQUEST)) {
                filter_customer_data = true;
                customer_id = extra.getInt(Customers.KEY_CUSTOMER_ID);
                mView.findViewById(R.id.customer_filterContainer).setVisibility(View.VISIBLE);
                OControls.setText(mView, R.id.customer_name, extra.getString("name"));
                mView.findViewById(R.id.cancel_filter).setOnClickListener(this);
            }
        }
        setHasSyncStatusObserver(TAG, this, db());
        initAdapter();
    }

    private void initAdapter() {
        if (getActivity() != null) {
            mList = (ListView) mView.findViewById(R.id.listview);
            mAdapter = new OCursorListAdapter(getActivity(), null, R.layout.crm_item);
            mAdapter.setOnViewBindListener(this);
            mList.setAdapter(mAdapter);
            setHasFloatingButton(mView, R.id.fabButton, mList, this);
            mAdapter.handleItemClickListener(mList, this);
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.cancel_filter:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.fabButton:
                Bundle types = new Bundle();
                types.putString("type", CRMLeads.Type.Opportunities.toString());
                types.putInt("stage_id", stage_id);
                IntentUtils.startActivity(getActivity(), CRMDetail.class, types);
                break;
            case R.id.stage_move:
                final ODataRow row = (ODataRow) v.getTag();
                final List<String> stageNames = new ArrayList<>();
                CRMCaseStage crmStage = new CRMCaseStage(getActivity(), null);
                final List<ODataRow> stages = crmStage.select(null, "type != ?",
                        new String[]{"lead"}, "sequence");
                int defaultSelected = -1;
                for (ODataRow stage : stages) {
                    stageNames.add(stage.getString("name"));
                    if (stage_id == stage.getInt(OColumn.ROW_ID)) {
                        defaultSelected = stageNames.indexOf(stage.getString("name"));
                    }
                }
                OChoiceDialog.get(getActivity()).withTitle("Move to").withOptions(stageNames, defaultSelected)
                        .show(new OChoiceDialog.OnChoiceSelectListener() {
                            @Override
                            public void choiceSelected(int position, String value) {
                                OValues stageValue = new OValues();
                                stageValue.put("stage_name", stages.get(position).
                                        getString("name"));
                                stageValue.put("stage_id", stages.get(position)
                                        .getInt(OColumn.ROW_ID));
                                stageValue.put("probability", stages.get(position).getFloat("probability"));
                                new CRMLead(getActivity(), null).update(OColumn.ROW_ID + "=?",
                                        new String[]{row.getString(OColumn.ROW_ID)}, stageValue);
                                Toast.makeText(getActivity(), row.getString("name") +
                                        " moved to stage " + stages.get(position).
                                        getString("name"), Toast.LENGTH_SHORT).show();
                            }
                        });
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
        String where = " type = ? and stage_id = ?";
        String[] whereArgs;
        List<String> args = new ArrayList<>();
        args.add("opportunity");
        args.add(stage_id + "");
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

        return new CursorLoader(getActivity(), db().uri(), null, where, whereArgs, "date_action DESC");
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
                    setHasSwipeRefreshView(mView, R.id.swipe_container, CRMOpportunities.this);
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
                    setHasSwipeRefreshView(mView, R.id.customer_no_items, CRMOpportunities.this);
                    OControls.setImage(mView, R.id.icon, R.drawable.ic_action_opportunities);
                    if (getActivity() != null)
                        OControls.setText(mView, R.id.title, OResource.string(getActivity(), R.string.label_no_opportunity_found));
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
        OControls.setGone(view, R.id.stage);
        OControls.setVisible(view, R.id.stage_move);
        //OControls.setText(view, R.id.stage, row.getString("stage_name"));
        OControls.setText(view, R.id.display_name, row.getString("display_name"));
        OControls.setText(view, R.id.assignee_name, row.getString("assignee_name"));
        String date = ODateUtils.convertToDefault(row.getString("create_date"),
                ODateUtils.DEFAULT_FORMAT, "MMMM, dd");
        OControls.setText(view, R.id.create_date, date);
        // Controls for opportunity
        syncBundle.putBoolean(KEY_IS_LEAD, false);
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
//        TextView tv = (TextView) view.findViewById(R.id.stage);
//        tv.setOnClickListener(this);
//        tv.setTag(row);
        view.findViewById(R.id.stage_move).setTag(row);
        view.findViewById(R.id.stage_move).setOnClickListener(this);
    }


    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        return new ArrayList<>();
    }

    @Override
    public Class<CRMLead> database() {
        return CRMLead.class;
    }

    @Override
    public void onRefresh() {
        if (inNetwork()) {
            parent().sync().requestSync(CRMLead.AUTHORITY, syncBundle);
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
        BottomSheet.Builder builder = new BottomSheet.Builder(getActivity());
        builder.listener(this);
        builder.setIconColor(_c(R.color.body_text_2));
        builder.setTextColor(_c(R.color.body_text_2));
        builder.setData(data);
        builder.actionListener(this);
        builder.setActionIcon(R.drawable.ic_action_edit);
        builder.title(data.getString(data.getColumnIndex("name")));
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
        final ODataRow row = OCursorUtils.toDatarow((Cursor) extras);
        mSheet.dismiss();
        CRMLead crmLead = (CRMLead) db();
        ResPartner partner = new ResPartner(getActivity(), null);
        switch (menu.getItemId()) {
            case R.id.menu_lead_convert_to_quotation:
                if (inNetwork()) {
                    Intent intent = new Intent(getActivity(), ConvertToQuotation.class);
                    intent.putExtras(row.getPrimaryBundleData());
                    parent().startActivityForResult(intent, REQUEST_CONVERT_TO_QUOTATION_WIZARD);
                } else {
                    Toast.makeText(getActivity(), R.string.toast_network_required,
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_lead_call_customer:
                String contact = (row.getString("phone").equals("false")) ?
                        (row.getString("mobile").equals("false")) ? "false" : row.getString("mobile") : row.getString("phone");
                if (contact.equals("false")) {
                    if (!row.getString("partner_id").equals("false")) {
                        contact = partner.getContact(getActivity(), row.getInt(OColumn.ROW_ID));
                        if (!contact.equals("false")) {
                            IntentUtils.requestCall(getActivity(), contact);
                        } else {
                            Toast.makeText(getActivity(), "No contact found !", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "No contact found !", Toast.LENGTH_LONG).
                                show();
                    }
                } else {
                    IntentUtils.requestCall(getActivity(), contact);
                }
                break;
            case R.id.menu_lead_customer_location:
                if (!row.getString("partner_id").equals("false")) {
                    String address = partner.getAddress(partner.browse(row.getInt("partner_id")));
                    if (!address.equals("false") && !TextUtils.isEmpty(address)) {
                        IntentUtils.redirectToMap(getActivity(), address);
                    } else {
                        Toast.makeText(getActivity(), _s(R.string.label_no_location_found), Toast.LENGTH_LONG).
                                show();
                    }
                } else {
                    Toast.makeText(getActivity(), _s(R.string.label_no_contact_found), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_lead_reschedule:
                List<String> choices = new ArrayList<>();
                choices.add(OResource.string(getActivity(), R.string.label_opt_schedule_log_call));
                choices.add(OResource.string(getActivity(), R.string.label_opt_schedule_meeting));
                OChoiceDialog.get(getActivity()).withOptions(choices, -1)
                        .show(new OChoiceDialog.OnChoiceSelectListener() {
                            @Override
                            public void choiceSelected(int position, String value) {
                                int opp_id = row.getInt(OColumn.ROW_ID);
                                switch (position) {
                                    case 0:
                                        Bundle extra = new Bundle();
                                        extra.putInt("opp_id", opp_id);
                                        IntentUtils.startActivity(getActivity(),
                                                PhoneCallDetail.class, extra);
                                        break;
                                    case 1: // Schedule meeting
                                        Bundle data = new Bundle();
//                                        data.putString(KEY_DATE, mFilterDate);
                                        data.putInt("opp_id", opp_id);
                                        IntentUtils.startActivity(getActivity(),
                                                EventDetail.class, data);
                                        break;
                                }
                            }
                        });
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
        if (requestCode == REQUEST_CONVERT_TO_QUOTATION_WIZARD && resultCode ==
                Activity.RESULT_OK) {
            CRMLead crmLead = (CRMLead) db();
            convertRequestRecord = crmLead.browse(data.getIntExtra(OColumn.ROW_ID, 0));
            crmLead.createQuotation(convertRequestRecord, data.getStringExtra("partner_id"),
                    data.getBooleanExtra("mark_won", false), createQuotationListener);
        }
    }

    CRMLead.OnOperationSuccessListener createQuotationListener = new CRMLead.
            OnOperationSuccessListener() {
        @Override
        public void OnSuccess() {
            Toast.makeText(getActivity(), OResource.string(getActivity(),
                    R.string.label_quotation_created) + " " +
                    convertRequestRecord.getString("name"), Toast.LENGTH_LONG).show();
            parent().sync().requestSync(SaleOrder.AUTHORITY);
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

    @Override
    public boolean onBackPressed() {
        if (mSheet != null && mSheet.isShowing()) {
            mSheet.dismiss();
            return false;
        }
        return true;
    }

    @Override
    protected void onNavSpinnerDestroy() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(0);
    }
}
