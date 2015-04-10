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
 * Created on 13/1/15 6:33 PM
 */
package com.odoo.addons.phonecall.features;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.addons.crm.models.CRMLead;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.ServerDataHelper;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.support.OdooFields;

import java.util.List;

import odoo.ODomain;

public class CustomerFinder {
    public static final String TAG = CustomerFinder.class.getSimpleName();
    private Context mContext;
    private IOnCustomerFindListener customerFindListener;
    private ResPartner resPartner;
    private CustomerFinderTask customerFinderTask;
    private Boolean dialed = false;

    public CustomerFinder(Context context) {
        mContext = context;
        resPartner = new ResPartner(context, null);
    }

    public void findCustomer(Boolean isDialed, String callerNumber) {
        Log.i(TAG, "Finding customer for " + callerNumber);
        dialed = isDialed;
        customerFinderTask = new CustomerFinderTask();
        customerFinderTask.execute(callerNumber);
    }

    public void setOnCustomerFindListener(IOnCustomerFindListener listener) {
        customerFindListener = listener;
    }

    private class CustomerFinderTask extends AsyncTask<String, Void, ODataRow> {
        private String mContactLast2Chars = "";
        private String mContactLast3Chars = "";


        @Override
        protected ODataRow doInBackground(String... params) {
            String number = params[0].trim().replaceAll(" ", "").replace("+", "");
            Log.d(TAG, "Checking for number " + number + " in database");
            mContactLast2Chars = number.substring(number.length() - 2);
            mContactLast3Chars = number.substring(number.length() - 3);
            String where = "phone like ? or phone like ? or mobile like ? or mobile like ?";
            String[] args = new String[]{"%" + mContactLast2Chars,
                    "%" + mContactLast3Chars, "%" + mContactLast2Chars,
                    "%" + mContactLast3Chars};
            ODataRow partner = null;
            for (ODataRow row : resPartner.select(null, where, args)) {
                String partnerPhone = row.getString("phone").trim().replaceAll(" ", "")
                        .replace("+", "");
                String partnerMobile = row.getString("mobile").trim().replaceAll(" ", "")
                        .replace("+", "");
                if (!partnerPhone.equals("false") &&
                        (partnerPhone.contains(number) || number.contains(partnerPhone))) {
                    partner = row;
                    break;
                }
                if (!partnerMobile.equals("false") &&
                        (partnerMobile.contains(number) || number.contains(partnerMobile))) {
                    partner = row;
                    break;
                }
            }
            if (partner == null) {
                ServerDataHelper helper = resPartner.getServerDataHelper();
                try {
                    ODomain domain = new ODomain();
                    domain.add("|");
                    domain.add("|");
                    domain.add("phone", "=like", "%" + mContactLast2Chars);
                    domain.add("phone", "=like", "%" + mContactLast3Chars);
                    domain.add("|");
                    domain.add("mobile", "=like", "%" + mContactLast2Chars);
                    domain.add("mobile", "=like", "%" + mContactLast3Chars);
                    OdooFields fields = new OdooFields(resPartner.getColumns());
                    List<ODataRow> partners = helper.searchRecords(fields, domain, 10);
                    if (partners.size() > 0) {
                        for (ODataRow row : partners) {
                            String phone = row.getString("phone").trim();
                            String mobile = row.getString("mobile").trim();
                            String contact = ((phone.equals("false") || TextUtils
                                    .isEmpty(phone)) ? mobile : phone).trim().replaceAll(" ", "")
                                    .replace("+", "");
                            if (number.contains(contact) || contact.contains(number)) {
                                return resPartner.quickCreateRecord(row);
                            }
                        }
                    } else {
                        Log.i(TAG, "No Customer found on server with number " + number);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // Finding Leads or Opportunity for customer in local
                CRMLead crmLead = new CRMLead(mContext, null);
                String projection[] = {"name", "company_currency", "probability", "planned_revenue", "type"};
                List<ODataRow> records = crmLead.select(projection, "partner_id = ?",
                        new String[]{partner.getString(OColumn.ROW_ID)}, "type DESC");
                if (records.size() > 0) {
                    ODataRow record = records.get(0);
                    String more = "";
                    if (records.size() > 1) {
                        more = " and " + (records.size() - 1) + " more...";
                    }
                    partner.put("lead_name", record.getString("name") + " " + more);
                    partner.put("opportunity_id", record.getInt(OColumn.ROW_ID));
                    if (!record.getString("type").equals("lead")) {
                        partner.put("probability", record.getString("planned_revenue") + " " +
                                record.getM2ORecord("company_currency").browse().getString("symbol")
                                + " at " + record.getString("probability") + "%");
                    } else {
                        partner.put("probability", "");
                    }
                }
            }
            return partner;
        }

        @Override
        protected void onPostExecute(ODataRow row) {
            super.onPostExecute(row);
            if (row != null) {
                customerFindListener.onCustomerFind(dialed, row);
            }
        }
    }
}
