package com.odoo.core.account.setup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.odoo.R;
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
                .registerReceiver(progressReceiver,
                        new IntentFilter(SetupIntentService.ACTION_PROGRESS_UPDATE));
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
        startService(new Intent(this, SetupIntentService.class));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(progressReceiver);
    }

    private void setupFinished() {
        OPreferenceManager preferenceManager = new OPreferenceManager(this);
        preferenceManager.setBoolean(KEY_APP_DATA_SETUP, true);
    }

    private BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int progress = intent.getExtras().getInt(SetupIntentService.EXTRA_PROGRESS);
            progressBar.setProgress(progress);
        }
    };
}
