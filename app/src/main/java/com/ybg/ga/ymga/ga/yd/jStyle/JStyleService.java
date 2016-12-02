/**
 * 
 */
package com.ybg.ga.ymga.ga.yd.jStyle;

import java.util.UUID;

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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTPrefix;

/**
 * @author 杨拔纲
 * 
 */
@SuppressLint("NewApi")
public class JStyleService extends Service {

	private UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
	// private UUID DIS_UUID = UUID
	// .fromString("0000180a-0000-1000-8000-00805f9b34fb");
	// private UUID FIRMWARE_REVISON_UUID = UUID
	// .fromString("00002a26-0000-1000-8000-00805f9b34fb");
	private UUID YH_SERVICE = UUID
			.fromString("0000FFF0-0000-1000-8000-00805F9B34FB");
	private UUID YH_SEND_UUID = UUID
			.fromString("0000FFF6-0000-1000-8000-00805F9B34FB");
	private UUID YH_RECEIVE_UUID = UUID
			.fromString("0000FFF7-0000-1000-8000-00805F9B34FB");

	private BluetoothManager bluetoothManager = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothGatt mBluetoothGatt = null;
	private BluetoothDevice device = null;
	private Intent sendInfoIntent = null;

	private boolean isConnected = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		if (mBluetoothAdapter == null) {
			bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			mBluetoothAdapter = bluetoothManager.getAdapter();
		}
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
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

	private void stop() {
		isConnected = false;
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
		if (device != null) {
			device = null;
		}
	}

	private void registerRecevier() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BTAction.getStopAction(BTPrefix.YD));
		intentFilter.addAction(BTAction.getConnectAction(BTPrefix.YD));
		intentFilter.addAction(BTAction.getSendCmdAction(BTPrefix.YD));
		registerReceiver(cmdBroadcastReceiver, intentFilter);
	}

	/**
	 * 连接到指定地址
	 * 
	 * @param btAddr
	 *            蓝牙地址
	 */
	@SuppressLint("DefaultLocale")
	private void connect(String btAddr) {
		if (!isConnected) {
			try {
				device = mBluetoothAdapter
						.getRemoteDevice(btAddr.toUpperCase());
				if (device == null) {
					sendMessage(BTMessage.DEVICE_NOT_FOUND, true);
				} else {
					mBluetoothGatt = device.connectGatt(this, false,
							mGattCallback);
				}
			} catch (Exception e) {
				isConnected = false;
			}
		}
	}

	/**
	 * 给前台发送信息。
	 * 
	 * @param info
	 *            需要发送的内容
	 * @param isError
	 *            是否是错误信息还是普通提示信息
	 */
	private void sendMessage(String info, boolean isError) {
		if (isError) {
			sendInfoIntent = new Intent(
					BTAction.getSendErrorAction(BTPrefix.YD));
		} else {
			sendInfoIntent = new Intent(BTAction.getSendInfoAction(BTPrefix.YD));
		}
		sendInfoIntent.putExtra(BTAction.INFO, info);
		sendBroadcast(sendInfoIntent);
	}

	// 命令接收器，接收前台发来命令
	private BroadcastReceiver cmdBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, final Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase(BTAction.getConnectAction(BTPrefix.YD))) {
				new Thread() {
					@Override
					public void run() {
						String addr = intent.getExtras().getString(
								BTAction.EXTRA_DEVICE_ADDRESS);
						connect(addr);
					}
				}.start();
			} else if (action.equalsIgnoreCase(BTAction
					.getSendCmdAction(BTPrefix.YD))) {
				// 接收到前台发来指令，发给蓝牙执行
				byte[] byteCmd = intent.getExtras().getByteArray(BTAction.CMD);
				boolean isRead = intent.getExtras().getBoolean("isRead");
				if (isRead) {
					readCharactCmd(byteCmd);
				} else {
					writeCharactCmd(byteCmd);
				}
			} else if (action.equalsIgnoreCase(BTAction
					.getStopAction(BTPrefix.YD))) {
				stop();
			}
		}

	};

	private void writeCharactCmd(byte[] data) {
		BluetoothGattService writeService = mBluetoothGatt
				.getService(YH_SERVICE);
		BluetoothGattCharacteristic mCmdCharac = writeService
				.getCharacteristic(YH_SEND_UUID);
		enableYHNotification();
		// setCharacteristicNotification(nNotifyCharac, true);
		mCmdCharac.setValue(data);
		try {
			Thread.sleep(0x1f4);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mBluetoothGatt.writeCharacteristic(mCmdCharac);
	}

	private void enableYHNotification() {
		BluetoothGattService mainService = mBluetoothGatt
				.getService(YH_SERVICE);
		BluetoothGattCharacteristic mainCharac = mainService
				.getCharacteristic(YH_RECEIVE_UUID);
		if (mBluetoothGatt.setCharacteristicNotification(mainCharac, true)) {
			BluetoothGattDescriptor clientConfig = mainCharac
					.getDescriptor(CCC);
			if (clientConfig != null) {
				clientConfig
						.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				mBluetoothGatt.writeDescriptor(clientConfig);
			}
		}
	}

	private void readCharactCmd(byte[] data) {
		BluetoothGattService readService = mBluetoothGatt
				.getService(YH_SERVICE);
		BluetoothGattCharacteristic mCmdCharac = readService
				.getCharacteristic(YH_SEND_UUID);
		mCmdCharac.setValue(data);
		mBluetoothGatt.readCharacteristic(mCmdCharac);
	}

	/**
	 * 手环主动发送数据代码，当前无用。
	 * 
	 * @param characteristic
	 * @param enabled
	 */
	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothGatt.setCharacteristicNotification(characteristic,
				enabled)) {
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(CCC);
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			// TODO
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				// 连接成功
				// 查找支持的service
				gatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				// 连接断开
				isConnected = false;
				Intent connectedIntent = new Intent(
						BTAction.getDisConnected(BTPrefix.YD));
				sendBroadcast(connectedIntent);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			// 接收到设备传来的数据
			byte[] value = characteristic.getValue();
			sendInfoIntent = new Intent(
					BTAction.getSendAnswerAction(BTPrefix.YD));
			sendInfoIntent.putExtra(BTAction.ACK, value);
			sendBroadcast(sendInfoIntent);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			// 给蓝牙设备发送数据完成
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// 发现己经完成，可以进行相操作。
				isConnected = true;
				Intent connectedIntent = new Intent(
						BTAction.getConnectedSuccess(BTPrefix.YD));
				sendBroadcast(connectedIntent);
			}
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			byte[] ack = characteristic.getValue();
			sendInfoIntent = new Intent(
					BTAction.getSendAnswerAction(BTPrefix.YD));
			sendInfoIntent.putExtra(BTAction.ACK, ack);
			sendBroadcast(sendInfoIntent);
		}

	};

}
