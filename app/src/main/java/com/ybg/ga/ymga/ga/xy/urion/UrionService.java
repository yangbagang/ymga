package com.ybg.ga.ymga.ga.xy.urion;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ybg.ga.ymga.bt.AbstractBLEService;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.preference.XYPreference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by yangbagang on 16/5/4.
 */
public class UrionService extends AbstractBLEService {

    private String VALID_NAME = "Bluetooth BP";

    @Override
    protected boolean foundTargetDevice(String deviceName, String deviceAddress) {
        boolean isValidName = VALID_NAME.equalsIgnoreCase(deviceName);
        if (isValidName) {
            XYPreference.getInstance().setXyDeviceAddr(deviceAddress);
        }
        return isValidName;
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
        return BTPrefix.XY;
    }

    @Override
    protected void readValue(byte[] values) {
        System.out.println("------values-------");
        for (byte b : values) {
            System.out.print(b + ",");
        }
        System.out.println();
        System.out.println("------values-------");
        byte[] buffer = new byte[16];
        InputStream is = new ByteArrayInputStream(values);
        try {
            is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
            return;
        }
        Head head = new Head();
        int[] f = CodeFormat.bytesToHexStringTwo(buffer, 6);
        head.analysis(f);
        if (head.getType() == Head.TYPE_ERROR) {
            // APP接收到血压仪的错误信息
            UrionError error = new UrionError();
            error.analysis(f);
            error.setHead(head);
            // 前台根据错误编码显示相应的提示
            sendMessage(error.getHumanErrorMsg(), true);
        } else if (head.getType() == Head.TYPE_RESULT) {
            // APP接收到血压仪的测量结果
            Data data = new Data();
            data.analysis(f);
            data.setHead(head);
            // 前台根据测试结果来画线性图
            Intent sendDataIntent = new Intent(
                    BTAction.getSendDataAction(getMsgPrefix()));
            sendDataIntent.putExtra(BTAction.DATA,
                    data.getStringValue());
            sendBroadcast(sendDataIntent);
        } else if (head.getType() == Head.TYPE_MESSAGE) {
            // APP接收到血压仪开始测量的通知
            Msg msg = new Msg();
            msg.analysis(f);

            msg.setHead(head);
            // sendMessage(msg.getStringMsg(), false);
        } else if (head.getType() == Head.TYPE_PRESSURE) {
            // APP接受到血压仪测量的压力数据
            Pressure pressure = new Pressure();
            pressure.analysis(f);
            pressure.setHead(head);
            // 每接收到一条数据就发送到前台，以改变进度条的显示
            Intent sendProgressIntent = new Intent(
                    BTAction.sendProgressAction(getMsgPrefix()));
            sendProgressIntent.putExtra(BTAction.PROGRESS,
                    pressure.getPressure());
            sendBroadcast(sendProgressIntent);
        }
    }

    /**
     * 给前台发送信息。
     *
     * @param info    需要发送的内容
     * @param isError 是否是错误信息还是普通提示信息
     */
    private void sendMessage(String info, boolean isError) {
        Intent sendInfoIntent;
        if (isError) {
            sendInfoIntent = new Intent(BTAction.getSendErrorAction(getMsgPrefix()));
        } else {
            sendInfoIntent = new Intent(BTAction.getSendInfoAction(getMsgPrefix()));
        }
        sendInfoIntent.putExtra(BTAction.INFO, info);
        sendBroadcast(sendInfoIntent);
    }

    @Override
    protected void readAck(byte[] acks) {
        readValue(acks);
    }

    public void sendStartCmd() {
        byte[] startCMD = {(byte) 0xfd, (byte) 0xfd, (byte) 0xFA, 0x05, 0x0D, 0x0A};
        writeCharactCmd(startCMD);
    }

    public void sendStopCmd() {
        byte[] stopCMD = {(byte) 0xFD, (byte) 0xFD, (byte) 0xFE, 0x06, 0x0D, 0x0A};
        writeCharactCmd(stopCMD);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private UrionBinder binder = new UrionBinder();

    public class UrionBinder extends Binder {

        public UrionService getService() {
            return UrionService.this;
        }

    }

}
