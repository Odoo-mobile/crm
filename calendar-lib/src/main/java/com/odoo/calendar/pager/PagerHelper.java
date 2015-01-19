package com.odoo.calendar.pager;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.calander.R;
import com.odoo.calendar.SysCal;
import com.odoo.calendar.SysCal.DateInfo;
import com.odoo.calendar.view.OdooCalendar.OdooCalendarDateSelectListener;

/**
 * 
 * @author Dharmang Soni <dharmangsoni@gmail.com>
 * 
 */
public class PagerHelper implements OnPageChangeListener, OnClickListener {

	private Context mContext;
	private LinearLayout mParent;
	private EventPagerAdapter mAdapter;
	private ViewPager mPager;
	private SysCal mCal;
	private int current_week_of_the_year = 0;
	private Resources res;
	private View recent_focused = null;
	private View current_week_view_layout = null;
	private View current_week_view_days = null;
	private int day_of_week = -1;
	private OdooCalendarDateSelectListener mOdooCalendarDateSelectListener = null;
	private PagerViewGenerateListener mPagerViewGenerateListener = null;
	private int view_get_delay = 500;

	public PagerHelper(Context context, LinearLayout parent) {
		mContext = context;
		mParent = parent;
		mCal = new SysCal(context);
		res = context.getResources();
		current_week_of_the_year = mCal.getWeekOfTheYear();
		initPager();
	}

	private void initPager() {
		mPager = (ViewPager) mParent.findViewById(R.id.pager);
		mAdapter = new EventPagerAdapter(mContext, mPager);
		mPager.setAdapter(mAdapter);
		mPager.setOnPageChangeListener(this);
		mAdapter.addView(getView(0,
				mCal.getWeekOfTheYear(current_week_of_the_year, -1)));
		current_week_view_layout = getView(1, current_week_of_the_year);
		mAdapter.addView(current_week_view_layout);
		mAdapter.addView(getView(2,
				mCal.getWeekOfTheYear(current_week_of_the_year, 1)));
		mPager.setCurrentItem(1);
	}

	public ViewPager getPagerView() {
		return mPager;
	}

