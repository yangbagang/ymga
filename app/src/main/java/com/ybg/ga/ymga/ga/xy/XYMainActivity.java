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

    // 使用进度条提示当前正在读取数据
    private ProgressDialog readProgressDialog = null;
    private ProgressBar xyProgressBar = null;

    private XYDataService xyDataService = null;
    private UrionService urionService = null;
    private XYUrionService xyUrionService = null;
    private Intent bindIntent = null;

    private String xyModel = null;

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
        // 测量中的值
        xyProgressBar = (ProgressBar) findViewById(R.id.xyProgressBar);
        xyProgressBar.setMax(300);
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
        if (hasRight && (urionService != null) || (xyUrionService != null)) {
            // 尝试启动设备并获取数据
            startMeasure();
            // 禁用此按钮，避免重复启动
            view.setEnabled(false);
            // 启动进度条
            readProgressDialog = ybgApp.getProgressDialog(XYMainActivity.this,
                    "正在测量...");
            readProgressDialog.show();
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
                    xyPJOperator.performClick();
                }
            }
        } else if (requestCode == AppConstat.BT_FOUND_REQUEST_CODE
                && resultCode == AppConstat.BT_FOUND_RESULT_CODE) {
            // 取得需要连接的蓝牙地址
            String xyDeviceAddr = data.getExtras().getString(
                    BTAction.EXTRA_DEVICE_ADDRESS);
            xyPreference.setXyDeviceAddr(xyDeviceAddr);
            // 修改状态，准备连接
            xyPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_START]);
            xyPJOperator
                    .setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
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
            // 开启后台程序
            Intent intent = new Intent(XYMainActivity.this, UrionService.class);
            getApplication().bindService(intent, urionConnection, Context.BIND_AUTO_CREATE);
            Intent intent2 = new Intent(XYMainActivity.this, XYUrionService.class);
            getApplication().bindService(intent2, xyUrionConnection, Context.BIND_AUTO_CREATE);
            // 注册广播
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BTAction.getSendInfoAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.getSendDataAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.getSendErrorAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.sendProgressAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.getConnectAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.getConnectedSuccess(BTPrefix.XY));
            intentFilter.addAction(BTAction.getDisConnected(BTPrefix.XY));
            getApplication().registerReceiver(xyMeasureBroadcastReceiver,
                    intentFilter);
        }
        bindIntent = new Intent(XYMainActivity.this, XYDataService.class);
        getApplication().bindService(bindIntent, mConnection,
                Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // 停止测量
        stopMeasure();
        // 停止接收广播
        if (null != urionService) {
            getApplication().unbindService(urionConnection);
        }
        if (null != xyUrionService) {
            getApplication().unbindService(xyUrionConnection);
        }
        getApplication().unregisterReceiver(xyMeasureBroadcastReceiver);
        getApplication().unbindService(mConnection);
        super.onStop();
    }

    private void startMeasure() {
        // 发送测量指令
        if ("urion_bt".equalsIgnoreCase(xyPreference.getXyDeviceModel())) {
            if (xyUrionService != null) {
                xyUrionService.connectAndStart(xyPreference.getXyDeviceAddr());
            }
        } else {
            if (urionService != null) {
                urionService.scanBLEDevice(true);
            }
        }

    }

    private void stopMeasure() {
        // 发送停止指令
    }

    private BroadcastReceiver xyMeasureBroadcastReceiver = new BroadcastReceiver() {

        @SuppressLint("SimpleDateFormat")
        private SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BTAction.getConnectAction(BTPrefix.XY).equals(action)) {
                xyPJName.setText("正在连接..");
            } else if (BTAction.getConnectedSuccess(BTPrefix.XY).equals(action)) {
                String name = xyPreference.getXyDeviceName();
                xyPJName.setText("己连接:  " + name);
                if (!xyPreference.hasAssign()) {
                    xyPreference.setHasAssign(true);
                }
                if (urionService != null) {
                    urionService.sendStartCmd();
                }
            } else if (BTAction.getDisConnected(BTPrefix.XY).equals(action)) {
                xyPJName.setText("连接己断开");
            } else if (action.equalsIgnoreCase(BTAction
                    .getSendErrorAction(BTPrefix.XY))) {
                String info = intent.getExtras().getString(BTAction.INFO);
                ybgApp.showMessage(getApplicationContext(), info);
                readProgressDialog.dismiss();
                xyPJOperator.setEnabled(true);
            } else if (action.equalsIgnoreCase(BTAction
                    .getSendInfoAction(BTPrefix.XY))) {
                String info = intent.getExtras().getString(BTAction.INFO);
                ybgApp.showMessage(getApplicationContext(), info);
            } else if (action.equalsIgnoreCase(BTAction
                    .getSendDataAction(BTPrefix.XY))) {
                String data = intent.getExtras().getString(BTAction.DATA);
                if (!xyPreference.hasAssign()) {
                    // 还未设置绑定状态，先设置绑定状态，避免重复动作
                    xyPreference.setHasAssign(true);
                }
                String[] xyData = data.split(",");
                // 显示数据
                xyMeasureData1.setText(xyData[0]);
                xyMeasureData2.setText(xyData[1]);
                xyMeasureData3.setText(xyData[2]);
                xyMeasureTimeLabel.setText("测量时间：" + sdf.format(new Date()));
                // 显示是否正常
                xyMeasureImage1.setImageResource(XYCheckUtil
                        .getImageResourceId(0, xyData[0]));
                xyMeasureImage2.setImageResource(XYCheckUtil
                        .getImageResourceId(1, xyData[1]));
                xyMeasureImage3.setImageResource(XYCheckUtil
                        .getImageResourceId(2, xyData[2]));
                xyMeasureResult.setText(XYCheckUtil.getNoticeMsg(xyData[0],
                        xyData[1], xyData[2]));
                readProgressDialog.dismiss();
                xyPJOperator.setEnabled(true);

                // 保存存数据
                try {
                    xyDataService.save(Integer.valueOf(xyData[0]),
                            Integer.valueOf(xyData[1]),
                            Integer.valueOf(xyData[2]));
                } catch (Exception e) {
                    ybgApp.showMessage(getApplicationContext(), "数据保存失败");
                }

                if (urionService != null) {
                    urionService.sendStopCmd();
                }
            } else if (action.equals(BTAction.sendProgressAction(BTPrefix.XY))) {
                int progressValue = intent.getExtras()
                        .getInt(BTAction.PROGRESS);
                xyProgressBar.setProgress(progressValue);
            }
        }

    };

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            xyDataService = ((XYDataService.XYDataBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // nothing
            xyDataService = null;
        }

    };

    private ServiceConnection urionConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            urionService = ((UrionService.UrionBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            urionService = null;
        }

    };

    private ServiceConnection xyUrionConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            xyUrionService = ((XYUrionService.XYUrionBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            xyUrionService = null;
        }

    };
}
