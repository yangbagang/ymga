package com.ybg.ga.ymga.ga.tw;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.ga.preference.TWPreference;

/**
 * Created by yangbagang on 2015/5/28.
 */
public class TwSettingActivity extends AppCompatActivity {

    private TWPreference twPreference = TWPreference.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();

    private RadioButton userTWCC = null;
    private RadioButton userTWFF = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tw_setting);

        initView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.twSettingToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        userTWCC = (RadioButton) findViewById(R.id.userTWCC);
        userTWFF = (RadioButton) findViewById(R.id.userTWFF);

        boolean ccIsDefaultUnit = twPreference.isCAsDefaultUnit();
        if (ccIsDefaultUnit) {
            userTWCC.setChecked(true);
            userTWFF.setChecked(false);
        } else {
            userTWCC.setChecked(false);
            userTWFF.setChecked(true);
        }

        userTWCC.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                userTWCC.setSelected(true);
                userTWFF.setSelected(false);

                twPreference.setCAsDefaultUnit(true);
            }

        });
        userTWFF.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                userTWCC.setSelected(false);
                userTWFF.setSelected(true);

                twPreference.setCAsDefaultUnit(false);
            }

        });
    }

    public void saveUserSetting(View view) {
        ybgApp.showMessage(getApplication(), "设置己经保存");
        finish();
    }

}
