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
 * Created on 9/1/15 5:27 PM
 */
package com.odoo.addons.calendar.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

import com.odoo.R;

import java.util.Calendar;
import java.util.Locale;

public class TodayIcon {
    public static final String TAG = TodayIcon.class.getSimpleName();
    private Context mContext;
    private Resources mRes;
    private TextPaint mPaint = new TextPaint();
    private Rect mBounds = new Rect();
    private Canvas mCanvas = new Canvas();
    private Bitmap mDefaultIcon;

    public TodayIcon(Context context) {
        mContext = context;
        mRes = mContext.getResources();
    }

    private int date() {
        return Calendar.getInstance(Locale.getDefault()).get(
                Calendar.DAY_OF_MONTH);
    }

    public static TodayIcon get(Context context) {
        return new TodayIcon(context);
    }

    public Drawable getIcon() {
        mPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        mPaint.setColor(Color.WHITE);
        mPaint.setTextAlign(Align.CENTER);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(mRes.getDimension(R.dimen.text_size_xxsmall));
        mDefaultIcon = BitmapFactory.decodeResource(mRes,
                R.drawable.ic_action_goto_today);
        Bitmap bmp = generate(mDefaultIcon.getWidth(), mDefaultIcon.getHeight());
        return new BitmapDrawable(mRes, bmp);
    }

    private Bitmap generate(int width, int height) {
        final String date = (date() < 10) ? "0" + date() + "" : date() + "";
        final Canvas c = mCanvas;
        final Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        c.setBitmap(bitmap);
        c.drawBitmap(mDefaultIcon, 0, 0, null);
        c.drawColor(Color.TRANSPARENT);
        mPaint.getTextBounds(date, 0, 2, mBounds);
        c.drawText(date, 0, 2, width / 2, 5 + height / 2
                + (mBounds.bottom - mBounds.top) / 2, mPaint);
        return bitmap;
    }
}
