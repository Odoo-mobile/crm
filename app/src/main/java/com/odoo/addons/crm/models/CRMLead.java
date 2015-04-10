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
 * Created on 13/1/15 10:07 AM
 */
package com.odoo.addons.crm.models;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.odoo.R;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.base.addons.res.ResCountry;
import com.odoo.base.addons.res.ResCurrency;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.account.BaseSettings;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OText;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.JSONUtils;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OResource;
import com.odoo.core.utils.reminder.ReminderUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import odoo.OArguments;
import odoo.ODomain;

public class CRMLead extends OModel {
    public static final String TAG = CRMLead.class.getSimpleName();
    public static final String AUTHORITY = "com.odoo.core.crm.provider.content.sync.crm_lead";
    public static final String KEY_LEAD = "lead";
    public static final String KEY_OPPORTUNITY = "opportunity";
    private Context mContext;

    @Odoo.onChange(method = "partnerIdOnChange")
    OColumn partner_id = new OColumn("Customer", ResPartner.class,
            OColumn.RelationType.ManyToOne).addDomain("customer", "=", "true");
    OColumn name = new OColumn("Name", OVarchar.class).setSize(64)
            .setRequired();
    OColumn email_from = new OColumn("Email", OVarchar.class).setSize(128);
    OColumn street = new OColumn("Street", OText.class);
    OColumn street2 = new OColumn("Street2", OText.class);
    OColumn city = new OColumn("City", OVarchar.class).setSize(100);
    OColumn zip = new OColumn("Zip", OVarchar.class).setSize(20);
    OColumn mobile = new OColumn("Mobile", OVarchar.class).setSize(20);
    OColumn phone = new OColumn("Phone", OVarchar.class).setSize(20);
    OColumn create_date = new OColumn("Creation Date", ODateTime.class);
    OColumn description = new OColumn("Internal Notes", OText.class);
    @Odoo.api.v7
    @Odoo.api.v8
    OColumn categ_ids = new OColumn("Tags", CRMCaseCateg.class,
            OColumn.RelationType.ManyToMany);
    @Odoo.api.v9alpha
    OColumn tag_ids = new OColumn("Tags", CRMCaseCateg.class,
            OColumn.RelationType.ManyToMany);
    OColumn contact_name = new OColumn("Contact Name", OVarchar.class);
    OColumn partner_name = new OColumn("Company Name", OVarchar.class);
    OColumn opt_out = new OColumn("Opt-Out", OBoolean.class);
    OColumn type = new OColumn("Type", OVarchar.class).setDefaultValue("lead");
    OColumn priority = new OColumn("Priority", OVarchar.class).setSize(10);
    OColumn date_open = new OColumn("Assigned", ODateTime.class);
    OColumn date_closed = new OColumn("Closed", ODateTime.class);
    OColumn stage_id = new OColumn("Stage", CRMCaseStage.class,
            OColumn.RelationType.ManyToOne);
    OColumn user_id = new OColumn("Salesperson", ResUsers.class,
            OColumn.RelationType.ManyToOne);
    OColumn referred = new OColumn("Referred By", OVarchar.class);
    OColumn company_id = new OColumn("Company", ResCompany.class,
            OColumn.RelationType.ManyToOne);
    OColumn country_id = new OColumn("Country", ResCountry.class,
            OColumn.RelationType.ManyToOne);
    OColumn company_currency = new OColumn("Company Currency",
            ResCurrency.class, OColumn.RelationType.ManyToOne);

    /**
     * Only used for type opportunity
     */

    OColumn probability = new OColumn("Success Rate (%)", OFloat.class).setSize(20).setDefaultValue("0.0");
    OColumn planned_revenue = new OColumn("Expected Revenue", OFloat.class).setSize(20).setDefaultValue("0.0");
    OColumn ref = new OColumn("Reference", OVarchar.class);
    OColumn ref2 = new OColumn("Reference 2", OVarchar.class);
    OColumn date_deadline = new OColumn("Expected Closing", ODate.class);
    OColumn date_action = new OColumn("Next Action", ODate.class);
    OColumn title_action = new OColumn("Next Action", OVarchar.class);
    OColumn planned_cost = new OColumn("Planned Cost", OFloat.class).setSize(20);

