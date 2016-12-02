/**
 *
 */
package com.ybg.ga.ymga.bt;

/**
 * @author 杨拔纲
 */
public final class BTAction {

    public final static String INFO = "info";

    public final static String DATA = "data";

    public final static String PROGRESS = "progress";

    public final static String CMD = "com.ybg.ga.ymga.bt.CMD";

    public final static String ACK = "com.ybg.ga.ymga.bt.ACK";

    /* */
    public final static String EXTRA_DEVICE_ADDRESS = "bt_device_address";

    /**
     * 停止设备并断开连接
     *
     * @param prefix
     * @return
     */
    public static String getStopAction(String prefix) {
        return prefix + "device_stop";
    }

    /**
     * 启动设备
     *
     * @param prefix
     * @return
     */
    public static String getStartAction(String prefix) {
        return prefix + "device_start";
    }

    /**
     * 连接设备并启动
     *
     * @param prefix
     * @return
     */
    public static String getConnectStartAction(String prefix) {
        return prefix + "device_connect_start";
    }

    /**
     * 连接设备
     *
     * @param prefix
     * @return
     */
    public static String getConnectAction(String prefix) {
        return prefix + "device_connect";
    }

    /**
     * 连接设备成功
     *
     * @param prefix
     * @return
     */
    public static String getConnectedSuccess(String prefix) {
        return prefix + "device_connect_success";
    }

    /**
     * 设备断开连接
     *
     * @param prefix
     * @return
     */
    public static String getDisConnected(String prefix) {
        return prefix + "device_connect_miss";
    }

    /**
     * 给前台发送数据
     *
     * @param prefix
     * @return
     */
    public static String getSendDataAction(String prefix) {
        return prefix + "send_data";
    }

    /**
     * 给前台发送通知信息
     *
     * @param prefix
     * @return
     */
    public static String getSendInfoAction(String prefix) {
        return prefix + "send_info";
    }

    /**
     * 给前台发送出错信息
     *
     * @param prefix
     * @return
     */
    public static String getSendErrorAction(String prefix) {
        return prefix + "send_error";
    }

    /**
     * 发送命令
     *
     * @param prefix
     * @return
     */
    public static String getSendCmdAction(String prefix) {
        return prefix + "send_cmd";
    }

    /**
     * 命令执行返馈
     *
     * @param prefix
     * @return
     */
    public static String getSendAnswerAction(String prefix) {
        return prefix + "send_answer";
    }

    public static String getUpdateAction(String prefix) {
        return prefix + "send_update";
    }

    public static String getPaiMinAction(String prefix) {
        return prefix + "send_paimin";
    }

    public static String sendProgressAction(String prefix) {
        return prefix + "send_progress";
    }
}
