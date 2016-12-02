/**
 * 
 */
package com.ybg.ga.ymga.ga.preference;

import com.ybg.ga.ymga.util.AppPreferences;
import com.ybg.ga.ymga.util.TimeUtil;

/**
 * @author 杨拔纲
 *
 */
public class YdPreference {

	private AppPreferences preference = AppPreferences.getInstance();
	
	private static YdPreference ydPreference = null;
	
	private String GPS = "gps";
	
	private String PEIJIAN = "peijian";
	
	private String SPORT_WALKING = "walking";
	
	private String SPORT_RUNNING = "running";
	
	private YdPreference() {
		
	}
	
	public static YdPreference getInstance() {
		if (ydPreference == null) {
			ydPreference = new YdPreference();
		}
		return ydPreference;
	}
	
	public boolean isGPSYd() {
		return GPS.equals(preference.getString("ydMethod", GPS));
	}
	
	public boolean isPjYd() {
		return !isGPSYd();
	}
	
	public void setGPSMethod() {
		preference.setString("ydMethod", GPS);
	}
	
	public void setPjMethod() {
		preference.setString("ydMethod", PEIJIAN);
	}
	
	/**
	 * 获取步长。一步的长度，单位米。
	 * @return
	 */
	public float getStepLength() {
		return preference.getFloat("stepLength", 0.55f);
	}
	
	/**
	 * 设置步长。一步的长度，单位米。
	 * @param value
	 */
	public void setStepLength(float value) {
		preference.setFloat("stepLength", value);
	}
	
	/**
	 * 查看运动方式。跑步为false，行走为true。
	 * @return
	 */
	public boolean isWalkSportType() {
		return SPORT_WALKING.equals(preference.getString("sportType", SPORT_WALKING));
	}
	
	/**
	 * 查看运动方式。跑步为true，行走为false。
	 * @return
	 */
	public boolean isRunSportType() {
		return !isWalkSportType();
	}
	
	/**
	 * 设置为走路方式
	 */
	public void setWalkSportType() {
		preference.setString("sportType", SPORT_WALKING);
	}
	
	/**
	 * 设置为跑步方式
	 */
	public void setRunSportType() {
		preference.setString("sportType", SPORT_RUNNING);
	}
	
	/**
	 * 获取目标步数。每天需要完成的计步数。
	 * @return
	 */
	public int getAimSteps() {
		return preference.getInt("aimStep", 10000);
	}
	
	/**
	 * 设置目标步数。每天需要完成的计步数。
	 * @return
	 */
	public void setAimSteps(int value) {
		preference.setInt("aimStep", value);
	}
	
	/**
	 * 获取计步器设备名称
	 * @return 设备名称
	 */
	public String getYdDeviceName() {
		return preference.getString("ydDeviceName", "");
	}
	
	/**
	 * 获取计步器设备地址
	 * @return 设备地址
	 */
	public String getYdDeviceAddr() {
		return preference.getString("ydDeviceAddr", "");
	}
	
	/**
	 * 获取计步器设备型号，匹配对应处理程序
	 * @return 设备型号
	 */
	public String getYdDeviceModel() {
		return preference.getString("ydDeviceModel", "");
	}
	
	/**
	 * 设置计步器设备名称
	 * @param value 名称
	 */
	public void setYdDeviceName(String value) {
		preference.setString("ydDeviceName", value);
	}
	
	/**
	 * 设置计步器设备地址
	 * @param value 地址
	 */
	public void setYdDeviceAddr(String value) {
		preference.setString("ydDeviceAddr", value);
	}
	
	/**
	 * 设置计步器设备型号
	 * @param value 型号
	 */
	public void setYdDeviceModel(String value) {
		preference.setString("ydDeviceModel", value);
	}
	
	/**
	 * 当前计步器管理设备是否己经绑定。己绑定为true，未绑定为false。
	 * @return
	 */
	public boolean hasAssign() {
		return preference.getBoolean("ydDeviceAssign", false);
	}
	
	public void setHasAssign(boolean value) {
		preference.setBoolean("ydDeviceAssign", value);
	}

	/**
	 * 获取最后计步器同步时间
	 * 
	 * @return
	 */
	public String getLastPeiJianSyncDate() {
		return preference.getString("lastPeiJianYDSyncDate",
				"1900-01-01");
	}

	/**
	 * 设置最后计步器同步时间
	 * 
	 * @param syncDate
	 */
	public void setLastPeiJianSyncDate(String syncDate) {
		preference.setString("lastPeiJianYDSyncDate", syncDate);
	}
	
	public boolean hasSync(int dayIndex) {
		return getLastPeiJianSyncDate().compareTo(TimeUtil.getDateByOffset(dayIndex)) >= 0;
	}
	
	/**
	 * 设置需要更新的同步日期
	 * @param needUpdateDay
	 */
	public void setNeedUpdateDay(String needUpdateDay) {
		preference.setString("needUpdateDay", needUpdateDay);
	}
	
	/**
	 * 获取需要更新的同步日期
	 * @return
	 */
	public String getNeedUpdateDay() {
		return preference.getString("needUpdateDay",
				"1900-01-01");
	}
	
	/**
	 * 检查某天是否需要更新数据
	 * @param day
	 * @return
	 */
	public boolean isNeedUpdateDay(String day) {
		return getNeedUpdateDay().equals(day);
	}
}
