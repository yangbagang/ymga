package com.ybg.ga.ymga.ga.tw;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTStatus;
import com.ybg.ga.ymga.ga.activity.SubActivity;
import com.ybg.ga.ymga.ga.preference.TWPreference;
import com.ybg.ga.ymga.ga.tw.ir.BluetoothService;
import com.ybg.ga.ymga.ga.tw.ir.IRConstants;
import com.ybg.ga.ymga.ga.tw.ir.IRDeviceService;
import com.ybg.ga.ymga.util.AppConstat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

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

    private TWDataService twDataService = null;
    private Intent bindIntent = null;
    private IRDeviceService irDeviceService = null;

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
        } else {
            // 未绑定
            twPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_CONNECT]);
        }
        twPJOperator.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_CONNECT]);
        twPJOperator.setEnabled(false);
    }

    public void twOperation(View view) {
        if (irDeviceService == null || !irDeviceService.hasBLESupportState()) {
            return;
        }
        String message = getString(R.string.permission_request_notice, getString(R.string
                .app_name), getString(R.string.permission_access_coarse_location));
        boolean checkResult = ybgApp.checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION,
                message, AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        if (checkResult) {
            // 尝试启动设备并获取数据
            startMeasure();
            // 禁用此按钮，避免重复启动
            view.setEnabled(false);
        }
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
            getApplication().bindService(new Intent(TWMainActivity.this, IRDeviceService.class), irConnection, Context.BIND_AUTO_CREATE);
            //System.out.println("onStart");
        }
        bindIntent = new Intent(TWMainActivity.this, TWDataService.class);
        getApplication().bindService(bindIntent, mConnection,
                Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    protected void onStop() {
        // 停止测量
        stopMeasure();
        // 停止接收广播
        if (null != irDeviceService) {
            irDeviceService.stop();
            irDeviceService = null;
            getApplication().unbindService(irConnection);
        }
        getApplication().unbindService(mConnection);
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        System.out.println("11111111");
        if (requestCode == AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION) {
            System.out.println("222222222");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                twPJOperator.performClick();
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
        System.out.println("startMeasure");
        // 发送连接指令
        if (!jiance && irDeviceService.hasBLESupportState()) {
            if (twPreference.hasAssign()) {
                irDeviceService.connect(twPreference.getTwDeviceAddr());
            } else {
                irDeviceService.scanBLEDevice(true);
            }
        }
    }

    private void stopMeasure() {
        // 发送停止指令

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            twDataService = ((TWDataService.TWDataBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // nothing
            twDataService = null;
        }

    };

    private ServiceConnection irConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            irDeviceService = ((IRDeviceService.IRDeviceBinder) service).getService();
            irDeviceService.setIrHandler(mHandler);
            twPJOperator.performClick();
            //System.out.println("onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            irDeviceService = null;
        }
    };

    // The Handler that gets information back from the BluetoothChatService
    private static boolean D = false;
    private static String TAG = "TWMainActivity";
    private boolean jiance = false;
    private int gaot = 500, dit = 500;
    private float ew;
    private String tt;
    private String readBuf;
    private String split;
    private StringTokenizer token;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case IRConstants.MESSAGE_STATE_CHANGE:
                    // 连接状态发生变化
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case IRDeviceService.STATE_CONNECTED:
                            // 己经连接好设备
                            twPJName.setText("设备己连接");
                            twPJOperator.setEnabled(false);
                            twPreference.setHasAssign(true);
                            //FE FD AA A0 0D 0A
                            //byte[] cmd = {(-2), (-3), (-86), (-96), 13, 10};
                            byte[] cmd = {(byte) 0xfe, (byte) 0xfd, (byte) 0xaa, (byte) 0xa0, 0x0d, 0x0a};
                            irDeviceService.writeCharactCmd(cmd);
                            jiance = true;
                            break;
                        case IRDeviceService.STATE_CONNECTING:
                            // 正在连接
                            twPJName.setText("正在连接设备...");
                            twPJOperator.setEnabled(false);
                            break;
                        case IRDeviceService.STATE_DISCONNECTED:
                            jiance = false;
                            twPJName.setText("无可用连接");
                            twPJOperator.setEnabled(true);
                            break;
                    }
                    break;
                case IRConstants.YYRESULT:
                    //人体温度，单位摄氏度
                    readBuf = (String) msg.obj;
                    split = ",";
                    token = new StringTokenizer(readBuf, split);
                    while (token.hasMoreTokens()) {
                        tt = token.nextToken();
                        if (gaot == 500) {
                            gaot = Integer.parseInt(tt);
                        } else {
                            dit = Integer.parseInt(tt);
                        }
                    }
                    ew = (float) (gaot * 256 + dit) / 10;
                    gaot = 500;
                    // 正常取得摄氏度
                    twMeasureTime.setText(sdf.format(new Date()));
                    setTemp(true, ew);
                    twDataService.save(ew);//保存均为摄氏度
                    break;
                case IRConstants.YYMU:
                    //环境温度，单位摄氏度
                    readBuf = (String) msg.obj;
                    split = ",";
                    token = new StringTokenizer(readBuf, split);
                    while (token.hasMoreTokens()) {
                        tt = token.nextToken();
                        if (gaot == 500) {
                            gaot = Integer.parseInt(tt);
                        } else {
                            dit = Integer.parseInt(tt);
                        }
                    }
                    ew = (float) (gaot * 256 + dit) / 10;
                    gaot = 500;
                    // 正常取得摄氏度
                    twMeasureTime.setText(getString(R.string.twMeasureTimeLabel) + " " + sdf.format(new Date()));
                    setTemp(true, ew);
                    twDataService.save(ew);//保存均为摄氏度
                    break;
                case IRConstants.FYRESULT:
                    //人体温度，单位华氏度
                    readBuf = (String) msg.obj;
                    split = ",";
                    token = new StringTokenizer(readBuf, split);
                    while (token.hasMoreTokens()) {
                        tt = token.nextToken();
                        if (gaot == 500) {
                            gaot = Integer.parseInt(tt);
                        } else {
                            dit = Integer.parseInt(tt);
                        }
                    }
                    ew = (float) (gaot * 256 + dit) / 10;
                    gaot = 500;
                    // 正常取得华氏度
                    twMeasureTime.setText(getString(R.string.twMeasureTimeLabel) + " " + sdf.format(new Date()));
                    setTemp(false, ew);
                    twDataService.save(WenduTool.f2c(ew));//保存均为摄氏度
                    break;
                case IRConstants.FYMU:
                    //环境温度，单位华氏度
                    readBuf = (String) msg.obj;
                    split = ",";
                    token = new StringTokenizer(readBuf, split);
                    while (token.hasMoreTokens()) {
                        tt = token.nextToken();
                        if (gaot == 500) {
                            gaot = Integer.parseInt(tt);
                        } else {
                            dit = Integer.parseInt(tt);
                        }
                    }
                    ew = (float) (gaot * 256 + dit) / 10;
                    gaot = 500;
                    // 正常取得华氏度
                    twMeasureTime.setText(sdf.format(new Date()));
                    setTemp(false, ew);
                    twDataService.save(WenduTool.f2c(ew));//保存均为摄氏度
                    break;
                case IRConstants.YYOK:
                    // 设备就绪
                    break;
                case IRConstants.EEONE:
                    showErrorMsg(IRConstants.EEONE);
                    break;
                case IRConstants.EETWO:
                    showErrorMsg(IRConstants.EETWO);
                    break;
                case IRConstants.EETHR:
                    showErrorMsg(IRConstants.EETHR);
                    break;
                case IRConstants.EEFOU:
                    showErrorMsg(IRConstants.EEFOU);
                    break;
                case IRConstants.EEFIV:
                    showErrorMsg(IRConstants.EEFIV);
                    break;
                case IRConstants.EESIX:
                    showErrorMsg(IRConstants.EESIX);
                    break;
                case IRConstants.EESEV:
                    showErrorMsg(IRConstants.EESEV);
                    break;
                case IRConstants.EEEIG:
                    showErrorMsg(IRConstants.EEEIG);
                    break;
                case IRConstants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    twPJName.setText("正在连接设备" + msg.getData().getString(IRConstants.DEVICE_NAME));
                    break;
                case IRConstants.MESSAGE_TOAST:
                    ybgApp.showMessage(getApplicationContext(), msg.getData().getString(IRConstants.TOAST));
                    break;
            }

        }

        private void showErrorMsg(int state) {
            String error = "出错啦！";
            switch (state) {
                case IRConstants.EEONE:
                    error = "量测温度过高";
                    break;
                case IRConstants.EETWO:
                    error = "量测温度过低";
                    break;
                case IRConstants.EETHR:
                    error = "环境温度过高";
                    break;
                case IRConstants.EEFOU:
                    error = "环境温度过低";
                    break;
                case IRConstants.EEFIV:
                    error = "硬件错误";
                    break;
                case IRConstants.EESIX:
                    error = "电压低";
                    break;
                case IRConstants.EESEV:
                    error = "量测温度过高";
                    break;
                case IRConstants.EEEIG:
                    error = "量测温度过低";
                    break;
            }
            ybgApp.showMessage(getApplication(), error);
        }
    };

    private void setTemp(boolean isCC, float value) {
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
