package com.ybg.ga.ymga.ga.tz;

import android.annotation.SuppressLint;
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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.bt.BTStatus;
import com.ybg.ga.ymga.ga.activity.SubActivity;
import com.ybg.ga.ymga.ga.preference.TZPreference;
import com.ybg.ga.ymga.ga.tz.furuik.BitMapTools;
import com.ybg.ga.ymga.ga.tz.furuik.BluetoothService;
import com.ybg.ga.ymga.ga.tz.furuik.Confing;
import com.ybg.ga.ymga.ga.tz.furuik.MyApplication;
import com.ybg.ga.ymga.ga.tz.lefu.LefuService;
import com.ybg.ga.ymga.ga.tz.lefu.ParseUtil;
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 杨拔纲
 */
@SuppressLint("NewApi")
public class TZMainActivity extends SubActivity {

    private TZPreference tzPreference = TZPreference.getInstance();
    private UserPreferences userPreference = UserPreferences.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();
    private Intent serviceIntent = null;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private TextView tzPJName = null;
    private Button tzPJOperator = null;

    private TextView tzMeasureTime = null;

    private TextView tzValue = null;
    private TextView tzZFValue = null;
    private TextView tzJRValue = null;
    private TextView tzSFValue = null;
    private TextView tzBMIValue = null;
    private TextView tzQZValue = null;
    private TextView tzGGValue = null;
    private TextView tzNZValue = null;
    private TextView tzJCValue = null;
    private TextView tzSTValue = null;

    private ImageView tzZFImage = null;
    private ImageView tzJRImage = null;
    private ImageView tzBMIImage = null;
    private ImageView tzQZImage = null;
    private ImageView tzSFImage = null;
    private ImageView tzGGImage = null;
    private ImageView tzNZImage = null;
    private ImageView tzJCImage = null;
    private ImageView tzSTImage = null;

