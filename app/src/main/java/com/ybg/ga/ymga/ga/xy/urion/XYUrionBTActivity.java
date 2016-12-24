/**
 *
 */
package com.ybg.ga.ymga.ga.xy.urion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.preference.XYPreference;
import com.ybg.ga.ymga.ga.xy.XYDataService;
import com.ybg.ga.ymga.util.AppConstat;

import java.text.SimpleDateFormat;

/**
 * @author 杨拔纲
 */
public class XYUrionBTActivity extends Activity {

    private XYPreference xyPreference = XYPreference.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();

    // 使用进度条提示当前正在读取数据
    private ProgressBar readProgressBar = null;
    private TextView readProgressTitle = null;

    private XYDataService xyDataService = null;
    private XYUrionService xyUrionService = null;
    private Intent bindIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.xy_jiance);

        /** 尝试初始化视图实例 **/
        initView();
    }

    private void initView() {
        readProgressBar = (ProgressBar) findViewById(R.id.xy_jiance_pb);
        readProgressTitle = (TextView) findViewById(R.id.xy_jiance_tv);
        boolean hasRight = checkPermission();
        if (hasRight && (xyUrionService != null)) {
            // 尝试启动设备并获取数据
            startMeasure();
            // 启动进度条
            readProgressTitle.setText("正在初始化...");
        }
    }

    private boolean checkPermission() {
        String message = getString(R.string.permission_request_notice, getString(R.string
                .app_name), getString(R.string.permission_access_coarse_location));
        return ybgApp.checkPermission(XYUrionBTActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
                message, AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION);
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
            Intent intent = new Intent(XYUrionBTActivity.this, XYUrionService.class);
            bindService(intent, xyUrionConnection, Context.BIND_AUTO_CREATE);
            // 注册广播
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BTAction.getSendInfoAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.getSendDataAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.getSendErrorAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.sendProgressAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.getConnectAction(BTPrefix.XY));
            intentFilter.addAction(BTAction.getConnectedSuccess(BTPrefix.XY));
            intentFilter.addAction(BTAction.getDisConnected(BTPrefix.XY));
            registerReceiver(xyMeasureBroadcastReceiver,
                    intentFilter);
        }
        bindIntent = new Intent(XYUrionBTActivity.this, XYDataService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // 停止测量
        stopMeasure();
        // 停止接收广播
        if (null != xyUrionService) {
            unbindService(xyUrionConnection);
        }
        unregisterReceiver(xyMeasureBroadcastReceiver);
        unbindService(mConnection);
        super.onStop();
    }

    private void startMeasure() {
        // 发送测量指令
        if (xyUrionService != null) {
            xyUrionService.connectAndStart(xyPreference.getXyDeviceAddr());
        }
    }

    private void stopMeasure() {
        // 发送停止指令
    }

    private void saveData(String data) {
        // 保存存数据
        try {
            String[] xyData = data.split(",");
            int sys = Integer.valueOf(xyData[0]);
            int dia = Integer.valueOf(xyData[1]);
            int pul = Integer.valueOf(xyData[2]);
            xyDataService.save(sys, dia, pul);

            Intent dataIntent = new Intent();
            dataIntent.putExtra("sys", sys);
            dataIntent.putExtra("dia", dia);
            dataIntent.putExtra("pul", pul);
            setResult(AppConstat.XY_MEASURE_RESULT_CODE, dataIntent);
            finish();
        } catch (Exception e) {
            ybgApp.showMessage(getApplicationContext(), "数据保存失败");
        }
    }

    private BroadcastReceiver xyMeasureBroadcastReceiver = new BroadcastReceiver() {

        @SuppressLint("SimpleDateFormat")
        private SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BTAction.getConnectAction(BTPrefix.XY).equals(action)) {
                readProgressTitle.setText("正在连接..");
            } else if (BTAction.getConnectedSuccess(BTPrefix.XY).equals(action)) {
                String name = xyPreference.getXyDeviceName();
                readProgressTitle.setText("己连接:  " + name);
                if (!xyPreference.hasAssign()) {
                    xyPreference.setHasAssign(true);
                }
            } else if (BTAction.getDisConnected(BTPrefix.XY).equals(action)) {
                readProgressTitle.setText("连接己断开");
            } else if (action.equalsIgnoreCase(BTAction
                    .getSendErrorAction(BTPrefix.XY))) {
                String info = intent.getExtras().getString(BTAction.INFO);
                ybgApp.showMessage(getApplicationContext(), info);
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

                saveData(data);
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

    private ServiceConnection xyUrionConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            xyUrionService = ((XYUrionService.XYUrionBinder) service).getService();
            startMeasure();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            xyUrionService = null;
        }

    };
}
