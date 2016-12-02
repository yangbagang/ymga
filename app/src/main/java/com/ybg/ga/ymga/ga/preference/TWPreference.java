package com.ybg.ga.ymga.ga.preference;

import com.ybg.ga.ymga.util.AppPreferences;

/**
 * Created by yangbagang on 2015/5/28.
 */
public class TWPreference {

    private AppPreferences preference = AppPreferences.getInstance();
    
    private static TWPreference twPreference = null;
    
    private TWPreference() {}
    
    public static TWPreference getInstance() {
        if (twPreference == null) {
            twPreference = new TWPreference();
        }
        return twPreference;
    }

    /**
     * 获取体温设备名称
     *
     * @return 设备名称
     */
    public String getTwDeviceName() {
        return preference.getString("twDeviceName", "");
    }

    /**
     * 获取体温设备地址
     *
     * @return 设备地址
     */
    public String getTwDeviceAddr() {
        return preference.getString("twDeviceAddr", "");
    }

    /**
     * 获取体温设备型号，匹配对应处理程序
     *
     * @return 设备型号
     */
    public String getTwDeviceModel() {
        return preference.getString("twDeviceModel", "");
    }

    /**
     * 设置体温设备名称
     *
     * @param value
     *            名称
     */
    public void setTwDeviceName(String value) {
        preference.setString("twDeviceName", value);
    }

    /**
     * 设置体温设备地址
     *
     * @param value
     *            地址
     */
    public void setTwDeviceAddr(String value) {
        preference.setString("twDeviceAddr", value);
    }

    /**
     * 设置体温设备型号
     *
     * @param value
     *            型号
     */
    public void setTwDeviceModel(String value) {
        preference.setString("twDeviceModel", value);
    }

    /**
     * 当前体温管理设备是否己经绑定。己绑定为true，未绑定为false。
     *
     * @return
     */
    public boolean hasAssign() {
        return preference.getBoolean("twDeviceAssign", false);
    }

    public void setHasAssign(boolean value) {
        preference.setBoolean("twDeviceAssign", value);
    }

    public boolean isCAsDefaultUnit() {
        return preference.getBoolean("cIsDefaultUnit", true);
    }

    public void setCAsDefaultUnit(boolean b) {
        preference.setBoolean("cIsDefaultUnit", b);
    }

}
