/**
 * 
 */
package com.ybg.ga.ymga.user;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;

/**
 * @author 杨拔纲
 *
 */
public class RegisterActivity extends Activity {
	
	private YbgApp ybgApp = YbgApp.getInstance();
	
	private Button registerButton = null;

	private RegisterReceiver registerReceiver;
	
	private String userType = "1";

	private Intent registerIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_register);
		setProgressBarIndeterminateVisibility(false);
		
		// 动态注册广播接收器
		registerReceiver = new RegisterReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UserAction.USER_REGISTER);
		registerReceiver(registerReceiver, intentFilter);
		
		final EditText userName = (EditText) findViewById(R.id.register_user_name);
		final EditText userPwd = (EditText) findViewById(R.id.register_user_pwd);
		
		ImageView resetUserNameImage = (ImageView) findViewById(R.id.resetRegisterUserName);
		resetUserNameImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userName.setText("");
			}
			
		});
		ImageView resetUserPwdImage = (ImageView) findViewById(R.id.resetRegisterUserPwd);
		resetUserPwdImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userPwd.setText("");
			}
			
		});
		
		Button loginButton = (Button) findViewById(R.id.register_user_login);
		registerButton = (Button) findViewById(R.id.register_user_register);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 转向登录
				Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();
			}
			
		});
		registerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 开始注册过程
				registerIntent = new Intent(RegisterActivity.this, RegisterService.class);
				registerIntent.putExtra("userName", userName.getText().toString());
				registerIntent.putExtra("userPwd", userPwd.getText().toString());
				registerIntent.putExtra("userType", userType);
				registerIntent.putExtra("os", ybgApp.getSysName());
				registerIntent.putExtra("osVersion", ybgApp.getSysVersion());
				registerIntent.putExtra("appVersion", ybgApp.getAppVersion(getApplicationContext()));
				registerIntent.putExtra("brand", ybgApp.getBrandInfo());
				registerIntent.putExtra("model", ybgApp.getModelInfo());
				registerIntent.putExtra("imei", ybgApp.getImeiNo(getApplicationContext()));
				startService(registerIntent);
				v.setClickable(false);
				setProgressBarIndeterminateVisibility(true);
			}
			
		});
		
		final RadioButton userTypePerson = (RadioButton) findViewById(R.id.userTypePerson);
		final RadioButton userTypeEnterPrise = (RadioButton) findViewById(R.id.userTypeEnterPrise);
		userTypePerson.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userTypePerson.setChecked(true);
				userTypeEnterPrise.setChecked(false);
				userType = "1";
			}

		});
		userTypeEnterPrise.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userTypePerson.setChecked(false);
				userTypeEnterPrise.setChecked(true);
				userType = "0";
			}

		});
	}
	
	@Override
	protected void onDestroy() {
		// 停止服务
		if(registerIntent != null) {
			stopService(registerIntent);
		}
		// 注销广播
		unregisterReceiver(registerReceiver);
		super.onDestroy();
	}

	private class RegisterReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 登录结果处理
			String registerResult = intent.getExtras().getString("registerResult");
			if ("ok".equals(registerResult)) {
				ybgApp.showMessage(getApplicationContext(), "注册成功");
				finish();
			} else {
				String msg = intent.getExtras().getString("msg");
				setProgressBarIndeterminateVisibility(false);
				registerButton.setClickable(true);
				ybgApp.showMessage(getApplicationContext(), msg);
			}
		}

	}
}
