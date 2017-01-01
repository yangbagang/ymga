package com.ybg.ga.ymga.ga.tw.ir;

import android.Manifest;
import android.app.Activity;
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
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.ga.preference.TWPreference;
import com.ybg.ga.ymga.ga.tw.TWDataService;
import com.ybg.ga.ymga.ga.tw.WenduTool;
import com.ybg.ga.ymga.util.AppConstat;

import java.util.StringTokenizer;

/**
 * Created by yangbagang on 2015/5/27.
 */
public class IRBTActivity extends Activity {

    private TWPreference twPreference = TWPreference.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();

    private TextView jianceTV = null;
    private ProgressBar progressBar = null;

    private TWDataService twDataService = null;
    private BluetoothService bluetoothService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tw_jiance);

        /** 尝试初始化视图实例 **/
        initView();
    }

    private void initView() {
        jianceTV = (TextView) findViewById(R.id.tw_jiance_tv);
        progressBar = (ProgressBar) findViewById(R.id.tw_jiance_pb);
    }

    private boolean checkRight() {
        String message = getString(R.string.permission_request_notice, getString(R.string
                .app_name), getString(R.string.permission_access_coarse_location));
        return ybgApp.checkPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION,
                message, AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION);
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
            // 开启后台程序
            bluetoothService = new BluetoothService(this, mHandler);
        }
        Intent bindIntent = new Intent(IRBTActivity.this, TWDataService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        checkRight();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 停止测量
        stopMeasure();
        // 停止接收广播
        if (null != bluetoothService) {
            bluetoothService.stop();
            bluetoothService = null;
        }
        unbindService(mConnection);
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
        // 发送连接指令
        if (!jiance) {
            bluetoothService.connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(twPreference.getTwDeviceAddr()));
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
                        case BluetoothService.STATE_CONNECTED:
                            // 己经连接好设备
                            jianceTV.setText("设备己连接");
                            twPreference.setHasAssign(true);
                            byte[] sends = {(-2), (-3), (-86), (-96), 13, 10};
                            bluetoothService.write(sends);
                            jiance = true;
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            // 正在连接
                            jianceTV.setText("正在连接设备...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            jiance = false;
                            jianceTV.setText("无可用连接");
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
                    // twMeasureTime.setText(sdf.format(new Date()));
                    twDataService.save(ew);//保存均为摄氏度
                    setTemp(true, ew);
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
                    // twMeasureTime.setText(sdf.format(new Date()));
                    twDataService.save(ew);//保存均为摄氏度
                    setTemp(true, ew);
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
                    // twMeasureTime.setText(sdf.format(new Date()));
                    twDataService.save(WenduTool.f2c(ew));//保存均为摄氏度
                    setTemp(false, ew);
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
                    // twMeasureTime.setText(sdf.format(new Date()));
                    twDataService.save(WenduTool.f2c(ew));//保存均为摄氏度
                    setTemp(false, ew);
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
                    jianceTV.setText("正在连接设备" + msg.getData().getString(IRConstants.DEVICE_NAME));
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
        Intent dataIntent = new Intent();
        dataIntent.putExtra("isCC", isCC);
        dataIntent.putExtra("value", value);
        setResult(AppConstat.TW_MEASURE_RESULT_CODE, dataIntent);
        finish();
    }

}
