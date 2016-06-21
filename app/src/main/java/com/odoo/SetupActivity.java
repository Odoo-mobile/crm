package com.odoo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;

public class SetupActivity extends AppCompatActivity {

    private ImageView userAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        init();
        OUser currentUser = OUser.current(this);
        if (!currentUser.getAvatar().equals("false")) {
            Bitmap bitmap = BitmapUtils.getBitmapImage(this, currentUser.getAvatar());
            if (bitmap != null)
                userAvatar.setImageBitmap(bitmap);
        }
    }

    private void init() {
        userAvatar = (ImageView) findViewById(R.id.user_avatar);
    }
}
