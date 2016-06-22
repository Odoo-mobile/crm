package com.odoo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OControls;

public class SetupActivity extends AppCompatActivity {

    private OUser mUser;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_setup);
        mUser = OUser.current(this);
        init();
    }

    private void init() {
        progressBar = (ProgressBar) findViewById(R.id.setupProgress);
        OControls.setText(findViewById(android.R.id.content), R.id.txvUsername,
                String.format("Hello %s", mUser.getName()));
        if (!mUser.getAvatar().equals("false")) {
            Bitmap bitmap = BitmapUtils.getBitmapImage(this, mUser.getAvatar());
            if (bitmap != null)
                OControls.setImage(findViewById(android.R.id.content), R.id.user_avatar, bitmap);
        }
    }
}