    private TZDataService tzDataService = null;
    private LefuService lefuService = null;
    private Intent bindIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tz_main);

        initView();
        initEvent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.tzMainToolbar);
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
        tzPJName = (TextView) findViewById(R.id.tzPJName);
        tzPJOperator = (Button) findViewById(R.id.tzPJOperator);
        tzMeasureTime = (TextView) findViewById(R.id.tzMeasureTimeLabel);
        // 各项数据
        tzValue = (TextView) findViewById(R.id.tzValue);
        tzZFValue = (TextView) findViewById(R.id.tzZFValue);
        tzJRValue = (TextView) findViewById(R.id.tzJRValue);
        tzSFValue = (TextView) findViewById(R.id.tzSFValue);
        tzBMIValue = (TextView) findViewById(R.id.tzBMIValue);
        tzQZValue = (TextView) findViewById(R.id.tzQZValue);
        tzGGValue = (TextView) findViewById(R.id.tzGGValue);
        tzNZValue = (TextView) findViewById(R.id.tzNZValue);
        tzJCValue = (TextView) findViewById(R.id.tzJCValue);
        tzSTValue = (TextView) findViewById(R.id.tzSTValue);
        // 各项数据是否正常的图
        tzZFImage = (ImageView) findViewById(R.id.tzZFImage);
        tzJRImage = (ImageView) findViewById(R.id.tzJRImage);
        tzBMIImage = (ImageView) findViewById(R.id.tzBMIImage);
        tzQZImage = (ImageView) findViewById(R.id.tzQZImage);
        tzSFImage = (ImageView) findViewById(R.id.tzSFImage);
        tzGGImage = (ImageView) findViewById(R.id.tzGGImage);
        tzNZImage = (ImageView) findViewById(R.id.tzNZImage);
        tzJCImage = (ImageView) findViewById(R.id.tzJCImage);
        tzSTImage = (ImageView) findViewById(R.id.tzSTImage);
    }

    private void initEvent() {
        if (tzPreference.hasAssign()) {
            // 己绑定，显示设备名称。准备连接操作
            tzPJName.setText(tzPreference.getTzDeviceName());
            tzPJOperator
                    .setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
        } else {
            // 未绑定，提示需要绑定。准备进行设备绑定操作。
            tzPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_ASSIGN]);
            tzPJOperator
                    .setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN]);
        }
    }

    public void tzOperation(View view) {
        String operator = ((Button) view).getText().toString();
        if (BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN].equals(operator)) {
            // 还未绑定，开始绑定过程
            Intent intent = new Intent(this, TZDeviceListActivity.class);
            getParent().startActivityForResult(intent,
                    AppConstat.TZ_DEVICE_REQUEST_CODE);
        } else if (BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]
                .equals(operator)) {
            // 尝试启动设备并获取数据
            startMeasure();
            // 禁用此按钮，避免重复启动
            view.setEnabled(false);
        }
    }

    private void startMeasure() {
        String tzDevice = tzPreference.getTzDeviceModel();
        String action = tzPJOperator.getText().toString();
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
            if (TzUtil.TZ_DEVICE_LEFU.equalsIgnoreCase(tzDevice)) {//体脂秤lefu
                if (lefuService != null) {
                    lefuService.scanBLEDevice(true);
                } else {
                    getApplicationContext().bindService(new Intent(TZMainActivity.this, LefuService.class),
                            lefuConnection, Context.BIND_AUTO_CREATE);
                }
            } else if (TzUtil.TZ_DEVICE_FURUIK.equalsIgnoreCase(tzDevice)) {//体脂秤furuik
                MyApplication.supportBle = true;
                MyApplication.mBluetoothAdapter = bluetoothAdapter;
                if (MyApplication.bleService != null) {
                    scanFuruikDevice(true);
                } else {
                    getApplicationContext().bindService(new Intent(TZMainActivity.this, BluetoothService.class), furuikConnection, Context.BIND_AUTO_CREATE);
                }
            }
        } else {
            ybgApp.showMessage(getApplicationContext(), "您的手机当前不支持蓝牙4.0，无法连接体指秤。");
            MyApplication.supportBle = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstat.TZ_DEVICE_REQUEST_CODE
                && resultCode == AppConstat.TZ_DEVICE_RESULT_CODE) {
            // 体重设备列表
            String tzDeviceName = data.getExtras().getString("tzDeviceName");
            String tzDeviceModel = data.getExtras().getString("tzDeviceModel");
            if (!"".equals(tzDeviceName) && !"".equals(tzDeviceModel)) {
                // 记录下设备名称及代号
                tzPreference.setTzDeviceName(tzDeviceName);
                tzPreference.setTzDeviceModel(tzDeviceModel);
                // 修改状态，准备连接
                tzPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_START]);
                tzPJOperator
                        .setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
            }
            startMeasure();
        }
        super.onActivityResult(requestCode, resultCode, data);
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

            // 注册广播
            IntentFilter intentFilter = new IntentFilter();
            //lefu
            intentFilter.addAction(BTAction.getConnectAction(BTPrefix.TZ));
            intentFilter.addAction(BTAction.getConnectedSuccess(BTPrefix.TZ));
            intentFilter.addAction(BTAction.getDisConnected(BTPrefix.TZ));
            intentFilter.addAction(BTAction.getSendErrorAction(BTPrefix.TZ));
            intentFilter.addAction(BTAction.getSendDataAction(BTPrefix.TZ));
            //furuik
            intentFilter.addAction(Confing.BLE_NOTiFY);
            intentFilter.addAction(Confing.BLE_Fat_Data);
            intentFilter.addAction(Confing.BLE_DISCONNECT);
            intentFilter.addAction(Confing.BLE_ChangeWei_Data);
            intentFilter.addAction(Confing.BLE_Confirm_Data);
            registerReceiver(broadcastReceiver, intentFilter);
        }
        bindIntent = new Intent(TZMainActivity.this, TZDataService.class);
        getApplicationContext().bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (TzUtil.TZ_DEVICE_LEFU.equals(tzPreference.getTzDeviceModel())) {
            if (lefuService != null) {
                lefuService.stop();
            }
            getApplicationContext().unbindService(lefuConnection);
        } else if (TzUtil.TZ_DEVICE_FURUIK.equals(tzPreference.getTzDeviceModel())) {
            getApplicationContext().unbindService(furuikConnection);
        }

        if (null != this.serviceIntent)
            stopService(this.serviceIntent);

        unregisterReceiver(broadcastReceiver);
        getApplicationContext().unbindService(mConnection);
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, TZSettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            tzPJOperator.setEnabled(true);
            //lefu
            if (BTAction.getConnectAction(BTPrefix.TZ).equals(action)) {
                tzPJName.setText("正在连接..");
            } else if (BTAction.getConnectedSuccess(BTPrefix.TZ).equals(action)) {
                String name = tzPreference.getTzDeviceName();
                tzPJName.setText("己连接:  " + name);
                if (!tzPreference.hasAssign()) {
                    tzPreference.setHasAssign(true);
                }
                tzPreference.setHasAssign(true);
                lefuService.sendUserInfo();
            } else if (BTAction.getDisConnected(BTPrefix.TZ).equals(action)) {
                tzPJName.setText("连接己断开");
            } else if (BTAction.getSendErrorAction(BTPrefix.TZ).equals(action)) {
                String errorCode = intent.getExtras().getString("error_code");
                if ("31".equals(errorCode)) {
                    tzPJName.setText("蓝牙错误");
                } else if ("33".equals(errorCode)) {
                    tzPJName.setText("脂肪错误");
                }
            } else if (BTAction.getSendDataAction(BTPrefix.TZ).equals(action)) {
                byte[] tzHex = intent.getExtras().getByteArray("tzHex");
                TzBean tzBean = ParseUtil.getTzBeanFromHex(tzHex);
                if (tzBean != null) {
                    processTzData(tzBean);
                }
                lefuService.sendStopCmd();
            } else if (action.equals(Confing.BLE_NOTiFY)) {
                tzPJName.setText("设备己连接");

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
                tzPJName.setText("设备连接己断开");
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
                TzBean tzBean = new TzBean(weight, fat, muscle, humidity, bmi, qzValue, bone, (int) visceral, (int) basal, age);
                processTzData(tzBean);
            } else if (action.equals(Confing.BLE_ChangeWei_Data)) {//测量中的体重
                float wei = intent.getFloatExtra("changewei", 0);
                tzValue.setText("" + wei);
            } else if (action.equals(Confing.BLE_Confirm_Data)) {//测量稳定后的体重
                float wei = intent.getFloatExtra("confirmwei", 0);
                tzValue.setText("" + wei);
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
                tzPJOperator.setEnabled(false);
            }
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

    private void scanFuruikDevice(boolean scanble) {
        if (scanble) {
            tzPJName.setText("正在查找设备...");
            if (MyApplication.mBluetoothAdapter.isEnabled()) {
                if (!mScanning) {
                    // 经过预定扫描期后停止扫描
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScanning = false;
                            MyApplication.mBluetoothAdapter.stopLeScan(startScanCallback);
                            tzPJName.setText("未连接!");
                            tzPJOperator.setEnabled(true);
                        }
                    }, SCAN_PERIOD);
                    mScanning = true;
                    MyApplication.mBluetoothAdapter.startLeScan(startScanCallback);
                    tzPJOperator.setEnabled(false);
                }
            }
        } else {
            tzPJName.setText("停止查找设备...");
            if (MyApplication.mBluetoothAdapter.isEnabled()) {
                MyApplication.mBluetoothAdapter.stopLeScan(stopScanCallback);
            }
        }
    }

    private void processTzData(TzBean tzBean) {
        tzMeasureTime.setVisibility(View.VISIBLE);
        tzMeasureTime.setText("测量时间：" + sdf.format(new Date()));
        if (tzBean != null) {
            tzValue.setText(new DecimalFormat("#.##").format(tzBean.getTzValue()));
            // 保存体重至preference
            userPreference.setBodyWeight(tzBean.getTzValue());
            // 瘦 　　　标准　　　　 轻度　　　　肥胖
            // 男性 不足10％ 　10～20％ 　　20～25％　　 25％以上
            // 女性 不足20％　 20～30％ 　　30～35％ 　　35％以上
            int sex = userPreference.getUserSex();
            float bodyFat = tzBean.getTzZFValue();
            if (sex == AppConstat.SEX_MALE) {
                if (bodyFat < 10) {
                    tzZFImage.setImageResource(R.mipmap.low);
                    tzQZImage.setImageResource(R.mipmap.normal);
                } else if (bodyFat > 20) {
                    tzZFImage.setImageResource(R.mipmap.high);
                    tzQZImage.setImageResource(R.mipmap.low);
                } else {
                    tzZFImage.setImageResource(R.mipmap.normal);
                    tzQZImage.setImageResource(R.mipmap.normal);
                }
            } else {
                if (bodyFat < 20) {
                    tzZFImage.setImageResource(R.mipmap.low);
                    tzQZImage.setImageResource(R.mipmap.normal);
                } else if (bodyFat > 30) {
                    tzZFImage.setImageResource(R.mipmap.high);
                    tzQZImage.setImageResource(R.mipmap.low);
                } else {
                    tzZFImage.setImageResource(R.mipmap.normal);
                    tzQZImage.setImageResource(R.mipmap.normal);
                }
            }
            tzZFValue.setText(new DecimalFormat("#.##").format(bodyFat));
            tzJRValue.setText(new DecimalFormat("#.##").format(tzBean.getTzJRValue()));
            tzJRImage.setImageResource(R.mipmap.normal);
            tzSFValue.setText(new DecimalFormat("#.##").format(tzBean.getTzSFValue()));
            tzSFImage.setImageResource(R.mipmap.normal);
            // 偏瘦： BMI指数 < 18 正常体重： BMI指数 = 18 - 25 超重： BMI指数 = 25 -
            // 30轻度肥胖： BMI指数 > 30中度肥胖： BMI指数 > 35重度肥胖： BMI指数 > 40
            float bodyHigh = userPreference.getBodyHigh();
            float bmi = 0f;
            if (bodyHigh != 0) {
                bmi = tzBean.getTzBMIValue();
                tzBMIValue.setText(new DecimalFormat("#.##").format(bmi));
                if (bmi < 18) {
                    tzBMIImage.setImageResource(R.mipmap.low);
                } else if (bmi > 25) {
                    tzBMIImage.setImageResource(R.mipmap.high);
                } else {
                    tzBMIImage.setImageResource(R.mipmap.normal);
                }
            } else {
                tzBMIValue.setText("--");
                tzBMIImage.setImageResource(R.mipmap.none);
            }
            tzQZValue.setText(new DecimalFormat("#.##").format(tzBean.getTzQZValue()));
            tzGGValue.setText(new DecimalFormat("#.##").format(tzBean.getTzGGValue()));
            tzGGImage.setImageResource(R.mipmap.normal);
            tzNZValue.setText(new DecimalFormat("#.##").format(tzBean.getTzNZValue()));
            if (tzBean.getTzNZValue() < 10) {
                tzNZImage.setImageResource(R.mipmap.normal);
            } else {
                tzNZImage.setImageResource(R.mipmap.high);
            }
            tzJCValue.setText("" + tzBean.getTzJCValue());
            tzJCImage.setImageResource(R.mipmap.normal);
            int age = tzBean.getTzSTValue();
            if (age > 0 && age < 100) {
                tzSTValue.setText("" + tzBean.getTzSTValue());
            } else {
                tzSTValue.setText("无");
                tzBean.setTzSTValue(0);
            }
            tzSTImage.setImageResource(R.mipmap.none);

            // 保存数据
            tzDataService.save(tzBean.getTzValue(),
                    tzBean.getTzZFValue(), tzBean.getTzJRValue(),
                    tzBean.getTzSFValue(), tzBean.getTzBMIValue(), tzBean.getTzQZValue(), tzBean.getTzGGValue(),
                    tzBean.getTzNZValue(), tzBean.getTzJCValue(), tzBean.getTzSTValue());
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
            tzPJOperator.setEnabled(true);
        }
    };

    private boolean mScanning = false;
    private static final long SCAN_PERIOD = 60 * 1000;
    private Handler mHandler = new Handler();
}
