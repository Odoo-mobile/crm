package com.odoo.core.account.setup.utils;

public enum Priority {

    /**
     * Auto sync all the data for the model in setting up activity.
     * It will be ignored at the sync service.
     */
    DEFAULT,
    /**
     * At the time of syncing xml data for given models.
     * Run at setting activity at last but before DEFAULT priority
     */
    LOW,
    /**
     * HIGH priority models are sync first before any operation performed.
     */
    HIGH,
    /**
     * MEDIUM priority models are sync after HIGH models sync finished.
     * Mainly User groups, models access rights
     */
    MEDIUM,

    /**
     * CONFIGURATION models
     */
    CONFIGURATION
}
