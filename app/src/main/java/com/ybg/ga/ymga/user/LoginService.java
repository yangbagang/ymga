/**
 * 
 */
package com.ybg.ga.ymga.user;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ybg.ga.ymga.util.AppConstat;
import com.ybg.ga.ymga.util.StringUtil;

/**
 * @author 杨拔纲
 * 
 */
public class LoginService extends Service {

	private void login(String userName, String password, String os,
			String osVersion, String appVersion, String brand, String model,
			String imei) {
		new LoginThread(userName, password, os, osVersion, appVersion, brand,
				model, imei).start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String userName = intent.getExtras().getString("userName");
		String userPwd = intent.getExtras().getString("userPwd");
		String os = intent.getExtras().getString("os");
		String osVersion = intent.getExtras().getString("osVersion");
		String appVersion = intent.getExtras().getString("appVersion");
		String brand = intent.getExtras().getString("brand");
		String model = intent.getExtras().getString("model");
		String imei = intent.getExtras().getString("imei");
		login(userName, userPwd, os, osVersion, appVersion, brand, model, imei);
		return super.onStartCommand(intent, flags, startId);
	}

	private class LoginThread extends Thread {

		private UserPreferences userPreferences = UserPreferences.getInstance();
		private Intent intent = new Intent(UserAction.USER_LOGIN);

		private String userName;
		private String password;
		private String os;
		private String osVersion;
		private String appVersion;
		private String brand;
		private String model;
		private String imei;

		public LoginThread(String userName, String password, String os,
				String osVersion, String appVersion, String brand,
				String model, String imei) {
			this.userName = userName;
			this.password = password;
			this.os = os;
			this.osVersion = osVersion;
			this.appVersion = appVersion;
			this.brand = brand;
			this.model = model;
			this.imei = imei;
		}

		public void run() {
			String userId = "";
			URL url = null;
			try {
				url = new URL(AppConstat.APP_HOST + "/user/login");
				HttpURLConnection urlConn = (HttpURLConnection) url
						.openConnection();
				urlConn.setDoInput(true);// 字节流
				urlConn.setDoOutput(true);// 字节流
				urlConn.setRequestMethod("POST");
				urlConn.setUseCaches(false);
				urlConn.setRequestProperty("Content_Type",
						"application/x-www-form-urlencoded");
				urlConn.setRequestProperty("Charset", "UTF-8");

				urlConn.connect();

				DataOutputStream dos = new DataOutputStream(
						urlConn.getOutputStream());
				StringBuffer sb = new StringBuffer();
				sb.append("sid=37a6259cc0c1dae299a7866489dff0bd");
				sb.append("&userName=" + userName);
				sb.append("&password=" + password);
				sb.append("&os=" + os);
				sb.append("&osVersion=" + osVersion);
				sb.append("&appVersion=" + appVersion);
				sb.append("&brand=" + brand);
				sb.append("&model=" + model);
				sb.append("&imei=" + imei);
				dos.writeUTF(sb.toString());
				dos.flush();
				dos.close();

				BufferedReader br = new BufferedReader(new InputStreamReader(
						urlConn.getInputStream()));
				String readLine = null;
				while ((readLine = br.readLine()) != null) {
					userId += readLine;
				}

				br.close();
				urlConn.disconnect();

				if (!"".equals(userId)) {
					intent.putExtra("loginResult", "ok");
					String[] uids = userId.split(",");
					userPreferences.setId(Long.valueOf(uids[0]));
					userPreferences.setUserId(uids[1]);
					userPreferences.setLoginName(uids[2]);
					userPreferences.setNickName(uids[3]);
					userPreferences.setBirthday(uids[4]);
					userPreferences.setBodyHigh(StringUtil.getFloatFromString(
							uids[5], 1.7f));
					userPreferences.setUserSex(StringUtil.getIntFromString(
							uids[6], AppConstat.SEX_MALE));
					userPreferences.setUserHeadImg(uids[7]);
					stopSelf();
				} else {
					intent.putExtra("loginResult", "fail");
					intent.putExtra("msg", "用户名或密码错误");
				}
			} catch (MalformedURLException e) {
				intent.putExtra("loginResult", "fail");
				intent.putExtra("msg", "地址格式错误");
			} catch (IOException e) {
				intent.putExtra("loginResult", "fail");
				intent.putExtra("msg", "网络错误");
			}
			sendBroadcast(intent);
		}
	}
}