    /**
     * Extra functional fields
     */
    @Odoo.Functional(method = "getDisplayName", store = true, depends = {
            "partner_id", "contact_name", "partner_name"})
    OColumn display_name = new OColumn("Display Name", OVarchar.class)
            .setLocalColumn();
    @Odoo.Functional(method = "storeAssigneeName", store = true, depends = {"user_id"})
    OColumn assignee_name = new OColumn("Assignee", OVarchar.class).setSize(100)
            .setLocalColumn();
    @Odoo.Functional(method = "storeStageName", store = true, depends = {"stage_id"})
    OColumn stage_name = new OColumn("Stage name", OVarchar.class).setLocalColumn();
    OColumn data_type = new OColumn("Data type", OVarchar.class).setSize(34)
            .setLocalColumn().setDefaultValue("opportunity");
    OColumn is_done = new OColumn("Mark as Done", OInteger.class)
            .setLocalColumn().setDefaultValue("0");

    OColumn color_index = new OColumn("Color index", OInteger.class).setSize(5)
            .setLocalColumn().setDefaultValue(7);

    public CRMLead(Context context, OUser user) {
        super(context, "crm.lead", user);
        mContext = context;
        setHasMailChatter(true);
        String serie = getOdooVersion().getServer_serie();
        if (serie.equals("8.saas~6")) {
            categ_ids.setName("tag_ids");
        }
    }

    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

