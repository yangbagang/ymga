package com.ybg.ga.ymga.ga.tw;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.IBinder;

import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.preference.TWPreference;
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;
import com.ybg.ga.ymga.util.AppDbHelper;
import com.ybg.ga.ymga.util.StringUtil;

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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yangbagang on 2015/5/28.
 */
public class TWDataService extends Service {

    private UserPreferences userPreference = UserPreferences.getInstance();
    private TWPreference twPreference = TWPreference.getInstance();
    private final OkHttpClient client = new OkHttpClient();
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
        String table = "tw_history";
        String[] columns = {"_id", "tw", "createTime"};
        String where = "flag=0";
        String[] whereArgs = null;
        String groupBy = null;
        String having = null;
        String order = "_id asc";
        Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
                order);
        while (c.moveToNext()) {
            int id = c.getInt(0);
            float tw = c.getFloat(1);
            String createTime = c.getString(2);
            new TWUploadThread(tw, createTime, id).start();
        }
    }

    /**
     * 保存体温数据，如己经登录，则同时上传到服务器。
     *
     * @param tw
     * @return
     */
    public int save(float tw) {
        // 保存数据
        String now = sdf.format(new Date());
        ContentValues newTWValus = new ContentValues();
        newTWValus.put("tw", tw);
        newTWValus.put("createTime", now);
        newTWValus.put("flag", 0);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert("tw_history", null, newTWValus);
        if (rowId == -1) {
            // 保存失败
            return 0;
        }
        int id = getLastId(db);
        // 上传到服务器
        if (userPreference.hasLogin()) {
            new TWUploadThread(tw, now, id).start();
        }
        return id;
    }

    public void delete(long twLocalId, long twRemoteId) {
        String where = "_id=" + twLocalId;
        String[] whereArgs = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("tw_history", where, whereArgs);
        // 如果己经登录，则同时删除服务器的数据
        if (userPreference.hasLogin()) {
            new TWDeleteThread(twRemoteId).start();
        }
    }

    public Cursor list() {
        String beginDate = "1900-01-01";
        String endDate = "2100-01-01";
        return list(beginDate, endDate);
    }

    public Cursor list(String beginDate, String endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String table = "tw_history";
        String[] columns = {"_id", "tw", "createTime",
                "remoteId"};
        String where = "createTime >= ? and createTime <= ?";
        String[] whereArgs = {beginDate, endDate};
        String groupBy = null;
        String having = null;
        String order = "createTime asc";
        Cursor c = db.query(table, columns, where, whereArgs, groupBy, having,
                order);
        return c;
    }

    private class TWUploadThread extends Thread {
        private float tw;
        private int twId;
        private String createTime;

        public TWUploadThread(float tw, String createTime,
                              int twId) {
            this.tw = tw;
            this.createTime = createTime;
            this.twId = twId;
        }

        public void run() {
            String userId = userPreference.getUserId();
            URL url = null;
            Intent uploadInfoIntent = new Intent(
                    BTAction.getSendInfoAction(BTPrefix.TW));
            try {
                url = new URL(AppConstat.APP_HOST + "/tw/save");
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
                sb.append("&tw=" + tw);
                sb.append("&createTime=" + createTime);
                dos.writeBytes(sb.toString());
                dos.flush();
                dos.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConn.getInputStream()));
                String readLine = null;
                String twRemoteId = "";
                while ((readLine = br.readLine()) != null) {
                    twRemoteId += readLine;
                }

                br.close();
                urlConn.disconnect();

                if ("0".equals(twRemoteId)) {
                    // 数据上传失败
                    uploadInfoIntent.putExtra(BTAction.INFO, "数据上传失败");
                } else {
                    ContentValues updateValues = new ContentValues();
                    updateValues.put("flag", 1);
                    updateValues.put("remoteId", twRemoteId);
                    String where = "_id=" + twId;
                    String[] whereArgs = null;
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.update("tw_history", updateValues, where, whereArgs);
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

    private class TWDeleteThread extends Thread {

        private long twRemoteId;

        public TWDeleteThread(long twRemoteId) {
            this.twRemoteId = twRemoteId;
        }

        public void run() {
            String userId = userPreference.getUserId();
            URL url = null;
            Intent uploadInfoIntent = new Intent(
                    BTAction.getSendInfoAction(BTPrefix.XY));
            try {
                url = new URL(AppConstat.APP_HOST + "/tw/delete");
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
                sb.append("&id=" + twRemoteId);
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

    public class TWDataBinder extends Binder {
        TWDataService getService() {
            return TWDataService.this;
        }
    }

    private final IBinder binder = new TWDataBinder();

    private int getLastId(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery(
                "select last_insert_rowid() from tw_history", null);
        int id = 0;
        if (cursor.moveToFirst())
            id = cursor.getInt(0);
        return id;
    }
}
