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
 * Created on 9/1/15 10:26 AM
 */
package com.odoo.addons.calendar.models;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.crm.models.CRMLead;
import com.odoo.addons.phonecall.models.CRMPhoneCalls;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.base.addons.res.ResUsers;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.OValues;
import com.odoo.core.orm.annotation.Odoo;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.ODate;
import com.odoo.core.orm.fields.types.ODateTime;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OText;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

import org.json.JSONArray;

import odoo.ODomain;

public class CalendarEvent extends OModel {
    public static final String TAG = CalendarEvent.class.getSimpleName();
    public static final String AUTHORITY = "com.odoo.core.crm.provider.content.sync.calendar_event";
    private Context mContext;

    OColumn name = new OColumn("Meeting Name", OVarchar.class).setSize(64).setRequired();
    @Odoo.api.v7
    OColumn date = new OColumn("Start Date", ODateTime.class);
    @Odoo.api.v8
    @Odoo.api.v9alpha
    OColumn start_date = new OColumn("Start Date", ODate.class);
    @Odoo.api.v8
    @Odoo.api.v9alpha
    OColumn start_datetime = new OColumn("Start Date", ODateTime.class);
    @Odoo.api.v7
    OColumn date_deadline = new OColumn("Dead Line", ODateTime.class);
    @Odoo.api.v8
    @Odoo.api.v9alpha
    OColumn stop_date = new OColumn("Stop Date", ODate.class);
    @Odoo.api.v8
    @Odoo.api.v9alpha
    OColumn stop_datetime = new OColumn("Stop Date", ODateTime.class);
    OColumn duration = new OColumn("Duration", OVarchar.class).setSize(32);
    OColumn allday = new OColumn("All Day", OBoolean.class);
    OColumn description = new OColumn("Description", OText.class);
    OColumn location = new OColumn("Location", OText.class);

    OColumn _class = new OColumn("Privacy", OSelection.class)
            .addSelection("public", "Public")
            .addSelection("private", "Private")
            .addSelection("confidential", "Public for Employees")
            .setDefaultValue("public");

    @Odoo.Functional(store = true, depends = {"date", "start_date",
            "start_datetime"}, method = "storeStartDate")
    OColumn date_start = new OColumn("Start Date", ODateTime.class)
            .setLocalColumn();

    @Odoo.Functional(store = true, depends = {"date_deadline", "stop_date",
            "stop_datetime"}, method = "storeStopDate")
    OColumn date_end = new OColumn("Start Date", ODateTime.class)
            .setLocalColumn();

    OColumn data_type = new OColumn("Data type", OVarchar.class).setSize(34)
            .setLocalColumn().setDefaultValue("event");

    OColumn is_done = new OColumn("Mark as Done", OInteger.class)
            .setLocalColumn().setDefaultValue("0");

    OColumn color_index = new OColumn("Color index", OInteger.class).setSize(5)
            .setLocalColumn().setDefaultValue(0);

    OColumn has_reminder = new OColumn("Has reminder", OBoolean.class).setLocalColumn()
            .setDefaultValue("false");
    OColumn reminder_datetime = new OColumn("Reminder type", ODateTime.class)
            .setDefaultValue("false").setLocalColumn();

    OColumn user_id = new OColumn("Owner", ResUsers.class, OColumn.RelationType.ManyToOne);
    OColumn partner_ids = new OColumn("Attendees", ResPartner.class, OColumn.RelationType.ManyToMany);


    // PhoneCalls link
    OColumn phonecall_id = new OColumn("Phone calls", CRMPhoneCalls.class, OColumn.RelationType.ManyToOne);
    //Opportunities id
    OColumn opportunity_id = new OColumn("Opportunities", CRMLead.class, OColumn.RelationType.ManyToOne)
            .addDomain("type", "=", "opportunity");

    public CalendarEvent(Context context, OUser user) {
        super(context, "calendar.event", user);
        mContext = context;
        if (getUser() != null && getUser().getVersion_number() != null) {
            int version = getUser().getVersion_number();
            if (version <= 7) {
                setModelName("crm.meeting");
            }
        }
        // Setting 'class' variable name not allowed in java.
        _class.setName("class");
        setHasMailChatter(true);
    }

    @Override
    public ODomain defaultDomain() {
        ODomain domain = new ODomain();
        if (getOdooVersion().getVersion_number() <= 7) {
            domain.add("|");
            domain.add("user_id", "=", getUser().getUser_id());
            domain.add("partner_ids", "in", new JSONArray().put(getUser().getPartner_id()));
        } else {
            domain.add("partner_ids", "in", new JSONArray().put(getUser().getPartner_id()));
        }
        domain.add("recurrency", "=", false);
        return domain;
    }

    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }

    public Uri agendaUri() {
        return uri().buildUpon().appendPath("full_agenda").build();
    }

    public String storeStartDate(OValues value) {
        if (value.contains("date")) {
            return value.getString("date");
        }
        if (!value.getString("start_date").equals("false"))
            return value.getString("start_date");
        return value.getString("start_datetime");
    }

    public String storeStopDate(OValues value) {
        if (value.contains("date_deadline")) {
            return value.getString("date_deadline");
        }
        if (!value.getString("stop_date").equals("false"))
            return value.getString("stop_date");
        return value.getString("stop_datetime");
    }
}
