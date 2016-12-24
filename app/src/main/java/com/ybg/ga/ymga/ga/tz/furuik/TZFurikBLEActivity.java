package com.ybg.ga.ymga.ga.tz.furuik;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.ga.preference.TZPreference;
import com.ybg.ga.ymga.ga.tz.TZDataService;
import com.ybg.ga.ymga.ga.tz.TzBean;
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 */
@SuppressLint("NewApi")
public class TZFurikBLEActivity extends Activity {

    private TZPreference tzPreference = TZPreference.getInstance();
    private UserPreferences userPreference = UserPreferences.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();
    private Intent serviceIntent = null;

    private TextView tzProgressTV = null;
    private ProgressBar tzProgressBar = null;

    private TZDataService tzDataService = null;
    private Intent bindIntent = null;

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
        return ybgApp.checkPermission(TZFurikBLEActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
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
                MyApplication.supportBle = false;
                return;
            }
            BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
            MyApplication.supportBle = true;
            MyApplication.mBluetoothAdapter = bluetoothAdapter;
            if (MyApplication.bleService != null) {
                scanFuruikDevice(true);
            } else {
                bindService(new Intent(TZFurikBLEActivity.this, BluetoothService.class),
                        furuikConnection, Context.BIND_AUTO_CREATE);
            }
        } else {
            ybgApp.showMessage(getApplicationContext(), "您的手机当前不支持蓝牙4.0，无法连接体指秤。");
            MyApplication.supportBle = false;
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
            //furuik
            intentFilter.addAction(Confing.BLE_NOTiFY);
            intentFilter.addAction(Confing.BLE_Fat_Data);
            intentFilter.addAction(Confing.BLE_DISCONNECT);
            intentFilter.addAction(Confing.BLE_ChangeWei_Data);
            intentFilter.addAction(Confing.BLE_Confirm_Data);
            registerReceiver(broadcastReceiver, intentFilter);
        }
        bindIntent = new Intent(TZFurikBLEActivity.this, TZDataService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        checkPermission();
    }

    @Override
    protected void onStop() {
        if (MyApplication.bleService != null) {
            unbindService(furuikConnection);
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

            if (action.equals(Confing.BLE_NOTiFY)) {
                tzProgressTV.setText("设备己连接");

                tzPreference.setTzDeviceAddr(MyApplication.Address);
                tzPreference.setHasAssign(true);

                //链接成功后发送人体参数
                //发送一次人体参数
                boolean sendUser = true;
                if (sendUser) {

                    long userid = userPreference.getId();
                    int userhigh = (int) (userPreference.getBodyHigh() * 100);
                    int userage = userPreference.getAge();
                    int usersex = userPreference.getUserSex();
                    Log.e("dddd", "发送人体参数 ： ++++ " + userid + " " + userhigh + " " + userage + " " + usersex);
                    byte jiaoyan = (byte) (0x02 + 0xE2 + 0x04 + userid + userhigh + userage + usersex);
                    byte[] sum = {0x02, (byte) 0xE2, 0x04,
                            (byte) userid, (byte) userhigh, (byte) userage, (byte) usersex
                            , jiaoyan, (byte) 0xaa};
                    Log.e("fat", "发送人体参数指令 ： ++++ " + BitMapTools.bytesToHexString(sum));

                    if (MyApplication.bleService != null) {
                        MyApplication.bleService.sendLight(sum);
                    }
                }
            } else if (action.equals(Confing.BLE_DISCONNECT)) {//蓝牙断开
                tzProgressTV.setText("设备连接己断开");
                //MyApplication.bleService.connect(tzPreference.getTzDeviceAddr());
                scanFuruikDevice(true);
            } else if (action.equals(Confing.BLE_Fat_Data)) {//获取数据
                float weight = intent.getFloatExtra("weight", 0);
                float bmi = intent.getFloatExtra("bmi", 0);
                float fat = intent.getFloatExtra("fat", 0);
                float humidity = intent.getFloatExtra("humidity", 0);
                float muscle = intent.getFloatExtra("muscle", 0);
                float bone = intent.getFloatExtra("bone", 0);
                float visceral = intent.getFloatExtra("visceral", 0);
                float basal = intent.getFloatExtra("basal", 0);
                int age = intent.getIntExtra("age", 0);
                float qzValue = (1 - fat / 100) * weight;
                TzBean tzBean = new TzBean(weight, fat, muscle, humidity, bmi, qzValue, bone,
                        (int) visceral, (int) basal, age);
                processTzData(tzBean);
            } else if (action.equals(Confing.BLE_ChangeWei_Data)) {//测量中的体重
                float wei = intent.getFloatExtra("changewei", 0);
                //tzValue.setText("" + wei);
                System.out.println("" + wei);
            } else if (action.equals(Confing.BLE_Confirm_Data)) {//测量稳定后的体重
                float wei = intent.getFloatExtra("confirmwei", 0);
                TzBean tzBean = new TzBean(wei, 0, 0, 0, 0, 0, 0,
                        0, 0, 0);
                processTzData(tzBean);
                ;
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

    private ServiceConnection furuikConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            MyApplication.bleService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyApplication.bleService = ((BluetoothService.BLEBinder) service).getService();
            if (!MyApplication.bleService.initialize()) {
                Log.e("BLE", "初始化失败");
            } else {
                Log.e("BLE", "初始化成功");
                scanFuruikDevice(true);
            }
        }
    };

    private void scanFuruikDevice(boolean scanble) {
        if (scanble) {
            tzProgressTV.setText("正在查找设备...");
            if (MyApplication.mBluetoothAdapter.isEnabled()) {
                if (!mScanning) {
                    // 经过预定扫描期后停止扫描
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScanning = false;
                            MyApplication.mBluetoothAdapter.stopLeScan(startScanCallback);
                            tzProgressTV.setText("未连接!");
                        }
                    }, SCAN_PERIOD);
                    mScanning = true;
                    MyApplication.mBluetoothAdapter.startLeScan(startScanCallback);
                }
            }
        } else {
            tzProgressTV.setText("停止查找设备...");
            if (MyApplication.mBluetoothAdapter.isEnabled()) {
                MyApplication.mBluetoothAdapter.stopLeScan(stopScanCallback);
            }
        }
    }

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

    // 搜索蓝牙回调
    @SuppressLint("NewApi")
    BluetoothAdapter.LeScanCallback startScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    System.out.println("Name: " + deviceName + ", addr: " + deviceAddress);
                    if (tzPreference.hasAssign()) {
                        if (deviceAddress.equalsIgnoreCase(tzPreference.getTzDeviceAddr())) {
                            MyApplication.bleService.connect(deviceAddress);
                            MyApplication.mBluetoothAdapter.stopLeScan(null);
                        }
                    } else {
                        if (deviceName.equalsIgnoreCase("ST-BL-1") | deviceName.equalsIgnoreCase("FSRK-FRK-001")) {
                            MyApplication.bleService.connect(deviceAddress);
                            MyApplication.mBluetoothAdapter.stopLeScan(null);
                        }
                    }

                }
            });
        }
    };

    BluetoothAdapter.LeScanCallback stopScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            //scanFuruikDevice(true);
            //tzPJOperator.setEnabled(true);
        }
    };

    private boolean mScanning = false;
    private static final long SCAN_PERIOD = 60 * 1000;
    private Handler mHandler = new Handler();
}
