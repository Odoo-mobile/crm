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
 * Created on 12/1/15 11:12 AM
 */
package com.odoo.addons.calendar.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;

import com.odoo.core.orm.ODataRow;

public class CalendarUtils {
    public static final String TAG = CalendarUtils.class.getSimpleName();
    private static String[] background_colors = {"#a24689", "#d40000",
            "#f24f1d", "#f5be27", "#0a7d40", "#35b579", "#029ce3", "#405ea8",
            "#7986c9", "#8b23a8", "#e37971", "#616161"};
    private static String[] color_label = {"Default Color", "Tomato",
            "Tangerine", "Banana", "Basil", "Sage", "Peacock", "Blueberry",
            "Lavender", "Grape", "Flamingo", "Graphite"};

    public static String[] getBackgroundColors() {
        return background_colors;
    }

    public static String[] getColorLabels() {
        return color_label;
    }

    public static int getBackgroundColor(int color_number) {
        if (color_number < background_colors.length) {
            return Color.parseColor(background_colors[color_number]);
        }
        return Color.parseColor("#ffffff");
    }

    public static String getColorLabel(int color_number) {
        if (color_number < color_label.length) {
            return color_label[color_number];
        }
        return "Default Color";
    }

    public static ODataRow getColorData(int index) {
        ODataRow clr = new ODataRow();
        clr.put("index", index);
        clr.put("code", background_colors[index]);
        clr.put("label", color_label[index]);
        return clr;
    }

    public static AlertDialog colorDialog(Context context, String selected,
                                          EventColorDialog.OnColorSelectListener listener) {
        EventColorDialog dialog = new EventColorDialog(context, selected,
                listener);
        return dialog.build();
    }
}
