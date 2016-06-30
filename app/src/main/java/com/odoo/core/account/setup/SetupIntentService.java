package com.odoo.core.account.setup;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.odoo.base.addons.ir.IrModule;
import com.odoo.config.BaseConfig;
import com.odoo.core.account.setup.utils.Priority;
import com.odoo.core.account.setup.utils.SetupUtils;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.service.OSyncAdapter;
import com.odoo.core.support.OUser;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
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

    public static final String TAG = SetupIntentService.class.getCanonicalName();
    public static final String ACTION_SETUP_RESPONSE = "setup_response";
    public static final String EXTRA_PROGRESS = "extra_progress_value";
    public static final String EXTRA_ERROR = "extra_error_response";
    public static final String KEY_DEPENDENCY_ERROR = "module_dependency_error";
    public static final String KEY_MODULES = "modules";
    public static final String KEY_SKIP_MODULE_CHECK = "skip_module_check";
    public static final String KEY_SETUP_FINISHED = "setup_finished";
    public static final String KEY_NO_APP_ACCESS = "no_application_access";
    public static final String KEY_RUNNING_TASK = "running_task";

    public static final String KEY_TASK_BASE_DATA = "base_data";
    public static final String KEY_TASK_USER_RIGHTS_GROUPS = "user_rights_and_groups";
    public static final String KEY_TASK_MODULE_CONFIGURATION = "application_configurations";
    public static final String KEY_TASK_FEATURE_DATA = "application_feature_data";

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
        Bundle extra = intent.getExtras();

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
        pushTaskStatus(KEY_TASK_BASE_DATA);
        syncModels(setupModels.get(Priority.HIGH));

        // Check for module dependency
        if (!extra.containsKey(KEY_SKIP_MODULE_CHECK) && !checkModuleDependency()) {
            return;
        }

        // Syncing user groups and model access rights (Access rights and res groups data)
        Log.v(TAG, "Processing access rights models");
        pushTaskStatus(KEY_TASK_USER_RIGHTS_GROUPS);
        syncModels(setupModels.get(Priority.MEDIUM));

        // Syncing xml id data for models
        Log.v(TAG, "Processing model data and module configuration");
        pushTaskStatus(KEY_TASK_MODULE_CONFIGURATION);
        syncModels(setupModels.get(Priority.LOW));
        //getting application configurations
        syncModels(setupModels.get(Priority.CONFIGURATION));

        // Check for user access to each modules.
        // Returns, if user have no any access to use sale/crm app
        if (!checkUserAccessGroup()) {
            Log.e(TAG, "User has no access to application.");
            return;
        }
        // Master records for each model references
        Log.v(TAG, "Processing master record models for each model");
        pushTaskStatus(KEY_TASK_FEATURE_DATA);
        syncModels(setupModels.get(Priority.DEFAULT));

        Log.v(TAG, "All set. Setup service finished.");
        Bundle data = new Bundle();
        data.putBoolean(KEY_SETUP_FINISHED, true);
        pushUpdate(data);
    }

    private void pushTaskStatus(String task) {
        Bundle data = new Bundle();
        data.putString(KEY_RUNNING_TASK, task);
        pushUpdate(data);
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
        Bundle extra = new Bundle();
        extra.putInt(EXTRA_PROGRESS, (totalFinishedTasks * 100) / totalTasks);
        pushUpdate(extra);
    }

    private SyncResult syncData(OModel model) {
        SyncResult result = new SyncResult();
        OSyncAdapter syncAdapter = new OSyncAdapter(getApplicationContext(), model.getClass(), null, true);
        syncAdapter.setModelLogOnly(true);
        syncAdapter.setModel(model);
        syncAdapter.checkForCreateDate(false);
        syncAdapter.onPerformSync(user.getAccount(), null, model.authority(), null, result);
        return result;
    }


    private void pushUpdate(Bundle extra) {
        extra = extra != null ? extra : Bundle.EMPTY;
        Intent intent = new Intent(ACTION_SETUP_RESPONSE);
        intent.putExtras(extra);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    private void pushError(Bundle extra) {
        Intent data = new Intent(ACTION_SETUP_RESPONSE);
        data.putExtras(extra);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(data);
    }

    private List<String> geModulesName(boolean checkForNotInstalled) {
        IrModule module = new IrModule(getApplicationContext(), user);
        List<String> modules = new ArrayList<>();
        for (ODataRow row : module.select()) {
            if (checkForNotInstalled) {
                if (!row.getString("state").equals("installed")) {
                    modules.add(row.getString("shortdesc"));
                }
            } else {
                modules.add(row.getString("shortdesc"));
            }
        }
        return modules;
    }

    private boolean checkModuleDependency() {
        List<String> modulesNotInstalled = geModulesName(true);
        if (!modulesNotInstalled.isEmpty()) {
            Log.e(TAG, "Dependency modules are not installed on server : " + modulesNotInstalled);
            Bundle data = new Bundle();
            data.putString(EXTRA_ERROR, KEY_DEPENDENCY_ERROR);
            data.putStringArray(KEY_MODULES, modulesNotInstalled.toArray(new String[modulesNotInstalled.size()]));
            pushError(data);
            return false;
        }
        return true;
    }

    private boolean checkUserAccessGroup() {
        for (String group : BaseConfig.USER_GROUPS) {
            if (user.hasGroup(group)) {
                return true;
            }
        }
        // No access to app
        List<String> modules = geModulesName(false);
        Bundle data = new Bundle();
        data.putString(EXTRA_ERROR, KEY_NO_APP_ACCESS);
        data.putStringArray(KEY_MODULES, modules.toArray(new String[modules.size()]));
        pushError(data);
        return false;
    }

}
