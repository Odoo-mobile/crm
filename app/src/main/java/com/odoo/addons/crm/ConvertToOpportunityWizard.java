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
 * Created on 27/1/15 3:07 PM
 */
package com.odoo.addons.crm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.addons.crm.models.CRMLead;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.list.OListAdapter;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.StringUtils;
import com.odoo.core.utils.controls.ExpandableHeightGridView;
import com.odoo.R;

import java.util.ArrayList;
import java.util.List;

import odoo.controls.OField;
import odoo.controls.OForm;

public class ConvertToOpportunityWizard extends ActionBarActivity implements View.OnClickListener, OField.IOnFieldValueChangeListener {
    public static final String TAG = ConvertToOpportunityWizard.class.getSimpleName();
    public static final String KEY_LEADS_IDS = "key_leads_ids";
    private Bundle extra;
    private OForm convert_form;
    private CRMLead crmLead = null;
    private OListAdapter mAdapter;
    private List<Object> items = new ArrayList<>();
    private ExpandableHeightGridView mOpportunityList;
    private OField conversation_action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crm_convert_to_opportunity);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getSupportActionBar().hide();
        setResult(RESULT_CANCELED);
        extra = getIntent().getExtras();
        convert_form = (OForm) findViewById(R.id.convert_form);
        convert_form.setEditable(true);
        convert_form.initForm(null);
        findViewById(R.id.create_opportunity).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);

        conversation_action = (OField) findViewById(R.id.conversation_action);
        conversation_action.setOnValueChangeListener(this);
        init();
    }

    private void init() {
        items.clear();
        mOpportunityList = (ExpandableHeightGridView) findViewById(R.id.opportunities);
        mOpportunityList.setExpanded(true);
        crmLead = new CRMLead(this, null);
        ODataRow lead = crmLead.browse(extra.getInt(OColumn.ROW_ID));
        items.addAll(crmLead.select(null,
                "partner_id = ? and id != ? and " + OColumn.ROW_ID + " != ?",
                new String[]{
                        lead.getString("partner_id"),
                        "0",
                        lead.getString(OColumn.ROW_ID)
                }));
        mAdapter = new OListAdapter(this, R.layout.crm_convert_to_opportunity_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(ConvertToOpportunityWizard.this)
                            .inflate(getResource(), parent, false);
                }
                ODataRow row = (ODataRow) getItem(position);
                OControls.setText(convertView, R.id.name, row.getString("name"));
                OControls.setText(convertView, R.id.stage, row.getString("stage_name"));
                OControls.setText(convertView, R.id.type, StringUtils.capitalizeString(row.getString("type")));
                String date = ODateUtils.convertToDefault(row.getString("create_date"),
                        ODateUtils.DEFAULT_FORMAT, "MMMM, dd");
                OControls.setText(convertView, R.id.create_date, date);
                convertView.findViewById(R.id.remove_lead).setTag(position);
                convertView.findViewById(R.id.remove_lead).setOnClickListener(ConvertToOpportunityWizard.this);
                return convertView;
            }
        };
        mOpportunityList.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_opportunity:
                ArrayList<Integer> ids = new ArrayList<>();
                for (Object data : items) {
                    ODataRow row = (ODataRow) data;
                    ids.add(row.getInt("id"));
                }
                Intent result = new Intent();
                result.putIntegerArrayListExtra(KEY_LEADS_IDS, ids);
                setResult(RESULT_OK, result);
                finish();
                break;
            case R.id.remove_lead:
                int pos = (int) v.getTag();
                items.remove(pos);
                mAdapter.notifiyDataChange(items);
                if (items.size() == 0) {
                    conversation_action.setValue(1);
                }
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }

    @Override
    public void onFieldValueChange(OField field, Object value) {
        ODataRow record = (ODataRow) value;
        int index = record.getInt(OColumn.ROW_ID);
        switch (index) {
            case -1:
            case 0:
                findViewById(R.id.opportunity_container).setVisibility(View.GONE);
                break;
            case 1:
                findViewById(R.id.opportunity_container).setVisibility(View.VISIBLE);
                init();
                break;
        }
    }
}
