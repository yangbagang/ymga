/**
 * 
 */
package com.ybg.ga.ymga.ga.tz;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 * 
 */
public class TZSettingActivity extends AppCompatActivity {

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private UserPreferences userPreference = UserPreferences.getInstance();
	private YbgApp ybgApp = YbgApp.getInstance();

	private EditText userBirthView = null;
	private RadioButton userSexMale = null;
	private RadioButton userSexFemale = null;
	private EditText userBodyHigh = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tz_setting);

		initView();

		Toolbar toolbar = (Toolbar) findViewById(R.id.tzSettingToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		userBirthView = (EditText) findViewById(R.id.userBirthday);
		userSexMale = (RadioButton) findViewById(R.id.userSexMale);
		userSexFemale = (RadioButton) findViewById(R.id.userSexFemale);
		userBodyHigh = (EditText) findViewById(R.id.userBodyHigh);

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
				new DatePickerDialog(TZSettingActivity.this, userBirthListener,
						calendar.get(Calendar.YEAR), calendar
								.get(Calendar.MONTH), calendar
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
	}

	public void saveUserSetting(View view) {
		try {
			float userBodyHighValue = Float.valueOf(userBodyHigh.getText()
					.toString());
			if (userBodyHighValue < 0.1 || userBodyHighValue > 3) {
				ybgApp.showMessage(getApplicationContext(), "身高不是有效数据！");
				return;
			}
			userPreference.setBodyHigh(userBodyHighValue);
			ybgApp.showMessage(getApplicationContext(), "设置己保存！");
			finish();
		} catch (NumberFormatException e) {
			ybgApp.showMessage(getApplicationContext(), "身高不是有效数据！");
		}
	}

}
