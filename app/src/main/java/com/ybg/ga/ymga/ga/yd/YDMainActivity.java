/**
 * 
 */
package com.ybg.ga.ymga.ga.yd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.ga.activity.OnResultAvailableListener;
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 * 
 */
public class YDMainActivity extends AppCompatActivity implements OnResultAvailableListener {

	private YdPreference ydPreference = YdPreference.getInstance();
	private YbgApp ybgApp = YbgApp.getInstance();
	private YDPeijianFragment pjFragment = null;
	private YDGPSFragment gpsFragment = null;
	private LinearLayout yd_method_option = null;

	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.yd_main);

		toolbar = (Toolbar) findViewById(R.id.ydMainToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		// 初始化
		pjFragment = new YDPeijianFragment();
		gpsFragment = new YDGPSFragment();
		// 运动方式切换
		yd_method_option = (LinearLayout) findViewById(R.id.yd_method_option);

		// 自动进入上次选择的运动方式，默认进入GPS计步方式
		changeFragmentContent();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void changeFragmentContent() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		if (ydPreference.isPjYd()) {
			toolbar.setTitle(R.string.yd_pj_title);
			transaction.replace(R.id.yd_content, pjFragment);
			String message = getString(R.string.permission_request_notice, getString(R.string
					.app_name), getString(R.string.permission_access_coarse_location));
			ybgApp.checkPermission(YDMainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION,
					message, AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION);
		} else {
			toolbar.setTitle(R.string.yd_gps_title);
			transaction.replace(R.id.yd_content, gpsFragment);
			String message = getString(R.string.permission_request_notice, getString(R.string
					.app_name), getString(R.string.permission_read_phone_state));
			ybgApp.checkPermission(YDMainActivity.this, Manifest.permission.READ_PHONE_STATE,
					message, AppConstat.PERMISSION_REQUEST_CODE_READ_PHONE_STATE);
		}
		transaction.commit();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == AppConstat.PERMISSION_REQUEST_CODE_READ_PHONE_STATE) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//TODO
			} else {
				String message1 = getString(R.string.permission_request_notice, getString(R.string
						.app_name), getString(R.string.permission_read_phone_state));
				String message2 = getString(R.string.permission_setting_notice, getString(R.string
						.app_name));
				ybgApp.showMessage(getApplicationContext(), message1 + message2);
			}
		} else if (requestCode == AppConstat.PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//TODO
			} else {
				String message1 = getString(R.string.permission_request_notice, getString(R.string
						.app_name), getString(R.string.permission_access_coarse_location));
				String message2 = getString(R.string.permission_setting_notice, getString(R.string
						.app_name));
				ybgApp.showMessage(getApplicationContext(), message1 + message2);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.yd_gps_pedometer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_switch) {
			if (yd_method_option.getVisibility() == View.VISIBLE) {
				yd_method_option.setVisibility(View.GONE);
			} else {
				yd_method_option.setVisibility(View.VISIBLE);
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * 进入配件运动方式
	 * 
	 * @param view
	 */
	public void enterPjMethod(View view) {
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			ybgApp.showMessage(getApplicationContext(),
					"您的设备不支持BLE，请升级到android 4.3以上。");
		} else {
			yd_method_option.setVisibility(View.GONE);
			// 支持BLE才能进入配件模式
			ydPreference.setPjMethod();
			changeFragmentContent();
		}
	}

	/**
	 * 进入GPS计步运动方式
	 * 
	 * @param view
	 */
	public void enterGPSMethod(View view) {
		yd_method_option.setVisibility(View.GONE);
		ydPreference.setGPSMethod();
		changeFragmentContent();
	}

	/**
	 * 进入运动相关设置界面
	 * 
	 * @param view
	 */
	public void enterYdSetting(View view) {
		yd_method_option.setVisibility(View.GONE);
		Intent intent = new Intent(this, YdSettingActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		if (ydPreference.isPjYd()) {
			pjFragment.setActivityResult(arg0, arg1, arg2);
		} else {
			//GPS方式的结果返回
		}
	}

	@Override
	public void setActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResult(requestCode, resultCode, data);
	}
}
