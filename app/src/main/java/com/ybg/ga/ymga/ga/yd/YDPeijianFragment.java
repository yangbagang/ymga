/**
 * 
 */
package com.ybg.ga.ymga.ga.yd;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BLEDeviceListActivity;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.bt.BTStatus;
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.ga.yd.jStyle.JStyleService;
import com.ybg.ga.ymga.ga.yd.jStyle.JStyleSettingActivity;
import com.ybg.ga.ymga.ui.RoundProgressBar;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * 配件运动-实时计步模式。手环的计步实时反馈到手机进行显示计算。
 * 
 * @author 杨拔纲
 * 
 */
public class YDPeijianFragment extends Fragment {

	private YdPreference ydPreference = YdPreference.getInstance();
	private YbgApp ybgApp = YbgApp.getInstance();
	private Intent ydSyncServiceIntent = null;

	private TextView ydPJName = null;
	private Button ydPJOperator = null;
	private Button pjSetingButton = null;
	private Button ydSyncButton = null;
	private TextView sync_status_num1 = null;
	private TextView sync_status_num2 = null;
	private TextView sync_status_num3 = null;
	private LinearLayout finishRingLayout = null;
	private RoundProgressBar completeProgressBar = null;
	private TextView ydPJDistanceValue = null;
	private TextView ydPJStepValue = null;
	private TextView ydPJCaloriesValue = null;

	private boolean hasRegisterReceiver = false;
	private boolean isRunning = false;
	private boolean beHibernate = false;
	private boolean isConnected = false;
	private int aimSteps = 10000;

	private YDDataService ydDataService = null;
	private Intent bindIntent = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/** 启动后台Service **/
		startService();
		bindIntent = new Intent(getActivity(), YDDataService.class);
		getActivity().getApplication().bindService(bindIntent, mConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.yd_pj_main, container, false);
		/** 尝试初始化视图实例 **/
		initView(view);
		/** 初始化按钮事件 **/
		initEvent();
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onDestroy() {
		stopService();
		getActivity().getApplication().unbindService(mConnection);
		super.onDestroy();
	}

