package com.odoo.widgets.snackbar;

import android.app.Activity;
import android.content.Context;

import com.odoo.widgets.snackbar.SnackbarBuilder.SnackbarDuration;
import com.odoo.widgets.snackbar.listeners.ActionClickListener;
import com.odoo.widgets.snackbar.listeners.EventListener;

public class SnackBar {
    private Context mContext;
    private SnackbarManager snack;
    private SnackbarBuilder bar;

    public SnackBar(Context context) {
        mContext = context;
        snack = SnackbarManager.getInstance();
    }

    public static SnackBar get(Context context) {
        return new SnackBar(context);
    }

    public SnackBar text(int res_id) {
        return text(mContext.getResources().getString(res_id));
    }

    public SnackBar text(String text) {
        bar = SnackbarBuilder.with(mContext).text(text);
        return this;
    }

    public SnackBar withEventListener(EventListener listener) {
        bar.eventListener(listener);
        return this;
    }

    public SnackBar withAction(int res_id, ActionClickListener action) {
        return withAction(mContext.getResources().getString(res_id), action);
    }

    public SnackBar withAction(String label, ActionClickListener action) {
        bar.actionLabel(label).actionListener(action);
        return this;
    }

    public SnackBar duration(SnackbarDuration duration) {
        bar.duration(duration);
        return this;
    }

    public SnackBar actionColor(int color) {
        bar.actionColor(color);
        return this;
    }

    public void show() {

        snack.show(bar, (Activity) mContext);
    }
}
