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
 * Created on 10/6/16 11:45 AM
 */
package com.odoo.core.utils.controls;

import android.app.Activity;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.odoo.R;
import com.odoo.core.utils.OControls;

import java.util.ArrayList;
import java.util.List;

public class OBottomSheet extends BottomSheetDialog implements View.OnClickListener, MenuItem.OnMenuItemClickListener {
    public static final String TAG = OBottomSheet.class.getSimpleName();
    private Activity activity;
    private int action_menus = -1;
    private int action_icon = -1;
    private String sheetTitle = null;
    private Object data = null;

    // Menu
    private Menu mMenu;
    private List<MenuItem> visibleMenuItems = new ArrayList<>();
    // Listeners
    private OSheetMenuCreateListener mOSheetMenuCreateListener;
    private OSheetItemClickListener mOSheetItemClickListener;
    private OSheetActionClickListener mOSheetActionClickListener;

    public OBottomSheet(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    public OBottomSheet setSheetActionsMenu(int action_menu) {
        action_menus = action_menu;
        mMenu = new MenuBuilder(getContext());
        MenuInflater menuInflater = activity.getMenuInflater();
        menuInflater.inflate(action_menu, mMenu);
        for (int i = 0; i < mMenu.size(); i++) {
            MenuItem item = mMenu.getItem(i);
            if (item.isVisible()) {
                visibleMenuItems.add(item);
            }
        }
        return this;
    }

    public OBottomSheet setSheetTitle(String title) {
        sheetTitle = title;
        return this;
    }

    @Override
    public void show() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet, null, false);
        if (sheetTitle == null && action_icon <= 0) {
            // set title container visibility to gone
            OControls.setGone(view, R.id.titleContainer);
        } else {
            OControls.setText(view, android.R.id.title, sheetTitle);
            if (action_icon > 0) {
                OControls.setImage(view, android.R.id.icon, action_icon);
                view.findViewById(android.R.id.icon).setOnClickListener(this);
            }
        }
        LinearLayout gridRowsContainer = (LinearLayout) view.findViewById(R.id.sheet_grid_rows_container);
        gridRowsContainer.removeAllViews();
        if (mOSheetMenuCreateListener != null) {
            mOSheetMenuCreateListener.onSheetMenuCreate(mMenu, data);
        }
        createGridView(gridRowsContainer);
        setContentView(view);
        super.show();
    }

    private void createGridView(LinearLayout gridRowContainer) {
        int menus = visibleMenuItems.size();
        int rows = (menus <= 3) ? 1 : ((int) Math.floor(menus / 3)) + ((menus % 3) != 0 ? 1 : 0);
        int index = 0;
        for (int i = 0; i < rows; i++) {
            LinearLayout rowView = (LinearLayout) LayoutInflater.from(getContext())
                    .inflate(R.layout.bottom_sheet_grid_row, gridRowContainer, false);
            for (int j = 0; j < 3; j++) {
                if (index < mMenu.size()) {
                    rowView.addView(getMenuView(visibleMenuItems.get(index), rowView));
                } else {
                    rowView.addView(getDummyView(rowView));
                }
                index++;
            }
            gridRowContainer.addView(rowView);
        }
    }

    private View getMenuView(final MenuItem menu, ViewGroup parent) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.bottom_sheet_grid_item, parent, false);
        ImageView icon = (ImageView) view.findViewById(R.id.grid_icon);
        icon.setImageDrawable(menu.getIcon());
        OControls.setText(view, R.id.grid_icon_title, menu.getTitle());
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMenuItemClick(menu);
            }
        });
        return view;
    }

    public View getDummyView(ViewGroup parent) {
        LinearLayout view = (LinearLayout) LayoutInflater.from(getContext())
                .inflate(R.layout.bottom_sheet_grid_item, parent, false);
        view.setVisibility(View.INVISIBLE);
        return view;
    }

    public OBottomSheet setActionIcon(int action_icon, OSheetActionClickListener callback) {
        this.action_icon = action_icon;
        mOSheetActionClickListener = callback;
        return this;
    }

    public OBottomSheet setData(Object data) {
        this.data = data;
        return this;
    }

    public OBottomSheet setSheetItemClickListener(OSheetItemClickListener callback) {
        mOSheetItemClickListener = callback;
        return this;
    }

    public OBottomSheet setSheetMenuCreateListener(OSheetMenuCreateListener callback) {
        mOSheetMenuCreateListener = callback;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == android.R.id.icon && mOSheetActionClickListener != null) {
            mOSheetActionClickListener.onSheetActionClick(this, data);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (mOSheetItemClickListener != null) {
            mOSheetItemClickListener.onSheetItemClick(this, item, data);
        }
        return true;
    }

    public interface OSheetItemClickListener {
        void onSheetItemClick(OBottomSheet sheet, MenuItem item, Object data);
    }

    public interface OSheetMenuCreateListener {
        void onSheetMenuCreate(Menu menu, Object data);
    }

    public interface OSheetActionClickListener {
        void onSheetActionClick(OBottomSheet sheet, Object data);
    }
}
