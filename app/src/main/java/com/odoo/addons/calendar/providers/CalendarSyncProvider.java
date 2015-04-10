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
 * Created on 9/1/15 10:25 AM
 */
package com.odoo.addons.calendar.providers;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;

import com.odoo.addons.calendar.models.CalendarEvent;
import com.odoo.addons.crm.models.CRMLead;
import com.odoo.addons.phonecall.models.CRMPhoneCalls;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.provider.BaseModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarSyncProvider extends BaseModelProvider {
    public static final String TAG = CalendarSyncProvider.class.getSimpleName();
    public static final int FULL_AGENDA = 114;

    @Override
    public boolean onCreate() {
        String path = new CalendarEvent(getContext(), null).getModelName().toLowerCase(Locale.getDefault());
        matcher.addURI(authority(), path + "/full_agenda", FULL_AGENDA);
        return super.onCreate();
    }


    @Override
    public void setModel(Uri uri) {
        super.setModel(uri);
        mModel = new CalendarEvent(getContext(), getUser(uri));
    }

    @Override
    public Cursor query(Uri uri, String[] base_projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        int match = matcher.match(uri);
        CalendarEvent events = new CalendarEvent(getContext(), null);
        if (match != FULL_AGENDA) {
            return super.query(uri, base_projection, selection, selectionArgs, sortOrder);
        }
        String date_start = selectionArgs[0];
        String filter = null;
        if (selectionArgs.length > 1)
            filter = selectionArgs[1];
        String where;
        List<String> args = new ArrayList<>();
        // Getting events
        MatrixCursor event_separator = new MatrixCursor(
                new String[]{OColumn.ROW_ID, "data_type", "name"});

        // Comparing date_start and date_end
        where = "(date(date_start) <= ? and date(date_end) >= ? )";
        args.add(date_start);
        args.add(date_start);

        if (filter != null) {
            where += " and name like ?";
            args.add(filter);
        }
        Cursor eventCR = getContext().getContentResolver().query(events.uri(),
                base_projection, where, args.toArray(new String[args.size()]), "is_done, date_start");
        if (eventCR.getCount() > 0)
            event_separator.addRow(new String[]{"0", "separator", "Meetings"});


        // Getting phone calls
        CRMPhoneCalls phoneCalls = new CRMPhoneCalls(getContext(), null);
        MatrixCursor phone_calls_separator = new MatrixCursor(
                new String[]{OColumn.ROW_ID, "data_type", "name"});

        // Comparing date
        where = "date(date) >=  ? and date(date) <= ? and (state = ? or state = ?)";
        args.clear();
        args.add(date_start);
        args.add(date_start);
        args.add("open");
        args.add("pending");
        if (filter != null) {
            where += " and (name like ? or description like ?)";
            args.add(filter);
            args.add(filter);
        }
        Cursor phoneCallsCR = getContext().getContentResolver().query(phoneCalls.uri(),
                base_projection, where, args.toArray(new String[args.size()]), "is_done , date");
        if (phoneCallsCR.getCount() > 0)
            phone_calls_separator.addRow(new String[]{"0", "separator", "Phone Calls"});

        // Getting opportunity
        CRMLead opportunity = new CRMLead(getContext(), null);
        MatrixCursor opportunity_separator = new MatrixCursor(
                new String[]{OColumn.ROW_ID, "data_type", "name"});
        // Comparing with create_date and date_action and type
        where = "(date(date_deadline) >= ? and date(date_deadline) <= ? or date(date_action) >= ? " +
                "and date(date_action) <= ?) and type = ?";
        args.clear();
        args.add(date_start);
        args.add(date_start);
        args.add(date_start);
        args.add(date_start);
        args.add("opportunity");
        if (filter != null) {
            where += " and (name like ? or description like ?)";
            args.add(filter);
            args.add(filter);
        }
        Cursor opportunityCR = getContext().getContentResolver().query(opportunity.uri(),
                base_projection, where, args.toArray(new String[args.size()]), sortOrder);
        if (opportunityCR.getCount() > 0)
            opportunity_separator.addRow(new String[]{"0", "separator", "Opportunities"});
        MergeCursor mergedData = new MergeCursor(new Cursor[]{
                event_separator, eventCR, phone_calls_separator, phoneCallsCR, opportunity_separator,
                opportunityCR});
        return mergedData;
    }

    @Override
    public String authority() {
        return CalendarEvent.AUTHORITY;
    }
}
