package com.odoo.core.account.setup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.odoo.R;
import com.odoo.config.BaseConfig;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OControls;
import com.odoo.core.utils.OPreferenceManager;

public class SetupActivity extends AppCompatActivity {

    public static final String KEY_APP_DATA_SETUP = "app_data_setup";
    private OUser mUser;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_setup);
        mUser = OUser.current(this);
        init();

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(setupResponseReceiver,
                        new IntentFilter(SetupIntentService.ACTION_SETUP_RESPONSE));
    }

    private void init() {
        progressBar = (ProgressBar) findViewById(R.id.setupProgress);
        progressBar.setMax(100);
        OControls.setText(findViewById(android.R.id.content), R.id.txvUsername,
                String.format("Hello %s", mUser.getName()));
        if (!mUser.getAvatar().equals("false")) {
            Bitmap bitmap = BitmapUtils.getBitmapImage(this, mUser.getAvatar());
            if (bitmap != null)
                OControls.setImage(findViewById(android.R.id.content), R.id.user_avatar, bitmap);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        startSetupService(null);
    }

    private void startSetupService(Bundle data) {
        data = data != null ? data : Bundle.EMPTY;
        Intent intent = new Intent(this, SetupIntentService.class);
        intent.putExtras(data);
        startService(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(setupResponseReceiver);
    }

    private void setupFinished() {
        OPreferenceManager preferenceManager = new OPreferenceManager(this);
        preferenceManager.setBoolean(KEY_APP_DATA_SETUP, true);
    }

    private BroadcastReceiver setupResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extra = intent.getExtras();
            if (extra.containsKey(SetupIntentService.EXTRA_ERROR)) {
                String errorKey = extra.getString(SetupIntentService.EXTRA_ERROR);
                assert errorKey != null;
                if (errorKey.equals(SetupIntentService.KEY_DEPENDENCY_ERROR)) {
                    String[] modules = extra.getStringArray(SetupIntentService.KEY_MODULES);
                    showNoModulesInstallDialog(modules);
                }

            } else {
                int progress = intent.getExtras().getInt(SetupIntentService.EXTRA_PROGRESS);
                progressBar.setProgress(progress);
            }
        }
    };


    private void showNoModulesInstallDialog(String[] modules) {

        final BottomSheetDialog sheetDialog = new BottomSheetDialog(this);

        View view = LayoutInflater.from(this).inflate(R.layout.base_setup_error_view, null, false);
        String mods = TextUtils.join(", ", modules);
        final boolean noAnyModule = modules.length == BaseConfig.DEPENDS_ON_MODULES.length;
        if (noAnyModule) {
            // No module installed on server
            OControls.setText(view, android.R.id.title, getString(R.string.title_no_modules));
            OControls.setText(view, android.R.id.content, getString(R.string.message_modules_not_installed, mods));

            OControls.setVisible(view, R.id.btnOkay);
            view.findViewById(R.id.btnOkay).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sheetDialog.isShowing()) sheetDialog.dismiss();
                    finish();
                }
            });
        } else {
            OControls.setText(view, android.R.id.title, getString(R.string.title_oops));
            OControls.setText(view, android.R.id.content,
                    getString(R.string.message_some_module_dependency_failed, mods, mods));

            OControls.setVisible(view, R.id.btnContinue);
            view.findViewById(R.id.btnContinue).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sheetDialog.isShowing()) sheetDialog.dismiss();
                    continueSetup();
                }
            });
        }

        sheetDialog.setContentView(view);
        sheetDialog.setCancelable(false);
        sheetDialog.setCanceledOnTouchOutside(false);
        sheetDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if (noAnyModule) finish();
                else continueSetup();
            }
        });
        sheetDialog.show();
    }

    private void continueSetup() {
        Bundle data = new Bundle();
        data.putBoolean(SetupIntentService.KEY_SKIP_MODULE_CHECK, true);
        startSetupService(data);
    }

}
