package com.odoo.calendar.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.odoo.calander.R;
import com.odoo.calendar.SysCal.DateInfo;
import com.odoo.calendar.pager.PagerHelper;
import com.odoo.calendar.pager.PagerHelper.PagerViewGenerateListener;

/**
 * @author Dharmang Soni <dharmangsoni@gmail.com>
 */
public class OdooCalendar extends LinearLayout implements
		PagerViewGenerateListener {

	public static final String TAG = OdooCalendar.class.getSimpleName();
	private PagerHelper helper;
	private LinearLayout mParent;
	private LinearLayout mEventsView;

	public OdooCalendar(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	public OdooCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public OdooCalendar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public OdooCalendar(Context context) {
		super(context);
		init(context, null);
	}

	private void init(Context context, AttributeSet attrs) {
		mParent = (LinearLayout) LayoutInflater.from(context).inflate(
				R.layout.event_container, this, false);
		setOrientation(LinearLayout.VERTICAL);
		helper = new PagerHelper(context, mParent);
		mEventsView = (LinearLayout) mParent
				.findViewById(R.id.events_container_view);
		addView(mParent);
	}

	public void setOdooCalendarDateSelectListener(
			OdooCalendarDateSelectListener l) {
		helper.setOdooCalendarDateSelectListener(l);
		helper.setPagerViewGenerateListener(this);
	}

	public interface OdooCalendarDateSelectListener {
		public View getEventsView(ViewGroup parent, DateInfo date);
	}

	public void goToToday() {
		helper.focusOnToday();
	}

	public void setViewGetDelay(int delay) {
		helper.setViewGetDelay(delay);
	}

	@Override
	public void OnPagerViewGenerate(View view) {
		mEventsView.removeAllViews();
		mEventsView.addView(view);
	}
}
