/**
 * 
 */
package com.ybg.ga.ymga.ga.yd.jStyle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 * 
 */
public class JStyleSettingActivity extends AppCompatActivity {

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private UserPreferences userPreference = UserPreferences.getInstance();
	private YdPreference ydPreference = YdPreference.getInstance();
	private YbgApp ybgApp = YbgApp.getInstance();

	private EditText userBirthView = null;
	private RadioButton userSexMale = null;
	private RadioButton userSexFemale = null;
	private EditText userBodyHigh = null;
	private EditText userBodyWeight = null;
	private EditText stepLength = null;
	private EditText sportAim = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.style_setting);

		Toolbar toolbar = (Toolbar) findViewById(R.id.jstyleSettingToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		initView();
		// 注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BTAction.getSendErrorAction(BTPrefix.YD));
		intentFilter.addAction(BTAction.getSendAnswerAction(BTPrefix.YD));
		registerReceiver(ydSyncBroadcastReceiver, intentFilter);
	}

	@Override
	protected void onDestroy() {
		// 停止接收广播
		unregisterReceiver(ydSyncBroadcastReceiver);
		super.onDestroy();
	}

	private void initView() {
		userBirthView = (EditText) findViewById(R.id.userBirthday);
		userSexMale = (RadioButton) findViewById(R.id.userSexMale);
		userSexFemale = (RadioButton) findViewById(R.id.userSexFemale);
		userBodyHigh = (EditText) findViewById(R.id.userBodyHigh);
		userBodyWeight = (EditText) findViewById(R.id.userBodyWeight);
		stepLength = (EditText) findViewById(R.id.ydStepLength);
		sportAim = (EditText) findViewById(R.id.ydSportAim);

		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		String birthday = userPreference.getBirthday();
		if ("".equals(birthday)) {
			Date now = calendar.getTime();
			userBirthView.setText(sdf.format(now));
		} else {
			userBirthView.setText(birthday);
		}

		final DatePickerDialog.OnDateSetListener userBirthListener = new DatePickerDialog.OnDateSetListener() {

			@SuppressLint("DefaultLocale")
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				String b = String.format("%d-%02d-%02d", year, monthOfYear + 1,
						dayOfMonth);
				userBirthView.setText(b);
				userPreference.setBirthday(b);
			}

		};
		userBirthView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new DatePickerDialog(JStyleSettingActivity.this,
						userBirthListener, calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH), calendar
								.get(Calendar.DAY_OF_MONTH)).show();
			}

		});

		int sex = userPreference.getUserSex();
		if (sex == AppConstat.SEX_FEMALE) {
			userSexMale.setChecked(false);
			userSexFemale.setChecked(true);
		} else {
			userSexMale.setChecked(true);
			userSexFemale.setChecked(false);
		}

		userSexMale.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userSexMale.setSelected(true);
				userSexFemale.setSelected(false);

				userPreference.setUserSex(AppConstat.SEX_MALE);
			}

		});
		userSexFemale.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userSexMale.setSelected(false);
				userSexFemale.setSelected(true);

				userPreference.setUserSex(AppConstat.SEX_FEMALE);
			}

		});

		userBodyHigh.setText("" + userPreference.getBodyHigh());
		userBodyWeight.setText("" + userPreference.getBodyWeight());
		stepLength.setText("" + ydPreference.getStepLength());
		sportAim.setText("" + ydPreference.getAimSteps());
	}

	public void saveUserSetting(View view) {
		float userBodyHighValue = 0f;
		float userBodyWeightValue = 0f;
		float stepLengthValue = 0f;
		boolean isOK = true;

		try {
			userBodyHighValue = Float
					.valueOf(userBodyHigh.getText().toString());
			userBodyWeightValue = Float.valueOf(userBodyWeight.getText()
					.toString());
			stepLengthValue = Float.valueOf(stepLength.getText().toString());

			// 先进行常规保存
			userPreference.setBodyHigh(userBodyHighValue);
			userPreference.setBodyWeight(userBodyWeightValue);
			ydPreference.setStepLength(stepLengthValue);
		} catch (NumberFormatException e) {
			ybgApp.showMessage(getApplicationContext(), "身高、体重、步长、运动目标都必需是数字！");
			isOK = false;
		}

		if (isOK) {
			// 保存设置到手环
			// 设置个人信息
			int sex = userPreference.getUserSex();
			int age = userPreference.getAge();
			byte[] saveCmd = JStyleCmd.getPersonInfoCmd(sex, age,
					userBodyHighValue, userBodyWeightValue, stepLengthValue);
			Intent intent = new Intent(BTAction.getSendCmdAction(BTPrefix.YD));
			intent.putExtra(BTAction.CMD, saveCmd);
			intent.putExtra("isRead", false);
			sendBroadcast(intent);
		}
	}

	public void saveSportAim(View view) {
		int sportAimValue = 0;
		boolean isOK = true;

		try {
			sportAimValue = Integer.valueOf(sportAim.getText().toString());

			// 先进行常规保存
			ydPreference.setAimSteps(sportAimValue);
		} catch (NumberFormatException e) {
			ybgApp.showMessage(getApplicationContext(), "运动目标都必需是数字！");
			isOK = false;
		}

		if (isOK) {
			// 设置目标
			byte[] aimCmd = JStyleCmd.getAimCmd(sportAimValue, 0);
			System.out.println("aim=" + sportAimValue);
			for (byte a : aimCmd) {
				System.out.print(a + ",");
			}
			System.out.println("aim=" + sportAimValue);
			Intent intent = new Intent(BTAction.getSendCmdAction(BTPrefix.YD));
			intent.putExtra(BTAction.CMD, aimCmd);
			intent.putExtra("isRead", false);
			sendBroadcast(intent);
		}
	}

	public void syncTime(View view) {
		// 同步时间
		byte[] syncCmd = JStyleCmd.getSyncTimeCmd();
		Intent intent = new Intent(BTAction.getSendCmdAction(BTPrefix.YD));
		intent.putExtra(BTAction.CMD, syncCmd);
		intent.putExtra("isRead", false);
		sendBroadcast(intent);
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
			} else if (action.equalsIgnoreCase(BTAction
					.getSendAnswerAction(BTPrefix.YD))) {
				// 执行的命令有了返馈
				byte[] ack = intent.getExtras().getByteArray(BTAction.ACK);
				// 返馈处理
				if (ack == null || ack.length < 2) {
					ybgApp.showMessage(getApplicationContext(), "返回数据异常。");
				} else {
					byte b = ack[0];
					if (b == 0x01) {
						ybgApp.showMessage(getApplicationContext(), "同步时间完成。");
					} else if (b == 0x02) {
						ybgApp.showMessage(getApplicationContext(), "个人信息设置完成。");
					} else if (b == 0x0b) {
						ybgApp.showMessage(getApplicationContext(), "运动目标设置完成。");
					}
				}
			}
		}

	};
}
