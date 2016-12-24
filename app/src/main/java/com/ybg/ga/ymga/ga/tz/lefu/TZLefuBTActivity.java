package com.ybg.ga.ymga.ga.tz.lefu;

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

import com.lefu.bluetoothauotpair.BluetoolUtil;
import com.lefu.bluetoothauotpair.BluetoothTools;
import com.lefu.bluetoothauotpair.PollingUtils;
import com.lefu.bluetoothauotpair.ScaneBluetoothService;
import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.ga.preference.TZPreference;
import com.ybg.ga.ymga.ga.tz.TZDataService;
import com.ybg.ga.ymga.ga.tz.TzBean;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 */
@SuppressLint("NewApi")
public class TZLefuBTActivity extends Activity {

    private TZPreference tzPreference = TZPreference.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();
    private Intent serviceIntent = null;

    private TextView tzProgressTV = null;
    private ProgressBar tzProgressBar = null;

    private TZDataService tzDataService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tz_jiance);

        initView();
    }

    private void initView() {
        tzProgressBar = (ProgressBar) findViewById(R.id.tz_jiance_pb);
        tzProgressTV = (TextView) findViewById(R.id.tz_jiance_tv);
    }

    private boolean checkPermission() {
        String message = getString(R.string.permission_request_notice, getString(R.string
                .app_name), getString(R.string.permission_access_coarse_location));
        return ybgApp.checkPermission(TZLefuBTActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
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

    private void startMeasure() {
        PollingUtils.startPollingService(TZLefuBTActivity.this, 10,
                ScaneBluetoothService.class, ScaneBluetoothService.ACTION);
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

            // 注册广播
            IntentFilter intentFilter = new IntentFilter();
            //lefu bt
            intentFilter.addAction(BluetoothTools.ACTION_NOT_FOUND_SERVER);
            intentFilter.addAction(BluetoothTools.ACTION_FOUND_DEVICE);
            intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
            intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
            intentFilter.addAction(BluetoothTools.ACTION_CONNECT_ERROR);
            intentFilter.addAction(BluetoothTools.ACTION_READ_DATA);
            registerReceiver(broadcastReceiver, intentFilter);
        }
        Intent bindIntent = new Intent(TZLefuBTActivity.this, TZDataService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        checkPermission();
    }

    @Override
    protected void onStop() {
        PollingUtils.stopPollingService(this, ScaneBluetoothService.class,
                ScaneBluetoothService.ACTION);
        if (null != BluetoolUtil.mBluetoothAdapter) {
            BluetoolUtil.mBluetoothAdapter.disable();
        }

        if (null != this.serviceIntent)
            stopService(this.serviceIntent);

        unregisterReceiver(broadcastReceiver);
        unbindService(mConnection);
        super.onStop();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //lefu bt
            if (BluetoothTools.ACTION_START_DISCOVERY.equals(action)) {
                tzProgressTV.setText("正在扫描..");
            } else if (BluetoothTools.ACTION_NOT_FOUND_SERVER.equals(action)) {
                tzProgressTV.setText("未连接!");
            } else if (BluetoothTools.ACTION_FOUND_DEVICE.equals(action)) {
                if (null != BluetoolUtil.lastDevice) {
                    String name = tzPreference.getTzDeviceName();
                    tzProgressTV.setText("发现设备:  " + name);
                }
            } else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
                if (null != BluetoolUtil.lastDevice) {
                    String address = BluetoolUtil.lastDevice.getAddress();
                    String name = tzPreference.getTzDeviceName();
                    tzProgressTV.setText("己连接:  " + name);
                    if (!tzPreference.hasAssign()) {
                        tzPreference.setHasAssign(true);
                    }
                    tzPreference.setTzDeviceAddr(address);
                }
            } else if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {
                if (null != BluetoolUtil.lastDevice) {
                    String name = tzPreference.getTzDeviceName();
                    tzProgressTV.setText("接收到数据:  " + name);
                }
            } else if (BluetoothTools.ACTION_CONNECT_ERROR.equals(action)) {
                if (null != BluetoolUtil.lastDevice) {
                    String name = tzPreference.getTzDeviceName();
                    tzProgressTV.setText("连接失败:  " + name);
                }
            } else if (BluetoothTools.ACTION_READ_DATA.equals(action)) {
                String msg = intent.getStringExtra("readMessage");
                TZCFRecoder recoder = CRUtil.parseMessage(msg);
                TzBean tzBean = new TzBean(recoder.getWeight(), recoder.getBodyFat(), recoder.getJirou(),
                        recoder.getBodyWater(), recoder.getBMI(), recoder.getQZValue(), recoder.getBone(),
                        recoder.getNeiZhang(), recoder.getCalorie(), 0);
                processTzData(tzBean);
            }
        }

    };

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            tzDataService = ((TZDataService.TZDataBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tzDataService = null;
        }

    };

    private void processTzData(TzBean tzBean) {
        if (tzBean != null) {
            // 保存数据
            tzDataService.save(tzBean.getTzValue(),
                    tzBean.getTzZFValue(), tzBean.getTzJRValue(),
                    tzBean.getTzSFValue(), tzBean.getTzBMIValue(), tzBean.getTzQZValue(), tzBean.getTzGGValue(),
                    tzBean.getTzNZValue(), tzBean.getTzJCValue(), tzBean.getTzSTValue());
            Intent dataIntent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("tzBean", tzBean);
            dataIntent.putExtras(bundle);
            setResult(AppConstat.TZ_MEASURE_RESULT_CODE, dataIntent);
            finish();
        }
    }

}
