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
 * Created on 13/2/15 11:56 AM
 */
package com.odoo.addons.crm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.R;
import com.odoo.addons.crm.models.CRMCaseStage;
import com.odoo.addons.crm.models.CRMLead;
import com.odoo.addons.customers.Customers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.addons.fragment.BaseFragment;
import com.odoo.core.support.drawer.ODrawerItem;
import com.odoo.core.support.list.OListAdapter;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.sys.IOnBackPressListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import odoo.controls.OControlHelper;

public class CRMOpportunitiesPager extends BaseFragment implements ViewPager.OnPageChangeListener, AdapterView.OnItemSelectedListener, IOnBackPressListener, SwipeRefreshLayout.OnRefreshListener {
    public static final String TAG = CRMOpportunitiesPager.class.getSimpleName();
    public static final String KEY_MENU = "key_menu_item";
    private CRMLeads.Type mType = CRMLeads.Type.Opportunities;
    private Context mContext;
    private Handler handler;
    private DataObserver observer;
    private Cursor cursor = null;
    private String[] projection = new String[]{"name"};
    private CRMCaseStage crmStage;
    public static final String KEY_STAGE_ID = "stage_id";
    public static final String KEY_FILTER = "key_filter";
    private ViewPager mPager;
    private PagerTabStrip mTabStrip;
    private HashMap<String, Fragment> mFragments = new HashMap<>();
    private StagePagerAdapter mAdapter;
    private Boolean filterCustomerOpp = false;
    private int customer_id = -1;
    private Spinner mNavSpinner = null;
    private OListAdapter mNavSpinnerAdapter = null;
    private List<Object> spinnerItems = new ArrayList<>();
    private int selectedPagerPosition = 0;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.crm_opportunity_pagger, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView = view;
        mContext = getActivity();
        parent().setOnBackPressListener(this);
        Bundle extra = getArguments();
        if (extra.containsKey(Customers.KEY_FILTER_REQUEST)) {
            filterCustomerOpp = true;
            customer_id = extra.getInt(Customers.KEY_CUSTOMER_ID);
        }

