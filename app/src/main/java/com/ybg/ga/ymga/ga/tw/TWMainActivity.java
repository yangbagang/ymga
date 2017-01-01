package com.ybg.ga.ymga.ga.tw;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTDeviceListActivity;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTStatus;
import com.ybg.ga.ymga.ga.activity.SubActivity;
import com.ybg.ga.ymga.ga.preference.TWPreference;
import com.ybg.ga.ymga.ga.tw.ir.IRBLEActivity;
import com.ybg.ga.ymga.ga.tw.ir.IRBTActivity;
import com.ybg.ga.ymga.util.AppConstat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yangbagang on 2015/5/27.
 */
public class TWMainActivity extends SubActivity {

    private TWPreference twPreference = TWPreference.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private TextView twPJName = null;
    private Button twPJOperator = null;

    private TextView twMeasureData = null;
    private TextView twMeasureTime = null;
    private ImageView twMeasureImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tw_main);

        /** 尝试初始化视图实例 **/
        initView();
        /** 初始化按钮事件 **/
        initEvent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.twMainToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initView() {
        // 标签及按钮
        twPJName = (TextView) findViewById(R.id.twPJName);
        twPJOperator = (Button) findViewById(R.id.twPJOperator);
        // 各项数据
        twMeasureData = (TextView) findViewById(R.id.twValue);
        twMeasureTime = (TextView) findViewById(R.id.twMeasureTime);
        twMeasureImage = (ImageView) findViewById(R.id.twUnit);
    }

    private void initEvent() {
        if (twPreference.hasAssign()) {
            // 己绑定，显示设备名称。准备连接操作
            twPJName.setText(twPreference.getTwDeviceName());
            twPJOperator.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
        } else {
            // 未绑定，提示需要绑定。准备进行设备绑定操作。
            twPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_ASSIGN]);
            twPJOperator.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN]);
        }
    }

    public void twOperation(View view) {
        String operator = ((Button) view).getText().toString();
        if (BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN].equals(operator)) {
            // 还未绑定，开始绑定过程
            Intent intent = new Intent(TWMainActivity.this, TWDeviceListActivity.class);
            getParent().startActivityForResult(intent,
                    AppConstat.TW_DEVICE_REQUEST_CODE);
            return;
        }
        String message = getString(R.string.permission_request_notice, getString(R.string
                .app_name), getString(R.string.permission_access_coarse_location));
        boolean hasRight = ybgApp.checkPermission(TWMainActivity.this, Manifest.permission
                        .ACCESS_COARSE_LOCATION,
                message, AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        if (hasRight) {
            startMeasure();
        }
    }

    private void startMeasure() {
        String model = twPreference.getTwDeviceModel();
        if (twPreference.hasAssign()) {
            if ("irBT".equalsIgnoreCase(model)) {
                //启动双模版
                Intent intent = new Intent(TWMainActivity.this, IRBTActivity.class);
                getParent().startActivityForResult(intent, AppConstat.TW_MEASURE_REQUEST_CODE);
            } else {
                //启动单模版
                Intent intent = new Intent(TWMainActivity.this, IRBLEActivity.class);
                getParent().startActivityForResult(intent, AppConstat.TW_MEASURE_REQUEST_CODE);
            }
        } else {
            // 还未绑定，开始绑定过程
            Intent intent = new Intent(TWMainActivity.this, TWDeviceListActivity.class);
            getParent().startActivityForResult(intent, AppConstat.TW_DEVICE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMeasure();
            } else {
                String message1 = getString(R.string.permission_request_notice, getString(R.string
                        .app_name), getString(R.string.permission_access_coarse_location));
                String message2 = getString(R.string.permission_setting_notice, getString(R.string
                        .app_name));
                ybgApp.showMessage(getApplicationContext(), message1 + message2);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstat.TW_DEVICE_REQUEST_CODE
                && resultCode == AppConstat.TW_DEVICE_RESULT_CODE) {
            // 体重设备列表
            String twDeviceName = data.getExtras().getString("twDeviceName");
            String twDeviceModel = data.getExtras().getString("twDeviceModel");
            if (!"".equals(twDeviceName) && !"".equals(twDeviceModel)) {
                // 记录下设备名称及代号
                twPreference.setTwDeviceName(twDeviceName);
                twPreference.setTwDeviceModel(twDeviceModel);
                twPJName.setText(twDeviceName);
                if ("irBT".equalsIgnoreCase(twDeviceModel)) {
                    // 开始扫描蓝牙设备
                    Intent intent = new Intent(this, BTDeviceListActivity.class);
                    getParent().startActivityForResult(intent,
                            AppConstat.BT_FOUND_REQUEST_CODE);
                } else {
                    // 开启单模
                    Intent intent = new Intent(TWMainActivity.this, IRBLEActivity.class);
                    getParent().startActivityForResult(intent, AppConstat.TW_MEASURE_REQUEST_CODE);
                }
            }
        } else if (requestCode == AppConstat.BT_FOUND_REQUEST_CODE
                && resultCode == AppConstat.BT_FOUND_RESULT_CODE) {
            // 取得需要连接的蓝牙地址
            String twDeviceAddr = data.getExtras().getString(
                    BTAction.EXTRA_DEVICE_ADDRESS);
            twPreference.setTwDeviceAddr(twDeviceAddr);
            // 开启双模
            Intent intent = new Intent(TWMainActivity.this, IRBTActivity.class);
            getParent().startActivityForResult(intent, AppConstat.TW_MEASURE_REQUEST_CODE);
        } else if (requestCode == AppConstat.TW_MEASURE_REQUEST_CODE
                && resultCode == AppConstat.TW_MEASURE_RESULT_CODE) {
            boolean isCC = data.getExtras().getBoolean("isCC");
            float value = data.getExtras().getFloat("value");
            setTemp(isCC, value);
            initEvent();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, TwSettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开启蓝牙
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (bluetoothAdapter == null) {
            ybgApp.showMessage(getApplicationContext(),
                    BTMessage.BLUETOOTH_ADAPTER_NOTFOUND);
        } else {
            // 如未开启，则先开启
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setTemp(boolean isCC, float value) {
        twMeasureTime.setText(sdf.format(new Date()));
        if (isCC) {
            // 得到摄氏度
            if (twPreference.isCAsDefaultUnit()) {
                // 默认显示也是摄氏度，可以直接显示。
                twMeasureData.setText("" + value);
                twMeasureImage.setImageResource(R.mipmap.cc);
            } else {
                // 默认显示华氏度，需要转换后才能显示。
                twMeasureData.setText(new DecimalFormat("#.#").format(WenduTool.c2f(value)));
                twMeasureImage.setImageResource(R.mipmap.ff);
            }
        } else {
            // 得到华氏度
            if (twPreference.isCAsDefaultUnit()) {
                // 默认是摄氏度，需要转换后才能显示。
                twMeasureData.setText(new DecimalFormat("#.#").format(WenduTool.f2c(value)));
                twMeasureImage.setImageResource(R.mipmap.cc);
            } else {
                // 默认显示华氏度，可以直接显示。
                twMeasureData.setText("" + value);
                twMeasureImage.setImageResource(R.mipmap.ff);
            }
        }
    }

}
