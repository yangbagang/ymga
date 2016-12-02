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

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;

/**
 * @author 杨拔纲
 *
 */
public class LoginActivity extends Activity {
	
	private YbgApp ybgApp = YbgApp.getInstance();
	
	private Button loginButton = null;

	private LoginReceiver loginReceiver;

	private Intent loginIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);
		setProgressBarIndeterminateVisibility(false);

		// 动态注册广播接收器
		loginReceiver = new LoginReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UserAction.USER_LOGIN);
		registerReceiver(loginReceiver, intentFilter);

		final EditText userName = (EditText) findViewById(R.id.login_user_name);
		final EditText userPwd = (EditText) findViewById(R.id.login_user_pwd);

		ImageView resetUserNameImage = (ImageView) findViewById(R.id.resetLoginUserName);
		resetUserNameImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userName.setText("");
			}

		});
		ImageView resetUserPwdImage = (ImageView) findViewById(R.id.resetLoginUserPwd);
		resetUserPwdImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				userPwd.setText("");
			}

		});

		loginButton = (Button) findViewById(R.id.login_user_login);
		Button registerButton = (Button) findViewById(R.id.login_user_register);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 开始登录过程
				loginIntent = new Intent(LoginActivity.this, LoginService.class);
				loginIntent.putExtra("userName", userName.getText().toString());
				loginIntent.putExtra("userPwd", userPwd.getText().toString());
				loginIntent.putExtra("os", ybgApp.getSysName());
				loginIntent.putExtra("osVersion", ybgApp.getSysVersion());
				loginIntent.putExtra("appVersion", ybgApp.getAppVersion(getApplicationContext()));
				loginIntent.putExtra("brand", ybgApp.getBrandInfo());
				loginIntent.putExtra("model", ybgApp.getModelInfo());
				loginIntent.putExtra("imei", ybgApp.getImeiNo(getApplicationContext()));
				startService(loginIntent);
				v.setClickable(false);
				setProgressBarIndeterminateVisibility(true);
			}

		});
		registerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 转向注册Activity
				Intent intent = new Intent(LoginActivity.this,
						RegisterActivity.class);
				startActivity(intent);
				finish();
			}

		});
	}

	@Override
	protected void onDestroy() {
		// 停止服务
		if(loginIntent != null) {
			stopService(loginIntent);
		}
		// 注销广播
		unregisterReceiver(loginReceiver);
		super.onDestroy();
	}

	private class LoginReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// 登录结果处理
			String loginResult = intent.getExtras().getString("loginResult");
			if ("ok".equals(loginResult)) {
				ybgApp.showMessage(getApplicationContext(), "登录成功");
				finish();
			} else {
				String msg = intent.getExtras().getString("msg");
				setProgressBarIndeterminateVisibility(false);
				loginButton.setClickable(true);
				ybgApp.showMessage(getApplicationContext(), msg);
			}
		}

	}
	
}
