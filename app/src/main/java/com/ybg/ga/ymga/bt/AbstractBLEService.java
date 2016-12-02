package com.ybg.ga.ymga.bt;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.util.UUID;

/**
 * Created by yangbagang on 16/2/27.
 */
@SuppressLint("NewApi")
public abstract class AbstractBLEService extends Service {

    protected final static String TAG = "BLEService";

    protected BluetoothManager mBluetoothManager = null;
    protected BluetoothAdapter mBluetoothAdapter = null;
    protected BluetoothGatt mBluetoothGatt = null;
    protected BluetoothDevice device = null;
    private Handler mHandler;
    protected int mConnectionState = STATE_DISCONNECTED;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    protected boolean isScanning = false;
    protected boolean isBLESupport = true;
    protected long SCAN_PERIOD = 1000 * 30;

    protected boolean needNotify = false;

    @Override
    public void onCreate() {
        super.onCreate();

        checkBLESupport();
        if (!isBLESupport) {
            //BLE is not support.
            return;
        }

        if (mBluetoothAdapter == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mHandler = new Handler();
    }

    private void checkBLESupport() {
        isBLESupport = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public boolean hasBLESupportState() {
        return isBLESupport;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mConnectionState == STATE_CONNECTED) {
            stop();
        }
        if (isScanning) {
            scanBLEDevice(false);
        }
    }

    public void stop() {
        mConnectionState = STATE_DISCONNECTED;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        if (device != null) {
            device = null;
        }
    }

    public void scanBLEDevice(final boolean enable) {
        System.out.println("scanBLEDevice " + enable);
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.stopLeScan(stopScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            isScanning = true;
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.startLeScan(startScanCallback);
            }
        } else {
            isScanning = false;
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.stopLeScan(stopScanCallback);
            }
        }
    }

    private BluetoothAdapter.LeScanCallback stopScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "stopScan");
        }
    };

    private BluetoothAdapter.LeScanCallback startScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();
            //System.out.println("Name: " + deviceName + ", addr: " + deviceAddress);
            if (foundTargetDevice(deviceName, deviceAddress)) {
                System.out.println("Found target device with name: " + deviceName + ", addr: " + deviceAddress);
                connect(deviceAddress);
                if (isScanning) {
                    mBluetoothAdapter.stopLeScan(stopScanCallback);
                    isScanning = false;
                }
            }
        }
    };

    protected abstract  boolean foundTargetDevice(String deviceName, String deviceAddress);

    public void connect(String bleAddr) {
        System.out.println("connect to: " + bleAddr);
        if (mConnectionState == STATE_DISCONNECTED) {
            try {
                device = mBluetoothAdapter.getRemoteDevice(bleAddr.toUpperCase());
                if (device == null) {
                    scanBLEDevice(true);
                } else {
                    mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
                    mConnectionState = STATE_CONNECTING;
                    sendStateUpdate();
                }
            } catch (Exception e) {
                mConnectionState = STATE_DISCONNECTED;
            }
        }
    }

    protected abstract UUID getServiceUUID();

    protected abstract UUID getCharacteristicUUID();

    protected abstract UUID getCMDUUID();

    protected abstract UUID getDescriptorUUID();

    protected abstract String getMsgPrefix();

    protected boolean isNeedNotify() {
        return needNotify;
    }

    public void writeCharactCmd(byte[] data) {
        BluetoothGattService writeService = mBluetoothGatt
                .getService(getServiceUUID());
        BluetoothGattCharacteristic mCmdCharac = writeService.getCharacteristic(getCMDUUID());
        if (isNeedNotify()) {
            enableNotification();
        }
        mCmdCharac.setValue(data);
        try {
            Thread.sleep(0x1f4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mBluetoothGatt.writeCharacteristic(mCmdCharac);
    }

    protected void enableNotification() {
        BluetoothGattService mainService = mBluetoothGatt
                .getService(getServiceUUID());
        BluetoothGattCharacteristic mainCharacter = mainService
                .getCharacteristic(getCharacteristicUUID());
        if (mBluetoothGatt.setCharacteristicNotification(mainCharacter, true)) {
            BluetoothGattDescriptor clientConfig = mainCharacter
                    .getDescriptor(getDescriptorUUID());
            if (clientConfig != null) {
                clientConfig
                        .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(clientConfig);
            }
        }
    }

    protected void sendStateUpdate() {
        Intent intent = null;
        switch (mConnectionState) {
            case STATE_CONNECTING:
                intent = new Intent(BTAction.getConnectAction(getMsgPrefix()));
                break;
            case STATE_CONNECTED:
                intent = new Intent(BTAction.getConnectedSuccess(getMsgPrefix()));
                break;
            case STATE_DISCONNECTED:
                intent = new Intent(BTAction.getDisConnected(getMsgPrefix()));
                break;
        }
        if (intent != null) {
            sendBroadcast(intent);
        }
    }

    protected abstract void readValue(byte[] values);

    protected abstract void readAck(byte[] acks);

    protected BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 连接成功
                mConnectionState = STATE_CONNECTED;
                // 查找支持的service
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 连接断开
                mConnectionState = STATE_DISCONNECTED;
                sendStateUpdate();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            // 接收到设备传来的数据
            byte[] value = characteristic.getValue();
            readValue(value);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            // 给蓝牙设备发送数据完成
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            System.out.println("writing status: " + status);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BluetoothGattService mBluetoothGattSevice = gatt.getService(getServiceUUID());
            BluetoothGattCharacteristic mBluetoothGattCharacteristic = mBluetoothGattSevice.getCharacteristic(getCharacteristicUUID());
            //激活通知
            final int charaProp = mBluetoothGattCharacteristic.getProperties();
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                Log.e("Ble", "具有通知属性。。。");
                needNotify = true;
                mBluetoothGatt.setCharacteristicNotification(mBluetoothGattCharacteristic, true);
                BluetoothGattDescriptor descriptor = mBluetoothGattCharacteristic.getDescriptor(getDescriptorUUID());
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    Log.e("Ble", mBluetoothGattCharacteristic.getUuid() + "激活通知属性： " + mBluetoothGatt.writeDescriptor(descriptor));
                    needNotify = false;
                }
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // 发现己经完成，可以进行相操作。
                mConnectionState = STATE_CONNECTED;
                sendStateUpdate();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte[] ack = characteristic.getValue();
            readAck(ack);
        }

    };

}
