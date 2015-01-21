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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.odoo.addons.sale.models.SaleOrder;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.logger.OLog;
import com.odoo.crm.R;

import odoo.controls.OField;
import odoo.controls.OForm;

import static com.odoo.addons.sale.Sales.Type;

public class SalesDetail extends ActionBarActivity {
    public static final String TAG = SalesDetail.class.getSimpleName();
    private Bundle extra;
    private OForm mForm;
    private ODataRow record;
    private SaleOrder sale;
    private ActionBar actionBar;
    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sale_detail);
        OActionBarUtils.setActionBar(this, true);
        actionBar = getSupportActionBar();
        sale = new SaleOrder(this, null);
        extra = getIntent().getExtras();
        init();
    }

    private void init() {
        mForm = (OForm) findViewById(R.id.saleForm);
        if (extra == null) {
            mForm.setEditable(true);
            mForm.initForm(null);
            actionBar.setTitle(R.string.label_new);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_close);
        } else {
            record = sale.browse(extra.getInt(OColumn.ROW_ID));
            if (record == null) {
                finish();
            }
            if (extra.getString("type").equals(Type.Quotation.toString())) {
                actionBar.setTitle(R.string.label_quotation);
            } else {
                actionBar.setTitle(R.string.label_sale_orders);
            }
            mForm.setEditable(false);
            mForm.initForm(record);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sale_detail, menu);
        this.menu = menu;
        if (!extra.getString("type").equals(Type.SaleOrder.toString())) {
            toggleMenu((getIntent().getExtras() == null) ? true : false);
            menu.findItem(R.id.menu_sale_confirm_sale).setVisible(true);
            menu.findItem(R.id.menu_sale_create_invoice).setVisible(false);
        } else {
            menu.findItem(R.id.menu_sale_save).setVisible(false);
            menu.findItem(R.id.menu_sale_edit).setVisible(false);
            menu.findItem(R.id.menu_sale_confirm_sale).setVisible(false);
            menu.findItem(R.id.menu_sale_create_invoice).setVisible(true);
        }
        if (extra != null && record.getString("state").equals("cancel")) {
            menu.findItem(R.id.menu_sale_new_copy_of_quotation).setVisible(true);
            menu.findItem(R.id.menu_sale_save).setVisible(false);
            menu.findItem(R.id.menu_sale_edit).setVisible(false);
            menu.findItem(R.id.menu_sale_confirm_sale).setVisible(false);
            menu.findItem(R.id.menu_sale_create_invoice).setVisible(false);
            menu.findItem(R.id.menu_sale_cancel_order).setVisible(false);
        } else {
            menu.findItem(R.id.menu_sale_cancel_order).setVisible(true);
            menu.findItem(R.id.menu_sale_new_copy_of_quotation).setVisible(false);
        }
        if (extra != null && record.getString("state").equals("progress"))
            menu.findItem(R.id.menu_sale_create_invoice).setTitle("View Invoice");
        return true;
    }

    private void toggleMenu(boolean editModel) {
        if (editModel) {
            // Create mode (visible: save only)
            menu.findItem(R.id.menu_sale_edit).setVisible(false);
            menu.findItem(R.id.menu_sale_save).setVisible(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_close);
            OField name = (OField) mForm.findViewById(R.id.fname);
            name.setEditable(false);
        } else {
            // display mode (visible: edit, more)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);
            menu.findItem(R.id.menu_sale_save).setVisible(false);
            menu.findItem(R.id.menu_sale_edit).setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        OValues values = mForm.getValues();
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mForm.getEditable() && getIntent().getExtras() == null)
                    finish();
                else {
                    if (mForm.getEditable()) {
                        mForm.setEditable(!mForm.getEditable());
                        toggleMenu(mForm.getEditable());
                    } else {
                        finish();
                    }
                }
                break;
            case R.id.menu_sale_edit:
                mForm.setEditable(!mForm.getEditable());
                toggleMenu(mForm.getEditable());
                break;
            case R.id.menu_sale_save:
                if (values != null) {
                    if (record != null) {
                        sale.update(record.getInt(OColumn.ROW_ID), values);
                        toggleMenu(false);
                        mForm.setEditable(false);
                    } else {
                        values.put("create_date", ODateUtils.getUTCDate());
                        values.put("user_id", ResUsers.myId(this));
                        sale.insert(values);
                        finish();
                    }
                }
                break;
            case R.id.menu_sale_cancel_order:
                OLog.log("Cancel");
                if (values != null) {
                    if (record != null) {
                        values.put("state", "cancel");
                        sale.update(record.getInt(OColumn.ROW_ID), values);
                        finish();
                    }
                }
                break;
            case R.id.menu_sale_confirm_sale:
                OLog.log("Confirm");
//                TODO check the order line or not
                if (values != null) {
                    if (record != null) {
                        if (extra != null && record.getFloat("amount_total") > 0) {
                            values.put("state", "manual");
                            sale.update(record.getInt(OColumn.ROW_ID), values);
                            finish();
                        } else {
                            OLog.log("You cannot a sales order which has no line");
                        }
                    }
                }
                break;
            case R.id.menu_sale_new_copy_of_quotation:
                OLog.log("Quotation");
                break;
            case R.id.menu_sale_create_invoice:
                if (extra != null && record.getString("state").equals("progress")) {
                    OLog.log("View");
                    if (values != null) {
                        if (record != null) {
                            values.put("state", "done");
                            sale.update(record.getInt(OColumn.ROW_ID), values);
                            finish();
                        }
                    }
                } else {
                    OLog.log("Create");
                    if (values != null) {
                        if (record != null) {
                            values.put("state", "progress");
                            sale.update(record.getInt(OColumn.ROW_ID), values);
                            finish();
                        }
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
