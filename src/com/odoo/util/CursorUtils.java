package com.odoo.util;

import android.database.Cursor;

import com.odoo.orm.ODataRow;

public class CursorUtils {
	public static Object getValue(Cursor c, String column) {
		Object value = false;
		int index = c.getColumnIndex(column);
		switch (c.getType(index)) {
		case Cursor.FIELD_TYPE_NULL:
			value = false;
			break;
		case Cursor.FIELD_TYPE_BLOB:
		case Cursor.FIELD_TYPE_STRING:
			value = c.getString(index);
			break;
		case Cursor.FIELD_TYPE_FLOAT:
			value = c.getFloat(index);
			break;
		case Cursor.FIELD_TYPE_INTEGER:
			value = c.getInt(index);
			break;
		}
		return value;
	}

	public static ODataRow toDataRow(Cursor cr) {
		ODataRow row = new ODataRow();
		for (String col : cr.getColumnNames()) {
			row.put(col, CursorUtils.getValue(cr, col));
		}
		return row;
	}

}