        crmStage = new CRMCaseStage(getActivity(), null);
        handler = new Handler();
        observer = new DataObserver(handler);
        parent().setHasActionBarSpinner(true);
        mNavSpinner = parent().getActionBarSpinner();
        initPager(view);
        initSpinner();
    }

    private void initSpinner() {
        if (getActivity() == null) {
            return;
        }
        spinnerItems.clear();
        spinnerItems.addAll(crmStage.select(null, "type!=?", new String[]{"lead"}, "sequence"));
        if (spinnerItems.isEmpty()) {
            parent().setHasActionBarSpinner(false);
            mPager.setVisibility(View.GONE);
            setHasSwipeRefreshView(mView, R.id.no_items_found, this);
            OControls.setVisible(mView, R.id.no_items_found);
            OControls.setVisible(mView, R.id.dashboard_no_item_view);
            OControls.setText(mView, R.id.title, OResource.string(getActivity(),
                    R.string.label_no_opportunity_found));
            OControls.setText(mView, R.id.subTitle, "");
            OControls.setImage(mView, R.id.icon, R.drawable.ic_action_opportunities);
            return;
        } else {
            mPager.setVisibility(View.VISIBLE);
            OControls.setGone(mView, R.id.no_items_found);
            OControls.setGone(mView, R.id.dashboard_no_item_view);
        }
        mNavSpinnerAdapter = new OListAdapter(getActivity(), R.layout.base_simple_list_item_1, spinnerItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getActivity()).inflate(R.layout.base_simple_list_item_1_selected
                            , parent, false);
                }
                return getSpinnerView(getItem(position), position, convertView, parent);
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getActivity()).inflate(getResource(), parent, false);
                }
                return getSpinnerView(getItem(position), position, convertView, parent);
            }
        };
        mNavSpinner.setAdapter(mNavSpinnerAdapter);
        mNavSpinner.setOnItemSelectedListener(this);
    }

    private View getSpinnerView(Object row, int pos, View view, ViewGroup parent) {
        ODataRow r = (ODataRow) row;
        OControls.setText(view, android.R.id.text1, r.getString("name"));
        return view;
    }

    private void initPager(View view) {
        getActivity().getContentResolver().registerContentObserver(
                crmStage.uri(), true, observer);
        initCR();
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setOnPageChangeListener(this);
        mTabStrip = (PagerTabStrip) view.findViewById(R.id.pager_title_strip);
        mTabStrip.setTabIndicatorColor(Color.WHITE);
        mPager.setOffscreenPageLimit(2);
        mAdapter = new StagePagerAdapter(cursor, getChildFragmentManager());
        mPager.setAdapter(mAdapter);
        for (int i = 0; i < mTabStrip.getChildCount(); ++i) {
            View nextChild = mTabStrip.getChildAt(i);
            if (nextChild instanceof TextView) {
                TextView textViewToConvert = (TextView) nextChild;
                textViewToConvert.setAllCaps(true);
                textViewToConvert.setTextColor(Color.WHITE);
                textViewToConvert.setTypeface(OControlHelper.boldFont());
            }
        }
    }

    private void initCR() {
        cursor = mContext.getContentResolver().query(crmStage.uri(),
                projection, "type != ?", new String[]{"lead"}, "sequence");
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {

    }

    @Override
    public void onPageSelected(int position) {
        mNavSpinner.setSelection(position);
        selectedPagerPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mPager.setCurrentItem(position, true);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onBackPressed() {
        if (mFragments.size() > 0) {
            return ((IOnBackPressListener) mFragments.get("index_" + mNavSpinner.getSelectedItemPosition())
            ).onBackPressed();
        }
        return true;
    }

    @Override
    public void onRefresh() {
        if (inNetwork()) {
            Bundle syncBundle = new Bundle();
            syncBundle.putBoolean(CRMOpportunities.KEY_IS_LEAD, false);
            parent().sync().requestSync(CRMLead.AUTHORITY, syncBundle);
            setSwipeRefreshing(true);
        } else {
            hideRefreshingProgress();
            Toast.makeText(getActivity(), _s(R.string.toast_network_required), Toast.LENGTH_LONG)
                    .show();
        }
    }

    private class StagePagerAdapter extends FragmentStatePagerAdapter {

        private String key_filter;

        public StagePagerAdapter(Cursor cursor, FragmentManager fm) {
            super(fm);
            key_filter = "opportunity";
        }

        @Override
        public CharSequence getPageTitle(int position) {
            cursor.moveToPosition(position);
            String name = cursor.getString(cursor.getColumnIndex("name"));
            int row_id = cursor.getInt(cursor.getColumnIndex(OColumn.ROW_ID));
            String where = "stage_id = ? and type != ?";
            List<String> args = new ArrayList<>();
            args.add(row_id + "");
            args.add("lead");
            if (filterCustomerOpp) {
                where += " and partner_id = ?";
                args.add(customer_id + "");
            }
            int count = db().count(where, args.toArray(new String[args.size()]));
            if (count > 0)
                name += " (" + count + ")";
            return name;
        }

        @Override
        public Fragment getItem(int index) {
            CRMOpportunities crm = new CRMOpportunities();
            cursor.moveToPosition(index);
            int stage_id = cursor.getInt(cursor.getColumnIndex(OColumn.ROW_ID));
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_STAGE_ID, stage_id);
            bundle.putString(KEY_FILTER, key_filter);
            bundle.putInt("index", index);
            if (filterCustomerOpp) {
                bundle.putBoolean(Customers.KEY_FILTER_REQUEST, true);
                bundle.putInt(Customers.KEY_CUSTOMER_ID, customer_id);
                bundle.putString("name", getArguments().getString("name"));
            }
            crm.setArguments(bundle);
            mFragments.put("index_" + index, crm);
            return crm;
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            super.restoreState(null, loader);
        }

        @Override
        public int getCount() {
            return cursor.getCount();
        }

    }

    @Override
    public List<ODrawerItem> drawerMenus(Context context) {
        return new ArrayList<>();
    }

    @Override
    public Class<CRMLead> database() {
        return CRMLead.class;
    }

    private class DataObserver extends ContentObserver {

        public DataObserver(Handler handler) {
            super(handler);
        }

        @SuppressLint("NewApi")
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        @SuppressLint("NewApi")
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            updatePager();
        }
    }

    public void updatePager() {
        initCR();
        initSpinner();
        mAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(selectedPagerPosition);
    }

}
