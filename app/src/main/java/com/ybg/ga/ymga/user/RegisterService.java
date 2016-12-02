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

/**
 * @author 杨拔纲
 * 
 */
public class RegisterService extends Service {

	private void register(String userName, String password, String userType,
			String os, String osVersion, String appVersion, String brand,
			String model, String imei) {
		new RegisterThread(userName, password, userType, os, osVersion,
				appVersion, brand, model, imei).start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String userName = intent.getExtras().getString("userName");
		String userPwd = intent.getExtras().getString("userPwd");
		String userType = intent.getExtras().getString("userType");
		String os = intent.getExtras().getString("os");
		String osVersion = intent.getExtras().getString("osVersion");
		String appVersion = intent.getExtras().getString("appVersion");
		String brand = intent.getExtras().getString("brand");
		String model = intent.getExtras().getString("model");
		String imei = intent.getExtras().getString("imei");
		register(userName, userPwd, userType, os, osVersion, appVersion, brand,
				model, imei);
		return super.onStartCommand(intent, flags, startId);
	}

	private class RegisterThread extends Thread {

		private UserPreferences userPreferences = UserPreferences.getInstance();
		private Intent intent = new Intent(UserAction.USER_REGISTER);

		private String userName;
		private String password;
		private String userType;
		private String os;
		private String osVersion;
		private String appVersion;
		private String brand;
		private String model;
		private String imei;

		public RegisterThread(String userName, String password,
				String userType, String os, String osVersion,
				String appVersion, String brand, String model, String imei) {
			this.userName = userName;
			this.password = password;
			this.userType = userType;
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
				url = new URL(AppConstat.APP_HOST + "/user/register");
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
				sb.append("&userType=" + userType);
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
					intent.putExtra("registerResult", "ok");
					String[] uids = userId.split(",");
					userPreferences.setId(Long.valueOf(uids[0]));
					userPreferences.setUserId(uids[1]);
					userPreferences.setLoginName(userName);
					userPreferences.setNickName(userName);
					stopSelf();
				} else {
					intent.putExtra("registerResult", "fail");
					intent.putExtra("msg", "注册失败，己存在相同名称的用户");
				}
			} catch (MalformedURLException e) {
				intent.putExtra("registerResult", "fail");
				intent.putExtra("msg", "地址格式错误");
			} catch (IOException e) {
				intent.putExtra("registerResult", "fail");
				intent.putExtra("msg", "网络错误");
			}
			sendBroadcast(intent);
		}
	}
}
