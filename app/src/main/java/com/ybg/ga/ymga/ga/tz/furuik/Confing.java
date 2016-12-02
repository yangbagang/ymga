package com.ybg.ga.ymga.ga.tz.furuik;

import android.content.IntentFilter;

/**
 * Created by yangbagang on 15/10/9.
 */
public class Confing {

    public static final String TITLE = "title";

    public static final String URL = "url";

    public static final String TYPE = "type";

    public static final String JSON = "xmlStr";

    public static final String BLE_NOTiFY = "ble.notifyChange";//激活通知
    public static final String BLE_Fat_Data = "ble.tizhong.value";
    public static final String BLE_DISCONNECT = "ble.disconnect";
    public static final String BLE_ChangeWei_Data = "ble.change.value";
    public static final String BLE_Confirm_Data = "ble.Confirm.value";

    /**添加意图过滤器*/
    public static IntentFilter getfilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Confing.BLE_NOTiFY);
        filter.addAction(Confing.BLE_Fat_Data);
        filter.addAction(Confing.BLE_DISCONNECT);
        filter.addAction(Confing.BLE_ChangeWei_Data);
        filter.addAction(Confing.BLE_Confirm_Data);
        return filter;
    }

}
