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
 * Created on 21/1/15 5:39 PM
 */
package com.odoo.addons.sale;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.odoo.addons.sale.models.SaleOrder;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.list.OListAdapter;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.logger.OLog;
import com.odoo.crm.R;

import java.util.ArrayList;
import java.util.List;

public class SaleOrderLine extends ActionBarActivity {
    public static final String TAG = SaleOrderLine.class.getSimpleName();
    private SaleOrder sale;
    private ActionBar actionBar;
    private Bundle extra;
    private ODataRow record;
    private ListView mList;
    private OListAdapter mAdapter;
    List<Object> objects = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sale_order_line);
        OLog.log("Order Line" + getIntent().getExtras());
        OActionBarUtils.setActionBar(this, true);
        actionBar = getSupportActionBar();
        sale = new SaleOrder(this, null);
        extra = getIntent().getExtras();
        actionBar.setTitle("Order Line");
        initAdapter();
    }

    private void initAdapter() {
        record = sale.browse(extra.getInt(OColumn.ROW_ID));
        TextView txvTotal = (TextView) findViewById(R.id.txvTotal);
        txvTotal.setText("Total  :  " + record.getString("amount_total"));
        List<ODataRow> lines = record.getO2MRecord("order_line").browseEach();
        objects.addAll(lines);
        mList = (ListView) findViewById(R.id.listview);
        mAdapter = new OListAdapter(getApplicationContext(), R.layout.sale_order_line_item, objects) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView = convertView;
                if (mView == null) {
                    mView = LayoutInflater.from(SaleOrderLine.this).inflate(getResource(), parent, false);
                }
                ODataRow row = (ODataRow) mAdapter.getItem(position);
                OControls.setText(mView, R.id.edtName, row.getString("name"));
                OControls.setText(mView, R.id.edtProductQty, row.getString("product_uom_qty"));
                OControls.setText(mView, R.id.edtProductPrice, row.getString("price_unit"));
                OControls.setText(mView, R.id.edtSubTotal, row.getString("price_subtotal"));

                return mView;
            }
        };
        mList.setAdapter(mAdapter);
    }
}
