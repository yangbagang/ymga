package com.ybg.ga.ymga.ga.tz.furuik;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangbagang on 15/10/9.
 */
public class MyApplication extends Application {

    private static MyApplication app;
    public static boolean supportBle=false;
    public static boolean SwitchMember = true;//切换用户
    public static boolean isInputUserMsg = false;//是否输入过测试体验用户

    public static List<Fatdata_JavaBean.Fat> fatdata_list = new ArrayList<Fatdata_JavaBean.Fat>();

    public static boolean Chable=false;
    public static BluetoothAdapter mBluetoothAdapter;
    public static BluetoothService bleService;
    public static String Address = "";

    public static int screenWidht;
    public static int screenHight;

    /** 大图图片的路径 */
    public String imagePath = "";
    /** 缩略图片的路径 */
    public String imageUpPath = "";

    public static float xScale;
    public static float yScale;


    public static MyApplication getInstance(){
        if(app == null){
            app = new MyApplication();
        }
        return app;
    }

}
