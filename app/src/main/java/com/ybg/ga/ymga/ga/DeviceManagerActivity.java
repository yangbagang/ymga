package com.ybg.ga.ymga.ga;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.ga.preference.TWPreference;
import com.ybg.ga.ymga.ga.preference.TZPreference;
import com.ybg.ga.ymga.ga.preference.XYPreference;
import com.ybg.ga.ymga.ga.preference.YdPreference;

/**
 * Created by yangbagang on 2017/1/1.
 */
public class DeviceManagerActivity extends AppCompatActivity {

    private TZPreference tzPreference = TZPreference.getInstance();
    private XYPreference xyPreference = XYPreference.getInstance();
    private YdPreference ydPreference = YdPreference.getInstance();
    private TWPreference twPreference = TWPreference.getInstance();

    private Button ydPjButton = null;
    private Button tzPjButton = null;
    private Button xyPjButton = null;
    private Button twPjButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_manager);
        initView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.deviceManagerToolbar);
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
        // 读取配件名称，激活解除绑定按钮
        TextView ydPjName = (TextView) findViewById(R.id.ydPjName);
        ydPjButton = (Button) findViewById(R.id.ydPjButton);
        if (ydPreference.hasAssign()) {
            ydPjName.setText("手环：" + ydPreference.getYdDeviceName());
            ydPjButton.setEnabled(true);
        }

        TextView tzPjName = (TextView) findViewById(R.id.tzPjName);
        tzPjButton = (Button) findViewById(R.id.tzPjButton);
        if (tzPreference.hasAssign()) {
            tzPjName.setText("电子秤：" + tzPreference.getTzDeviceName());
            tzPjButton.setEnabled(true);
        }

        TextView xyPjName = (TextView) findViewById(R.id.xyPjName);
        xyPjButton = (Button) findViewById(R.id.xyPjButton);
        if (xyPreference.hasAssign()) {
            xyPjName.setText("血压计：" + xyPreference.getXyDeviceName());
            xyPjButton.setEnabled(true);
        }

        TextView twPjName = (TextView) findViewById(R.id.twPjName);
        twPjButton = (Button) findViewById(R.id.twPjButton);
        if (twPreference.hasAssign()) {
            twPjName.setText("体温计：" + twPreference.getTwDeviceName());
            twPjButton.setEnabled(true);
        }
    }

    public void removeXYDevice(View view) {
        xyPreference.setXyDeviceAddr("");
        xyPreference.setXyDeviceModel("");
        xyPreference.setXyDeviceName("");
        xyPreference.setHasAssign(false);
        xyPjButton.setEnabled(false);
    }

    public void removeTZDevice(View view) {
        tzPreference.setTzDeviceAddr("");
        tzPreference.setTzDeviceModel("");
        tzPreference.setTzDeviceName("");
        tzPreference.setHasAssign(false);
        tzPjButton.setEnabled(false);
    }

    public void removeYDDevice(View view) {
        ydPreference.setYdDeviceAddr("");
        ydPreference.setYdDeviceModel("");
        ydPreference.setYdDeviceName("");
        ydPreference.setHasAssign(false);
        ydPjButton.setEnabled(false);
    }

    public void removeTWDevice(View view) {
        twPreference.setTwDeviceAddr("");
        twPreference.setTwDeviceModel("");
        twPreference.setTwDeviceName("");
        twPreference.setHasAssign(false);
        twPjButton.setEnabled(false);
    }

}
