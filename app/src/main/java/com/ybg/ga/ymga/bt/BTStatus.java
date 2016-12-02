/**
 *
 */
package com.ybg.ga.ymga.bt;

/**
 * @author 杨拔纲
 */
public class BTStatus {

    public static String[] BT_LABELS = {"您还未绑定设备", "设备还未连接", "设备连接失败",
            "设备还未启动", "设备启动失败", "设备数据读取", "可以停止设备", "己绑定设备无法连接"};

    public static String[] BT_BUTTONS = {"绑定", "连接", "连接", "启动", "启动", "读取",
            "停止", "绑定"};

    public static int BT_STATU_NOT_ASSIGN = 0;

    public static int BT_STATU_NOT_CONNECT = 1;

    public static int BT_STATU_CONNECT_FAIL = 2;

    public static int BT_STATU_NOT_START = 3;

    public static int BT_STATU_RESTART = 4;

    public static int BT_STATU_READ_DATA = 5;

    public static int BT_STATU_STOP = 6;

    public static int BT_STATU_REASSIGN = 7;
}