    public ODataRow partnerIdOnChange(ODataRow row) {
        ODataRow rec = new ODataRow();
        String display_name = "";
        String contact_name = "";
        ResCountry country = new ResCountry(mContext, null);
        try {
            rec.put("partner_name", row.getString("name"));
            rec.put("partner_name", rec.getString("partner_name"));
            if (!row.getString("parent_id").equals("false")) {
                if (row.get("parent_id") instanceof JSONArray) {
                    JSONArray parent_id = new JSONArray(
                            row.getString("parent_id"));
                    rec.put("partner_name", parent_id.get(1));
                    display_name = parent_id.getString(1);
                    contact_name = parent_id.getString(1);
                } else {
                    ODataRow parent_id = row.getM2ORecord("parent_id").browse();
                    if (parent_id != null) {
                        rec.put("partner_name", parent_id.getString("name"));
                        display_name = parent_id.getString("name");
                        contact_name = parent_id.getString("name");
                    }

                }
                if (!TextUtils.isEmpty(display_name)) {
                    display_name += " (" + row.getString("name") + ")";
                    contact_name = row.getString("name");
                } else {
                    display_name += row.getString("name");
                }
            } else {
                display_name = row.getString("name");

            }
            Integer country_id = 0;
            if (!row.getString("country_id").equals("false")) {
                if (row.get("country_id") instanceof JSONArray) {
                    JSONArray country_data = new JSONArray(
                            row.getString("country_id"));
                    country_id = country.selectRowId(country_data.getInt(0));
                    if (country_id == null) {
                        country_id = 0;
                    }
                } else {
                    ODataRow country_data = row.getM2ORecord("country_id")
                            .browse();
                    if (country_data != null) {
                        country_id = country_data.getInt(OColumn.ROW_ID);
                    }
                }
                if (country_id != 0)
                    rec.put("country_id", country_id);
            }
            rec.put("display_name", display_name);
            rec.put("contact_name", contact_name);
            rec.put("street", row.getString("street"));
            rec.put("street2", row.getString("street2"));
            rec.put("city", row.getString("city"));
            rec.put("zip", row.getString("zip"));
            rec.put("email_from", row.getString("email"));
            rec.put("phone", row.getString("phone"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rec;
    }

    public String getDisplayName(OValues row) {
        String name = "";
        try {
            if (!row.getString("partner_id").equals("false")) {
                JSONArray partner_id = new JSONArray(
                        row.getString("partner_id"));
                name = partner_id.getString(1);
            } else if (!row.getString("partner_name").equals("false")) {
                name = row.getString("partner_name");
            }
            if (!row.getString("contact_name").equals("false")) {
                name += (TextUtils.isEmpty(name)) ? row
                        .getString("contact_name") : " ("
                        + row.getString("contact_name") + ")";
            }
            if (TextUtils.isEmpty(name)) {
                name = "No Partner";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public String storeAssigneeName(OValues vals) {
        try {
            if (!vals.getString("user_id").equals("false")) {
                JSONArray user_id = new JSONArray(vals.getString("user_id"));
                return user_id.getString(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unassigned";
    }

    public String storeStageName(OValues values) {
        try {
            JSONArray stage_id = new JSONArray(values.getString("stage_id"));
            return stage_id.getString(1);
        } catch (Exception e) {

        }
        return "false";
    }


    public void convertToOpportunity(final ODataRow lead, final List<Integer> other_lead_ids,
                                     final OnOperationSuccessListener listener) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(mContext);
                dialog.setTitle(R.string.title_please_wait);
                dialog.setMessage(OResource.string(mContext, R.string.title_working));
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    odoo.Odoo odoo = getServerDataHelper().getOdoo();
                    // Creating wizard record
                    JSONObject values = new JSONObject();
                    values.put("name", (other_lead_ids.size() > 0) ? "merge" : "convert");
                    Object partner_id = false;
                    ODataRow partner = null;
                    if (!lead.getString("partner_id").equals("false")) {
                        ResPartner resPartner = new ResPartner(mContext, getUser());
                        partner = resPartner.browse(lead.getInt("partner_id"));
                        partner_id = partner.getInt("id");
                    }
                    values.put("action", (partner == null) ? "create" : "exist");
                    values.put("partner_id", partner_id);

                    JSONObject context = new JSONObject();
                    context.put("stage_type", "lead");
                    context.put("active_id", lead.getInt("id"));
                    other_lead_ids.add(lead.getInt("id"));
                    context.put("active_ids", JSONUtils.<Integer>toArray(other_lead_ids));
                    context.put("active_model", "crm.lead");
                    odoo.updateContext(context);
                    JSONObject result = odoo.createNew("crm.lead2opportunity.partner", values);
                    int lead_to_opp_partner_id = result.getInt("result");

                    // Converting lead to opportunity
                    OArguments arg = new OArguments();
                    arg.add(lead_to_opp_partner_id);
                    arg.add(context);
                    odoo.call_kw("crm.lead2opportunity.partner", "action_apply", arg.get());
                    OValues val = new OValues();
                    val.put("type", "opportunity");
                    for (int id : other_lead_ids) {
                        update(selectRowId(id), val);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dialog.dismiss();
                if (listener != null) {
                    listener.OnSuccess();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                dialog.dismiss();
                if (listener != null) {
                    listener.OnCancelled();
                }
            }
        }.execute();
    }

    private void _markWonLost(String type, ODataRow record) {
        OArguments oArguments = new OArguments();
        oArguments.add(new JSONArray().put(record.getInt("id")));
        getServerDataHelper().callMethod("case_mark_" + type, oArguments, new JSONObject());
        CRMCaseStage stage = new CRMCaseStage(mContext, getUser());
        String key = (type.equals("won")) ? "Won" : (record.getString("type").equals("lead")) ? "Dead" : "Lost";
        ODataRow row = stage.browse(null, "name = ?", new String[]{key});
        if (row != null) {
            OValues values = new OValues();
            values.put("stage_id", row.getInt(OColumn.ROW_ID));
            values.put("stage_name", row.getString("name"));
            values.put("probability", row.getFloat("probability"));
            update(record.getInt(OColumn.ROW_ID), values);
        }
    }

    /**
     * Setting reminder for lead/opportunity
     *
     * @param row_id
     */
    public void setReminder(int row_id) {
        ODataRow row = browse(row_id);
        String time = " " + BaseSettings.getDayStartTime(mContext);
        Date now = new Date();
        Bundle extra = row.getPrimaryBundleData();
        extra.putString(ReminderUtils.KEY_REMINDER_TYPE, "opportunity");
        if (!row.getString("date_deadline").equals("false")) {
            row.put("date_deadline", row.getString("date_deadline") + time);
            Date date_deadline = ODateUtils.createDateObject(row.getString("date_deadline"),
                    ODateUtils.DEFAULT_FORMAT, false);
            if (now.compareTo(date_deadline) < 0) {
                extra.putBoolean("expiry_date", true);
                if (ReminderUtils.get(mContext).resetReminder(date_deadline, extra)) {
                    // Nothing to do. Reminder set for expiry date
                }
            }
        }

        if (!row.getString("date_action").equals("false")) {
            row.put("date_action", row.getString("date_action") + time);
            Date date_action = ODateUtils.createDateObject(row.getString("date_action"),
                    ODateUtils.DEFAULT_FORMAT, false);
            if (now.compareTo(date_action) < 0) {
                extra.putBoolean("expiry_date", false);
                if (ReminderUtils.get(mContext).resetReminder(date_action, extra)) {
                    // Nothing to do. Reminder set for next date action
                }
            }

        }
    }

    public void markWonLost(final String type, final ODataRow record, final OnOperationSuccessListener listener) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(mContext);
                dialog.setTitle(R.string.title_please_wait);
                dialog.setMessage(OResource.string(mContext, R.string.title_working));
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                _markWonLost(type, record);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dialog.dismiss();
                if (listener != null) {
                    listener.OnSuccess();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                dialog.dismiss();
                if (listener != null) {
                    listener.OnCancelled();
                }
            }
        }.execute();

    }


    public void createQuotation(final ODataRow lead, final String partnerId, final boolean close, final OnOperationSuccessListener listener) {
        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(mContext);
                dialog.setTitle(R.string.title_please_wait);
                dialog.setMessage(OResource.string(mContext, R.string.title_working));
                dialog.setCancelable(false);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    odoo.Odoo odoo = getServerDataHelper().getOdoo();
                    // Creating wizard record
                    JSONObject values = new JSONObject();
                    ResPartner resPartner = new ResPartner(mContext, getUser());
                    ODataRow partner = resPartner.browse(new String[]{}, Integer.parseInt(partnerId));
                    values.put("partner_id", partner.getInt("id"));
                    values.put("close", close);
                    JSONObject context = new JSONObject();
                    context.put("stage_type", lead.getString("type"));
                    context.put("active_id", lead.getInt("id"));
                    context.put("active_ids", new JSONArray().put(lead.getInt("id")));
                    context.put("active_model", "crm.lead");
                    odoo.updateContext(context);
                    JSONObject result = odoo.createNew("crm.make.sale", values);
                    int quotation_wizard_id = result.getInt("result");

                    // Creating quotation
                    OArguments arg = new OArguments();
                    arg.add(quotation_wizard_id);
                    arg.add(context);
                    odoo.call_kw("crm.make.sale", "makeOrder", arg.get());
                    Thread.sleep(500);
                    // if close = true
                    if (close)
                        _markWonLost("won", lead);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                dialog.dismiss();
                if (listener != null) {
                    listener.OnSuccess();
                }
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                dialog.dismiss();
                if (listener != null) {
                    listener.OnCancelled();
                }
            }
        }.execute();
    }


    @Override
    public ODomain defaultDomain() {
        ODomain domain = new ODomain();
        domain.add("|");
        domain.add("user_id", "=", getUser().getUser_id());
        domain.add("user_id", "=", false);
        return domain;
    }

    public static interface OnOperationSuccessListener {
        public void OnSuccess();

        public void OnCancelled();
    }
}
