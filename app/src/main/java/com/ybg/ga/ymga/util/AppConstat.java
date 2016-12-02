/**
 *
 */
package com.ybg.ga.ymga.util;

/**
 * @author 杨拔纲
 */
public class AppConstat {

    // 首选项文件名
    public static String PREFERENCE_FILE_NAME = "ga_data";

    // 是否首次使用
    public static String IS_FIRST_USE = "isFirstUse";

    //public static String APP_HOST = "http://192.168.2.233:8080/app";
    public static String APP_HOST = "http://183.57.41.230/app";
    //b6bc6da44a4e6bd6bad56c3eb49311b5
    //b5454413feaf0e4c49633b1c729e44c6
    //aec102b5cf1db29158b2568e74cfd88a
    public final static String WX_APP_ID = "wx040ee0900bbdcacd";

    // 性别
    public static int SEX_MALE = 1;
    public static int SEX_FEMALE = 0;

    // 血压计
    public static int XY_SEARCH_REQUEST_CODE = 100;
    public static int XY_SEARCH_RESULT_CODE = 101;
    public static int XY_SEARCH_CANCEL_CODE = 102;
    public static int XY_DEVICE_REQUEST_CODE = 120;
    public static int XY_DEVICE_RESULT_CODE = 121;
    public static int XY_DEVICE_CANCEL_CODE = 122;

    // BlueTooth
    public static int BT_FOUND_REQUEST_CODE = 200;
    public static int BT_FOUND_RESULT_CODE = 201;

    // 体重
    public static int TZ_SEARCH_REQUEST_CODE = 300;
    public static int TZ_SEARCH_RESULT_CODE = 301;
    public static int TZ_SEARCH_CANCEL_CODE = 302;
    public static int TZ_DEVICE_REQUEST_CODE = 320;
    public static int TZ_DEVICE_RESULT_CODE = 321;
    public static int TZ_DEVICE_CANCEL_CODE = 322;

    // 运动
    public static int YD_DEVICE_REQUEST_CODE = 400;
    public static int YD_DEVICE_RESULT_CODE = 401;
    public static int YD_SEARCH_REQUEST_CODE = 420;
    public static int YD_SEARCH_RESULT_CODE = 421;
    public static int YD_SEARCH_CANCEL_CODE = 422;

    // 体温
    public static int TW_SEARCH_REQUEST_CODE = 500;
    public static int TW_SEARCH_RESULT_CODE = 501;
    public static int TW_SEARCH_CANCEL_CODE = 502;
    public static int TW_DEVICE_REQUEST_CODE = 520;
    public static int TW_DEVICE_RESULT_CODE = 521;
    public static int TW_DEVICE_CANCEL_CODE = 522;

    // 好友
    public static int MEMBER_ADD_REQUEST_CODE = 800;
    public static int MEMBER_ADD_RESULT_CODE = 801;
    public static int FRIEND_ADD_REQUEST_CODE = 900;
    public static int FRIEND_ADD_RESULT_CODE = 901;

    // 权限
    public static int PERMISSION_REQUEST_CODE_ACCESS_COARSE_LOCATION = 10;
    public static int PERMISSION_REQUEST_CODE_TZ = 20;
    public static int PERMISSION_REQUEST_CODE_TW = 30;
    public static int PERMISSION_REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 40;
    public static int PERMISSION_REQUEST_CODE_READ_PHONE_STATE = 50;
}
