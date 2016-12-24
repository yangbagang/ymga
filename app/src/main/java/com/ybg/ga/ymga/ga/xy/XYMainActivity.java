/**
 *
 */
package com.ybg.ga.ymga.ga.xy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTDeviceListActivity;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.bt.BTStatus;
import com.ybg.ga.ymga.ga.activity.SubActivity;
import com.ybg.ga.ymga.ga.preference.XYPreference;
import com.ybg.ga.ymga.ga.xy.urion.UrionService;
import com.ybg.ga.ymga.ga.xy.urion.XYUrionBLEActivity;
import com.ybg.ga.ymga.ga.xy.urion.XYUrionBTActivity;
import com.ybg.ga.ymga.ga.xy.urion.XYUrionService;
import com.ybg.ga.ymga.util.AppConstat;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 杨拔纲
 */
public class XYMainActivity extends SubActivity {

    private XYPreference xyPreference = XYPreference.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();

    private TextView xyPJName = null;
    private Button xyPJOperator = null;

    private TextView xyMeasureData1 = null;
    private TextView xyMeasureData2 = null;
    private TextView xyMeasureData3 = null;
    private TextView xyMeasureTimeLabel = null;
    private TextView xyMeasureResult = null;
    private ImageView xyMeasureImage1 = null;
    private ImageView xyMeasureImage2 = null;
    private ImageView xyMeasureImage3 = null;

    private String xyModel = null;

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.xy_main);

        /** 尝试初始化视图实例 **/
        initView();
        /** 初始化按钮事件 **/
        initEvent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.xyMainToolbar);
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
        xyPJName = (TextView) findViewById(R.id.xyStateLabel);
        xyPJOperator = (Button) findViewById(R.id.xyOperatorButton);
        // 各项数据
        xyMeasureData1 = (TextView) findViewById(R.id.xyMeasureData1);
        xyMeasureData2 = (TextView) findViewById(R.id.xyMeasureData2);
        xyMeasureData3 = (TextView) findViewById(R.id.xyMeasureData3);
        xyMeasureTimeLabel = (TextView) findViewById(R.id.xyMeasureTimeLabel);
        xyMeasureResult = (TextView) findViewById(R.id.xyMeasureResult);
        // 各项数据是否正常的图
        xyMeasureImage1 = (ImageView) findViewById(R.id.xyMeasureImage1);
        xyMeasureImage2 = (ImageView) findViewById(R.id.xyMeasureImage2);
        xyMeasureImage3 = (ImageView) findViewById(R.id.xyMeasureImage3);
    }

    private void initEvent() {
        if (xyPreference.hasAssign()) {
            // 己绑定，显示设备名称。准备连接操作
            xyPJName.setText(xyPreference.getXyDeviceName());
            xyPJOperator.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
        } else {
            // 未绑定，提示需要绑定。准备进行设备绑定操作。
            xyPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_ASSIGN]);
            xyPJOperator.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN]);
        }
    }

    public void xyOperation(View view) {
        String operator = ((Button) view).getText().toString();
        if (xyModel == null && BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN].equals(operator)) {
            // 还未绑定，开始绑定过程
            Intent intent = new Intent(XYMainActivity.this,
                    XYDeviceListActivity.class);
            getParent().startActivityForResult(intent,
                    AppConstat.XY_DEVICE_REQUEST_CODE);
            return;
        }
        String message = getString(R.string.permission_request_notice, getString(R.string
                .app_name), getString(R.string.permission_access_coarse_location));
        boolean hasRight = ybgApp.checkPermission(XYMainActivity.this, Manifest.permission
                        .ACCESS_COARSE_LOCATION,
                message, AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        if (hasRight) {
            String model = xyPreference.getXyDeviceModel();
            if ("urion_bt".equalsIgnoreCase(model)) {
                Intent intent = new Intent(XYMainActivity.this, XYUrionBTActivity.class);
                getParent().startActivityForResult(intent, AppConstat.XY_MEASURE_REQUEST_CODE);
            } else {
                Intent intent = new Intent(XYMainActivity.this, XYUrionBLEActivity.class);
                getParent().startActivityForResult(intent, AppConstat.XY_MEASURE_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                xyPJOperator.performClick();
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
        if (requestCode == AppConstat.XY_DEVICE_REQUEST_CODE
                && resultCode == AppConstat.XY_DEVICE_RESULT_CODE) {
            // 体重设备列表
            String xyDeviceName = data.getExtras().getString("xyDeviceName");
            String xyDeviceModel = data.getExtras().getString("xyDeviceModel");
            if (!"".equals(xyDeviceName) && !"".equals(xyDeviceModel)) {
                // 记录下设备名称及代号
                xyPreference.setXyDeviceName(xyDeviceName);
                xyPreference.setXyDeviceModel(xyDeviceModel);
                xyPJName.setText(xyDeviceName);
                xyModel = xyDeviceModel;
                if ("urion_bt".equalsIgnoreCase(xyDeviceModel)) {
                    // 开始扫描蓝牙设备
                    Intent intent = new Intent(this, BTDeviceListActivity.class);
                    getParent().startActivityForResult(intent,
                            AppConstat.BT_FOUND_REQUEST_CODE);
                } else {
                    // 开启单模
                    Intent intent = new Intent(XYMainActivity.this, XYUrionBLEActivity.class);
                    getParent().startActivityForResult(intent, AppConstat.XY_MEASURE_REQUEST_CODE);
                }
            }
        } else if (requestCode == AppConstat.BT_FOUND_REQUEST_CODE
                && resultCode == AppConstat.BT_FOUND_RESULT_CODE) {
            // 取得需要连接的蓝牙地址
            String xyDeviceAddr = data.getExtras().getString(
                    BTAction.EXTRA_DEVICE_ADDRESS);
            xyPreference.setXyDeviceAddr(xyDeviceAddr);
            // 开启双模
            Intent intent = new Intent(XYMainActivity.this, XYUrionBTActivity.class);
            getParent().startActivityForResult(intent, AppConstat.XY_MEASURE_REQUEST_CODE);
        } else if (requestCode == AppConstat.XY_MEASURE_REQUEST_CODE
                && resultCode == AppConstat.XY_MEASURE_RESULT_CODE) {
            int sys = data.getExtras().getInt("sys");
            int dia = data.getExtras().getInt("dia");
            int pul = data.getExtras().getInt("pul");
            // 显示数据
            xyMeasureData1.setText(sys);
            xyMeasureData2.setText(dia);
            xyMeasureData3.setText(pul);
            xyMeasureTimeLabel.setText("测量时间：" + sdf.format(new Date()));
            // 显示是否正常
            xyMeasureImage1.setImageResource(XYCheckUtil
                    .getImageResourceId(0, sys));
            xyMeasureImage2.setImageResource(XYCheckUtil
                    .getImageResourceId(1, dia));
            xyMeasureImage3.setImageResource(XYCheckUtil
                    .getImageResourceId(2, pul));
            xyMeasureResult.setText(XYCheckUtil.getNoticeMsg(sys,
                    dia, pul));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, XySettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
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

}
