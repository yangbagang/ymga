package com.ybg.ga.ymga;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.ybg.ga.ymga.ga.MainActivity;
import com.ybg.ga.ymga.util.AppConstat;
import com.ybg.ga.ymga.util.AppPreferences;

/**
 * 引导类
 * 
 * @author 杨拔纲
 * 
 */
public class WelcomeActivity extends Activity {

	private AppPreferences preferences = AppPreferences.getInstance();

	private YbgApp app = YbgApp.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// 初始化首选项配置
		if (!preferences.hasInit()) {
			preferences.init(getSharedPreferences(
					AppConstat.PREFERENCE_FILE_NAME, Activity.MODE_PRIVATE));
		}

		if (app.isFirstUse()) {
			// 检查是否是首次使用，如果是，则启动简介页
			app.setFirstUse(false);
			Intent intent = new Intent(this, AppIntroActivity.class);
			startActivity(intent);
			finish();
		} else {
			// 启动主页
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

}
