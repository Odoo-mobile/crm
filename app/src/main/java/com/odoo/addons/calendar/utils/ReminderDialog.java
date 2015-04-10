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
 * Created on 12/1/15 3:47 PM
 */
package com.odoo.addons.calendar.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.odoo.core.account.BaseSettings;
import com.odoo.core.support.list.OListAdapter;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.ODateUtils;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.core.utils.OResource;
import com.odoo.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReminderDialog implements AdapterView.OnItemClickListener {
    public static final String TAG = ReminderDialog.class.getSimpleName();

    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;
    private Context mContext;
    private ReminderType mType;
    private OListAdapter mAdapter;
    private OnReminderValueSelectListener mOnReminderValueSelectListener = null;
    private List<Object> reminderTypes = new ArrayList<>();
    private OPreferenceManager mPref;

    public enum ReminderType {
        FullDayEvent, TimeBasedEvent
    }

    public ReminderDialog(Context context, ReminderType type) {
        mContext = context;
        reminderTypes.clear();
        mPref = new OPreferenceManager(mContext);
        mType = type;
        List<ReminderItem> reminders = new ArrayList<>();
        reminders.add(0, new ReminderItem(0, OResource.string(mContext, R.string.no_notification), "false"));
        switch (mType) {
            case FullDayEvent:
                // At your working day start time
                String workingStartTime = ODateUtils.parseDate(mPref.getString(BaseSettings.KEY_LEAD_WORK_DAY_START_TIME,
                                OResource.string(mContext, R.string.default_day_start_time)), ODateUtils.DEFAULT_TIME_FORMAT,
                        ODateUtils.DEFAULT_TIME_FORMAT);
                reminders.add(1, new ReminderItem(1,
                        OResource.string(mContext, R.string.on_your_working_day_start_time),
                        workingStartTime));
                // At 9 AM
                reminders.add(2, new ReminderItem(2,
                        String.format(OResource.string(mContext, R.string.on_the_day_at), "9 AM"), "9:00 AM"));
                // before day at 11:30 PM
                reminders.add(3, new ReminderItem(3,
                        String.format(OResource.string(mContext, R.string.day_before_at), "11:30 PM"), "11:30 PM"));
                // before day at 5:00 PM
                reminders.add(4, new ReminderItem(4,
                        String.format(OResource.string(mContext, R.string.day_before_at), "5 PM"), "5:00 PM"));
                break;
            case TimeBasedEvent:
                // At the time of event
                reminders.add(new ReminderItem(1, OResource.string(mContext, R.string.at_the_time_of_event), 1));
                // 30 min before
                reminders.add(new ReminderItem(2,
                        String.format(OResource.string(mContext, R.string.minutes_before), "30"), 30));
                // 10 min before
                reminders.add(new ReminderItem(3,
                        String.format(OResource.string(mContext, R.string.minutes_before), "10"), 10));
                break;
        }
        //TODO Custom reminder: reminders.add(new ReminderItem(4, OResource.string(mContext, R.string.custom), -1));
        reminderTypes.addAll(reminders);
    }

    public List<Object> getReminderTypes() {
        return reminderTypes;
    }

    public void show() {
        mBuilder = new AlertDialog.Builder(mContext);
        mBuilder.setView(generateView());
        mDialog = mBuilder.create();
        mDialog.show();
    }

    public static ReminderItem getDefault(Context context, boolean allDay) {
        ReminderDialog dialog = new ReminderDialog(context,
                (allDay) ? ReminderType.FullDayEvent : ReminderType.TimeBasedEvent);
        return (ReminderItem) dialog.getReminderTypes().get(1);
    }

    private View generateView() {
        AbsListView.LayoutParams param = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        ListView list = new ListView(mContext);
        list.setLayoutParams(param);
        mAdapter = new OListAdapter(mContext, R.layout.reminder_item_view, reminderTypes) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null)
                    convertView = LayoutInflater.from(mContext).inflate(getResource(),
                            parent, false);
                ReminderItem item = (ReminderItem) getItem(position);
                OControls.setText(convertView, R.id.reminderTitle, item.getTitle());
                return convertView;
            }
        };
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(this);
        return list;
    }

    public static Date getReminderDateTime(String eventDateTime, Boolean allDay, ReminderItem item) {
        String format = (allDay) ? ODateUtils.DEFAULT_DATE_FORMAT : ODateUtils.DEFAULT_FORMAT;
        Date eventDate = ODateUtils.createDateObject(eventDateTime, format, false);
        if (item.getRequest_code() != 0) {
            Date dayBefore = ODateUtils.getDateDayBefore(eventDate, 1);
            if (allDay) {
                switch (item.getRequest_code()) {
                    case 1:
                        return ODateUtils.createDateObject(eventDateTime + " " + item.getValue(),
                                ODateUtils.DEFAULT_FORMAT, true);
                    case 2:
                        return ODateUtils.setDateTime(eventDate, 9, 0, 0);
                    case 3:
                        return ODateUtils.setDateTime(dayBefore, 23, 30, 0);
                    case 4:
                        return ODateUtils.setDateTime(dayBefore, 17, 0, 0);
                }
            } else {
                switch (item.getRequest_code()) {
                    case 1:
                        return eventDate;
                    case 2:
                    case 3:
                        return ODateUtils.getDateMinuteBefore(eventDate, (Integer) item.getValue());
                }
            }
        }
        return null;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ReminderItem item = (ReminderItem) mAdapter.getItem(position);
        switch (item.getRequest_code()) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (mOnReminderValueSelectListener != null) {
                    mOnReminderValueSelectListener.onReminderItemSelect(item);
                }
                mDialog.dismiss();
                break;
            case 4:
                // TODO: Open custom dialog for reminder config
                break;
        }
    }

    public void setOnReminderValueSelectListener(OnReminderValueSelectListener listener) {
        mOnReminderValueSelectListener = listener;
    }

    public interface OnReminderValueSelectListener {
        public void onReminderItemSelect(ReminderItem value);
    }

    public static class ReminderItem {
        int request_code;
        String title;
        Object value;

        public ReminderItem(int request_code, String title, Object value) {
            this.request_code = request_code;
            this.title = title;
            this.value = value;
        }

        public int getRequest_code() {
            return request_code;
        }

        public void setRequest_code(int request_code) {
            this.request_code = request_code;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }
}
