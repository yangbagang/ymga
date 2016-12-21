/**
 * 
 */
package com.ybg.ga.ymga.user;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 * 
 */
public class UserSettingActivity extends AppCompatActivity {

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private UserPreferences userPreference = UserPreferences.getInstance();
	private YbgApp ybgApp = YbgApp.getInstance();

	private Intent bindIntent = null;
	private UserSettingService userSettingService = null;

	private EditText userBirthView = null;
	private RadioButton userSexMale = null;
	private RadioButton userSexFemale = null;
	private EditText userNickName = null;
	private EditText userBodyHigh = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_setting);

		Toolbar toolbar = (Toolbar) findViewById(R.id.userSettingToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		initView();

		bindIntent = new Intent(UserSettingActivity.this,
				UserSettingService.class);
		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BTAction.getSendInfoAction(BTPrefix.USER));
		registerReceiver(broadcastReceiver, intentFilter);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		unbindService(mConnection);
		super.onDestroy();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			userSettingService = ((UserSettingService.UserSettingBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			userSettingService = null;
		}

	};

	private void initView() {
		userBirthView = (EditText) findViewById(R.id.userBirthday);
		userSexMale = (RadioButton) findViewById(R.id.userSexMale);
		userSexFemale = (RadioButton) findViewById(R.id.userSexFemale);
		userNickName = (EditText) findViewById(R.id.userNickName);
		userBodyHigh = (EditText) findViewById(R.id.userBodyHigh);

		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		String birthday = userPreference.getBirthday();
		if ("".equals(birthday)) {
			Date now = calendar.getTime();
			userBirthView.setText(sdf.format(now));
			userPreference.setBirthday(sdf.format(now));
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
				new DatePickerDialog(UserSettingActivity.this,
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

		userNickName.setText(userPreference.getNickName());
		userBodyHigh.setText("" + userPreference.getBodyHigh());
	}

	public void saveUserSetting(View view) {
		String userNickNameValue = userNickName.getText().toString();
		if (userNickNameValue == null || "".equals(userNickNameValue)) {
			ybgApp.showMessage(getApplicationContext(), "呢称不能为空！");
			return;
		}
		userPreference.setNickName(userNickNameValue);

		try {
			float userBodyHighValue = Float.valueOf(userBodyHigh.getText()
					.toString());
			if (userBodyHighValue < 0.1 || userBodyHighValue > 3) {
				ybgApp.showMessage(getApplicationContext(), "身高不是有效数据！");
				return;
			}
			userPreference.setBodyHigh(userBodyHighValue);
			// 上传设置至服务器
			if (userPreference.hasLogin()) {
				userSettingService.saveSetting();
			} else {
				ybgApp.showMessage(getApplicationContext(), "设置己保存！");
			}
		} catch (NumberFormatException e) {
			ybgApp.showMessage(getApplicationContext(), "身高不是有效数据！");
		}
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BTAction.getSendInfoAction(BTPrefix.USER).equals(action)) {
				String info = intent.getExtras().getString(BTAction.INFO);
				if (!"ok".equals(info)) {
					// 发送失败，显示提示信息。
					ybgApp.showMessage(getApplicationContext(), info);
				} else {
					// 发送成功，刷新列表
					ybgApp.showMessage(getApplicationContext(), "设置成功！");
					finish();
				}
			}
		}

	};
}
