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
 * Created on 28/1/15 10:57 AM
 */
package com.odoo.addons.crm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.addons.crm.models.CRMLead;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.crm.R;

import odoo.controls.OForm;


public class ConvertToQuotation extends ActionBarActivity implements View.OnClickListener {
    public static final String TAG = ConvertToQuotation.class.getSimpleName();
    private Bundle extra;
    private OForm convert_form;
    private CRMLead crmLead = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crm_convert_to_quotation);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getSupportActionBar().hide();
        setResult(RESULT_CANCELED);
        extra = getIntent().getExtras();
        convert_form = (OForm) findViewById(R.id.convert_form);
        convert_form.setEditable(true);
        convert_form.initForm(null);
        findViewById(R.id.create_quotation).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        crmLead = new CRMLead(this, null);
        ODataRow lead = crmLead.browse(extra.getInt(OColumn.ROW_ID));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_quotation:
                boolean mark_won = convert_form.getValues().getBoolean("mark_won");
                Intent data = new Intent();
                data.putExtra("mark_won", mark_won);
                setResult(RESULT_OK, data);
                finish();
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }
}
