/**
 * 
 */
package com.ybg.ga.ymga.ga.yd;

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
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.callback.HttpRemoteCallback;
import com.ybg.ga.ymga.ga.preference.YdPreference;
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
 * 运动数据处理相关service
 * 
 * @author 杨拔纲
 * 
 */
public class YDDataService extends Service {

	private UserPreferences userPreference = UserPreferences.getInstance();
	private YdPreference ydPreference = YdPreference.getInstance();
	private YbgApp ybgApp = YbgApp.getInstance();
	private final OkHttpClient client = new OkHttpClient();
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
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

		// 注册广播
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mReceiver, mFilter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	private void uploadData() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String table = "yd_history";
		String[] columns = { "_id", "steps", "distance", "calorie", "ydtime",
				"createDate", "createTime", "type" };
		String where = "flag=0";
		String[] whereArgs = null;
		String groupBy = null;
		String having = null;
		String order = "_id asc";
		Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
				order);
		while (c.moveToNext()) {
			int id = c.getInt(0);
			int steps = c.getInt(1);
			float distance = c.getFloat(2);
			float calorie = c.getFloat(3);
			int ydtime = c.getInt(4);
			String createDate = c.getString(5);
			String createTime = c.getString(6);
			int type = c.getInt(7);

			new YDUploadThread(steps, distance, calorie, ydtime, createDate,
					createTime, type, id).start();
		}
	}

	/**
	 * 保存运动数据，如己经登录，则同时上传到服务器。
	 * 
	 * @param steps
	 * @param distance
	 * @param calorie
	 * @param ydtime
	 * @param type
	 * @return
	 */
	public int save(int steps, float distance, float calorie, int ydtime,
			int type) {
		// 保存数据
		Date now = new Date();
		String timestamp = sdf.format(now);
		String date = sdf2.format(now);
		ContentValues newYDValus = new ContentValues();
		newYDValus.put("steps", steps);
		newYDValus.put("distance", distance);
		newYDValus.put("calorie", calorie);
		newYDValus.put("ydtime", ydtime);
		newYDValus.put("createDate", date);
		newYDValus.put("createTime", timestamp);
		newYDValus.put("type", type);
		newYDValus.put("flag", 0);

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = db.insert("yd_history", null, newYDValus);
		if (rowId == -1) {
			// 保存失败
			return 0;
		}
		int id = getLastId(db);
		// 上传到服务器
		if (userPreference.hasLogin()) {
			new YDUploadThread(steps, distance, calorie, ydtime, date,
					timestamp, type, id).start();
		}
		return id;
	}

	public long getRemoteId(int _id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String table = "yd_history";
		String[] columns = { "_id", "remoteId" };
		String where = "_id=?";
		String[] whereArgs = { "" + _id };
		String groupBy = null;
		String having = null;
		String order = "_id desc";
		Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
				order);
		if (c.moveToFirst()) {
			return c.getLong(c.getColumnIndex("remoteId"));
		}
		return 0l;
	}

	public void delete(long ydLocalId, long ydRemoteId) {
		String where = "_id=" + ydLocalId;
		String[] whereArgs = null;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete("yd_history", where, whereArgs);
		// 如果己经登录，则同时删除服务器的数据
		if (userPreference.hasLogin()) {
			new YDDeleteThread(ydRemoteId).start();
		}
	}

	public Cursor list() {
		String beginDate = "1900-01-01";
		String endDate = "2100-01-01";
		return list(beginDate, endDate);
	}

	public Cursor list(String beginDate, String endDate) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String table = "yd_history";
		String[] columns = { "_id", "steps", "distance", "calorie",
				"createDate", "createTime", "type", "remoteId" };
		String where = null;
		String[] whereArgs = null;
		String groupBy = null;
		String having = null;
		String order = "_id desc";
		Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
				order);
		return c;
	}

	public Cursor listActivitySum(String startDate, String endDate) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// 按天查询运动情况
		String sql = "select createDate,sum(steps),sum(distance),sum(calorie) from yd_history "
				+ "where createDate>=? and createDate<=? group by createDate order by createDate asc";
		return db.rawQuery(sql, new String[] { startDate, endDate });

	}

	public Cursor listSleepSum(String startDate, String endDate) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		// 按天查询睡眠情况
		String sql = "select createDate,sum(deep+shallow+jittery) from yd_sleep "
				+ "where createDate>=? and createDate<=? group by createDate order by createDate asc";
		return db.rawQuery(sql, new String[] { startDate, endDate });

	}

	public Integer getStepsByDate(String date) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String sql = "select sum(steps) from yd_history "
				+ "where createDate=?";
		Cursor cursor = db.rawQuery(sql, new String[] { date });
		if (cursor == null)
			return 0;
		int result = 0;
		if (cursor.moveToFirst()) {
			result = cursor.getInt(0);
		}
		cursor.close();
		return result;
	}

	public Cursor listGPSDetail(String endDate) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String table = "yd_history";
		String[] columns = { "createTime", "steps", "distance", "calorie" };
		String where = "createDate=? and type=0";
		String[] whereArgs = { endDate };
		String groupBy = null;
		String having = null;
		String order = "_id desc";
		Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
				order);
		return c;
	}

	public Cursor listSyncActivityDetail(String endDate) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String table = "yd_sync_activity";
		String[] columns = { "timeIndex", "steps", "distance", "calorie" };
		String where = "createDate=?";
		String[] whereArgs = { endDate };
		String groupBy = null;
		String having = null;
		String order = "timeIndex asc";
		Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
				order);
		return c;
	}

	public Cursor listSyncSleepDetail(String endDate) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String table = "yd_sync_sleep";
		String[] columns = { "timeIndex", "sm1", "sm2", "sm3", "sm4", "sm5",
				"sm6", "sm7", "sm8" };
		String where = "createDate=?";
		String[] whereArgs = { endDate };
		String groupBy = null;
		String having = null;
		String order = "timeIndex asc";
		Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
				order);
		return c;
	}

	/**
	 * 保存同步数据
	 * 
	 * @param syncDate
	 * @param timeIndex
	 * @param steps
	 * @param distance
	 * @param calories
	 */
	public void saveSyncActivity(String syncDate, int timeIndex, int steps,
			float distance, float calories) {
		if (ydPreference.isNeedUpdateDay(syncDate)) {
			// 此项数据此前己经保存过，需要进行更新操作。
			ContentValues updateValus = new ContentValues();
			updateValus.put("steps", steps);
			updateValus.put("distance", distance);
			updateValus.put("calorie", calories);
			String where = "createDate=? and timeIndex=?";
			String[] whereArgs = { syncDate, "" + timeIndex };
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.update("yd_sync_activity", updateValus, where, whereArgs);
		} else {
			// 这是新数据，直接插入数据库。
			ContentValues newYDValus = new ContentValues();
			newYDValus.put("steps", steps);
			newYDValus.put("distance", distance);
			newYDValus.put("calorie", calories);
			newYDValus.put("createDate", syncDate);
			newYDValus.put("timeIndex", timeIndex);
			newYDValus.put("remoteId", 0);
			newYDValus.put("flag", 0);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.insert("yd_sync_activity", null, newYDValus);
		}
	}

	/**
	 * 保存同步睡眠
	 * 
	 * @param syncDate
	 * @param timeIndex
	 * @param sm1
	 * @param sm2
	 * @param sm3
	 * @param sm4
	 * @param sm5
	 * @param sm6
	 * @param sm7
	 * @param sm8
	 */
	public void saveSyncSleep(String syncDate, int timeIndex, int sm1, int sm2,
			int sm3, int sm4, int sm5, int sm6, int sm7, int sm8) {
		if (ydPreference.isNeedUpdateDay(syncDate)) {
			// 此前有保存，再次保存需要更新。
			ContentValues updateValus = new ContentValues();
			updateValus.put("sm1", sm1);
			updateValus.put("sm2", sm2);
			updateValus.put("sm3", sm3);
			updateValus.put("sm4", sm4);
			updateValus.put("sm5", sm5);
			updateValus.put("sm6", sm6);
			updateValus.put("sm7", sm7);
			updateValus.put("sm8", sm8);
			String where = "createDate=? and timeIndex=?";
			String[] whereArgs = { syncDate, "" + timeIndex };
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.update("yd_sync_sleep", updateValus, where, whereArgs);
		} else {
			ContentValues newYDValus = new ContentValues();
			newYDValus.put("sm1", sm1);
			newYDValus.put("sm2", sm2);
			newYDValus.put("sm3", sm3);
			newYDValus.put("sm4", sm4);
			newYDValus.put("sm5", sm5);
			newYDValus.put("sm6", sm6);
			newYDValus.put("sm7", sm7);
			newYDValus.put("sm8", sm8);
			newYDValus.put("createDate", syncDate);
			newYDValus.put("timeIndex", timeIndex);
			newYDValus.put("remoteId", 0);
			newYDValus.put("flag", 0);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.insert("yd_sync_sleep", null, newYDValus);
		}
	}

	public void updateSyncData(String updateDate) {
		// 生成活动数据
		int steps = 0;
		float distance = 0f;
		float calorie = 0f;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String sql = "select sum(steps), sum(distance), sum(calorie) from yd_sync_activity where createDate=?";
		Cursor c = db.rawQuery(sql, new String[] { updateDate });
		if (c != null) {
			if (c.moveToFirst()) {
				steps = c.getInt(0);
				distance = c.getFloat(1);
				calorie = c.getFloat(2);
			}
		}
		updateActivityData(steps, distance, calorie, updateDate);// 1配件0GPS

		// 生成睡眠数据
		String table = "yd_sync_sleep";
		String[] columns = { "sm1", "sm2", "sm3", "sm4", "sm5", "sm6", "sm7",
				"sm8" };
		String where = "createDate=?";
		String[] whereArgs = { updateDate };
		String groupBy = null;
		String having = null;
		String order = null;
		c = db.query(table, columns, where, whereArgs, groupBy, having, order);
		int deep = 0;
		int shallow = 0;
		int jittery = 0;
		if (c != null) {
			while (c.moveToNext()) {
				int sm1 = c.getInt(c.getColumnIndex("sm1"));
				int sm2 = c.getInt(c.getColumnIndex("sm2"));
				int sm3 = c.getInt(c.getColumnIndex("sm3"));
				int sm4 = c.getInt(c.getColumnIndex("sm4"));
				int sm5 = c.getInt(c.getColumnIndex("sm5"));
				int sm6 = c.getInt(c.getColumnIndex("sm6"));
				int sm7 = c.getInt(c.getColumnIndex("sm7"));
				int sm8 = c.getInt(c.getColumnIndex("sm8"));
				if (sm1 != 0) {
					if (sm1 < 43) {
						deep += 2;
					} else if (sm1 < 86) {
						shallow += 2;
					} else {
						jittery += 2;
					}
				}

				if (sm2 != 0) {
					if (sm2 < 43) {
						deep += 2;
					} else if (sm2 < 86) {
						shallow += 2;
					} else {
						jittery += 2;
					}
				}

				if (sm3 != 0) {
					if (sm3 < 43) {
						deep += 2;
					} else if (sm3 < 86) {
						shallow += 2;
					} else {
						jittery += 2;
					}
				}

				if (sm4 != 0) {
					if (sm4 < 43) {
						deep += 2;
					} else if (sm4 < 86) {
						shallow += 2;
					} else {
						jittery += 2;
					}
				}

				if (sm5 != 0) {
					if (sm5 < 43) {
						deep += 2;
					} else if (sm5 < 86) {
						shallow += 2;
					} else {
						jittery += 2;
					}
				}

				if (sm6 != 0) {
					if (sm6 < 43) {
						deep += 2;
					} else if (sm6 < 86) {
						shallow += 2;
					} else {
						jittery += 2;
					}
				}

				if (sm7 != 0) {
					if (sm7 < 43) {
						deep += 2;
					} else if (sm7 < 86) {
						shallow += 2;
					} else {
						jittery += 2;
					}
				}

				if (sm8 != 0) {
					if (sm8 < 43) {
						deep += 1;
					} else if (sm8 < 86) {
						shallow += 1;
					} else {
						jittery += 1;
					}
				}
			}
		}

		updateSleepData(deep, shallow, jittery, updateDate);
	}

	private void updateSleepData(int deep, int shallow, int jittery,
			String updateDate) {
		if (ydPreference.isNeedUpdateDay(updateDate)) {
			// 需要更新数据
			ContentValues updateValus = new ContentValues();
			updateValus.put("deep", deep);
			updateValus.put("shallow", shallow);
			updateValus.put("jittery", jittery);
			updateValus.put("flag", 0);
			String where = "createDate=?";
			String[] whereArgs = { updateDate };
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.update("yd_sleep", updateValus, where, whereArgs);
		} else {
			// 新增数据
			ContentValues newYDValus = new ContentValues();
			newYDValus.put("deep", deep);
			newYDValus.put("shallow", shallow);
			newYDValus.put("jittery", jittery);
			newYDValus.put("createDate", updateDate);
			newYDValus.put("remoteId", 0);
			newYDValus.put("flag", 0);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.insert("yd_sleep", null, newYDValus);
		}
	}

	private void updateActivityData(int steps, float distance, float calorie,
			String updateDate) {
		if (ydPreference.isNeedUpdateDay(updateDate)) {
			// 需要更新数据
			ContentValues updateValus = new ContentValues();
			updateValus.put("steps", steps);
			updateValus.put("distance", distance);
			updateValus.put("calorie", calorie);
			updateValus.put("flag", 0);
			String where = "createDate=? and type=1";
			String[] whereArgs = { updateDate };
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.update("yd_history", updateValus, where, whereArgs);
			if (userPreference.hasLogin()) {
				uploadData();
			}
		} else {
			// 新增数据
			ContentValues newYDValus = new ContentValues();
			newYDValus.put("steps", steps);
			newYDValus.put("distance", distance);
			newYDValus.put("calorie", calorie);
			newYDValus.put("ydtime", 0);
			newYDValus.put("createDate", updateDate);
			newYDValus.put("createTime", updateDate);
			newYDValus.put("type", 1);// 1配件0GPS
			newYDValus.put("flag", 0);

			SQLiteDatabase db = dbHelper.getWritableDatabase();
			long rowId = db.insert("yd_history", null, newYDValus);
			if (rowId == -1) {
				// 保存失败
				return;
			}
			int id = getLastId(db);
			// 上传到服务器
			if (userPreference.hasLogin()) {
				new YDUploadThread(steps, distance, calorie, 0, updateDate,
						updateDate, 1, id).start();
			}
		}
	}

	public float[] getAcitivity(String date) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String sql = "select sum(steps), sum(distance), sum(calorie) from yd_history where createDate=?";
		Cursor c = db.rawQuery(sql, new String[] { date });
		if (c != null) {
			if (c.moveToFirst()) {
				float[] activity = new float[3];
				activity[0] = c.getInt(0);
				activity[1] = c.getFloat(1);
				activity[2] = c.getFloat(2);
				return activity;
			}
		}
		return null;
	}

	public int[] getSleeps(String date) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String sql = "select sum(deep), sum(shallow), sum(jittery) from yd_sleep where createDate=?";
		Cursor c = db.rawQuery(sql, new String[] { date });
		if (c != null) {
			if (c.moveToFirst()) {
				int[] sleeps = new int[3];
				sleeps[0] = c.getInt(0);
				sleeps[1] = c.getInt(1);
				sleeps[2] = c.getInt(2);
				return sleeps;
			}
		}
		return null;
	}

	public void getMaxSteps() {
		new MaxStepsThread().start();
	}

	private class YDUploadThread extends Thread {
		private int steps;
		private float distance;
		private float calorie;
		private int ydtime;
		private int id;
		private String createTime;
		private String createDate;
		private int type;

		public YDUploadThread(int steps, float distance, float calorie,
				int ydtime, String createDate, String createTime, int type,
				int id) {
			this.steps = steps;
			this.distance = distance;
			this.calorie = calorie;
			this.ydtime = ydtime;
			this.type = type;
			this.createDate = createDate;
			this.createTime = createTime;
			this.id = id;
		}

		public void run() {
			String userId = userPreference.getUserId();
			URL url = null;
			Intent uploadInfoIntent = new Intent(
					BTAction.getSendInfoAction(BTPrefix.YD));
			try {
				url = new URL(AppConstat.APP_HOST + "/yd/save");
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
				sb.append("&steps=" + steps);
				sb.append("&distance=" + distance);
				sb.append("&calorie=" + calorie);
				sb.append("&ydtime=" + ydtime);
				sb.append("&type=" + type);
				sb.append("&createDate=" + createDate);
				sb.append("&createTime=" + createTime);
				dos.writeBytes(sb.toString());
				dos.flush();
				dos.close();

				BufferedReader br = new BufferedReader(new InputStreamReader(
						urlConn.getInputStream()));
				String readLine = null;
				String ydRemoteId = "";
				while ((readLine = br.readLine()) != null) {
					ydRemoteId += readLine;
				}

				br.close();
				urlConn.disconnect();

				if ("0".equals(ydRemoteId)) {
					// 数据上传失败
					uploadInfoIntent.putExtra(BTAction.INFO, "数据上传失败");
				} else {
					ContentValues updateValues = new ContentValues();
					updateValues.put("flag", 1);
					updateValues.put("remoteId", ydRemoteId);
					String where = "_id=" + id;
					String[] whereArgs = null;
					SQLiteDatabase db = dbHelper.getWritableDatabase();
					db.update("yd_history", updateValues, where, whereArgs);
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

	private class YDDeleteThread extends Thread {

		private long ydRemoteId;

		public YDDeleteThread(long ydRemoteId) {
			this.ydRemoteId = ydRemoteId;
		}

		public void run() {
			String userId = userPreference.getUserId();
			URL url = null;
			Intent uploadInfoIntent = new Intent(
					BTAction.getSendInfoAction(BTPrefix.YD));
			try {
				url = new URL(AppConstat.APP_HOST + "/yd/delete");
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
				sb.append("&id=" + ydRemoteId);
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

	public class YDDataBinder extends Binder {
		public YDDataService getService() {
			return YDDataService.this;
		}
	}

	private final IBinder binder = new YDDataBinder();

	private int getLastId(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery(
				"select last_insert_rowid() from yd_history", null);
		int id = 0;
		if (cursor.moveToFirst())
			id = cursor.getInt(0);
		return id;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				// 网络状态已经改变
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					// 有网络可用，上传未上传数据。
					if (userPreference.hasLogin()) {
						uploadData();
					}
				} else {
					// 无网络可用，暂时不处理。
				}
			}
		}
	};
	
	private class MaxStepsThread extends Thread {

		@Override
		public void run() {
			int maxSteps = 0;
			if (userPreference.hasLogin()) {
				// 己经登录，查询服务器上最大值
				maxSteps = getRemoteMaxSteps();
			} else {
				// 未登录，查询本地最大值
				maxSteps = getLocaleMaxSteps();
			}
			Intent intent = new Intent("YD_MAXSTEPS");
			intent.putExtra("maxSteps", maxSteps);
			sendBroadcast(intent);
		}
		
		private int getRemoteMaxSteps() {
			int maxSteps = 0;
			URL url = null;
			try {
				url = new URL(AppConstat.APP_HOST + "/yd/getUserMaxSteps");
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
				sb.append("userId=" + userPreference.getUserId());
				dos.writeBytes(sb.toString());
				dos.flush();
				dos.close();

				BufferedReader br = new BufferedReader(new InputStreamReader(
						urlConn.getInputStream()));
				String readLine = null;
				String strResult = "";
				while ((readLine = br.readLine()) != null) {
					strResult += readLine;
				}

				br.close();
				urlConn.disconnect();
				maxSteps = Integer.parseInt(strResult);
			} catch (Exception e) {
				// nothing
			}
			return maxSteps;
		}
		
		private int getLocaleMaxSteps() {
			int maxSteps = 0;
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			String sql = "select max(steps) from yd_history";
			Cursor c = db.rawQuery(sql, null);
			if (c != null) {
				if (c.moveToFirst()) {
					maxSteps = c.getInt(0);
				}
			}
			return maxSteps;
		}
	}

}
