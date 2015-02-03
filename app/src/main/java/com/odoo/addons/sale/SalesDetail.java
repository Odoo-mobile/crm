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
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.addons.sale.models.ProductProduct;
import com.odoo.addons.sale.models.SaleOrder;
import com.odoo.addons.sale.models.SalesOrderLine;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.ServerDataHelper;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.StringUtils;
import com.odoo.core.utils.logger.OLog;
import com.odoo.crm.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import odoo.OArguments;
import odoo.controls.ExpandableListControl;
import odoo.controls.OField;
import odoo.controls.OForm;

import static com.odoo.addons.sale.Sales.Type;

public class SalesDetail extends ActionBarActivity implements View.OnClickListener {
    public static final String TAG = SalesDetail.class.getSimpleName();
    public static final int REQUEST_ADD_ITEMS = 323;
    private Bundle extra;
    private OForm mForm;
    private ODataRow record;
    private SaleOrder sale;
    private ActionBar actionBar;
    private Menu menu;
    private ExpandableListControl mList;
    private ExpandableListControl.ExpandableListAdapter mAdapter;
    private List<Object> objects = new ArrayList<>();
    private HashMap<String, Float> lineValues = new HashMap<>();
    private TextView txvType, currency, total_amt;
    private ODataRow currencyObj;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sale_detail);
        OActionBarUtils.setActionBar(this, true);
        actionBar = getSupportActionBar();
        sale = new SaleOrder(this, null);
        extra = getIntent().getExtras();
        currencyObj = sale.currency();
        init();
        initAdapter();
    }

    private void init() {
        mForm = (OForm) findViewById(R.id.saleForm);
        mForm.setEditable(true);
        txvType = (TextView) findViewById(R.id.txvType);
        currency = (TextView) findViewById(R.id.currency);
        currency.setText(currencyObj.getString("name"));
        total_amt = (TextView) findViewById(R.id.fTotal);
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
            if (!record.getString("partner_id").equals("false")) {
                OnCustomerChangeUpdate onCustomerChangeUpdate = new OnCustomerChangeUpdate();
                onCustomerChangeUpdate.execute(record.getM2ORecord("partner_id").browse());
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
            total_amt.setText(String.format("%.2f", record.getFloat("amount_total")));
            mForm.initForm(record);
        }
    }

    private void initAdapter() {

        mList = (ExpandableListControl) findViewById(R.id.expListOrderLine);
        mList.setVisibility(View.VISIBLE);
        if (extra != null) {
            record = sale.browse(extra.getInt(OColumn.ROW_ID));
            List<ODataRow> lines = record.getO2MRecord("order_line").browseEach();
            for (ODataRow line : lines) {
                lineValues.put(line.getM2ORecord("product_id").browse().getString("id"),
                        line.getFloat("product_uom_qty"));
            }
            objects.addAll(lines);
        }
        mAdapter = mList.getAdapter(R.layout.sale_order_line_item, objects,
                new ExpandableListControl.ExpandableListAdapterGetViewListener() {
                    @Override
                    public View getView(int position, View mView, ViewGroup parent) {
                        ODataRow row = (ODataRow) mAdapter.getItem(position);
                        OControls.setText(mView, R.id.edtName, row.getString("name"));
                        OControls.setText(mView, R.id.edtProductQty, row.getString("product_uom_qty"));
                        OControls.setText(mView, R.id.edtProductPrice, String.format("%.2f", row.getFloat("price_unit")));
                        OControls.setText(mView, R.id.edtSubTotal, String.format("%.2f", row.getFloat("price_subtotal")));
                        return mView;
                    }
                });
        mAdapter.notifyDataSetChanged(objects);
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
                    if (app.inNetwork()) {
                        values.put("partner_name", partner.getName(values.getInt("partner_id")));
                        //TODO: Mange order lines
                        for (Object line : objects) {
                            ODataRow row = (ODataRow) line;
                            OLog.log(">>>>>>>>> " + row);
                        }
                        if (false == true) {
                            if (record != null) {
                                if (record.getString("state").equals("cancel")) {
                                    if (app.inNetwork()) {
                                        OArguments args = new OArguments();
                                        args.add(new JSONArray().put(extra.getInt("id")));
                                        sale.getServerDataHelper().callMethod("copy_quotation", args);
                                    } else {
                                        Toast.makeText(this, R.string.toast_network_required, Toast.LENGTH_LONG).show();
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
                                values.put("currency_symbol", currencyObj.getString("name"));
                                values.put("currency_id", currencyObj.getInt(OColumn.ROW_ID));
                                values.put("order_line_count", "(No Lines)");
                                values.put("amount_total", "0.0");
                                sale.insert(values);
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(this, R.string.toast_network_required, Toast.LENGTH_LONG).show();
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
                        OAlert.showWarning(this, R.string.label_no_order_line + "");
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
            Toast.makeText(SalesDetail.this, R.string.label_quotation_confirm, Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        public void OnCancelled() {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layoutAddItem:
                if (mForm.getValues() != null) {
                    Intent intent = new Intent(this, AddProductLineWizard.class);
                    Bundle extra = new Bundle();
                    for (String key : lineValues.keySet()) {
                        extra.putFloat(key, lineValues.get(key));
                    }
                    intent.putExtras(extra);
                    startActivityForResult(intent, REQUEST_ADD_ITEMS);
                }
                break;
        }
    }

    private class OnCustomerChangeUpdate extends AsyncTask<ODataRow, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SalesDetail.this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.title_please_wait);
            progressDialog.setMessage(OResource.string(SalesDetail.this, R.string.title_working));
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(ODataRow... params) {
            sale.onPartnerIdChange(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

    private class OnProductChange extends AsyncTask<HashMap<String, Float>, Void, List<ODataRow>> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SalesDetail.this);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(R.string.title_please_wait);
            progressDialog.setMessage(OResource.string(SalesDetail.this, R.string.title_working));
            progressDialog.show();
        }

        @Override
        protected List<ODataRow> doInBackground(HashMap<String, Float>... params) {
            List<ODataRow> items = new ArrayList<>();
            try {
                ProductProduct productProduct = new ProductProduct(SalesDetail.this, sale.getUser());
                SalesOrderLine saleLine = new SalesOrderLine(SalesDetail.this, sale.getUser());
                ResPartner partner = new ResPartner(SalesDetail.this, sale.getUser());
                ODataRow customer = partner.browse(mForm.getValues().getInt("partner_id"));
                ServerDataHelper helper = saleLine.getServerDataHelper();
                for (String key : params[0].keySet()) {
                    ODataRow product = productProduct.browse(productProduct.selectRowId(Integer.parseInt(key)));
                    Float qty = params[0].get(key);
                    OArguments arguments = new OArguments();
                    arguments.add(new JSONArray());
                    int pricelist = customer.getInt("pricelist_id");
                    arguments.add(pricelist); // Price List for customer
                    arguments.add(product.getInt("id")); // product id
                    arguments.add(qty); // Quantity
                    arguments.add(false); // UOM
                    arguments.add(qty); // Qty_UOS
                    arguments.add(false);// UOS
                    arguments.add(product.getString("name"));
                    arguments.add(customer.getInt("id")); // Partner id
                    arguments.add(false); // lang
                    arguments.add(true); // update_tax
                    arguments.add(customer.getString("date_order")); // date order
                    arguments.add(false); // packaging
                    arguments.add(customer.getString("fiscal_position"));// fiscal position
                    arguments.add(false); // flag

                    JSONObject context = new JSONObject();
                    context.put("partner_id", customer.getInt("id"));
                    context.put("quantity", qty);
                    context.put("pricelist", pricelist);
                    JSONObject res = ((JSONObject) helper.callMethod("product_id_change", arguments, context))
                            .getJSONObject("value");
                    OValues values = new OValues();
                    values.put("product_id", product.getInt("id"));
                    values.put("name", res.get("name"));
                    values.put("product_uom_qty", res.get("product_uos_qty"));
                    values.put("price_unit", res.get("price_unit"));
                    values.put("price_subtotal", res.getDouble("price_unit") * res.getDouble("product_uos_qty"));
                    if (extra != null)
                        values.put("order_id", extra.getInt(OColumn.ROW_ID));
                    items.add(values.toDataRow());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<ODataRow> row) {
            super.onPostExecute(row);
            if (row != null) {
                objects.clear();
                objects.addAll(row);
                mAdapter.notifyDataSetChanged(objects);
                float total = 0.0f;
                for (ODataRow rec : row) {
                    total += rec.getFloat("price_subtotal");
                }
                total_amt.setText(String.format("%.2f", total));
            }
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_ITEMS && resultCode == Activity.RESULT_OK) {
            lineValues.clear();
            for (String key : data.getExtras().keySet()) {
                if (data.getExtras().getFloat(key) > 0)
                    lineValues.put(key, data.getExtras().getFloat(key));
            }
            OnProductChange onProductChange = new OnProductChange();
            onProductChange.execute(lineValues);
        }
    }

}
