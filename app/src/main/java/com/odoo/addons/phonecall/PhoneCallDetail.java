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
import com.odoo.core.utils.reminder.ReminderUtils;
import com.odoo.crm.R;

import java.util.Date;

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
    private Menu mMenu;

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

                long start_time = Long.parseLong(extra.getString(PhoneStateReceiver.KEY_DURATION_START));
                long end_time = Long.parseLong(extra.getString(PhoneStateReceiver.KEY_DURATION_END));
                long duration = (end_time - start_time);
                record.put("duration", ODateUtils.durationToFloat(duration));

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_phonecall_detail, menu);
        mMenu = menu;
        if (extra == null) {
            mMenu.findItem(R.id.menu_phonecall_edit).setVisible(false);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_close);
        } else {
            if (extra.containsKey(KEY_LOG_CALL_REQUEST)) {
                mMenu.findItem(R.id.menu_phonecall_edit).setVisible(false);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_close);
            } else {
                mMenu.findItem(R.id.menu_phonecall_save).setVisible(false);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (extra != null && mForm.getEditable()) {
                    mForm.setEditable(false);
                    mMenu.findItem(R.id.menu_phonecall_edit).setVisible(true);
                    mMenu.findItem(R.id.menu_phonecall_save).setVisible(false);
                    actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_arrow_back);
                } else
                    finish();
                break;
            case R.id.menu_phonecall_edit:
                mMenu.findItem(R.id.menu_phonecall_edit).setVisible(false);
                mMenu.findItem(R.id.menu_phonecall_save).setVisible(true);
                mForm.setEditable(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_navigation_close);
                break;
            case R.id.menu_phonecall_save:
                OValues values = mForm.getValues();
                if (values != null) {
                    values.put("user_id", ResUsers.myId(this));
                    ResPartner partner = new ResPartner(this, null);
                    ODataRow row = partner.browse(values.getInt("partner_id"));
                    values.put("customer_name", row.getString("name"));
                    CRMLead crmLead = new CRMLead(this, null);
                    ODataRow lead = crmLead.browse(values.getInt("opportunity_id"));
                    values.put("lead_name", "false");
                    if (lead != null) {
                        values.put("lead_name", lead.getString("name"));
                    }
                    values.put("call_type", "false");
                    CRMPhoneCallsCategory category = new CRMPhoneCallsCategory(this, null);
                    ODataRow categ_id = category.browse(values.getInt("categ_id"));
                    if (categ_id != null) {
                        values.put("call_type", categ_id.getString("name"));
                    }
                    values.put("state", "pending");

                    Date date = ODateUtils.createDateObject(values.getString("date"),
                            ODateUtils.DEFAULT_FORMAT, false);
                    Date now = new Date();
                    if (now.compareTo(date) < 0) {
                        values.put("has_reminder", "true");
                        Bundle extra = row.getPrimaryBundleData();
                        extra.putString(ReminderUtils.KEY_REMINDER_TYPE, "phonecall");
                        ReminderUtils.get(getApplicationContext()).setReminder(date, extra);
                    }
                    if (extra == null)
                        crmPhoneCalls.insert(values);
                    else
                        crmPhoneCalls.update(extra.getInt(OColumn.ROW_ID), values);
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
