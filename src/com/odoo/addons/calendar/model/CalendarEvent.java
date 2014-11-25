package com.odoo.addons.calendar.model;

import android.content.Context;

import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.util.ODate;

public class CalendarEvent extends OModel {

	Context mContext = null;
	OColumn name = new OColumn("Name", OVarchar.class, 64);
	OColumn date = new OColumn("Date", ODate.class, 64);
	OColumn duration = new OColumn("Duration", OVarchar.class, 32);
	OColumn allday = new OColumn("All Day", OBoolean.class);
	OColumn description = new OColumn("Description", OText.class);
	OColumn location = new OColumn("Location", OText.class);
	OColumn date_deadline = new OColumn("Dead Line", ODateTime.class, 64);

	public CalendarEvent(Context context) {
		super(context, "calendar.event");
		if (getOdooVersion() != null) {
			int version = getOdooVersion().getVersion_number();
			if (version == 7) {
				setModelName("crm.meeting");
			}
		}

		mContext = context;
	}
}
