package com.odoo.addons.calendar.providers.events;

import android.content.Context;
import android.net.Uri;

import com.odoo.addons.calendar.model.CalendarEvent;
import com.odoo.orm.OModel;
import com.odoo.support.provider.OContentProvider;

public class CalendarEventProvider extends OContentProvider {
	public static String AUTHORITY = "com.odoo.addons.calendar.provides.events";
	public static final String PATH = "calendar_events";
	public static final Uri CONTENT_URI = OContentProvider.buildURI(AUTHORITY,
			PATH);

	@Override
	public OModel model(Context context) {
		return new CalendarEvent(context);
	}

	@Override
	public String authority() {
		return CalendarEventProvider.AUTHORITY;
	}

	@Override
	public String path() {
		return CalendarEventProvider.PATH;
	}

	@Override
	public Uri uri() {
		return CalendarEventProvider.CONTENT_URI;
	}
}
