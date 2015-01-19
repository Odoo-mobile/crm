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
 * Created on 12/1/15 5:25 PM
 */
package com.odoo.core.utils;

import android.app.AlertDialog;
import android.content.Context;

import com.odoo.crm.R;

public class OAlert {
    public static final String TAG = OAlert.class.getSimpleName();

    private enum Type {
        Alert, Warning, Error
    }

    public static void showAlert(Context context, String message) {
        show(context, message, Type.Alert);
    }

    public static void showWarning(Context context, String message) {
        show(context, message, Type.Warning);
    }

    public static void showError(Context context, String message) {
        show(context, message, Type.Error);
    }

    private static void show(Context context, String message, Type type) {
        AlertDialog.Builder mBuilder;
        mBuilder = new AlertDialog.Builder(context);
        switch (type) {
            case Alert:
                mBuilder.setTitle(R.string.label_alert);
                break;
            case Error:
                mBuilder.setTitle(R.string.label_error);
                break;
            case Warning:
                mBuilder.setTitle(R.string.label_warning);
        }
        mBuilder.setMessage(message);
        mBuilder.setPositiveButton(R.string.label_ok, null);
        mBuilder.create().show();
    }
}
