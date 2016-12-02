/**
 *
 */
package com.ybg.ga.ymga.bt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * @author 杨拔纲
 */
public abstract class AbstractBTService extends Service {

    protected BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();

    protected BluetoothDevice device = null;

    protected BluetoothSocket socket = null;

    protected Intent sendInfoIntent = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        registerRecevier();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        stop();
        unregisterReceiver(cmdBroadcastReceiver);
        super.onDestroy();
    }

    protected abstract void start();

    protected abstract void stop();

    protected abstract String getPrefix();

    protected void registerRecevier() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTAction.getConnectAction(getPrefix()));
        intentFilter.addAction(BTAction.getStartAction(getPrefix()));
        intentFilter.addAction(BTAction.getStopAction(getPrefix()));
        intentFilter.addAction(BTAction.getConnectStartAction(getPrefix()));
        registerReceiver(cmdBroadcastReceiver, intentFilter);
    }

    protected void connect(String btAddr) {
        device = findDeviceByAddr(btAddr);
        if (device == null) {
            sendMessage(BTMessage.DEVICE_NOT_FOUND, true);
        } else {
            try {
                socket = device.createRfcommSocketToServiceRecord(getUUID());
                socket.connect();
            } catch (IOException e) {
                sendMessage(BTMessage.CONNECT_FAIL, true);
            }
        }
    }

    protected void connectAndStart(String btAddr) {
        connect(btAddr);
        start();
    }

    private BluetoothDevice findDeviceByAddr(String btAddr) {
        BluetoothDevice bluetoothDevice = null;
        try {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(btAddr
                    .toUpperCase());
        } catch (Exception e) {
            sendMessage(BTMessage.DEVICE_NOT_FOUND, true);
        }
        return bluetoothDevice;
    }

    protected void write(OutputStream outStream, byte[] f) {
        if (outStream != null) {
            try {
                outStream.write(f);
            } catch (IOException e) {
                sendMessage(BTMessage.SEND_CMD_FAIL, true);
            }
        }
    }

    /**
     * 给前台发送信息。
     *
     * @param info    需要发送的内容
     * @param isError 是否是错误信息还是普通提示信息
     */
    protected void sendMessage(String info, boolean isError) {
        if (isError) {
            sendInfoIntent = new Intent(BTAction.getSendErrorAction(getPrefix()));
        } else {
            sendInfoIntent = new Intent(BTAction.getSendInfoAction(getPrefix()));
        }
        sendInfoIntent.putExtra(BTAction.INFO, info);
        sendBroadcast(sendInfoIntent);
    }

    protected UUID getUUID() {
        // 提供默认UUID。非默认UUID需要重写此方法。
        return UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    protected BroadcastReceiver cmdBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(BTAction.getConnectAction(getPrefix()))) {
                new Thread() {
                    @Override
                    public void run() {
                        String addr = intent.getExtras().getString(
                                BTAction.EXTRA_DEVICE_ADDRESS);
                        connect(addr);
                    }
                }.start();
            } else if (action.equalsIgnoreCase(BTAction
                    .getStartAction(getPrefix()))) {
                start();
            } else if (action.equalsIgnoreCase(BTAction
                    .getStopAction(getPrefix()))) {
                stop();
            } else if (action.equalsIgnoreCase(BTAction
                    .getConnectStartAction(getPrefix()))) {
                new Thread() {
                    @Override
                    public void run() {
                        String addr = intent.getExtras().getString(
                                BTAction.EXTRA_DEVICE_ADDRESS);
                        connectAndStart(addr);
                    }
                }.start();
            }
        }

    };
}
