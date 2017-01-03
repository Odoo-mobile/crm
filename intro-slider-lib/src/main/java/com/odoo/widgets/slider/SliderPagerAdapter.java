package com.odoo.widgets.slider;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class SliderPagerAdapter extends FragmentStatePagerAdapter {

    public static final String KEY_POSITION = "key_pos";
    private Context mContext = null;
    private List<SliderItem> mItems = new ArrayList<SliderItem>();

    public SliderPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        PageFragment frag = new PageFragment();
        frag.setAdapter(this);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_POSITION, position);
        frag.setArguments(bundle);
        return frag;

    }

    public SliderItem getSliderItem(int position) {
        return mItems.get(position);
    }

    public void initPager(Context context, List<SliderItem> items) {
        mContext = context;
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    public interface SliderBuilderListener {
        View getCustomView(Context context, SliderItem item,
                           ViewGroup parent);
    }

}
