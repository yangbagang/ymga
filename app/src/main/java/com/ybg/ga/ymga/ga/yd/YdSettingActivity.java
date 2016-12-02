/**
 * 
 */
package com.ybg.ga.ymga.ga.yd;

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
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 * 
 */
public class YdSettingActivity extends AppCompatActivity {

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
		setContentView(R.layout.yd_setting);

		initView();

		Toolbar toolbar = (Toolbar) findViewById(R.id.ydSettingToolbar);
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
				new DatePickerDialog(YdSettingActivity.this, userBirthListener,
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
		userBodyWeight.setText("" + userPreference.getBodyWeight());
		stepLength.setText("" + ydPreference.getStepLength());
		sportAim.setText("" + ydPreference.getAimSteps());
	}

	public void saveUserSetting(View view) {

		try {
			float userBodyHighValue = Float.valueOf(userBodyHigh.getText()
					.toString());
			float userBodyWeightValue = Float.valueOf(userBodyWeight.getText()
					.toString());
			float stepLengthValue = Float.valueOf(stepLength.getText()
					.toString());
			int sportAimValue = Integer.valueOf(sportAim.getText().toString());

			userPreference.setBodyHigh(userBodyHighValue);
			userPreference.setBodyWeight(userBodyWeightValue);
			ydPreference.setStepLength(stepLengthValue);
			ydPreference.setAimSteps(sportAimValue);

			ybgApp.showMessage(getApplicationContext(), "设置己保存！");
			finish();
		} catch (NumberFormatException e) {
			ybgApp.showMessage(getApplicationContext(), "身高、体重、步长、运动目标都必需是数字！");
		}
	}

}
