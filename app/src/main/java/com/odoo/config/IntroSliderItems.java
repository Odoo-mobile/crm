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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.core.utils.OControls;
import com.odoo.crm.R;
import com.odoo.widgets.slider.SliderItem;
import com.odoo.widgets.slider.SliderPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class IntroSliderItems implements SliderPagerAdapter.SliderBuilderListener {
    public static final String TAG = IntroSliderItems.class.getSimpleName();

    public List<SliderItem> getItems() {
        List<SliderItem> items = new ArrayList<>();
        items.add(new SliderItem("Agenda", "Easily share your tasks and get invitations from others",
                R.drawable.slide_1, this)
                .putExtra("sub_title", "Today Agenda"));
        items.add(new SliderItem("Material Design", "Easily share your tasks and get invitations from others",
                R.drawable.slide_2, this)
                .putExtra("sub_title", "Simple, Flat, Easy"));
        items.add(new SliderItem("Call Identity", "Easily share your tasks and get invitations from others",
                R.drawable.slide_3, this)
                .putExtra("sub_title", "Identify your customer"));
        items.add(new SliderItem("Reminder Notification", "Easily share your tasks and get invitations from others",
                R.drawable.slide_4, this)
                .putExtra("sub_title", "Reminder with quick actions"));
        items.add(new SliderItem("Easy operations", "Easily share your tasks and get invitations from others",
                R.drawable.slide_5, this)
                .putExtra("sub_title", "Quick operations"));
        items.add(new SliderItem("Manageable lines", "Easily share your tasks and get invitations from others",
                R.drawable.slide_6, this)
                .putExtra("sub_title", "Easily manage order lines"));
        return items;
    }

    @Override
    public View getCustomView(Context context, SliderItem item, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.base_intro_slider_view, parent, false);
        OControls.setText(view, R.id.big_title, item.getTitle());
        OControls.setImage(view, R.id.slider_image, item.getImagePath());
        OControls.setText(view, R.id.sub_title, item.getExtras().get("sub_title").toString());
        OControls.setText(view, R.id.description, item.getContent());
        return view;
    }
}
