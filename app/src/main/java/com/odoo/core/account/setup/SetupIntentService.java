package com.odoo.core.account.setup;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.odoo.core.account.setup.utils.Priority;
import com.odoo.core.account.setup.utils.SetupUtils;
import com.odoo.core.orm.OModel;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.support.OUser;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;

import odoo.Odoo;

/**
 * Setup Service
 * 1. Syncing for HIGH priority models
 * 2. Getting user groups and model access based on group
 * 3. Syncing models with DEFAULT priority (Such as reference models)
 * 5. TODO: What next ?
 */
public class SetupIntentService extends IntentService {

    public static final String ACTION_PROGRESS_UPDATE = "progress_update";
    public static final String EXTRA_PROGRESS = "extra_progress_value";
    public static final String TAG = SetupIntentService.class.getCanonicalName();
    private Odoo odoo;
    private SetupUtils setupUtils;
    private OUser user;
    private int totalFinishedTasks = 0;
    private int totalTasks = 4;

    public SetupIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        user = OUser.current(getApplicationContext()); // FIXME: can be also request form sync
        Log.d(TAG, "Setup Started for User : " + user.getAndroidName());
        setupUtils = new SetupUtils(getApplicationContext(), user);
        odoo = OSyncAdapter.createOdooInstance(getApplicationContext(), user);
        if (odoo == null) {
            Log.e(TAG, "Unable to create odoo instance for user : " + user.getAndroidName());
            return;
        }
        HashMap<Priority, List<Class<? extends OModel>>> setupModels = setupUtils.getSetupModels();

        // Syncing high priority models first (The Base data)
        Log.v(TAG, "Processing HIGH priority models");
        syncModels(setupModels.get(Priority.HIGH));

        // Syncing user groups and model access rights (Access rights and res groups data)
        Log.v(TAG, "Processing access rights models");
        syncModels(setupModels.get(Priority.MEDIUM));

        // Syncing xml id data for models
        syncModels(setupModels.get(Priority.LOW));

        // Master records for each model references
        Log.v(TAG, "Processing master record models for each model");
        syncModels(setupModels.get(Priority.DEFAULT));

        Log.v(TAG, "All set. Setup service finished.");
    }

    private void syncModels(List<Class<? extends OModel>> models) {
        for (Class<? extends OModel> modelCls : models) {
            try {
                Constructor constructor = modelCls.getConstructor(Context.class, OUser.class);
                OModel model = (OModel) constructor.newInstance(getApplicationContext(), user);
                SyncResult result = syncData(model);
                //TODO: Deal with result B)
            } catch (Exception e) {
                Log.e(TAG, "Model object create fail: " + e.getMessage(), e);
            }
        }
        totalFinishedTasks++;
        pushProgress();
    }

    private SyncResult syncData(OModel model) {
        SyncResult result = new SyncResult();
        OSyncAdapter syncAdapter = new OSyncAdapter(getApplicationContext(), model.getClass(), null, true);
        syncAdapter.setModelLogOnly(true);
        syncAdapter.setModel(model);
        syncAdapter.checkForWriteCreateDate(false);
        syncAdapter.onPerformSync(user.getAccount(), null, model.authority(), null, result);
        return result;
    }


    private void pushProgress() {
        Intent data = new Intent(ACTION_PROGRESS_UPDATE);
        data.putExtra(EXTRA_PROGRESS, (totalFinishedTasks * 100) / totalTasks);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(data);
    }

}
