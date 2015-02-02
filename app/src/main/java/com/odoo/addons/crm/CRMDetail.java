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
 * Created on 13/1/15 3:40 PM
 */
package com.odoo.addons.crm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.odoo.App;
import com.odoo.addons.crm.models.CRMCaseStage;
import com.odoo.addons.crm.models.CRMLead;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.OAlert;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.StringUtils;
import com.odoo.crm.R;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OForm;

public class CRMDetail extends ActionBarActivity {
    public static final String TAG = CRMDetail.class.getSimpleName();
    public static final int REQUEST_CONVERT_TO_OPPORTUNITY_WIZARD = 1223;
    public static final int REQUEST_CONVERT_TO_QUOTATION_WIZARD = 1224;
    private Bundle extra;
    private OForm mForm;
    private ODataRow record;
    private CRMLead crmLead;
    private ActionBar actionBar;
    private Menu menu;
    private String wonLost = "won";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crm_detail);
        OActionBarUtils.setActionBar(this, true);
        actionBar = getSupportActionBar();
        crmLead = new CRMLead(this, null);
        extra = getIntent().getExtras();
        init();
    }

    private void init() {
        mForm = (OForm) findViewById(R.id.crmLeadForm);
        if (!extra.containsKey(OColumn.ROW_ID)) {
            if (extra.getString("type").equals(CRM.Type.Opportunities.toString())) {
                findViewById(R.id.opportunity_controls).setVisibility(View.VISIBLE);
            }
            mForm.initForm(null);
            actionBar.setTitle(R.string.label_tag_new);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_close);
        } else {
            initFormValues();
        }
        mForm.setEditable(true);

    }

    private void initFormValues() {
        record = crmLead.browse(extra.getInt(OColumn.ROW_ID));
        if (record == null) {
            finish();
        }
        if (!record.getString("type").equals("lead")) {
            actionBar.setTitle(R.string.label_opportunity);
            findViewById(R.id.opportunity_controls).setVisibility(View.VISIBLE);
        } else {
            actionBar.setTitle(R.string.label_lead);
        }
        mForm.initForm(record);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lead_detail, menu);
        this.menu = menu;
        toggleMenu();
        return true;
    }

    private void toggleMenu() {
        if (!extra.containsKey(OColumn.ROW_ID)) {
            menu.findItem(R.id.menu_lead_detail_more).setVisible(false);
        } else {
            initFormValues();
            menu.findItem(R.id.menu_lead_detail_more).setVisible(true);
            if (record.getString("type").equals(CRMLead.KEY_LEAD)) {
                menu.findItem(R.id.menu_lead_convert_to_quotation).setVisible(false);
                menu.findItem(R.id.menu_mark_won).setVisible(false);
            } else if (record.getString("type").equals(crmLead.KEY_OPPORTUNITY)) {
                menu.findItem(R.id.menu_lead_convert_to_opportunity).setVisible(false);
            }
        }
        menu.findItem(R.id.menu_lead_save).setVisible(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_close);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        App app = (App) getApplicationContext();
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_lead_save:
                OValues values = mForm.getValues();
                if (values != null) {
                    if (record != null) {
                        crmLead.update(record.getInt(OColumn.ROW_ID), values);
                    } else {
                        values.put("create_date", ODateUtils.getUTCDate());
                        values.put("user_id", ResUsers.myId(this));
                        CRMCaseStage stages = new CRMCaseStage(this, null);
                        ODataRow row = stages.browse(new String[]{"name"}, "name = ?", new String[]{"New"});
                        if (row != null) {
                            values.put("stage_id", row.getInt(OColumn.ROW_ID));
                            values.put("stage_name", row.getString("name"));
                        }
                        values.put("display_name", values.getString("partner_name"));
                        values.put("assignee_name", crmLead.getUser().getName());
                        crmLead.insert(values);
                    }
                    finish();

                }
                break;
            case R.id.menu_lead_convert_to_opportunity:
                if (record.getInt("id") == 0) {
                    OAlert.showWarning(this, "Need to sync before converting to Opportunity");
                } else {
                    if (app.inNetwork()) {
                        int count = crmLead.count("id != ? and partner_id = ? and " + OColumn.ROW_ID + " != ?"
                                , new String[]{
                                "0",
                                record.getInt("partner_id") + "",
                                record.getString(OColumn.ROW_ID)
                        });
                        if (count > 0) {
                            Intent intent = new Intent(this, ConvertToOpportunityWizard.class);
                            intent.putExtras(record.getPrimaryBundleData());
                            startActivityForResult(intent, REQUEST_CONVERT_TO_OPPORTUNITY_WIZARD);
                        } else {
                            crmLead.convertToOpportunity(record, new ArrayList<Integer>(), convertDoneListener);
                        }
                    } else {
                        Toast.makeText(this, R.string.toast_network_required, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.menu_mark_won:
                if (app.inNetwork()) {
                    crmLead.markWonLost(wonLost, record, markDoneListener);
                } else {
                    Toast.makeText(this, R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_mark_lost:
                wonLost = "lost";
                if (app.inNetwork()) {
                    crmLead.markWonLost(wonLost, record, markDoneListener);
                } else {
                    Toast.makeText(this, R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.menu_lead_convert_to_quotation:
                if (app.inNetwork()) {
                    Intent intent = new Intent(this, ConvertToQuotation.class);
                    intent.putExtras(record.getPrimaryBundleData());
                    startActivityForResult(intent, REQUEST_CONVERT_TO_QUOTATION_WIZARD);
                } else {
                    Toast.makeText(this, R.string.toast_network_required, Toast.LENGTH_LONG).show();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONVERT_TO_OPPORTUNITY_WIZARD && resultCode == RESULT_OK) {
            List<Integer> ids = data.getIntegerArrayListExtra(ConvertToOpportunityWizard.KEY_LEADS_IDS);
            crmLead.convertToOpportunity(record, ids, convertDoneListener);
        }
        if (requestCode == REQUEST_CONVERT_TO_QUOTATION_WIZARD && resultCode == Activity.RESULT_OK) {
            crmLead.createQuotation(record, data.getBooleanExtra("mark_won", false), createQuotationListener);
        }
    }

    CRMLead.OnOperationSuccessListener createQuotationListener = new CRMLead.OnOperationSuccessListener() {
        @Override
        public void OnSuccess() {
            Toast.makeText(CRMDetail.this, "Quotation created for " +
                    record.getString("name"), Toast.LENGTH_LONG).show();
        }

        @Override
        public void OnCancelled() {

        }
    };
    CRMLead.OnOperationSuccessListener markDoneListener = new CRMLead.OnOperationSuccessListener() {
        @Override
        public void OnSuccess() {
            Toast.makeText(CRMDetail.this, StringUtils.capitalizeString(record.getString("type"))
                    + " marked " + wonLost, Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        public void OnCancelled() {

        }
    };
    CRMLead.OnOperationSuccessListener convertDoneListener = new CRMLead.OnOperationSuccessListener() {
        @Override
        public void OnSuccess() {
            Toast.makeText(CRMDetail.this, "Converted to opportunity", Toast.LENGTH_LONG).show();
            finish();
        }

        @Override
        public void OnCancelled() {

        }
    };
}
