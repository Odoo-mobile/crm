/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 29/6/16 3:47 PM
 */
package com.odoo.core.support.hintcase;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;

import com.joanfuentes.hintcase.HintCase;
import com.joanfuentes.hintcase.RectangularShape;
import com.joanfuentes.hintcaseassets.contentholderanimators.FadeInContentHolderAnimator;
import com.joanfuentes.hintcaseassets.hintcontentholders.SimpleHintContentHolder;
import com.joanfuentes.hintcaseassets.shapeanimators.RevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapeanimators.RevealRectangularShapeAnimator;
import com.joanfuentes.hintcaseassets.shapeanimators.UnrevealCircleShapeAnimator;
import com.joanfuentes.hintcaseassets.shapeanimators.UnrevealRectangularShapeAnimator;
import com.joanfuentes.hintcaseassets.shapes.CircularShape;
import com.odoo.R;
import com.odoo.core.utils.OPreferenceManager;

import java.util.HashMap;

public class HintCaseUtils implements HintCase.OnClosedListener {
    public static final String TAG = HintCaseUtils.class.getSimpleName();
    private Activity mActivity;
    private View mView;
    private HashMap<String, HintCaseItem> hintCaseItems = new HashMap<>();
    private int currentIndex = 0;
    private String hintCaseKey = null;
    private Boolean isShowing = false;

    private OPreferenceManager preferenceManager;

    public static HintCaseUtils init(Activity activity, String key) {
        return new HintCaseUtils(activity, key);
    }

    public HintCaseUtils(Activity activity, String key) {
        mActivity = activity;
        hintCaseKey = key;
        mView = mActivity.getWindow().getDecorView();
        preferenceManager = new OPreferenceManager(mView.getContext());
    }

    public HintCaseUtils addHint(HintCaseItem item) {
        hintCaseItems.put("item_" + hintCaseItems.size(), item);
        return this;
    }

    public void show() {
        if (!isShowing) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadHint(getNextItem());
                    isShowing = true;
                }
            }, 700);
        }
    }

    private HintCaseItem getNextItem() {
        String key = "item_" + currentIndex;
        if (hintCaseItems.containsKey(key))
            return hintCaseItems.get(key);
        return null;
    }

    private void loadHint(HintCaseItem item) {
        if (item == null) {
            finishHintCases();
            return;
        }
        if (!isDone()) {
            SimpleHintContentHolder.Builder hintBlock = new SimpleHintContentHolder.Builder(mActivity);
            hintBlock.setContentTitle(item.getTitle());
            hintBlock.setContentText(item.getContent());
            hintBlock.setTitleStyle(R.style.HintCaseTitle);
            hintBlock.setContentStyle(R.style.HintCaseContent);

            if (item.getDrawableImage() != -1) {
                hintBlock.setImageDrawableId(item.getDrawableImage());
            }

            HintCase hintCase = new HintCase(mView);
            hintCase.setBackgroundColor(Color.parseColor("#aa000000"));
            hintCase.setHintBlock(hintBlock.build(), new FadeInContentHolderAnimator());
            if (item.isCircleShape()) {
                hintCase.setTarget(mView.findViewById(item.getViewId()), new CircularShape(), HintCase.TARGET_IS_CLICKABLE);
                hintCase.setShapeAnimators(new RevealCircleShapeAnimator(), new UnrevealCircleShapeAnimator());
            } else {
                hintCase.setTarget(mView.findViewById(item.getViewId()), new RectangularShape(), HintCase.TARGET_IS_CLICKABLE);
                hintCase.setShapeAnimators(new RevealRectangularShapeAnimator(), new UnrevealRectangularShapeAnimator());
            }
            hintCase.setOnClosedListener(this);
            hintCase.show();
        }
    }

    public Boolean isDone() {
        return preferenceManager.getBoolean(hintCaseKey, false);
    }

    @Override
    public void onClosed() {
        currentIndex++;
        loadHint(getNextItem());
    }

    private void finishHintCases() {
        if (!isDone())
            preferenceManager.setBoolean(hintCaseKey, true);
        isShowing = false;
    }
}