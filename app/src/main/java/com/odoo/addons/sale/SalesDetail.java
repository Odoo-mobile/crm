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
 * Created on 13/1/15 5:09 PM
 */
package com.odoo.addons.sale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.addons.sale.models.SaleOrder;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.list.OListAdapter;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.StringUtils;
import com.odoo.core.utils.controls.ExpandableHeightGridView;
import com.odoo.core.utils.logger.OLog;
import com.odoo.core.utils.sys.IOnActivityResultListener;
import com.odoo.crm.R;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import odoo.OArguments;
import odoo.controls.OField;
import odoo.controls.OForm;

import static com.odoo.addons.sale.Sales.Type;

public class SalesDetail extends ActionBarActivity implements View.OnClickListener, IOnActivityResultListener {
    public static final String TAG = SalesDetail.class.getSimpleName();
    public static final int REQUEST_ADD_ITEM = 323;
    private Bundle extra;
    private OForm mForm;
    private ODataRow record;
    private SaleOrder sale;
    private ActionBar actionBar;
    private Menu menu;
    private ExpandableHeightGridView mList;
    private OListAdapter mAdapter;
    List<Object> objects = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sale_detail);
        OActionBarUtils.setActionBar(this, true);
        actionBar = getSupportActionBar();
        sale = new SaleOrder(this, null);
        extra = getIntent().getExtras();
        init();
        initAdapter();
    }

    private void init() {
        mForm = (OForm) findViewById(R.id.saleForm);
        mForm.setEditable(true);
        TextView txvType = (TextView) findViewById(R.id.txvType);
        TextView currency = (TextView) findViewById(R.id.currency);
        TextView total_amt = (TextView) findViewById(R.id.fTotal);
        LinearLayout layoutAddItem = (LinearLayout) findViewById(R.id.layoutAddItem);
        layoutAddItem.setOnClickListener(this);
        if (extra == null) {
            mForm.initForm(null);
            actionBar.setTitle(R.string.label_new);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_close);
            txvType.setText(R.string.label_quotation);
        } else {
            record = sale.browse(extra.getInt(OColumn.ROW_ID));
            if (record == null) {
                finish();
            }
            if (extra.getString("type").equals(Type.Quotation.toString())) {
                actionBar.setTitle(R.string.label_quotation);
                txvType.setText(R.string.label_quotation);
            } else {
                actionBar.setTitle(R.string.label_sale_orders);
                txvType.setText(R.string.label_sale_orders);
                mForm.setEditable(false);
            }
            currency.setText(record.getM2ORecord("currency_id").browse().getString("name"));
            total_amt.setText(record.getString("amount_total"));
            mForm.initForm(record);
        }
    }

    private void initAdapter() {
        if (extra != null) {
            mList = (ExpandableHeightGridView) findViewById(R.id.expListOrderLine);
            mList.setVisibility(View.VISIBLE);
            record = sale.browse(extra.getInt(OColumn.ROW_ID));
            List<ODataRow> lines = record.getO2MRecord("order_line").browseEach();
            objects.addAll(lines);
            mAdapter = new OListAdapter(this, R.layout.sale_order_line_item, objects) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View mView = convertView;
                    if (mView == null) {
                        mView = LayoutInflater.from(SalesDetail.this).inflate(getResource(), parent, false);
                    }
                    ODataRow row = (ODataRow) mAdapter.getItem(position);
                    OControls.setText(mView, R.id.edtName, row.getString("name"));
                    OControls.setText(mView, R.id.edtProductQty, row.getString("product_uom_qty"));
                    OControls.setText(mView, R.id.edtProductPrice, row.getString("price_unit"));
                    OControls.setText(mView, R.id.edtSubTotal, row.getString("price_subtotal"));
                    return mView;
                }
            };
            mList.setExpanded(true);
            mList.setAdapter(mAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sale_detail, menu);
        this.menu = menu;
        OField name = (OField) mForm.findViewById(R.id.fname);
        name.setEditable(false);
        if (extra != null && !extra.getString("type").equals(Type.SaleOrder.toString())) {
            menu.findItem(R.id.menu_sale_confirm_sale).setVisible(true);
        } else {
            menu.findItem(R.id.menu_sale_save).setVisible(false);
            menu.findItem(R.id.menu_sale_confirm_sale).setVisible(false);
        }
        if (extra != null && record.getString("state").equals("cancel")) {
            menu.findItem(R.id.menu_sale_save).setVisible(true).setTitle("Copy Quotation");
            menu.findItem(R.id.menu_sale_detail_more).setVisible(false);
            mForm.setEditable(true);
        } else {
            menu.findItem(R.id.menu_sale_cancel_order).setVisible(true);
            menu.findItem(R.id.menu_sale_new_copy_of_quotation).setVisible(false);
        }
        if (extra == null) {
            menu.findItem(R.id.menu_sale_save).setVisible(true);
            menu.findItem(R.id.menu_sale_detail_more).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        OValues values = mForm.getValues();
        ResPartner partner = new ResPartner(this, null);
        App app = (App) getApplicationContext();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_sale_save:
                if (values != null) {
                    values.put("partner_name", partner.getName(values.getInt("partner_id")));
                    if (record != null) {
                        if (record.getString("state").equals("cancel")) {
                            if (app.inNetwork()) {
                                OArguments args = new OArguments();
                                args.add(new JSONArray().put(extra.getInt("id")));
                                sale.getServerDataHelper().callMethod("copy_quotation", args);
                                Toast.makeText(this, "Copy of quotation...", Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show();
                            }
                        } else
                            sale.update(record.getInt(OColumn.ROW_ID), values);
                        finish();
                    } else {
                        values.put("name", "/");
                        values.put("create_date", ODateUtils.getUTCDate());
                        values.put("user_id", ResUsers.myId(this));
                        values.put("state", "draft");
                        values.put("state_title", sale.getStateTitle(values));
                        ODataRow currency = sale.currency();
                        values.put("currency_symbol", currency.getString("name"));
                        values.put("currency_id", currency.getInt(OColumn.ROW_ID));
                        values.put("order_line_count", "(No Lines)");
                        values.put("amount_total", "0.0");
                        sale.insert(values);
                        finish();
                    }
                }
                break;
            case R.id.menu_sale_cancel_order:
                if (record != null) {
                    if (app.inNetwork()) {
                        sale.cancelOrder(Sales.Type.valueOf(extra.getString("type")), record, cancelOrder);
                    } else {
                        Toast.makeText(this, R.string.toast_network_required, Toast.LENGTH_LONG).show();
                    }
                    finish();
                }
                break;
            case R.id.menu_sale_confirm_sale:
                if (record != null) {
                    if (extra != null && record.getFloat("amount_total") > 0) {
                        if (app.inNetwork()) {
                            sale.confirmSale(record, confirmSale);
                        } else {
                            Toast.makeText(this, R.string.toast_network_required, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        OAlert.showWarning(this, "You cannot a sales order which has no line");
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    SaleOrder.OnOperationSuccessListener cancelOrder = new SaleOrder.OnOperationSuccessListener() {
        @Override
        public void OnSuccess() {
            Toast.makeText(SalesDetail.this, StringUtils.capitalizeString(extra.getString("type"))
                    + " cancelled", Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        public void OnCancelled() {

        }
    };

    SaleOrder.OnOperationSuccessListener confirmSale = new SaleOrder.OnOperationSuccessListener() {
        @Override
        public void OnSuccess() {
            Toast.makeText(SalesDetail.this, "Quotation confirmed !", Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        public void OnCancelled() {

        }
    };

    @Override
    public void onClick(View v) {
        if (extra != null && !record.getString("state").equals("cancel")) {
//            IntentUtils.startActivity(this, SaleAddItem.class, extra);
            Intent intent = new Intent(this, AddProductLineWizard.class);
            intent.putExtras(extra);
            startActivityForResult(intent, REQUEST_ADD_ITEM);
        }
    }

    @Override
    public void onOdooActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_ITEM && resultCode == Activity.RESULT_OK) {
            OLog.log(">>>> data " + data);
        } else
            OLog.log(">>>> else " + data);
    }
}
