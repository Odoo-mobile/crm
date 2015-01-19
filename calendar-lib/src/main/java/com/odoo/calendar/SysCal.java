package com.odoo.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;

public class SysCal {

	private Calendar mCal;

	public SysCal(Context context) {
	}

	public String getMonthName(int week_of_the_year) {
		mCal = Calendar.getInstance(Locale.getDefault());
		mCal.set(Calendar.WEEK_OF_YEAR, week_of_the_year);
		mCal.set(Calendar.DAY_OF_MONTH, weekStartEndDate(week_of_the_year)[0]);
		return mCal.getDisplayName(Calendar.MONTH, Calendar.LONG,
				Locale.getDefault())
				+ " " + mCal.get(Calendar.YEAR);
	}

	public int[] weekStartEndDate(int week_of_the_year) {
		mCal = Calendar.getInstance(Locale.getDefault());
		int year = mCal.get(Calendar.YEAR);
		mCal.clear();
		mCal.set(Calendar.YEAR, year);
		mCal.set(Calendar.WEEK_OF_YEAR, week_of_the_year);
		int first_day_of_week = mCal.get(Calendar.DAY_OF_MONTH);
		mCal.add(Calendar.DAY_OF_YEAR, 6);
		int last_day_of_week = mCal.get(Calendar.DAY_OF_MONTH);
		return new int[] { first_day_of_week, last_day_of_week };
	}

	public int[] weekStartEndDate() {
		int week_of_the_month = mCal.get(Calendar.WEEK_OF_MONTH);
		return weekStartEndDate(week_of_the_month);
	}

	public int getCurrentDate() {
		Calendar mCal = Calendar.getInstance(Locale.getDefault());
		return mCal.get(Calendar.DAY_OF_MONTH);
	}

	public int getWeekOfTheYear() {
		mCal = Calendar.getInstance(Locale.getDefault());
		return mCal.get(Calendar.WEEK_OF_YEAR);
	}

	public int getWeekOfTheYear(int week_of_the_year, int offset) {
		mCal = Calendar.getInstance(Locale.getDefault());
		mCal.set(Calendar.WEEK_OF_YEAR, week_of_the_year + offset);
		return mCal.get(Calendar.WEEK_OF_YEAR);
	}

	private boolean isToday(Calendar date) {
		Calendar mCal = Calendar.getInstance(Locale.getDefault());
		return (date.get(Calendar.DAY_OF_MONTH) == mCal
				.get(Calendar.DAY_OF_MONTH)
				&& date.get(Calendar.MONTH) == mCal.get(Calendar.MONTH) && date
					.get(Calendar.YEAR) == mCal.get(Calendar.YEAR));
	}

	public List<DateInfo> getWeekDates(int week_of_the_year) {
		List<DateInfo> dates = new ArrayList<SysCal.DateInfo>();
		mCal = Calendar.getInstance(Locale.getDefault());
		mCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		mCal.set(Calendar.WEEK_OF_YEAR, week_of_the_year);
		int year = mCal.get(Calendar.YEAR);
		int month = mCal.get(Calendar.MONTH) + 1;
		boolean isToday = isToday(mCal);
		dates.add(new DateInfo(0, mCal.get(Calendar.DAY_OF_MONTH),
				getFullDayName(mCal.get(Calendar.DAY_OF_WEEK)),
				getShortDayName(mCal.get(Calendar.DAY_OF_WEEK)), month, year,
				isToday));
		for (int i = 1; i <= 6; i++) {
			mCal.add(Calendar.DAY_OF_YEAR, 1);
			year = mCal.get(Calendar.YEAR);
			month = mCal.get(Calendar.MONTH) + 1;
			isToday = isToday(mCal);
			dates.add(new DateInfo(i, mCal.get(Calendar.DAY_OF_MONTH),
					getFullDayName(mCal.get(Calendar.DAY_OF_WEEK)),
					getShortDayName(mCal.get(Calendar.DAY_OF_WEEK)), month,
					year, isToday));
		}
		return dates;
	}

	public int getDayOfWeek() {
		mCal = Calendar.getInstance(Locale.getDefault());
		return mCal.get(Calendar.DAY_OF_WEEK);
	}

	public String getFullDayName(int day) {
		String[] days = new String[] { "SUNDAY", "MONDAY", "TUESDAY",
				"WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY" };
		return days[day - 1];
	}

	public String getShortDayName(int day) {
		String[] days = new String[] { "SUN", "MON", "TUE", "WED", "THU",
				"FRI", "SAT" };
		return days[day - 1];
	}

	public static class DateInfo {
		String dayFullName;
		String dayShortName;
		int date;
		int index;
		int month;
		int year;
		boolean isToday = false;

		public DateInfo(int index, int date, String dayFullName,
				String dayShortName, int month, int year, boolean isToday) {
			this.index = index;
			this.date = date;
			this.dayFullName = dayFullName;
			this.dayShortName = dayShortName;
			this.isToday = isToday;
			this.month = month;
			this.year = year;
		}

		public int getMonth() {
			return month;
		}

		public int getYear() {
			return year;
		}

		public int getIndex() {
			return index;
		}

		public String getDayFullName() {
			return dayFullName;
		}

		public void setDayFullName(String dayFullName) {
			this.dayFullName = dayFullName;
		}

		public String getDayShortName() {
			return dayShortName;
		}

		public void setDayShortName(String dayShortName) {
			this.dayShortName = dayShortName;
		}

		public int getDate() {
			return date;
		}

		public void setDate(int date) {
			this.date = date;
		}

		public boolean isToday() {
			return isToday;
		}

		public void setToday(boolean isToday) {
			this.isToday = isToday;
		}

	}
}
