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
 * Created on 13/1/15 4:55 PM
 */
package com.odoo.addons.phonecall;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.odoo.addons.phonecall.features.receivers.PhoneStateReceiver;
import com.odoo.addons.phonecall.models.CRMPhoneCalls;
import com.odoo.addons.phonecall.models.CRMPhoneCallsCategory;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.notification.ONotificationBuilder;
import com.odoo.crm.R;

import odoo.controls.OForm;

public class PhoneCallDetail extends ActionBarActivity {
    public static final String TAG = PhoneCallDetail.class.getSimpleName();
    public static final String KEY_LOG_CALL_REQUEST = "key_log_call_request";
    public static final String KEY_PHONE_NUMBER = "key_phone_number";
    public static final String KEY_OPPORTUNITY_ID = "key_opportunity_id";
    private ActionBar actionBar;
    private Bundle extra;
    private OForm mForm;
    private ODataRow record;
    private CRMPhoneCalls crmPhoneCalls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crm_phonecall_detail);
        crmPhoneCalls = new CRMPhoneCalls(this, null);
        OActionBarUtils.setActionBar(this, true);
        actionBar = getSupportActionBar();
        actionBar.setTitle("Schedule Call");
        extra = getIntent().getExtras();
        init();
    }

    private void init() {
        mForm = (OForm) findViewById(R.id.phoneLogForm);
        if (extra != null) {
            if (!extra.containsKey(KEY_LOG_CALL_REQUEST)) {
                // Record request
                record = crmPhoneCalls.browse(extra.getInt(OColumn.ROW_ID));
                mForm.initForm(record);
            } else {
                // Logging new call
                String action = getIntent().getAction();
                if (action != null) {
                    ONotificationBuilder.cancelNotification(this, extra.getInt("notification_id"));
                    if (action.equals(PhoneStateReceiver.ACTION_CALL_BACK)) {
                        String contactNumber = extra.getString(KEY_PHONE_NUMBER);
                        IntentUtils.requestCall(this, contactNumber);
                        finish();
                    }
                }

                ODataRow record = new ODataRow();
                record.put("partner_id", extra.getInt(OColumn.ROW_ID));
                record.put("partner_phone", extra.getString(KEY_PHONE_NUMBER));
                record.put("opportunity_id", extra.getInt(KEY_OPPORTUNITY_ID));
                CRMPhoneCallsCategory.Type bound = CRMPhoneCallsCategory.Type.Inbound;
                if (!extra.getBoolean("in_bound", false)) {
                    bound = CRMPhoneCallsCategory.Type.OutBound;
                }
                record.put("categ_id", CRMPhoneCallsCategory.getId(this, bound));
                mForm.setEditable(true);
                mForm.initForm(record);
            }
        } else {
            mForm.setEditable(true);
            mForm.initForm(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
