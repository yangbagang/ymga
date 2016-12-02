/**
 * 
 */
package com.ybg.ga.ymga.bt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 * 
 */
public class BTDeviceListActivity extends Activity {

	private BluetoothAdapter mBtAdapter;
	private List<String> deviceAddressList;
	private List<String> pairedDeviceAddressList;
	private ArrayAdapter<String> foundDevicesArrayAdapter;
	private ArrayAdapter<String> pairedDevicesArrayAdapter;

	private Button scanButton = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.bt_device_list);

		setResult(Activity.RESULT_CANCELED);

		scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		deviceAddressList = new ArrayList<String>();
		pairedDeviceAddressList = new ArrayList<String>();
		foundDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.found_device_name);
		pairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.found_device_name);

		ListView newDevicesListView = (ListView) findViewById(R.id.found_devices);
		newDevicesListView.setAdapter(foundDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(foundDeviceClickListener);

		ListView pairedDevicesListView = (ListView) findViewById(R.id.paired_devices);
		pairedDevicesListView.setAdapter(pairedDevicesArrayAdapter);
		pairedDevicesListView.setOnItemClickListener(pairedDeviceClickListener);

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);

		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesArrayAdapter.add(device.getName());
				pairedDeviceAddressList.add(device.getAddress());
			}
		} else {
			String noDevices = getResources().getText(R.string.label_not_found)
					.toString();
			pairedDevicesArrayAdapter.add(noDevices);
		}
	}

	protected void onDestroy() {
		super.onDestroy();

		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		this.unregisterReceiver(mReceiver);
	}

	private void doDiscovery() {

		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.button_scan);

		findViewById(R.id.title_found_devices).setVisibility(View.VISIBLE);

		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		mBtAdapter.startDiscovery();
	}

	private OnItemClickListener foundDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			mBtAdapter.cancelDiscovery();

			String address = deviceAddressList.get(arg2);

			Intent intent = new Intent();
			intent.putExtra(BTAction.EXTRA_DEVICE_ADDRESS, address);

			setResult(AppConstat.BT_FOUND_RESULT_CODE, intent);
			finish();
		}
	};

	private OnItemClickListener pairedDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			mBtAdapter.cancelDiscovery();

			String address = pairedDeviceAddressList.get(arg2);

			Intent intent = new Intent();
			intent.putExtra(BTAction.EXTRA_DEVICE_ADDRESS, address);

			setResult(AppConstat.BT_FOUND_RESULT_CODE, intent);
			finish();
		}
	};

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// 过滤重复内容
				if (device.getBondState() != BluetoothDevice.BOND_BONDED
						&& !deviceAddressList.contains(device.getAddress())) {
					String name = device.getName();
					if (name != null && !"".equals(name)) {
						foundDevicesArrayAdapter.add(device.getName());
						deviceAddressList.add(device.getAddress());
					}
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				setProgressBarIndeterminateVisibility(false);
				setTitle(R.string.bt_select_device);
				if (foundDevicesArrayAdapter.getCount() == 0) {
					String noDevices = getResources().getText(
							R.string.bt_none_found).toString();
					foundDevicesArrayAdapter.add(noDevices);
				}
				scanButton.setVisibility(View.VISIBLE);
			}
		}
	};

}