	@Override
	public void onPageScrollStateChanged(int pos) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int pos) {
		final Integer position;
		if (pos == 0) {
			current_week_of_the_year = current_week_of_the_year - 1;
			position = mAdapter.addView(getView(pos, current_week_of_the_year),
					pos) + 1;
		} else if (pos == mAdapter.getCount() - 1) {
			current_week_of_the_year = current_week_of_the_year + 1;
			mAdapter.addView(
					getView(mAdapter.getCount(), current_week_of_the_year),
					mAdapter.getCount());
			position = pos;
		} else {
			position = pos;
		}
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				ViewGroup days = (ViewGroup) mAdapter.getView(position)
						.findViewById(R.id.week_days);
				if (day_of_week == -1)
					day_of_week = mCal.getDayOfWeek() - 1;
				onClick(days.getChildAt(day_of_week));

			}
		}, view_get_delay);
	}

	public void setViewGetDelay(int delay) {
		view_get_delay = delay;
	}

	private View getView(int position, int week_of_the_year) {
		ViewGroup layout = (ViewGroup) LayoutInflater.from(mContext).inflate(
				R.layout.calendar_week_view, mPager, false);
		((TextView) layout.findViewById(R.id.month_name)).setText(mCal
				.getMonthName(week_of_the_year));
		LinearLayout week_days = (LinearLayout) layout
				.findViewById(R.id.week_days);
		List<DateInfo> dates = mCal.getWeekDates(week_of_the_year);
		for (DateInfo date : dates) {
			createView(position, layout, week_days, date);
		}
		if (mCal.getWeekOfTheYear() == week_of_the_year) {
			current_week_view_days = layout.findViewById(R.id.week_days);
		}
		return layout;
	}

	private View createView(int position, View layout, ViewGroup week_days,
			DateInfo date) {
		View day = LayoutInflater.from(mContext).inflate(
				R.layout.week_day_view, week_days, false);
		day.setTag(position);
		day.setOnClickListener(this);
		TextView txvDay = (TextView) day.findViewById(R.id.day_name);
		txvDay.setText(date.getDayShortName());
		TextView txvDate = (TextView) day.findViewById(R.id.day_date);
		txvDate.setText(date.getDate() + "");
		View indicator = day.findViewById(R.id.day_indicator);
		indicator.setTag(date);
		if (date.isToday()) {
			indicator.setBackgroundResource(R.drawable.selected_week_day);
			txvDay.setTextColor(res.getColor(R.color.day_name_selected));
			txvDate.setTextColor(res.getColor(R.color.day_date_selected));
			current_week_view_days = day;
		} else {
			indicator.setBackgroundColor(Color.TRANSPARENT);
			txvDay.setTextColor(res.getColor(R.color.day_name));
			txvDate.setTextColor(res.getColor(R.color.day_date));
		}
		((ViewGroup) layout.findViewById(R.id.week_days)).addView(day);
		return layout;
	}

	@Override
	public void onClick(View v) {
		if (v == recent_focused)
			return;
		if (recent_focused != null) {
			resetView(recent_focused);
		}
		ViewGroup cDays = (ViewGroup) current_week_view_days;
		for (int i = 0; i < cDays.getChildCount(); i++) {
			DateInfo cDate = (DateInfo) cDays.getChildAt(i)
					.findViewById(R.id.day_indicator).getTag();
			if (cDate.isToday()) {
				cDays.getChildAt(i).findViewById(R.id.day_indicator)
						.setBackgroundColor(Color.TRANSPARENT);
			}
		}
		recent_focused = v;
		DateInfo dt = (DateInfo) v.findViewById(R.id.day_indicator).getTag();
		if (mOdooCalendarDateSelectListener != null) {
			View eventsView = mOdooCalendarDateSelectListener.getEventsView(
					mParent, dt);
			if (mPagerViewGenerateListener != null) {
				mPagerViewGenerateListener.OnPagerViewGenerate(eventsView);
			}
		}
		day_of_week = dt.getIndex();
		focusView(v);
	}

	private void focusView(View day) {
		((TextView) day.findViewById(R.id.day_name)).setTextColor(res
				.getColor(R.color.date_focused));
		((TextView) day.findViewById(R.id.day_date)).setTextColor(res
				.getColor(R.color.date_focused));
		day.findViewById(R.id.day_indicator).setBackgroundResource(
				R.drawable.focused_week_day);
	}

	private void resetView(View day) {
		DateInfo date = (DateInfo) day.findViewById(R.id.day_indicator)
				.getTag();
		if (date.isToday()) {
			((TextView) day.findViewById(R.id.day_name)).setTextColor(res
					.getColor(R.color.day_name_selected));
			((TextView) day.findViewById(R.id.day_date)).setTextColor(res
					.getColor(R.color.day_date_selected));
			day.findViewById(R.id.day_indicator).setBackgroundResource(
					R.drawable.selected_week_day);
		} else {
			((TextView) day.findViewById(R.id.day_name)).setTextColor(res
					.getColor(R.color.day_name));
			((TextView) day.findViewById(R.id.day_date)).setTextColor(res
					.getColor(R.color.day_date));
			day.findViewById(R.id.day_indicator).setBackgroundColor(
					Color.TRANSPARENT);
		}
	}

	public void setOdooCalendarDateSelectListener(
			OdooCalendarDateSelectListener l) {
		mOdooCalendarDateSelectListener = l;
	}

	public void setPagerViewGenerateListener(PagerViewGenerateListener l) {
		mPagerViewGenerateListener = l;
	}

	public interface PagerViewGenerateListener {
		public void OnPagerViewGenerate(View view);
	}

	public void focusOnToday() {
		int index = mAdapter.indexOf(current_week_view_layout);
		mPager.setCurrentItem(index);
	}
}
