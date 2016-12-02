package com.ybg.ga.ymga.ga.tz.furuik;

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
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.ybg.ga.ymga.user.UserPreferences;

import java.util.UUID;

/**
 * Created by yangbagang on 15/10/8.
 */
@SuppressLint("NewApi")
public class BluetoothService extends Service {

    private final IBinder ibinder = new BLEBinder();
    Handler handler = new Handler() {
    };

    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattCharacteristic mBluetoothGattCharacteristic1 = null;
    private String bleAddress;
    private BluetoothDevice mBluetoothDevice = null;
    private long timeLong = 0;
    private boolean sendUser = false;
    private String fatValue = "";
    private int indexdata = 0;
    //	private boolean isnext = false;
    private float Wendingweight = 0;


    public static final String CHARACTERISTIC_UUID_RETURN = "00002902-0000-1000-8000-00805f9b34fb";
    public static final String SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String CH_UUID = "0000fff6-0000-1000-8000-00805f9b34fb";

    private UserPreferences userPreferences = UserPreferences.getInstance();

    BluetoothGattCallback mGattcallback = new BluetoothGattCallback() {

        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

//			System.out.println("status=" + status +"  newState="+newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                System.out.println("连接成功");
                System.out.println("发现服务 ： " + gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                MyApplication.Chable = false;
                Log.e("ser", "蓝牙断开连接");
                disconnect();

            }

        }


        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            for (BluetoothGattService service : gatt.getServices()) {
                System.out.print("found service: " + service.getUuid().toString());
            }
            BluetoothGattService mBluetoothGattSevice = gatt.getService(UUID.fromString(SERVICE_UUID));
            mBluetoothGattCharacteristic1 = mBluetoothGattSevice.getCharacteristic(UUID.fromString(CH_UUID));
            //激活通知
            final int charaProp = mBluetoothGattCharacteristic1.getProperties();
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                Log.e("Ble", "具有通知属性。。。");
                mBluetoothGatt.setCharacteristicNotification(mBluetoothGattCharacteristic1, true);
                BluetoothGattDescriptor descriptor = mBluetoothGattCharacteristic1.getDescriptor(UUID.fromString(CHARACTERISTIC_UUID_RETURN));
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    Log.e("Ble", mBluetoothGattCharacteristic1.getUuid() + "激活通知属性： " + mBluetoothGatt.writeDescriptor(descriptor));
                    sendBroadCast(Confing.BLE_NOTiFY, "connect", "体重秤连接成功");//激活通知属性发送广播
                    MyApplication.Chable = true;

                }
            }

        }

        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {


        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String codeString = BitMapTools.bytesToHexString(characteristic.getValue());
//			if(!codeString.contains("02d20f")){
            Log.e("BLE code", "####" + codeString);
//			}

            if (codeString.substring(0, 6).equals("02d603") &&
                    codeString.substring(codeString.length() - 2).equals("aa")) {//变化中的体重02d603 0000 01dcaa
                String wei = codeString.substring(6, 10);
                float weight = (float) (Integer.parseInt(wei, 16) / 10.0);
                Log.e("变化中体重", userPreferences.getName() + "-------------------------------------" + weight);

                Intent intent = new Intent(Confing.BLE_ChangeWei_Data);
                intent.putExtra("changewei", weight);
                sendBroadCast(intent);

                //发送一次人体参数
                sendUser = false;
                if (!sendUser) {

                    long userid = userPreferences.getId();
                    int userhigh = (int) (userPreferences.getBodyHigh() * 100);
                    int userage = userPreferences.getAge();
                    int usersex = userPreferences.getUserSex();
                    Log.e("dddd", "发送人体参数 ： ++++ " + userid + " " + userhigh + " " + userage + " " + usersex);
                    byte jiaoyan = (byte) (0x02 + 0xE2 + 0x04 + userid + userhigh + userage + usersex);
                    byte[] sum = {0x02, (byte) 0xE2, 0x04,
                            (byte) userid, (byte) userhigh, (byte) userage, (byte) usersex
                            , jiaoyan, (byte) 0xaa};
                    Log.e("fat", "发送人体参数指令 ： ++++ " + BitMapTools.bytesToHexString(sum));
                    sendLight(sum);
                    sendUser = true;
                }


            } else if (codeString.substring(0, 8).equals("02d00101")) {

                //秤端接收失败  发送人体参数
                //if (MyApplication.member != null) {

                    long userid = userPreferences.getId();
                    int userhigh = (int) (userPreferences.getBodyHigh() * 100);
                    int userage = userPreferences.getAge();
                    Log.e("age", "" + userage);
                    int usersex = userPreferences.getUserSex();
                    Log.e("dddd", "发送失败再次发送人体参数 ： ++++ " + userid + " " + userhigh + " " + userage + " " + usersex);
                    byte jiaoyan = (byte) (0x02 + 0xE2 + 0x04 + userid + userhigh + userage + usersex);
                    byte[] sum = {0x02, (byte) 0xE2, 0x04,
                            (byte) userid, (byte) userhigh, (byte) userage, (byte) usersex
                            , jiaoyan, (byte) 0xaa};
                    sendLight(sum);
                //}

            } else if (codeString.substring(0, 8).equals("02d00100")) {

                Log.d("成功", "接收人体参数成功返回 ： " + codeString);

            } else if (codeString.substring(0, 6).equals("02d103") &&
                    codeString.substring(codeString.length() - 2).equals("aa")) {//锁定体重
                String wei = codeString.substring(6, 10);
                float weight = (float) (Integer.parseInt(wei, 16) / 10.0);
                Log.e("锁定体重", "+++++++++++++++++++++++++++++" + weight);
                sendUser = false;

                if (Wendingweight != weight) {

                    float userhigh = userPreferences.getBodyHigh();
                    float BMI = (float) (Math.round(weight / (userhigh * userhigh) * 10)) / 10;

                    Intent intent = new Intent(Confing.BLE_Confirm_Data);
                    intent.putExtra("confirmwei", weight);
                    intent.putExtra("confirmBMI", BMI);
                    sendBroadCast(intent);
                }

                Wendingweight = weight;

                //APP端发送确认收到数据命令 ， 秤将停止重复发送
//				byte jiaoyan = (byte) (0x02+0xE0+0x01);
//				byte[] sum = {0x02,(byte) 0xe0,0x01,0x00,jiaoyan,(byte) 0xaa};
//				sendLight(sum);
                //
            } else {//脂肪等数据  02d20f01f3005e0295001b022204ae00ac2a12a5aa
                Log.e("BLE code", "===========" + codeString);

                fatValue += codeString;

                if (indexdata == 5) {//累加5条数据

                    if (fatValue.contains("02d20f")) {//测量正常；


                        int in = fatValue.indexOf("02d20f");
                        Log.e("脂肪数据", "累加数据" + fatValue);
                        String per = fatValue.substring(in, in + 42);
                        Log.e("脂肪数据", "完整数据" + per);
                        String data = fatValue.substring(in + 6, in + 38);
                        Log.e("脂肪数据", "有效数据" + data);


                        //体重 脂肪
                        float weight = (float) (Integer.parseInt(data.substring(0, 4), 16) / 10.0);
                        float fat = (float) (Integer.parseInt(data.substring(4, 8), 16) / 10.0);
                        float humidity = (float) (Integer.parseInt(data.substring(8, 12), 16) / 10.0);
                        float bone = (float) (Integer.parseInt(data.substring(12, 16), 16) / 10.0);
                        float muscle = (float) (Integer.parseInt(data.substring(16, 20), 16) / 10.0);
                        float basal = (float) (Integer.parseInt(data.substring(20, 24), 16));
                        float bmi = (float) (Integer.parseInt(data.substring(24, 28), 16) / 10.0);
                        float visceral = (float) (Integer.parseInt(data.substring(28, 30), 16) / 10.0);
                        int age = (Integer.parseInt(data.substring(30, 32), 16));


                        Log.e("data", "体重:" + weight + "  脂肪:" + fat
                                + "  水分:" + humidity + "  骨量 :" + bone + "  肌肉 :" + muscle
                                + "  基础代谢:" + basal + "  BMI:" + bmi + "  内脏脂肪:" + visceral
                                + "  年龄 :" + age);

                        //发送广播
                        Intent intent = new Intent(Confing.BLE_Fat_Data);
                        intent.putExtra("weight", weight);
                        intent.putExtra("bmi", bmi);
                        intent.putExtra("fat", fat);
                        intent.putExtra("humidity", humidity);
                        intent.putExtra("muscle", muscle);
                        intent.putExtra("bone", bone);
                        intent.putExtra("visceral", visceral);
                        intent.putExtra("basal", basal);
                        intent.putExtra("age", age);
                        sendBroadCast(intent);
                    } else {
                        Log.e("service", "----Error--------Error--------Error--------Error--------Error----");
                    }


//					APP端发送确认收到数据命令 ， 秤将停止重复发送
                    byte jiaoyan = (byte) (0x02 + 0xE0 + 0x01);
                    byte[] sum = {0x02, (byte) 0xe0, 0x01, 0x00, jiaoyan, (byte) 0xaa};
                    sendLight(sum);
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            fatValue = "";
                            indexdata = 0;
                        }
                    }, 500);
                }
                indexdata++;
            }
        }

    };


    private void broadcastUpdate(String action, String extra,
                                 BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();
        Log.i("ddddddddd", "uuid:" + characteristic.getUuid().toString());
        if (data != null && data.length > 0) {
//			Log.i("dddddddddd", "接收数据字节数：" + data.length);
//			System.out.println("数据:" + Tools.bytesToHexString(data));
            intent.putExtra(extra, data);
            sendBroadcast(intent);
        }
    }


    //初始化
    public boolean initialize() {

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                return false;
            }
        }
        MyApplication.mBluetoothAdapter = mBluetoothAdapter;

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
//			System.out.println("提示开启蓝牙");
        }

        return true;
    }


    //判断是否打开蓝牙
    public void openBluetooth() {

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }


    //根据Max 地址链接蓝牙
    public boolean connect(String address) {
        Log.e("connect address", address);
        MyApplication.Address = address;

        if (mBluetoothAdapter == null) {
            return false;
        }


        if (bleAddress != null && address.equals(bleAddress) && mBluetoothGatt != null) {
            Log.e("connect ", "...........");
            if (mBluetoothGatt.connect()) {
//				Log.e("connect ", "判断是否连接。。。");
                return true;
            } else {
                return false;
            }
        }

        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mBluetoothDevice == null) {
            return false;
        }

        mBluetoothGatt = mBluetoothDevice.connectGatt(this, false, mGattcallback);
        bleAddress = address;

        return true;

    }

    /**
     * 断开蓝牙连接
     */
    public void disconnect() {
//		   System.out.println("DisConnect");

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return;
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
            }
            mBluetoothGatt = null;
        }
        MyApplication.Chable = false;
        sendBroadCast(Confing.BLE_DISCONNECT, "disconnect", "脂肪秤断开连接");//断开连接发送广播
    }

    /**
     * 发送广播
     */
    private void sendBroadCast(final String action, String key, String value) {
        final Intent intent = new Intent(action);
        if (!value.equals("")) {
            intent.putExtra(key, value);
        }
        sendBroadcast(intent);
    }

    /**
     * 发送脂肪等数据广播
     */
    private void sendBroadCast(Intent intent) {
        sendBroadcast(intent);
    }

    /**
     * 发送指令
     */
    public void sendLight(byte[] sum) {
        if (mBluetoothGattCharacteristic1 != null && mBluetoothGatt != null) {
            mBluetoothGattCharacteristic1.setValue(sum);
            Log.e("send", "####" +
                    mBluetoothGatt.writeCharacteristic(mBluetoothGattCharacteristic1) + "       "
                    + BitMapTools.bytesToHexString(sum));
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return ibinder;
    }


    public class BLEBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }

    }

}
