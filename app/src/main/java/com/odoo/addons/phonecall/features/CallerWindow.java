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
 * Created on 13/1/15 6:11 PM
 */
package com.odoo.addons.phonecall.features;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.odoo.core.orm.ODataRow;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.R;

public class CallerWindow {
    public static final String TAG = CallerWindow.class.getSimpleName();
    public static final String KEY_CALLER_WINDOW = "key_caller_window";
    private WindowManager windowManager;
    private Context context;
    private OPreferenceManager mPref;
    private KeyguardManager keyguardManager;
    private View callerView = null;

    public CallerWindow(Context context) {
        this.context = context;
        mPref = new OPreferenceManager(context);
        windowManager = (WindowManager) context.getSystemService(Activity.WINDOW_SERVICE);
        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        mPref.setBoolean(KEY_CALLER_WINDOW, false);
    }

    private WindowManager.LayoutParams getWindowParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP;
        return params;
    }

    private View buildView() {
        View view = LayoutInflater.from(context).inflate(R.layout.crm_caller_window_layout,
                null);

        return view;
    }

    public boolean inIdleMode() {
        return keyguardManager.inKeyguardRestrictedInputMode();
    }

    public boolean isLollipop() {
        return (android.os.Build.VERSION.SDK_INT > 19);
    }

    private void bindView(ODataRow row) {
        OControls.setText(callerView, R.id.partner_name, row.getString("name"));
        OControls.setText(callerView, R.id.company_name, row.getString("company_name"));
        Bitmap bmp;
        if (row.getString("image_small").equals("false")) {
            bmp = BitmapUtils.getAlphabetImage(context, row.getString("name"));
        } else {
            String base64;
            if (row.getString("large_image").equals("false")) {
                base64 = row.getString("image_small");
            } else {
                base64 = row.getString("large_image");
            }
            bmp = BitmapUtils.getBitmapImage(context, base64);
        }
        if (row.getString("lead_name").equals("false")) {
            row.put("lead_name", "No any lead found");
        }
        if (row.getString("probability").equals("false"))
            row.put("probability", "");
        OControls.setImage(callerView, R.id.customerImage, bmp);
        OControls.setText(callerView, R.id.leadName, row.getString("lead_name"));
        OControls.setText(callerView, R.id.oppProbability, row.getString("probability"));
        OControls.setText(callerView, R.id.partner_contact, row.getString("caller_contact"));
    }

    public void show(final Boolean dialed, final ODataRow row) {
        if (!mPref.getBoolean(KEY_CALLER_WINDOW, false)) {
            Log.i(TAG, "Showing caller window");
            mPref.setBoolean(KEY_CALLER_WINDOW, true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    callerView = buildView();
                    bindView(row);
                    WindowManager.LayoutParams params = getWindowParams();
                    if (!dialed && !inIdleMode() && isLollipop()) {
                        params.gravity = Gravity.BOTTOM;
                    }
                    windowManager.addView(callerView, params);
                }
            }, 1000);
        }
    }

    public void dismiss() {
        if (mPref.getBoolean(KEY_CALLER_WINDOW, false)) {
            Log.i(TAG, "Removing caller window");
            mPref.setBoolean(KEY_CALLER_WINDOW, false);
            try {
                if (callerView != null)
                    windowManager.removeViewImmediate(callerView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isShowing() {
        return mPref.getBoolean(KEY_CALLER_WINDOW, false);
    }
}
