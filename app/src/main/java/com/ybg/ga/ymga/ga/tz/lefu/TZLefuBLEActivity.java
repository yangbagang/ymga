package com.ybg.ga.ymga.ga.tz.lefu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
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
import com.ybg.ga.ymga.ga.preference.TZPreference;
import com.ybg.ga.ymga.ga.tz.TZDataService;
import com.ybg.ga.ymga.ga.tz.TzBean;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 */
@SuppressLint("NewApi")
public class TZLefuBLEActivity extends Activity {

    private TZPreference tzPreference = TZPreference.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();
    private Intent serviceIntent = null;

    private TextView tzProgressTV = null;
    private ProgressBar tzProgressBar = null;

    private TZDataService tzDataService = null;
    private LefuService lefuService = null;

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
        return ybgApp.checkPermission(TZLefuBLEActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
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
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                ybgApp.showMessage(getApplicationContext(), "您的手机当前不支持蓝牙4.0，无法连接体指秤。");
                return;
            }
            BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }

            if (lefuService != null) {
                lefuService.scanBLEDevice(true);
            } else {
                bindService(new Intent(TZLefuBLEActivity.this, LefuService.class),
                        lefuConnection, Context.BIND_AUTO_CREATE);
            }
        } else {
            ybgApp.showMessage(getApplicationContext(), "您的手机当前不支持蓝牙4.0，无法连接体指秤。");
        }
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
            //lefu ble
            intentFilter.addAction(BTAction.getConnectAction(BTPrefix.TZ));
            intentFilter.addAction(BTAction.getConnectedSuccess(BTPrefix.TZ));
            intentFilter.addAction(BTAction.getDisConnected(BTPrefix.TZ));
            intentFilter.addAction(BTAction.getSendErrorAction(BTPrefix.TZ));
            intentFilter.addAction(BTAction.getSendDataAction(BTPrefix.TZ));
            registerReceiver(broadcastReceiver, intentFilter);
        }
        Intent bindIntent = new Intent(TZLefuBLEActivity.this, TZDataService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        checkPermission();
    }

    @Override
    protected void onStop() {
        if (lefuService != null) {
            lefuService.stop();
        }
        unbindService(lefuConnection);

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
            //lefu ble
            if (BTAction.getConnectAction(BTPrefix.TZ).equals(action)) {
                tzProgressTV.setText("正在连接..");
            } else if (BTAction.getConnectedSuccess(BTPrefix.TZ).equals(action)) {
                String name = tzPreference.getTzDeviceName();
                tzProgressTV.setText("己连接:  " + name);
                if (!tzPreference.hasAssign()) {
                    tzPreference.setHasAssign(true);
                }
                lefuService.sendUserInfo();
            } else if (BTAction.getDisConnected(BTPrefix.TZ).equals(action)) {
                tzProgressTV.setText("连接己断开");
            } else if (BTAction.getSendErrorAction(BTPrefix.TZ).equals(action)) {
                String errorCode = intent.getExtras().getString("error_code");
                if ("31".equals(errorCode)) {
                    tzProgressTV.setText("蓝牙错误");
                } else if ("33".equals(errorCode)) {
                    tzProgressTV.setText("脂肪错误");
                }
            } else if (BTAction.getSendDataAction(BTPrefix.TZ).equals(action)) {
                byte[] tzHex = intent.getExtras().getByteArray("tzHex");
                TzBean tzBean = ParseUtil.getTzBeanFromHex(tzHex);
                if (tzBean != null) {
                    processTzData(tzBean);
                }
                lefuService.sendStopCmd();
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

    private ServiceConnection lefuConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            lefuService = ((LefuService.LefuBinder) service).getService();
            lefuService.scanBLEDevice(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            lefuService = null;
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
