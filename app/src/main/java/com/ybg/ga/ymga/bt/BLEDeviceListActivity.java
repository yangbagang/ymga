/**
 *
 */
package com.ybg.ga.ymga.bt;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
 */
@SuppressLint("NewApi")
public class BLEDeviceListActivity extends Activity {

    private BluetoothAdapter mBluetoothAdapter;
    private List<String> deviceAddressList;
    private ArrayAdapter<String> foundDevicesArrayAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private static final long SCAN_PERIOD = 10000;

    private Button scanButton = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.bt_device_list);

        mHandler = new Handler();
        setResult(Activity.RESULT_CANCELED);

        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        deviceAddressList = new ArrayList<String>();
        foundDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.found_device_name);

        ListView newDevicesListView = (ListView) findViewById(R.id.found_devices);
        newDevicesListView.setAdapter(foundDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(foundDeviceClickListener);

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (mScanning) {
            scanLeDevice(false);
        }
    }

    private void doDiscovery() {
        if (mScanning) {
            scanLeDevice(false);
        }
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.button_scan);
        findViewById(R.id.title_found_devices).setVisibility(View.VISIBLE);
        deviceAddressList.clear();
        foundDevicesArrayAdapter.clear();
        scanLeDevice(true);
    }

    private OnItemClickListener foundDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            scanLeDevice(false);
            String address = deviceAddressList.get(arg2);
            Intent intent = new Intent();
            intent.putExtra(BTAction.EXTRA_DEVICE_ADDRESS, address);
            setResult(AppConstat.BT_FOUND_RESULT_CODE, intent);
            finish();
        }
    };

    @SuppressWarnings("deprecation")
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi,
                             byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String name = device.getName();
                    String address = device.getAddress();
                    if (name != null && !deviceAddressList.contains(address)) {
                        foundDevicesArrayAdapter.add(name);
                        deviceAddressList.add(address);
                    }
                }
            });
        }
    };
}
