package com.odoo.calendar.pager;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author Dharmang Soni <dharmangsoni@gmail.com>
 * 
 */
public class EventPagerAdapter extends PagerAdapter {

	private List<View> mViews = new ArrayList<View>();

	public EventPagerAdapter(Context context, ViewPager pager) {
	}

	@Override
	public int getCount() {
		return mViews.size();
	}

	@Override
	public int getItemPosition(Object object) {
		int index = mViews.indexOf(object);
		if (index == -1)
			return POSITION_NONE;
		else
			return index;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View v = mViews.get(position);
		container.addView(v);
		return v;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(mViews.get(position));
	}

	public int addView(View v) {
		return addView(v, mViews.size());
	}

	public int addView(View v, int position) {
		mViews.add(position, v);
		notifyDataSetChanged();
		return position;
	}

	public int removeView(ViewPager pager, View v) {
		return removeView(pager, mViews.indexOf(v));
	}

	public int removeView(ViewPager pager, int position) {
		pager.setAdapter(null);
		mViews.remove(position);
		pager.setAdapter(this);
		notifyDataSetChanged();
		return position;
	}

	public int indexOf(View v) {
		return mViews.indexOf(v);
	}

	public View getView(int position) {
		return mViews.get(position);
	}
}
