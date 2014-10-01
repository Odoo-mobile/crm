/*
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
 */
package com.odoo.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/**
 * The Class OEDate.
 */
@SuppressLint("SimpleDateFormat")
public class ODate {
	public static final String TAG = ODate.class.getSimpleName();
	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
	public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

	/** The time format. */
	static SimpleDateFormat timeFormat, dateFormat;

	public static String getDate(Context context, String date, String toTimezone) {
		return ODate.getDate(context, date, toTimezone, null);
	}

	public static String getDate(Context context, String date,
			String toTimezone, String format) {

		if (date.equals("false")) {
			return date;
		}

		Calendar cal = Calendar.getInstance();
		Date originalDate = convertToDate(date);
		cal.setTime(originalDate);

		Date oDate = removeTime(originalDate);
		Date today = removeTime(currentDate());
		String finalDateTime = "";
		if (format == null) {
			dateFormat = new SimpleDateFormat(dateFormat(context));
			timeFormat = new SimpleDateFormat(timeFormat(context));
			dateFormat.setTimeZone(TimeZone.getTimeZone(toTimezone));
			timeFormat.setTimeZone(TimeZone.getTimeZone(toTimezone));
			if (today.compareTo(oDate) < 0) {
				// sending date
				finalDateTime = dateFormat.format(oDate);
			} else {
				// sending time because it's today.
				finalDateTime = timeFormat.format(convertToTimezone(cal,
						toTimezone).getTime());
			}
		} else {
			dateFormat = new SimpleDateFormat(format);
			dateFormat.setTimeZone(TimeZone.getTimeZone(toTimezone));
			finalDateTime = dateFormat.format(convertFullToTimezone(cal,
					toTimezone).getTime());
		}

		return finalDateTime;

	}

	public static String dateFormat(Context context) {
		return Settings.System.getString(context.getContentResolver(),
				Settings.System.DATE_FORMAT);
	}

	public static String timeFormat(Context context) {
		String time_hour = Settings.System.getString(
				context.getContentResolver(), Settings.System.TIME_12_24);

		String time_format = "HH:mm";
		if (time_hour.equals("12"))
			time_format = "hh:mm aaa";
		return time_format;
	}

	public static String getDateFromMilis(long timeInMilis) {
		String date = "";
		Date original = new Date(timeInMilis);
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date parsedDate = null;
		try {
			parsedDate = formatter.parse(formatter.format(original).toString());
			SimpleDateFormat destFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			destFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			date = destFormat.format(parsedDate);
		} catch (Exception e) {
		}

		return date;
	}

	public static long getDateTimeInMilis(String dateString) {
		Calendar cal = Calendar.getInstance();
		Date date = convertToDate(dateString);
		cal.setTime(date);
		return cal.getTimeInMillis();
	}

	private static Date currentDate() {
		return new Date();
	}

	private static Date convertToDate(String date) {
		return convertToDate(date, "yyyy-MM-dd HH:mm:ss", true);
	}

	/**
	 * Convert to date.
	 * 
	 * @param date
	 *            the date
	 * @return the date
	 */
	public static Date convertToDate(String date, String format,
			boolean defaultTimeZone) {
		Date dt = null;
		try {
			SimpleDateFormat temp = new SimpleDateFormat(format);
			if (defaultTimeZone)
				temp.setTimeZone(TimeZone.getTimeZone("GMT"));
			dt = temp.parse(date);
		} catch (Exception e) {
			Log.e(TAG, "Date format must be yyyy-MM-dd HH:mm:ss");
			e.printStackTrace();
		}
		return dt;
	}

	/**
	 * Convert to timezone.
	 * 
	 * @param cal
	 *            the cal
	 * @param timezone
	 *            the timezone
	 * @return the calendar
	 */
	private static Calendar convertToTimezone(Calendar cal, String timezone) {

		Calendar localTime = Calendar.getInstance();
		localTime.set(Calendar.HOUR, cal.get(Calendar.HOUR));
		localTime.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
		localTime.set(Calendar.SECOND, cal.get(Calendar.SECOND));

		Calendar convertedTime = new GregorianCalendar(
				TimeZone.getTimeZone(timezone));
		convertedTime.setTimeInMillis(localTime.getTimeInMillis());
		return convertedTime;
	}

	private static Calendar convertFullToTimezone(Calendar cal, String timezone) {
		Calendar convertedTime = new GregorianCalendar(
				TimeZone.getTimeZone(timezone));
		convertedTime.setTimeInMillis(cal.getTimeInMillis());
		return convertedTime;
	}

	/**
	 * Removes the time.
	 * 
	 * @param date
	 *            the date
	 * @return the date
	 */
	private static Date removeTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static String getDate() {
		SimpleDateFormat gmtFormat = new SimpleDateFormat();
		gmtFormat.applyPattern(DEFAULT_FORMAT);
		TimeZone gmtTime = TimeZone.getTimeZone("GMT");
		gmtFormat.setTimeZone(gmtTime);
		return gmtFormat.format(new Date());
	}

	public static String getDateBefore(int days) {
		Date today = new Date();
		Calendar cal = new GregorianCalendar();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_MONTH, days * -1);
		Date date = cal.getTime();
		SimpleDateFormat gmtFormat = new SimpleDateFormat();
		gmtFormat.applyPattern("yyyy-MM-dd 00:00:00");
		TimeZone gmtTime = TimeZone.getTimeZone("GMT");
		gmtFormat.setTimeZone(gmtTime);
		return gmtFormat.format(date);
	}

	public static String getUTCDate(String defaultFormat) {
		return getUTCDate(new Date(), defaultFormat);
	}

	public static String getUTCDate(Date dt, String defaultFormat) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dt);
		Calendar utcCal = convertFullToTimezone(cal, "GMT");
		dateFormat = new SimpleDateFormat(defaultFormat);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(utcCal.getTime());
	}
}
