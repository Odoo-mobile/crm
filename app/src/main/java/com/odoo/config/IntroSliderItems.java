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
 * Created on 13/2/15 4:16 PM
 */
package com.odoo.config;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.core.utils.OControls;
import com.odoo.R;
import com.odoo.widgets.slider.SliderItem;
import com.odoo.widgets.slider.SliderPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class IntroSliderItems implements SliderPagerAdapter.SliderBuilderListener, View.OnClickListener {
    public static final String TAG = IntroSliderItems.class.getSimpleName();
    private Context mContext;

    public List<SliderItem> getItems() {
        List<SliderItem> items = new ArrayList<>();
        items.add(new SliderItem("Daily Planner", "Odoo CRM keeps you organized, focused and more productive",
                R.drawable.slide_1, this)
                .putExtra("sub_title", "Manage everything in one place"));
        items.add(new SliderItem("Live caller ID", "Get information about customer and recent opportunity before you pickup the phone.",
                R.drawable.slide_3, this)
                .putExtra("sub_title", "See who's calling"));
        items.add(new SliderItem("Reminders", "Use reminders to make sure no phone-calls, meetings or opportunities forgotten",
                R.drawable.slide_4, this)
                .putExtra("sub_title", "Reminder with quick actions"));
        items.add(new SliderItem("Easy actions", "Odoo CRM offers simple, quick and easy actions at your fingertips",
                R.drawable.slide_5, this)
                .putExtra("sub_title", "Getting things done quickly"));
        items.add(new SliderItem("Manage quotations", "Create/Manage quotations and manage order lines easily",
                R.drawable.slide_6, this)
                .putExtra("sub_title", "Easily manage order lines"));
        items.add(new SliderItem("", "All the data automatically synchronized with server when you re-connect to internet",
                R.drawable.no_network, this)
                .putExtra("sub_title", "Works offline"));
        items.add(new SliderItem("", "Works with Odoo Saas cloud",
                R.drawable.saas_support, this)
                .putExtra("sub_title", "Odoo Saas Support"));
        items.add(new SliderItem("Let's Start", "",
                R.drawable.odoo_shaded, this)
                .putExtra("sub_title", "Start exploring Odoo CRM"));
        return items;
    }

    @Override
    public View getCustomView(Context context, SliderItem item, ViewGroup parent) {
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.base_intro_slider_view, parent, false);
        OControls.setText(view, R.id.big_title, item.getTitle());
        OControls.setImage(view, R.id.slider_image, item.getImagePath());
        OControls.setText(view, R.id.sub_title, item.getExtras().get("sub_title").toString());
        OControls.setText(view, R.id.description, item.getContent());
        if (item.getImagePath() == R.drawable.odoo_shaded) {
            OControls.setGone(view, R.id.description);
            OControls.setVisible(view, R.id.btnSliderFinish);
            OControls.setText(view, R.id.btnSliderFinish, "GOT IT, LET'S GO!");
            view.findViewById(R.id.btnSliderFinish).setOnClickListener(this);
        } else {
            OControls.setVisible(view, R.id.description);
            OControls.setGone(view, R.id.btnSliderFinish);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSliderFinish) {
            ((Activity) mContext).finish();
        }
    }
}
