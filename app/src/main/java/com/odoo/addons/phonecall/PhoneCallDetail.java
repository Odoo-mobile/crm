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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.odoo.addons.crm.models.CRMLead;
import com.odoo.addons.phonecall.features.receivers.PhoneStateReceiver;
import com.odoo.addons.phonecall.models.CRMPhoneCalls;
import com.odoo.addons.phonecall.models.CRMPhoneCallsCategory;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.utils.IntentUtils;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.notification.ONotificationBuilder;
import com.odoo.core.utils.reminder.ReminderReceiver;
import com.odoo.crm.R;

import java.util.Date;

import odoo.controls.OField;
import odoo.controls.OForm;

public class PhoneCallDetail extends ActionBarActivity implements OField.IOnFieldValueChangeListener {
    public static final String TAG = PhoneCallDetail.class.getSimpleName();
    public static final String KEY_LOG_CALL_REQUEST = "key_log_call_request";
    public static final String KEY_PHONE_NUMBER = "key_phone_number";
    public static final String KEY_OPPORTUNITY_ID = "key_opportunity_id";
    private ActionBar actionBar;
    private Bundle extra;
    private OForm mForm;
    private ODataRow record;
    private CRMPhoneCalls crmPhoneCalls;
    private OField phoneCallDate, opportunity_id;
    private String logType = "done";
    private Boolean updateOpportunity = false;
    private CRMLead crmLead = null;
    private OForm opportunity_action_form;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crm_phonecall_detail);
        crmPhoneCalls = new CRMPhoneCalls(this, null);
        OActionBarUtils.setActionBar(this, true);
        actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.label_log_call);
        extra = getIntent().getExtras();
        crmLead = new CRMLead(this, null);
        init();
    }

    private void init() {
        mForm = (OForm) findViewById(R.id.phoneLogForm);
        opportunity_action_form = (OForm) findViewById(R.id.opportunity_action_form);
        phoneCallDate = (OField) findViewById(R.id.phoneCallDate);
        opportunity_id = (OField) findViewById(R.id.opportunity_id);
        opportunity_id.setOnValueChangeListener(this);
        phoneCallDate.setOnValueChangeListener(this);
        mForm.setEditable(true);
        if (extra != null) {
            String action = getIntent().getAction();
            if (!extra.containsKey(KEY_LOG_CALL_REQUEST)) {

                if (extra.containsKey("opp_id")) {
                    ODataRow opp_rec = new ODataRow();
                    opp_rec.put("opportunity_id", extra.getInt("opp_id"));
                    boolean partner_edit = true;
                    if (!crmLead.browse(extra.getInt("opp_id")).getString("partner_id").equals("false")) {
                        opp_rec.put("partner_id", crmLead.browse(extra.getInt("opp_id")).getInt("partner_id"));
                        partner_edit = false;
                    }
                    mForm.initForm(opp_rec);
                    ((OField) mForm.findViewById(R.id.partner_id)).setEditable(partner_edit);
                    ((OField) mForm.findViewById(R.id.opportunity_id)).setEditable(false);
                    return;
                }

                if (action != null) {
                    if (action.equals(ReminderReceiver.ACTION_PHONE_CALL_REMINDER_CALLBACK)) {
                        String contact = extra.getString("contact");
                        if (!contact.equals("false")) {
                            IntentUtils.requestCall(this, contact);
                        } else {
                            Toast.makeText(this, R.string.label_no_contact_found, Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                    if (action.equals(ReminderReceiver.ACTION_PHONE_CALL_REMINDER_DONE)) {
                        OValues values = new OValues();
                        values.put("is_done", "1");
                        values.put("state", "done");
                        crmPhoneCalls.update(extra.getInt(OColumn.ROW_ID), values);
                        crmPhoneCalls.setReminder(extra.getInt(OColumn.ROW_ID));
                        Toast.makeText(this, R.string.toast_phone_call_marked_done, Toast.LENGTH_LONG).show();
                    }
                    ONotificationBuilder.cancelNotification(this, extra.getInt(OColumn.ROW_ID));
                }
                // Record request
                record = crmPhoneCalls.browse(extra.getInt(OColumn.ROW_ID));
                mForm.initForm(record);
            } else {
                // Logging new call
                if (action != null) {
                    ONotificationBuilder.cancelNotification(this, extra.getInt("notification_id"));
                    if (action.equals(PhoneStateReceiver.ACTION_CALL_BACK)) {
                        String contactNumber = extra.getString(KEY_PHONE_NUMBER);
                        IntentUtils.requestCall(this, contactNumber);
                        finish();
                    }
                }
                ODataRow data_record = new ODataRow();
                data_record.put("partner_id", extra.getInt(OColumn.ROW_ID));
                data_record.put("partner_phone", extra.getString(KEY_PHONE_NUMBER));
                int opp_id = extra.getInt(KEY_OPPORTUNITY_ID);
                data_record.put("opportunity_id", opp_id);
                if (extra.containsKey(PhoneStateReceiver.KEY_DURATION_START)) {
                    long start_time = Long.parseLong(extra.getString(PhoneStateReceiver.KEY_DURATION_START));
                    long end_time = Long.parseLong(extra.getString(PhoneStateReceiver.KEY_DURATION_END));
                    long duration = (end_time - start_time);
                    data_record.put("duration", ODateUtils.durationToFloat(duration));
                }
                CRMPhoneCallsCategory.Type bound = CRMPhoneCallsCategory.Type.Inbound;
                if (!extra.getBoolean("in_bound", false)) {
                    bound = CRMPhoneCallsCategory.Type.OutBound;
                }
                data_record.put("categ_id", CRMPhoneCallsCategory.getId(this, bound));
                mForm.initForm(data_record);
            }
        } else {
            mForm.initForm(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_phonecall_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_phonecall_save:
                OValues values = mForm.getValues();
                if (values != null) {
                    values.put("user_id", ResUsers.myId(this));
                    ResPartner partner = new ResPartner(this, null);
                    ODataRow row = partner.browse(values.getInt("partner_id"));
                    values.put("customer_name", row.getString("name"));
                    ODataRow lead = crmLead.browse(values.getInt("opportunity_id"));
                    values.put("lead_name", "");
                    if (lead != null) {
                        values.put("lead_name", lead.getString("name"));
                    }
                    if (updateOpportunity) {
                        OValues opp_values = opportunity_action_form.getValues();
                        if (opp_values != null) {
                            crmLead.update(lead.getInt(OColumn.ROW_ID), opp_values);
                            crmLead.setReminder(lead.getInt(OColumn.ROW_ID));
                        }
                    }
                    values.put("call_type", "false");
                    CRMPhoneCallsCategory category = new CRMPhoneCallsCategory(this, null);
                    ODataRow categ_id = category.browse(values.getInt("categ_id"));
                    if (categ_id != null) {
                        values.put("call_type", categ_id.getString("name"));
                    }
                    values.put("state", logType);
                    if (extra == null || extra.containsKey("opp_id")
                            || extra.containsKey(KEY_LOG_CALL_REQUEST) || extra.containsKey("call_id")) {
                        int row_id = crmPhoneCalls.insert(values);
                        crmPhoneCalls.setReminder(row_id);
                    } else {
                        crmPhoneCalls.update(extra.getInt(OColumn.ROW_ID), values);
                        crmPhoneCalls.setReminder(extra.getInt(OColumn.ROW_ID));
                    }
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFieldValueChange(OField field, Object value) {
        if (field.getFieldName().equals("opportunity_id")) {
            ODataRow lead = (ODataRow) value;
            updateOpportunity = false;
            if (!lead.getString("type").equals("lead")) {
                updateOpportunity = true;
                opportunity_action_form.setEditable(true);
                opportunity_action_form.initForm(lead);
            }
            findViewById(R.id.opportunity_action_container).setVisibility(
                    (updateOpportunity) ? View.VISIBLE : View.GONE);
        } else {
            if (!value.toString().equals("now()")) {
                Date selectedDate = ODateUtils.createDateObject(value.toString(), ODateUtils.DEFAULT_FORMAT, false);
                Date now = new Date();
                if (now.compareTo(selectedDate) >= 0) {
                    actionBar.setTitle(R.string.label_log_call);
                    logType = "done";
                } else {
                    logType = "open";
                    actionBar.setTitle(R.string.label_schedule_call);
                }
            }
        }
    }
}
