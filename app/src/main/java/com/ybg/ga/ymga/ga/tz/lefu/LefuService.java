package com.ybg.ga.ymga.ga.tz.lefu;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ybg.ga.ymga.bt.AbstractBLEService;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.preference.TZPreference;
import com.ybg.ga.ymga.user.UserPreferences;

import java.util.UUID;

/**
 * Created by yangbagang on 16/4/22.
 */
public class LefuService extends AbstractBLEService {

    private String validName = "Electronic Scale";

    private UserPreferences userPreferences = UserPreferences.getInstance();

    private IBinder iBinder = new LefuBinder();

    @Override
    protected boolean foundTargetDevice(String deviceName, String deviceAddress) {
        boolean isValidName = validName.equalsIgnoreCase(deviceName);
        if (isValidName) {
            TZPreference.getInstance().setTzDeviceAddr(deviceAddress);
        }
        return isValidName;
    }

    @Override
    protected UUID getServiceUUID() {
        return UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected UUID getCharacteristicUUID() {
        return UUID.fromString("0000fff4-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected UUID getCMDUUID() {
        return UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected UUID getDescriptorUUID() {
        return UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }

    @Override
    protected String getMsgPrefix() {
        return BTPrefix.TZ;
    }

    @Override
    protected void readValue(byte[] values) {
        readAck(values);
    }

    @Override
    protected void readAck(byte[] acks) {
        if (acks == null) {
            return;
        }

        System.out.println("============");
        for (byte b : acks) {
            System.out.print("," + b);
        }
        System.out.println("");
        System.out.println("============");

        if (acks[0] == 0x31) {
            Intent intent = new Intent(BTAction.getSendErrorAction(BTPrefix.TZ));
            intent.putExtra("error_code", "31");
            sendBroadcast(intent);
        } else if (acks[0] == 0x33) {
            Intent intent = new Intent(BTAction.getSendErrorAction(BTPrefix.TZ));
            intent.putExtra("error_code", "33");
            sendBroadcast(intent);
        } else if (acks.length > 15){
            Intent intent = new Intent(BTAction.getSendDataAction(BTPrefix.TZ));
            intent.putExtra("tzHex", acks);
            sendBroadcast(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    public class LefuBinder extends Binder {
        public LefuService getService() {
            return LefuService.this;
        }
    }

    public void sendUserInfo() {
        byte sex = (byte) userPreferences.getUserSex();
        byte bodyHigh = (byte) (userPreferences.getBodyHigh() * 100);
        byte age = (byte) userPreferences.getAge();
        //01表示单位为kg
        byte verify = (byte) (sex ^ bodyHigh ^ age ^ 1);
        byte[] cmd = {(byte) 0xfe, 00, sex, 00, bodyHigh, age, 01, verify};
        writeCharactCmd(cmd);

    }

    public void sendStopCmd() {
        byte[] cmd = {(byte) 0xFD, 35, 00, 00, 00, 00, 00, 0x35};
        writeCharactCmd(cmd);
    }

}
