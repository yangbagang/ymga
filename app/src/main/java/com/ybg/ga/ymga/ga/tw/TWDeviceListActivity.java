package com.ybg.ga.ymga.ga.tw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * Created by yangbagang on 2015/5/28.
 */
public class TWDeviceListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tw_device_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.twDeviceListToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void selectTWDevice(View view) {
        Intent intent = new Intent();
        if(view.getId() == R.id.twIRLabel1) {
            intent.putExtra("twDeviceName", "AT-U106");
            intent.putExtra("twDeviceModel", "ir");
        }
        setResult(AppConstat.TW_DEVICE_RESULT_CODE, intent);
        finish();
    }

}
