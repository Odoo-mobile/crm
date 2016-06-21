package com.odoo.core.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.odoo.OdooActivity;
import com.odoo.R;
import com.odoo.SetupActivity;
import com.odoo.config.IntroSliderItems;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooUserLoginSelectorDialog;
import com.odoo.core.utils.OPreferenceManager;
import com.odoo.widgets.slider.SliderView;

public class AppIntro extends AppCompatActivity implements
        OdooUserLoginSelectorDialog.IUserLoginSelectListener {
    public static final String KEY_FRESH_LOGIN = "key_fresh_login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_intro);
        SliderView sliderView = (SliderView) findViewById(R.id.sliderView);
        IntroSliderItems sliderItems = new IntroSliderItems();

        if (OdooAccountManager.anyActiveUser(this)) {
            if (!startSetupActivity()) {
                startOdooActivity();
            }
        } else if (OdooAccountManager.hasAnyAccount(this)) {
            onRequestAccountSelect();
        }

        if (!sliderItems.getItems().isEmpty()) {
            sliderView.setItems(getSupportFragmentManager(), sliderItems.getItems());
        } else {
            finish();
        }

        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AppIntro.this, OdooLogin.class));
                finish();
            }
        });
    }

    private boolean startSetupActivity() {
        OPreferenceManager preferenceManager = new OPreferenceManager(this);
        if (!preferenceManager.getBoolean(KEY_FRESH_LOGIN, false)) {
            preferenceManager.setBoolean(KEY_FRESH_LOGIN, true);
            startActivity(new Intent(this, SetupActivity.class));
            finish();
            return true;
        }
        return false;
    }

    @Override
    public void onUserSelected(OUser user) {
        OdooAccountManager.login(this, user.getAndroidName());
        if (!startSetupActivity()) {
            startOdooActivity();
        }
    }

    @Override
    public void onNewAccountRequest() {

    }

    @Override
    public void onCancelSelect() {

    }

    public void startOdooActivity() {
        startActivity(new Intent(AppIntro.this, OdooActivity.class));
        finish();
    }

    @Override
    public void onRequestAccountSelect() {
        OdooUserLoginSelectorDialog dialog = new OdooUserLoginSelectorDialog(this);
        dialog.setUserLoginSelectListener(this);
        dialog.show();
    }
}
