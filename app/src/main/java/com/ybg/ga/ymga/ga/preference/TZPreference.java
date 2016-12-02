/**
 * 
 */
package com.ybg.ga.ymga.ga.preference;

import com.ybg.ga.ymga.util.AppPreferences;

/**
 * @author 杨拔纲
 * 
 */
public class TZPreference {

	private AppPreferences preference = AppPreferences.getInstance();

	private static TZPreference tzPreference = null;

	private TZPreference() {

	}

	public static TZPreference getInstance() {
		if (tzPreference == null) {
			tzPreference = new TZPreference();
		}
		return tzPreference;
	}

	/**
	 * 获取体重设备名称
	 * 
	 * @return 设备名称
	 */
	public String getTzDeviceName() {
		return preference.getString("tzDeviceName", "");
	}

	/**
	 * 获取体重设备地址
	 * 
	 * @return 设备地址
	 */
	public String getTzDeviceAddr() {
		return preference.getString("tzDeviceAddr", "");
	}

	/**
	 * 获取体重设备型号，匹配对应处理程序
	 * 
	 * @return 设备型号
	 */
	public String getTzDeviceModel() {
		return preference.getString("tzDeviceModel", "");
	}

	/**
	 * 设置体重设备名称
	 * 
	 * @param value
	 *            名称
	 */
	public void setTzDeviceName(String value) {
		preference.setString("tzDeviceName", value);
	}

	/**
	 * 设置体重设备地址
	 * 
	 * @param value
	 *            地址
	 */
	public void setTzDeviceAddr(String value) {
		preference.setString("tzDeviceAddr", value);
	}

	/**
	 * 设置体重设备型号
	 * 
	 * @param value
	 *            型号
	 */
	public void setTzDeviceModel(String value) {
		preference.setString("tzDeviceModel", value);
	}

	/**
	 * 当前体重管理设备是否己经绑定。己绑定为true，未绑定为false。
	 * 
	 * @return
	 */
	public boolean hasAssign() {
		return preference.getBoolean("tzDeviceAssign", false);
	}

	public void setHasAssign(boolean value) {
		preference.setBoolean("tzDeviceAssign", value);
	}

}
