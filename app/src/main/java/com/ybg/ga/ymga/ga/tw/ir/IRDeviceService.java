package com.ybg.ga.ymga.ga.tw.ir;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ybg.ga.ymga.bt.AbstractBLEService;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.preference.TWPreference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by yangbagang on 16/2/27.
 */
@SuppressLint("NewApi")
public class IRDeviceService extends AbstractBLEService {

    private static final String irDeviceName = "Bluetooth BP";
    private TWPreference twPreference = TWPreference.getInstance();

    @Override
    protected boolean foundTargetDevice(String deviceName, String deviceAddress) {
        boolean isIRDevice = irDeviceName.equalsIgnoreCase(deviceName);
        if(isIRDevice) {
            twPreference.setTwDeviceAddr(deviceAddress);
        }
        return isIRDevice;
    }

    @Override
    protected UUID getServiceUUID() {
        return UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected UUID getCharacteristicUUID() {
        return UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected UUID getCMDUUID() {
        return UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected UUID getDescriptorUUID() {
        return UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected String getMsgPrefix() {
        return BTPrefix.TW;
    }

    @Override
    protected void sendStateUpdate() {
        irHandler.obtainMessage(IRConstants.MESSAGE_STATE_CHANGE, mConnectionState, -1).sendToTarget();
    }

    @Override
    public void connect(String bleAddr) {
        System.out.println("connect to: " + bleAddr);
        if (mConnectionState == STATE_DISCONNECTED) {
            try {
                device = mBluetoothAdapter.getRemoteDevice(bleAddr.toUpperCase());
                if (device == null) {
                    scanBLEDevice(true);
                } else {
                    mBluetoothGatt = device.connectGatt(IRDeviceService.this, false, mGattCallback);
                    mConnectionState = STATE_CONNECTING;
                    sendStateUpdate();
                }
            } catch (Exception e) {
                mConnectionState = STATE_DISCONNECTED;
            }
        }
    }

    @Override
    protected void readValue(byte[] acks) {
        byte[] values = new byte[acks.length - 1];
        for (int i = 1; i < acks.length; i++) {
            values[i - 1] = acks[i];
        }
        byte[] buffer = new byte[16];
        int bytes;
        Data dt = new Data();
        InputStream is = new ByteArrayInputStream(values);
        try {
            bytes = is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
            return;
        }
        int[] f = CodeFormat.bytesToHexStringTwo(buffer, 8);
        dt.analysis(f);
        int a = dt.getHead();
        int two = dt.getDatatwo();
        if (a == 253) {
            com[0] = dt.getHead();
            com[1] = dt.getDataone();
            com[2] = dt.getDatatwo();
            com[3] = dt.getThree();
            com[4] = dt.getFour();
            com[5] = dt.getFive();
            com[6] = dt.getSex();
            if (com[5] == 13 && com[6] == 10) {
                if ((com[1] == 26 | com[1] == 21) && com[2] == 170) {
                    irHandler.obtainMessage(IRConstants.YYOK,
                            bytes, -1, asd).sendToTarget();
                }
                if (com[1] == 26 | com[1] == 21) {
                    if (com[2] == 129 && com[4] == 1) {
                        irHandler.obtainMessage(IRConstants.EEONE,
                                bytes, -1, buffer).sendToTarget();
                    }
                    if (com[2] == 130 && com[4] == 2) {
                        irHandler.obtainMessage(IRConstants.EETWO,
                                bytes, -1, buffer).sendToTarget();
                    }
                    if (com[2] == 131 && com[4] == 3) {
                        irHandler.obtainMessage(IRConstants.EETHR,
                                bytes, -1, buffer).sendToTarget();
                    }
                    if (com[2] == 132 && com[4] == 4) {
                        irHandler.obtainMessage(IRConstants.EEFOU,
                                bytes, -1, buffer).sendToTarget();
                    }
                    if (com[2] == 133 && com[4] == 5) {
                        irHandler.obtainMessage(IRConstants.EEFIV,
                                bytes, -1, buffer).sendToTarget();
                    }
                    if (com[2] == 134 && com[4] == 6) {
                        irHandler.obtainMessage(IRConstants.EESIX,
                                bytes, -1, buffer).sendToTarget();
                    }
                    if (com[2] == 135 && com[4] == 7) {
                        irHandler.obtainMessage(IRConstants.EESEV,
                                bytes, -1, buffer).sendToTarget();
                    }
                    if (com[2] == 136 && com[4] == 8) {
                        irHandler.obtainMessage(IRConstants.EEEIG,
                                bytes, -1, buffer).sendToTarget();
                    }
                }
                // 摄氏度
                if (com[1] == 26) {
                    //人体温度
                    if (com[2] == 1) {
                        asd = com[3] + "" + "," + com[4] + "";
                        irHandler.obtainMessage(
                                IRConstants.YYRESULT, bytes, -1,
                                asd).sendToTarget();
                    }
                    //目标 摄氏度
                    if (com[2] == 0) {
                        asd = com[3] + "" + "," + com[4] + "";
                        irHandler.obtainMessage(IRConstants.YYMU,
                                bytes, -1, asd).sendToTarget();
                    }
                }
                // F
                if (com[1] == 21) {
                    //人体F
                    if (com[2] == 1) {
                        asd = com[3] + "" + "," + com[4] + "";
                        irHandler.obtainMessage(
                                IRConstants.FYRESULT, bytes, -1,
                                asd).sendToTarget();
                    }
                    //目标 F
                    if (com[2] == 0) {
                        asd = com[3] + "" + "," + com[4] + "";
                        irHandler.obtainMessage(IRConstants.FYMU,
                                bytes, -1, asd).sendToTarget();
                    }
                }
            }
        }
    }

    @Override
    protected void readAck(byte[] acks) {
        readValue(acks);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return irDeviceBinder;
    }
    
    public class IRDeviceBinder extends Binder {
        public IRDeviceService getService() {
            return IRDeviceService.this;
        }
    }

    public void setIrHandler(Handler irHandler) {
        this.irHandler = irHandler;
    }
    
    private IRDeviceBinder irDeviceBinder = new IRDeviceBinder();
    private Handler irHandler;
    private int[] com = {0, 0, 0, 0, 0, 0, 0};
    private String asd;
}
