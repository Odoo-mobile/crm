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
 * Created on 29/6/16 3:48 PM
 */
package com.odoo.core.support.hintcase;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;

public class HintCaseItem {
    private String title, content;
    @DrawableRes
    private int drawableImage = -1;
    private boolean circleShape = false;
    @IdRes
    private int viewId = -1;

    public HintCaseItem() {

    }

    public HintCaseItem(String title, String content, @IdRes int viewId) {
        this.title = title;
        this.content = content;
        this.viewId = viewId;
    }

    public HintCaseItem(String title, String content, @IdRes int viewId, @DrawableRes int drawableImage) {
        this.title = title;
        this.content = content;
        this.viewId = viewId;
        this.drawableImage = drawableImage;
    }

    public HintCaseItem(String title, String content, @IdRes int viewId, @DrawableRes int drawableImage, boolean circleShape) {
        this.title = title;
        this.content = content;
        this.viewId = viewId;
        this.drawableImage = drawableImage;
        this.circleShape = circleShape;
    }

    public String getTitle() {
        return title;
    }

    public HintCaseItem setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public HintCaseItem setContent(String content) {
        this.content = content;
        return this;
    }

    public int getDrawableImage() {
        return drawableImage;
    }

    public HintCaseItem setDrawableImage(int drawableImage) {
        this.drawableImage = drawableImage;
        return this;
    }

    public boolean isCircleShape() {
        return circleShape;
    }

    public HintCaseItem withCircleShape() {
        this.circleShape = true;
        return this;
    }

    public int getViewId() {
        return viewId;
    }

    public HintCaseItem setViewId(int viewId) {
        this.viewId = viewId;
        return this;
    }
}
