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
 * Created on 12/1/15 11:13 AM
 */
package com.odoo.addons.calendar.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.odoo.core.orm.ODataRow;
import com.odoo.R;

import java.util.ArrayList;
import java.util.List;

public class EventColorDialog implements AdapterView.OnItemClickListener {
    public static final String TAG = EventColorDialog.class.getSimpleName();
    private Builder builder = null;
    private Context mContext;
    private ArrayAdapter<ODataRow> mAdapter;
    private List<ODataRow> colors = new ArrayList<ODataRow>();
    private String selectedColor;
    private OnColorSelectListener mOnColorSelectListener;
    private AlertDialog alertDialog;

    public EventColorDialog(Context context, String selected_color,
                            OnColorSelectListener listener) {
        mContext = context;
        selectedColor = selected_color;
        mOnColorSelectListener = listener;
        String[] bg_colors = CalendarUtils.getBackgroundColors();
        String[] color_labels = CalendarUtils.getColorLabels();
        for (int i = 0; i < bg_colors.length; i++) {
            ODataRow clr = new ODataRow();
            clr.put("index", i);
            clr.put("code", bg_colors[i]);
            clr.put("label", color_labels[i]);
            colors.add(clr);
        }
    }

    public AlertDialog build() {
        builder = new Builder(mContext);
        builder.setView(getColorGrid());
        alertDialog = builder.create();
        return alertDialog;
    }

    private View getColorGrid() {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.event_color_grid, null, false);
        initGrid((GridView) layout.findViewById(R.id.event_grid));
        return layout;
    }

    private void initGrid(GridView view) {
        mAdapter = new ArrayAdapter<ODataRow>(mContext,
                R.layout.event_color_chooser_item, colors) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ODataRow row = colors.get(position);
                View view = convertView;
                if (view == null) {
                    view = LayoutInflater.from(mContext).inflate(
                            R.layout.event_color_chooser_item, parent, false);
                }

                TextView color_label = (TextView) view
                        .findViewById(R.id.color_label);
                color_label.setText(row.getString("label"));
                ImageView color_view = (ImageView) view
                        .findViewById(R.id.color_view);
                color_view.setColorFilter(Color.parseColor(row
                        .getString("code")));

                boolean mSelected = (selectedColor.equals(row.getString("code")));
                if (mSelected) {
                    color_label.setTextColor(mContext.getResources().getColor(
                            R.color.theme_secondary_dark));
                    view.findViewById(R.id.color_view_selected).setVisibility(
                            View.VISIBLE);
                } else {
                    color_label.setTextColor(Color.parseColor("#414141"));
                    view.findViewById(R.id.color_view_selected).setVisibility(
                            View.GONE);
                }
                return view;
            }
        };
        view.setAdapter(mAdapter);
        view.setOnItemClickListener(this);
    }

    public interface OnColorSelectListener {
        public void colorSelected(ODataRow color_data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (mOnColorSelectListener != null) {
            mOnColorSelectListener.colorSelected(colors.get(position));
        }
        alertDialog.dismiss();
    }
}
