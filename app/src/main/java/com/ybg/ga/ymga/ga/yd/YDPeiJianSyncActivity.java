/**
 * 
 */
package com.ybg.ga.ymga.ga.yd;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.ga.yd.jStyle.JStyleCmd;
import com.ybg.ga.ymga.util.BCDUtil;
import com.ybg.ga.ymga.util.ByteUtil;
import com.ybg.ga.ymga.util.TimeUtil;

/**
 * @author 杨拔纲
 * 
 */
public class YDPeiJianSyncActivity extends AppCompatActivity {

	private YdPreference ydPreference = YdPreference.getInstance();
	private YbgApp ybgApp = YbgApp.getInstance();
	private Intent syncIntent = null;

	private boolean hasRegisterReceiver = false;
	private byte dayIndex = 30;

	// 使用进度条提示当前正在读取数据
	private ProgressDialog readProgressDialog = null;
	private YDDataService ydDataService = null;
	private Intent bindIntent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.yd_pj_sync);
		ydPreference.setPjMethod();

		Toolbar toolbar = (Toolbar) findViewById(R.id.ydPjSyncToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//toolbar.setNavigationIcon();
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		/** 启动后台Service **/
		startService();

		// bind service
		bindIntent = new Intent(YDPeiJianSyncActivity.this, YDDataService.class);
		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		unbindService(mConnection);
		stopService();
		super.onDestroy();
	}

	private void startSync() {
		// 启动进度条
		readProgressDialog = new ProgressDialog(this);
		readProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		readProgressDialog.setTitle("正在读取");
		readProgressDialog.setMessage("开始查询数据，请稍等片刻...");
		readProgressDialog.setMax(95);
		readProgressDialog.setProgress(0);
		readProgressDialog.setIndeterminate(false);
		readProgressDialog.setCancelable(true);
		readProgressDialog.show();
		// 尝试启动设备并获取数据
		startSyncData();
	}

	private void startService() {
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
			// 注册广播
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(BTAction.getSendInfoAction(BTPrefix.YD));
			intentFilter.addAction(BTAction.getSendErrorAction(BTPrefix.YD));
			intentFilter.addAction(BTAction.getSendAnswerAction(BTPrefix.YD));
			registerReceiver(ydSyncBroadcastReceiver, intentFilter);
			hasRegisterReceiver = true;
		}
	}

	private void stopService() {
		// 停止接收广播
		if (hasRegisterReceiver) {
			unregisterReceiver(ydSyncBroadcastReceiver);
		}
	}

	private void startSyncData() {
		moveToNextDay();
	}

	private BroadcastReceiver ydSyncBroadcastReceiver = new BroadcastReceiver() {

		@SuppressLint("SimpleDateFormat")
		private SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equalsIgnoreCase(BTAction
					.getSendErrorAction(BTPrefix.YD))) {
				String info = intent.getExtras().getString(BTAction.INFO);
				ybgApp.showMessage(getApplicationContext(), info);
				if (readProgressDialog != null) {
					readProgressDialog.dismiss();
				}
			} else if (action.equalsIgnoreCase(BTAction
					.getSendInfoAction(BTPrefix.YD))) {
				String info = intent.getExtras().getString(BTAction.INFO);
				ybgApp.showMessage(getApplicationContext(), info);
			} else if (action.equalsIgnoreCase(BTAction
					.getSendAnswerAction(BTPrefix.YD))) {
				// 执行的命令有了返馈
				byte[] ack = intent.getExtras().getByteArray(BTAction.ACK);
				// 返馈处理
				if (ack == null || ack.length < 2) {
					ybgApp.showMessage(getApplicationContext(), "返回数据异常。");
				} else {
					int c = ByteUtil.byteToInt(ack[0]);
					if (c == 0x43) {
						// 收到查询反馈
						int hasData = 255 & ack[1];
						if (hasData == 0xff) {
							// 此日无数据
							moveToNextDay();
						} else {
							// 读取数据
							readAckData(ack);
						}
					} else {
						// 出错
						moveToNextDay();
					}
				}
			}
		}

	};

	private void moveToNextDay() {
		moveToNotSyncDate();
		// 如果己经同步完成，则退出，恢复初始状态。
		if (dayIndex == -1) {
			// 己经到了最后一天
			// 关闭进度条
			if (readProgressDialog != null) {
				readProgressDialog.dismiss();
			}
			dayIndex = 30;
			// 同步完成，自动退出。
			ybgApp.showMessage(getApplicationContext(), "数据读取完成。");
			finish();
		} else {
			// 发送查询指令
			syncIntent = new Intent(BTAction.getSendCmdAction(BTPrefix.YD));
			byte[] syncCmd = JStyleCmd.getReadDetailCmd(dayIndex);
			syncIntent.putExtra(BTAction.CMD, syncCmd);
			syncIntent.putExtra("isRead", false);
			sendBroadcast(syncIntent);
		}
	}

	private void moveToNotSyncDate() {
		while (dayIndex >= 0) {
			dayIndex--;
			if (!ydPreference.hasSync(dayIndex))
				break;
		}
	}

	private void readAckData(byte[] ack) {
		// 时间解析有误，注解
		int year = Integer.parseInt(BCDUtil.bcd2Str(new byte[]{ack[2]})) + 2000;//年份
		int month = Integer.parseInt(BCDUtil.bcd2Str(new byte[]{ack[3]}));//月份
		int day = Integer.parseInt(BCDUtil.bcd2Str(new byte[]{ack[4]}));//天数
		//System.out.println(year + "-" + month + "-" + day);
		int timeIndex = ack[5];// 时间索引，15分钟一个刻度。
		boolean isActivity = ack[6] == 0;// ==0x00运动数据,0xff睡眠
		//String syncDate = TimeUtil.getDateByOffset(dayIndex);
		StringBuffer sb = new StringBuffer();
		sb.append(year);
		sb.append("-");
		if (month > 9) {
			sb.append(month);
		} else {
			sb.append("0" + month);
		}
		sb.append("-");
		if (day > 9) {
			sb.append(day);
		} else {
			sb.append("0" + day);
		}
		String syncDate = sb.toString();
		System.out.println("syncDate=" + syncDate);
		if (isActivity) {
			// 运动数据
			int steps = 256 * (255 & ack[10]) + (255 & ack[9]);
			float calories = (256 * (255 & ack[8]) + (255 & ack[7])) * 0.01f;
			float distance = (256 * (255 & ack[12]) + (255 & ack[11])) * 0.01f;
			ydDataService.saveSyncActivity(syncDate, timeIndex, steps,
					distance, calories);
		} else {
			byte sm1 = ack[7];// 第1个2分钟
			byte sm2 = ack[8];// 第2个2分钟
			byte sm3 = ack[9];// 第3个2分钟
			byte sm4 = ack[10];// 第4个2分钟
			byte sm5 = ack[11];// 第5个2分钟
			byte sm6 = ack[12];// 第6个2分钟
			byte sm7 = ack[13];// 第7个2分钟
			byte sm8 = ack[14];// 第8个2分钟
			ydDataService.saveSyncSleep(syncDate, timeIndex, sm1, sm2, sm3,
					sm4, sm5, sm6, sm7, sm8);
		}

		readProgressDialog.setMessage("正在读取" + syncDate + "数据...");
		readProgressDialog.setProgress(timeIndex);
		if (timeIndex == 95) {
			// 该日最后一条记录
			ydDataService.updateSyncData(syncDate);
			if (dayIndex > 0) {
				// 不记录当天的同步
				ydPreference.setLastPeiJianSyncDate(syncDate);
			} else {
				// 当天记录需要在下次同步时更新
				ydPreference.setNeedUpdateDay(syncDate);
			}
			moveToNextDay();
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ydDataService = ((YDDataService.YDDataBinder) service).getService();
			// 开始同步数据
			startSync();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			ydDataService = null;
		}

	};
}
