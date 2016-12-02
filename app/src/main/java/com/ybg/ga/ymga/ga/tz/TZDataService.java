/**
 * 
 */
package com.ybg.ga.ymga.ga.tz;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;

import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.preference.TZPreference;
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;
import com.ybg.ga.ymga.util.AppDbHelper;
import com.ybg.ga.ymga.util.StringUtil;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 体重数据处理相关service
 * 
 * @author 杨拔纲
 * 
 */
public class TZDataService extends Service {

	private UserPreferences userPreference = UserPreferences.getInstance();
	private TZPreference tzPreference = TZPreference.getInstance();
	private final OkHttpClient client = new OkHttpClient();
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private AppDbHelper dbHelper;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		dbHelper = new AppDbHelper(this);
		super.onCreate();

		// 启动新线程，尝试上传还未上传的数据
		if (userPreference.hasLogin()) {
			uploadData();
		}
	}

	private void uploadData() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String table = "tz_history";
		String[] columns = { "_id", "tzValue", "tzZFValue", "tzJRValue",
				"tzSFValue", "tzBMIValue", "tzQZValue", "tzGGValue",
				"tzNZValue", "tzJCValue", "tzSTValue", "createTime" };
		String where = "flag=0";
		String[] whereArgs = null;
		String groupBy = null;
		String having = null;
		String order = "_id asc";
		Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
				order);
		while (c.moveToNext()) {
			int id = c.getInt(0);
			float tzValue = c.getFloat(1);
			float tzZFValue = c.getFloat(2);
			float tzJRValue = c.getFloat(3);
			float tzSFValue = c.getFloat(4);
			float tzBMIValue = c.getFloat(5);
			float tzQZValue = c.getFloat(6);
			float tzGGValue = c.getFloat(7);
			int tzNZValue = c.getInt(8);
			int tzJCValue = c.getInt(9);
			int tzSTValue = c.getInt(10);
			String createTime = c.getString(11);

			new TZUploadThread(tzValue, tzZFValue, tzJRValue, tzSFValue,
					tzBMIValue, tzQZValue, tzGGValue, tzNZValue, tzJCValue,
					tzSTValue, createTime, id).start();
		}
	}

	/**
	 * 保存体重数据，如己经登录，则同时上传到服务器。
	 * @param tzValue
	 * @param tzZFValue
	 * @param tzJRValue
	 * @param tzSFValue
	 * @param tzBMIValue
	 * @param tzQZValue
	 * @param tzGGValue
	 * @param tzNZValue
	 * @param tzJCValue
	 * @param tzSTValue
	 * @return
	 */
	public int save(float tzValue, float tzZFValue, float tzJRValue,
			float tzSFValue, float tzBMIValue, float tzQZValue,
			float tzGGValue, int tzNZValue, int tzJCValue, int tzSTValue) {
		// 保存数据
		String now = sdf.format(new Date());
		ContentValues newTZValus = new ContentValues();
		newTZValus.put("tzValue", tzValue);
		newTZValus.put("tzZFValue", tzZFValue);
		newTZValus.put("tzJRValue", tzJRValue);
		newTZValus.put("tzSFValue", tzSFValue);
		newTZValus.put("tzBMIValue", tzBMIValue);
		newTZValus.put("tzQZValue", tzQZValue);
		newTZValus.put("tzGGValue", tzGGValue);
		newTZValus.put("tzNZValue", tzNZValue);
		newTZValus.put("tzJCValue", tzJCValue);
		newTZValus.put("tzSTValue", tzSTValue);
		newTZValus.put("createTime", now);
		newTZValus.put("flag", 0);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert("tz_history", null, newTZValus);
		if (rowId == -1) {
			// 保存失败
			return 0;
		}
		int id = getLastId(db);
		// 上传到服务器
		if (userPreference.hasLogin()) {
			new TZUploadThread(tzValue, tzZFValue, tzJRValue, tzSFValue,
					tzBMIValue, tzQZValue, tzGGValue, tzNZValue, tzJCValue,
					tzSTValue, now, id).start();
		}
		return id;
	}

	public void delete(long tzLocalId, long tzRemoteId) {
		// Define 'where' part of query.
		String where = "_id=" + tzLocalId;
		// Specify arguments in placeholder order.
		String[] whereArgs = null;
		// Issue SQL statement.
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete("tz_history", where, whereArgs);
		// 如果己经登录，则同时删除服务器的数据
		if (userPreference.hasLogin()) {
			new TZDeleteThread(tzRemoteId).start();
		}
	}

	public Cursor list() {
		String beginDate = "1900-01-01";
		String endDate = "2100-01-01";
		return list(beginDate, endDate);
	}

	public Cursor list(String beginDate, String endDate) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String table = "tz_history";
		String[] columns = { "_id", "tzValue", "tzZFValue", "tzJRValue",
				"tzSFValue", "tzBMIValue", "tzQZValue", "tzGGValue",
				"tzNZValue", "tzJCValue", "tzSTValue", "createTime", "remoteId" };
		String where = "createTime >= ? and createTime <= ?";
		String[] whereArgs = { beginDate, endDate };
		String groupBy = null;
		String having = null;
		String order = "_id desc";
		Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
				order);
		return c;
	}

	private class TZUploadThread extends Thread {
		private float tzValue;
		private float tzZFValue;
		private float tzJRValue;
		private float tzSFValue;
		private float tzBMIValue;
		private float tzQZValue;
		private float tzGGValue;
		private int tzNZValue;
		private int tzJCValue;
		private int tzSTValue;
		private int id;
		private String createTime;

		public TZUploadThread(float tzValue, float tzZFValue, float tzJRValue,
				float tzSFValue, float tzBMIValue, float tzQZValue,
				float tzGGValue, int tzNZValue, int tzJCValue, int tzSTValue,
				String createTime, int id) {
			this.tzValue = tzValue;
			this.tzZFValue = tzZFValue;
			this.tzJRValue = tzJRValue;
			this.tzSFValue = tzSFValue;
			this.tzBMIValue = tzBMIValue;
			this.tzQZValue = tzQZValue;
			this.tzGGValue = tzGGValue;
			this.tzNZValue = tzNZValue;
			this.tzJCValue = tzJCValue;
			this.tzSTValue = tzSTValue;
			this.createTime = createTime;
			this.id = id;
		}

		public void run() {
			String userId = userPreference.getUserId();
			URL url = null;
			Intent uploadInfoIntent = new Intent(
					BTAction.getSendInfoAction(BTPrefix.TZ));
			try {
				url = new URL(AppConstat.APP_HOST + "/tz/save");
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
				sb.append("userId=" + userId);
				sb.append("&tzValue=" + tzValue);
				sb.append("&tzZFValue=" + tzZFValue);
				sb.append("&tzJRValue=" + tzJRValue);
				sb.append("&tzSFValue=" + tzSFValue);
				sb.append("&tzBMIValue=" + tzBMIValue);
				sb.append("&tzQZValue=" + tzQZValue);
				sb.append("&tzGGValue=" + tzGGValue);
				sb.append("&tzNZValue=" + tzNZValue);
				sb.append("&tzJCValue=" + tzJCValue);
				sb.append("&tzSTValue=" + tzSTValue);
				sb.append("&createTime=" + createTime);
				dos.writeBytes(sb.toString());
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
					uploadInfoIntent.putExtra(BTAction.INFO, "数据上传失败");
				} else {
					ContentValues updateValues = new ContentValues();
					updateValues.put("flag", 1);
					updateValues.put("remoteId", remoteId);
					String where = "_id=" + id;
					String[] whereArgs = null;
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					db.update("tz_history", updateValues, where, whereArgs);
					uploadInfoIntent.putExtra(BTAction.INFO, "数据上传成功");
				}
			} catch (MalformedURLException e) {
				uploadInfoIntent.putExtra(BTAction.INFO, "地址格式错误");
			} catch (IOException e) {
				uploadInfoIntent.putExtra(BTAction.INFO, "网络错误");
			}
			sendBroadcast(uploadInfoIntent);
		}
	}

	private class TZDeleteThread extends Thread {

		private long tzRemoteId;

		public TZDeleteThread(long tzRemoteId) {
			this.tzRemoteId = tzRemoteId;
		}

		public void run() {
			String userId = userPreference.getUserId();
			URL url = null;
			Intent uploadInfoIntent = new Intent(
					BTAction.getSendInfoAction(BTPrefix.XY));
			try {
				url = new URL(AppConstat.APP_HOST + "/tz/delete");
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
				sb.append("userId=" + userId);
				sb.append("&id=" + tzRemoteId);
				dos.writeBytes(sb.toString());
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
					// 数据删除失败
					uploadInfoIntent.putExtra(BTAction.INFO, "数据删除失败");
				} else {
					uploadInfoIntent.putExtra(BTAction.INFO, "数据己删除");
				}
			} catch (MalformedURLException e) {
				uploadInfoIntent.putExtra(BTAction.INFO, "地址格式错误");
			} catch (IOException e) {
				uploadInfoIntent.putExtra(BTAction.INFO, "网络错误");
			}
			sendBroadcast(uploadInfoIntent);
		}
	}

	public class TZDataBinder extends Binder {
		TZDataService getService() {
			return TZDataService.this;
		}
	}

	private final IBinder binder = new TZDataBinder();

	private int getLastId(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery(
				"select last_insert_rowid() from tz_history", null);
		int id = 0;
		if (cursor.moveToFirst())
			id = cursor.getInt(0);
		return id;
	}
}
