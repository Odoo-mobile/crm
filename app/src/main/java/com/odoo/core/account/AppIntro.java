package com.odoo.core.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.odoo.R;
import com.odoo.config.IntroSliderItems;
import com.odoo.widgets.slider.SliderView;

public class AppIntro extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_intro);
        SliderView sliderView = (SliderView) findViewById(R.id.sliderView);
        IntroSliderItems sliderItems = new IntroSliderItems();
        if (!sliderItems.getItems().isEmpty()) {
            sliderView.setItems(getSupportFragmentManager(), sliderItems.getItems());
        } else {
            finish();
        }

        findViewById(R.id.btnStart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AppIntro.this,OdooLogin.class));
                finish();
            }
        });
    }
}
