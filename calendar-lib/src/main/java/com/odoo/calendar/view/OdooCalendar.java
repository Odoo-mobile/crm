package com.odoo.calendar.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.odoo.calander.R;
import com.odoo.calendar.SysCal.DateInfo;
import com.odoo.calendar.pager.PagerHelper;
import com.odoo.calendar.pager.PagerHelper.PagerViewGenerateListener;

import java.util.List;

/**
 * @author Dharmang Soni <dharmangsoni@gmail.com>
 */
public class OdooCalendar extends LinearLayout implements
        PagerViewGenerateListener {

    public static final String TAG = OdooCalendar.class.getSimpleName();
    private PagerHelper helper;
    private LinearLayout mParent;
    private LinearLayout mEventsView;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
        public List<DateDataObject> weekDataInfo(List<DateInfo> week_days);

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


    public static class DateDataObject {
        String date;
        Boolean hasData;

        public DateDataObject(String date, Boolean hasData) {
            this.date = date;
            this.hasData = hasData;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Boolean getHasData() {
            return hasData;
        }

        public void setHasData(Boolean hasData) {
            this.hasData = hasData;
        }
    }
}