	private void searchData() {
		// 开始查询相关数据
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
				Locale.getDefault());
		String today = sdf.format(new Date());
		Cursor cursor = ydDataService.listActivitySum(today, today);
		if (cursor.moveToFirst()) {
			int steps = cursor.getInt(1);
			float distance = cursor.getFloat(2);
			float colraris = cursor.getFloat(3);
			if (completeProgressBar != null) {
				completeProgressBar.setProgress(steps);
			}
			sync_status_num2.setText("" + steps + "步");
			int percent = steps * 100 / aimSteps;
			sync_status_num3.setText("" + percent + "%");
			ydPJDistanceValue.setText(new DecimalFormat("#.##")
					.format(distance));
			ydPJStepValue.setText("" + steps);
			ydPJCaloriesValue
					.setText(new DecimalFormat("#.#").format(colraris));
		}
		// 查询最高记录
		ydDataService.getMaxSteps();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			ydDataService = ((YDDataService.YDDataBinder) service).getService();
			drawRoundProgress();
			searchData();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			ydDataService = null;
		}

	};

	private void initEvent() {
		if (ydPreference.hasAssign()) {
			// 己绑定
			if (isConnected) {
				// 己连接，显示设备名称。准备启动实时计步
				ydPJName.setText(ydPreference.getYdDeviceName());
				ydPJOperator
						.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
			} else {
				// 未连接，显示设备名称。准备连接设备
				ydPJName.setText(ydPreference.getYdDeviceName());
				ydPJOperator
						.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_CONNECT]);
			}
		} else {
			// 未绑定，提示需要绑定。准备进行设备绑定操作。
			ydPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_ASSIGN]);
			ydPJOperator
					.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN]);
		}
		ydPJOperator.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ydOperation(v);
			}

		});
	}

	private void ydOperation(View view) {
		String operator = ((Button) view).getText().toString();
		if (BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN].equals(operator)) {
			// 还未绑定，开始绑定过程
			Intent intent = new Intent(getActivity(),
					YdDeviceListActivity.class);
			getActivity().getParent().startActivityForResult(intent,
					AppConstat.YD_DEVICE_REQUEST_CODE);
		} else if (BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_CONNECT]
				.equals(operator)) {
			// 尝试启动设备并获取数据
			startConnect();
			// 禁用此按钮，避免重复启动
			view.setEnabled(false);
			// 设置标签文字
			ydPJName.setText("正在连接" + ydPreference.getYdDeviceName());
		} else if (BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]
				.equals(operator)) {
			// 尝试启动设备并获取数据
			startPedometer();
			// 禁用此按钮，避免重复启动
			view.setEnabled(false);
			// 修改标签
			ydPJName.setText("正在进入实时模式" + ydPreference.getYdDeviceName());
		} else if (BTStatus.BT_BUTTONS[BTStatus.BT_STATU_STOP].equals(operator)) {
			// 尝试启动设备并获取数据
			stopPedometer();
			// 禁用此按钮，避免重复启动
			view.setEnabled(false);
			// 修改标签
			ydPJName.setText("正在停止实时模式" + ydPreference.getYdDeviceName());
		}
	}

	private void initView(View view) {
		aimSteps = ydPreference.getAimSteps();
		// 操作按钮
		ydPJName = (TextView) view.findViewById(R.id.ydPJName);
		ydPJOperator = (Button) view.findViewById(R.id.ydPJOperator);
		pjSetingButton = (Button) view.findViewById(R.id.pjSetingButton);
		ydSyncButton = (Button) view.findViewById(R.id.ydSyncButton);
		// 计步数据
		sync_status_num1 = (TextView) view.findViewById(R.id.sync_status_num1);
		sync_status_num2 = (TextView) view.findViewById(R.id.sync_status_num2);
		sync_status_num3 = (TextView) view.findViewById(R.id.sync_status_num3);
		finishRingLayout = (LinearLayout) view
				.findViewById(R.id.finishRingLayout);
		ydPJDistanceValue = (TextView) view
				.findViewById(R.id.ydPJDistanceValue);
		ydPJStepValue = (TextView) view.findViewById(R.id.ydPJStepValue);
		ydPJCaloriesValue = (TextView) view
				.findViewById(R.id.ydPJCaloriesValue);
		pjSetingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),
						JStyleSettingActivity.class);
				startActivity(intent);
			}

		});
		ydSyncButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 进入同步模式同步数据
				Intent intent = new Intent(getActivity(),
						YDPeiJianSyncActivity.class);
				startActivity(intent);
			}

		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == AppConstat.YD_DEVICE_REQUEST_CODE
				&& resultCode == AppConstat.YD_DEVICE_RESULT_CODE) {
			// 运动设备列表
			String ydDeviceName = data.getExtras().getString("ydDeviceName");
			String ydDeviceModel = data.getExtras().getString("ydDeviceModel");
			if (!"".equals(ydDeviceName) && !"".equals(ydDeviceModel)) {
				// 记录下设备名称及代号
				ydPreference.setYdDeviceName(ydDeviceName);
				ydPreference.setYdDeviceModel(ydDeviceModel);
				ydPJName.setText(ydDeviceName);
				// 开始扫描蓝牙设备
				Intent intent = new Intent(getActivity(),
						BLEDeviceListActivity.class);
				getActivity().getParent().startActivityForResult(intent,
						AppConstat.BT_FOUND_REQUEST_CODE);
			}
		} else if (requestCode == AppConstat.BT_FOUND_REQUEST_CODE
				&& resultCode == AppConstat.BT_FOUND_RESULT_CODE) {
			// 取得需要连接的蓝牙地址
			String ydDeviceAddr = data.getExtras().getString(
					BTAction.EXTRA_DEVICE_ADDRESS);
			ydPreference.setYdDeviceAddr(ydDeviceAddr);
			// 修改状态，准备连接
			ydPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_CONNECT]);
			ydPJOperator
					.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_CONNECT]);
			// 开始尝试进行连接
			ydPJOperator.setEnabled(false);
			Intent connectIntent = new Intent(
					BTAction.getConnectAction(BTPrefix.YD));
			connectIntent.putExtra(BTAction.EXTRA_DEVICE_ADDRESS, ydDeviceAddr);
			getActivity().sendBroadcast(connectIntent);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startService() {
		// 开启蓝牙
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			ybgApp.showMessage(getActivity().getApplicationContext(),
					BTMessage.BLUETOOTH_ADAPTER_NOTFOUND);
		} else {
			// 如未开启，则先开启
			if (!bluetoothAdapter.isEnabled()) {
				bluetoothAdapter.enable();
			}
			// 开启后台程序
			ydSyncServiceIntent = new Intent(getActivity(), JStyleService.class);
			getActivity().startService(ydSyncServiceIntent);

			// 注册广播
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(BTAction.getSendInfoAction(BTPrefix.YD));
			intentFilter.addAction(BTAction.getSendErrorAction(BTPrefix.YD));
			intentFilter.addAction(BTAction.getConnectedSuccess(BTPrefix.YD));
			intentFilter.addAction(BTAction.getDisConnected(BTPrefix.YD));
			intentFilter.addAction(BTAction.getSendAnswerAction(BTPrefix.YD));
			intentFilter.addAction("YD_MAXSTEPS");
			getActivity().registerReceiver(ydSyncBroadcastReceiver,
					intentFilter);
			hasRegisterReceiver = true;
		}
	}

	private void stopService() {
		// 停止
		if (isRunning) {
			stopPedometer();
			isRunning = false;
		}
		if (null != ydSyncServiceIntent && !beHibernate) {
			// 如不是切换到同步模式，则停止后台线程。
			getActivity().stopService(ydSyncServiceIntent);
		}
		// 停止接收广播
		if (hasRegisterReceiver) {
			getActivity().unregisterReceiver(ydSyncBroadcastReceiver);
		}
	}

	private void startConnect() {
		// 发送连接指令
		Intent startSyncIntent = new Intent(
				BTAction.getConnectAction(BTPrefix.YD));
		startSyncIntent.putExtra(BTAction.EXTRA_DEVICE_ADDRESS,
				ydPreference.getYdDeviceAddr());
		getActivity().sendBroadcast(startSyncIntent);
	}

	private void startPedometer() {
		// 发送实时计步指令，进入实时计步模式
		Intent startIntent = new Intent(BTAction.getSendCmdAction(BTPrefix.YD));
		byte[] startCmd = { 0x09, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00,
				00, 00, 00, 0x09 };
		startIntent.putExtra(BTAction.CMD, startCmd);
		startIntent.putExtra("isRead", false);
		getActivity().sendBroadcast(startIntent);
		isRunning = true;
	}

	private void stopPedometer() {
		// 发送停止指令，停止实时模式
		Intent stopIntent = new Intent(BTAction.getSendCmdAction(BTPrefix.YD));
		byte[] stopCmd = { 0x10, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00,
				00, 00, 00, 0x10 };
		stopIntent.putExtra(BTAction.CMD, stopCmd);
		stopIntent.putExtra("isRead", false);
		getActivity().sendBroadcast(stopIntent);
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
				// 接收到错误提示
				String info = intent.getExtras().getString(BTAction.INFO);
				ybgApp.showMessage(getActivity().getApplicationContext(), info);
				ydPJOperator.setEnabled(true);
			} else if (action.equalsIgnoreCase(BTAction
					.getSendInfoAction(BTPrefix.YD))) {
				// 接收到信息提示
				String info = intent.getExtras().getString(BTAction.INFO);
				ybgApp.showMessage(getActivity().getApplicationContext(), info);
			} else if (action.equalsIgnoreCase(BTAction
					.getSendAnswerAction(BTPrefix.YD))) {
				// 执行的命令有了返馈
				byte[] ack = intent.getExtras().getByteArray(BTAction.ACK);
				// 返馈处理
				if (ack == null || ack.length < 2) {
					ybgApp.showMessage(getActivity().getApplicationContext(),
							"返回数据异常。");
				} else {
					if (ack[0] == 0x09) {
						// 接收到实时计步反馈
						int steps = (256 * (255 & ack[1])
								+ (256 * (255 & ack[2])) + ((255 & ack[3])));
						float colraris = ((256 * 256 * (255 & ack[7]))
								+ (256 * (255 & ack[8])) + (255 & ack[9])) * 0.01f;
						float distance = ((256 * 256 * (255 & ack[10]))
								+ (256 * (255 & ack[11])) + (255 & ack[12])) * 0.01f;
						// 显示数据
						if (completeProgressBar != null) {
							completeProgressBar.setProgress(steps);
						}
						sync_status_num2.setText("" + steps + "步");
						int percent = steps * 100 / aimSteps;
						sync_status_num3.setText("" + percent + "%");
						ydPJDistanceValue.setText(new DecimalFormat("#.##")
								.format(distance));
						ydPJStepValue.setText("" + steps);
						ydPJCaloriesValue.setText(new DecimalFormat("#.#")
								.format(colraris));
						// 修改标签及按钮
						ydPJName.setText(ydPreference.getYdDeviceName()
								+ "进入实时计步模式");
						ydPJOperator
								.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_STOP]);
						ydPJOperator.setEnabled(true);
					} else if (ack[0] == 0x0A) {
						// 停止实时计步完成
						// 发送数据保存指令
						// 修改标签及按钮
						ydPJName.setText(ydPreference.getYdDeviceName()
								+ "实时计步模式停止");
						ydPJOperator
								.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
						ydPJOperator.setEnabled(true);
					}
				}
				// 启用按钮
				ydPJOperator.setEnabled(true);
			} else if (action.equalsIgnoreCase(BTAction
					.getConnectedSuccess(BTPrefix.YD))) {
				// 成功连接到指定目标
				if (!ydPreference.hasAssign()) {
					// 还未设置绑定状态，先设置绑定状态，避免重复动作
					ydPreference.setHasAssign(true);
				}
				ydPJName.setText(ydPreference.getYdDeviceName() + "己连接");
				ydPJOperator
						.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
				ydPJOperator.setEnabled(true);
				pjSetingButton.setEnabled(true);
				ydSyncButton.setEnabled(true);
				isConnected = true;
			} else if (action.equalsIgnoreCase(BTAction
					.getDisConnected(BTPrefix.YD))) {
				// 连接己断开
				ydPJName.setText(ydPreference.getYdDeviceName() + "连接己断开");
				ydPJOperator
						.setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_CONNECT]);
				ydPJOperator.setEnabled(true);
				pjSetingButton.setEnabled(false);
				ydSyncButton.setEnabled(false);
				isConnected = false;
			} else if ("YD_MAXSTEPS".equals(action)) {
				int maxSteps = intent.getExtras().getInt("maxSteps");
				sync_status_num1.setText("" + maxSteps + "步");
			}
		}

	};

	public void setActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResult(requestCode, resultCode, data);
	}

	private void drawRoundProgress() {
		// 实例化圆形进度条
		completeProgressBar = new RoundProgressBar(getActivity()
				.getApplicationContext());
		// 设置颜色
		completeProgressBar.setCricleColor(0xfff5bd00);
		completeProgressBar.setCricleProgressColor(0xff58b334);
		// 设置圆环的宽度
		int maxWidth = Math.min(finishRingLayout.getMeasuredHeight(),
				finishRingLayout.getMeasuredWidth());
		completeProgressBar.setRoundWidth((float) (maxWidth * 0.1));
		// 设置最大进度
		completeProgressBar.setMax(aimSteps);
		// 设置显示中间的进度
		completeProgressBar.setTextIsDisplayable(false);
		// 设置空心进度的风格
		completeProgressBar.setStyle(0);

		finishRingLayout.removeAllViews();
		finishRingLayout.addView(completeProgressBar);
	}

}
