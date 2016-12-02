/**
 * 
 */
package com.ybg.ga.ymga.ga.preference;

import com.ybg.ga.ymga.util.AppPreferences;

/**
 * @author 杨拔纲
 * 
 */
public class XYPreference {

	private AppPreferences preference = AppPreferences.getInstance();

	private static XYPreference xyPreference = null;

	private XYPreference() {

	}

	public static XYPreference getInstance() {
		if (xyPreference == null) {
			xyPreference = new XYPreference();
		}
		return xyPreference;
	}

	/**
	 * 获取血压设备名称
	 * 
	 * @return 设备名称
	 */
	public String getXyDeviceName() {
		return preference.getString("xyDeviceName", "");
	}

	/**
	 * 获取血压设备地址
	 * 
	 * @return 设备地址
	 */
	public String getXyDeviceAddr() {
		return preference.getString("xyDeviceAddr", "");
	}

	/**
	 * 获取血压设备型号，匹配对应处理程序
	 * 
	 * @return 设备型号
	 */
	public String getXyDeviceModel() {
		return preference.getString("xyDeviceModel", "");
	}

	/**
	 * 设置血压设备名称
	 * 
	 * @param value
	 *            名称
	 */
	public void setXyDeviceName(String value) {
		preference.setString("xyDeviceName", value);
	}

	/**
	 * 设置血压设备地址
	 * 
	 * @param value
	 *            地址
	 */
	public void setXyDeviceAddr(String value) {
		preference.setString("xyDeviceAddr", value);
	}

	/**
	 * 设置血压设备型号
	 * 
	 * @param value
	 *            型号
	 */
	public void setXyDeviceModel(String value) {
		preference.setString("xyDeviceModel", value);
	}

	/**
	 * 当前血压管理设备是否己经绑定。己绑定为true，未绑定为false。
	 * 
	 * @return
	 */
	public boolean hasAssign() {
		return preference.getBoolean("xyDeviceAssign", false);
	}

	public void setHasAssign(boolean value) {
		preference.setBoolean("xyDeviceAssign", value);
	}

}
