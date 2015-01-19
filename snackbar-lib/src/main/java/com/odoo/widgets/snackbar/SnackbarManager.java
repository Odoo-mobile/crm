package com.odoo.widgets.snackbar;

import android.app.Activity;

public class SnackbarManager {

	private static SnackbarManager INSTANCE = new SnackbarManager();

	public static SnackbarManager getInstance() {
		return INSTANCE;
	}

	private SnackbarBuilder currentSnackbar;

	private SnackbarManager() {
	}

	public void show(SnackbarBuilder snackbar, Activity target) {
		if (currentSnackbar != null) {
			currentSnackbar.dismiss();
		}
		currentSnackbar = snackbar;
		currentSnackbar.show(target);
	}
}
