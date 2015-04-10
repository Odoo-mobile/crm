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
 * Created on 2/1/15 2:25 PM
 */
package com.odoo.addons.customers.providers;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.provider.BaseModelProvider;
import com.odoo.core.support.OdooFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import odoo.ODomain;

public class CustomersSyncProvider extends BaseModelProvider {
    public static final String TAG = CustomersSyncProvider.class.getSimpleName();
    public static final int LIVE_SEARCHABLE_CUSTOMER = 116;

    @Override
    public boolean onCreate() {
        String path = new ResPartner(getContext(), null).getModelName().toLowerCase(Locale.getDefault());
        matcher.addURI(authority(), path + "/live_searchable_customer", LIVE_SEARCHABLE_CUSTOMER);
        return super.onCreate();
    }

    @Override
    public void setModel(Uri uri) {
        super.setModel(uri);
        mModel = new ResPartner(getContext(), getUser(uri));
    }

    @Override
    public Cursor query(Uri uri, String[] base_projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = matcher.match(uri);
        if (match != LIVE_SEARCHABLE_CUSTOMER) {
            return super.query(uri, base_projection, selection, selectionArgs, sortOrder);
        }
        ResPartner partner = new ResPartner(getContext(), null);
        Cursor cr = super.query(partner.uri(), base_projection, selection, selectionArgs, sortOrder);
        if (cr.getCount() <= 0) {
            String searchName = null;
            if (selectionArgs != null && selectionArgs.length > 0) {
                searchName = selectionArgs[selectionArgs.length - 1];
            }
            if (searchName != null) {
                List<ODataRow> records = getRecords(searchName, partner);
                if (records.size() > 0) {
                    List<String> keys = new ArrayList<>();
                    keys.addAll(records.get(0).keys());
                    keys.add(OColumn.ROW_ID);
                    MatrixCursor cursor = new MatrixCursor(keys.toArray(new String[keys.size()]));
                    for (ODataRow row : records) {
                        List<Object> values = row.values();
                        values.add(0);
                        cursor.addRow(values);
                    }
                    return cursor;
                }
            }
        }
        return cr;
    }

    @Override
    public String authority() {
        return ResPartner.AUTHORITY;
    }


    public List<ODataRow> getRecords(String searchName, OModel model) {
        List<ODataRow> items = new ArrayList<>();
        try {
            OdooFields fields = new OdooFields(new String[]{"name", "image_small", "email"});
            ODomain domain = new ODomain();
            domain.add("name", "=ilike", "%" + searchName);
            List<ODataRow> records = model.getServerDataHelper().searchRecords(fields, domain, 10);
            items.addAll(records);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}
