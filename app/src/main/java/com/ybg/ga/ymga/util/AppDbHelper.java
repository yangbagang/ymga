/**
 *
 */
package com.ybg.ga.ymga.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author 杨拔纲
 */
public class AppDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    public final static int DATABASE_VERSION = 1;
    public final static String DATABASE_NAME = "ga.db";
    // 血压记录
    public final static String CREATE_XY_TABLE = "create table xy_history(_id INTEGER primary key autoincrement,"
            + " sys smallint, dia smallint, pul smallint, createTime timestamp, flag smallint, remoteId long)";
    public final static String DROP_XY_TABLE = "drop table if exists xy_history";
    // 体重记录
    public final static String CREATE_TZ_TABLE = "create table tz_history(_id INTEGER primary key autoincrement,"
            + " tzValue float, tzZFValue float, tzJRValue float, tzSFValue float, tzBMIValue float, tzQZValue" +
            " float, tzGGValue float, tzNZValue smallint, tzJCValue smallint, tzSTValue float, createTime timestamp," +
            " flag smallint, remoteId long)";
    public final static String DROP_TZ_TABLE = "drop table if exists tz_history";
    // 运动记录
    public final static String CREATE_YD_TABLE = "create table yd_history(_id INTEGER primary key autoincrement,"
            + " steps integer, distance float, calorie float, ydtime integer, createDate date, createTime timestamp, " +
            " type smallint, flag smallint, remoteId long)";
    public final static String CREATE_YD_SYNC_ACTIVITY_TABLE = "create table yd_sync_activity(_id INTEGER primary key autoincrement,"
            + " steps integer, distance float, calorie float, createDate date, timeIndex smallint, " +
            " flag smallint, remoteId long)";
    public final static String CREATE_YD_SYNC_SLEEP_TABLE = "create table yd_sync_sleep(_id INTEGER primary key autoincrement,"
            + " sm1 smallint, sm2 smallint, sm3 smallint, sm4 smallint, sm5 smallint, sm6 smallint, sm7 smallint, " +
            " sm8 smallint, createDate date, timeIndex smallint,flag smallint, remoteId long)";
    public final static String CREATE_YD_SLEEP_TABLE = "create table yd_sleep(_id INTEGER primary key autoincrement,"
            + " deep smallint, shallow smallint, jittery smallint, createDate date, flag smallint, remoteId long)";
    public final static String DROP_YD_TABLE = "drop table if exists yd_history";
    public final static String DROP_YD_SLEEP_TABLE = "drop table if exists yd_sleep";
    public final static String DROP_YD_SYNC_TABLE = "drop table if exists yd_sync_activity";
    public final static String DROP_YD_SYNC_SLEEP_TABLE = "drop table if exists yd_sync_sleep";
    // 体温
    public final static String CREATE_TW_HISTORY = "create table tw_history(_id INTEGER primary key autoincrement,"
            + " tw float, createTime timestamp, flag smallint, remoteId long)";
    public final static String DROP_TW_HISTORY = "drop table if exists tw_history";

    public AppDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
     * .SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 执行建表指令
        // 血压
        db.execSQL(CREATE_XY_TABLE);
        // 体重
        db.execSQL(CREATE_TZ_TABLE);
        // 运动
        db.execSQL(CREATE_YD_TABLE);
        db.execSQL(CREATE_YD_SLEEP_TABLE);
        db.execSQL(CREATE_YD_SYNC_ACTIVITY_TABLE);
        db.execSQL(CREATE_YD_SYNC_SLEEP_TABLE);
        // 体温
        db.execSQL(CREATE_TW_HISTORY);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
     * .SQLiteDatabase, int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
