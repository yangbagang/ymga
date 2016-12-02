/**
 * 
 */
package com.ybg.ga.ymga.user;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.util.AppConstat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author 杨拔纲
 * 
 */
public class UserSettingService extends Service {

	private UserPreferences userPreference = UserPreferences.getInstance();

	private final OkHttpClient client = new OkHttpClient();

	private final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg; charset=utf-8");

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	public class UserSettingBinder extends Binder {
		public UserSettingService getService() {
			return UserSettingService.this;
		}
	}

	private final IBinder binder = new UserSettingBinder();

	public void saveSetting() {
		new SaveSettingThread().start();
	}

	private class SaveSettingThread extends Thread {

		public void run() {
			String userId = userPreference.getUserId();
			URL url = null;
			Intent userIntent = new Intent(
					BTAction.getSendInfoAction(BTPrefix.USER));
			try {
				url = new URL(AppConstat.APP_HOST + "/user/update");
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
				sb.append("sid=" + userId);
				sb.append("&userId=" + userId);
				sb.append("&birthday=" + userPreference.getBirthday());
				sb.append("&sex=" + userPreference.getUserSex());
				sb.append("&nickName=" + userPreference.getNickName());
				sb.append("&bodyHigh=" + userPreference.getBodyHigh());
				dos.writeUTF(sb.toString());
				dos.flush();
				dos.close();

				BufferedReader br = new BufferedReader(new InputStreamReader(
						urlConn.getInputStream()));
				String readLine = null;
				String remoteId = "";
				while ((readLine = br.readLine()) != null) {
					remoteId += readLine;
				}

				br.close();
				urlConn.disconnect();

				if ("0".equals(remoteId)) {
					// 数据上传失败
					userIntent.putExtra(BTAction.INFO, "设置上传失败，请稍候再保存一次。");
				} else {
					// 发送广播
					userIntent.putExtra(BTAction.INFO, "ok");
				}
			} catch (MalformedURLException e) {
				userIntent.putExtra(BTAction.INFO, "地址格式错误");
			} catch (IOException e) {
				userIntent.putExtra(BTAction.INFO, "网络错误");
			}
			sendBroadcast(userIntent);
		}
	}

	public void uploadUserImg(String userImg) {
		String url = AppConstat.APP_HOST + "/user/uploadImg";
		String userId = userPreference.getUserId();
		// 压缩再上传
		File imgFile = new File(userImg);
		RequestBody requestBody = new MultipartBody.Builder()
				.setType(MultipartBody.FORM)
				.addFormDataPart("userId", userId)
				.addFormDataPart("userImg", imgFile.getName(),
						RequestBody.create(MEDIA_TYPE_JPG, imgFile))
				.build();

		Request request = new Request.Builder()
				.url(url)
				.post(requestBody)
				.build();
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				System.out.println("error=" + e.getLocalizedMessage());
				sendMsg("头像上传失败。");
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String remoteText = response.body().string();
				if (!"0".equals(remoteText)) {
					userPreference.setUserHeadImg(remoteText);
					sendMsg("头像上传完成。");
					Intent intent = new Intent(UserAction.USER_LOGIN);
					intent.putExtra("loginResult", "ok");
					sendBroadcast(intent);
				}
			}
		});
	}

	private void sendMsg(String msg) {
		Intent intent = new Intent("UPLOAD_USERIMG");
		intent.putExtra("msg", msg);
		sendBroadcast(intent);
	}

}
