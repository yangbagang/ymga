/**
 * 
 */
package com.ybg.ga.ymga.user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import com.ybg.ga.ymga.util.AppConstat;
import com.ybg.ga.ymga.util.AppPreferences;

/**
 * @author 杨拔纲
 * 
 */
public class UserPreferences {

	private AppPreferences preference = AppPreferences.getInstance();

	private static UserPreferences userPreferences = null;

	private UserPreferences() {

	}

	public static UserPreferences getInstance() {
		if (userPreferences == null) {
			userPreferences = new UserPreferences();
		}
		return userPreferences;
	}
	
	public long getId() {
		return preference.getLong("id", 0l);
	}
	
	public void setId(long id) {
		preference.setLong("id", id);
	}

	public String getBirthday() {
		return preference.getString("birthday", "");
	}

	public void setBirthday(String value) {
		preference.setString("birthday", value);
	}

	public int getUserSex() {
		return preference.getInt("sex", AppConstat.SEX_MALE);
	}

	public void setUserSex(int value) {
		preference.setInt("sex", value);
	}

	public boolean hasLogin() {
		return !"0".equals(getUserId());
	}

	public String getLoginName() {
		return preference.getString("loginName", "");
	}

	public void setLoginName(String loginName) {
		preference.setString("loginName", loginName);
	}

	public String getNickName() {
		return preference.getString("nickName", "");
	}

	public void setNickName(String nickName) {
		preference.setString("nickName", nickName);
	}

	public String getUserId() {
		return preference.getString("userId", "0");
	}

	public void setUserId(String userId) {
		preference.setString("userId", userId);
	}

	public String getName() {
		if (!hasLogin()) {
			return "游客";
		}
		if (!"".equals(getNickName())) {
			return getNickName();
		}
		return getLoginName();
	}

	/**
	 * 获取体重。单位千克。
	 * 
	 * @return
	 */
	public float getBodyWeight() {
		return preference.getFloat("bodyWeight", 60f);
	}

	/**
	 * 设置体重。单位千克。
	 * 
	 * @param value
	 */
	public void setBodyWeight(float value) {
		preference.setFloat("bodyWeight", value);
	}

	/**
	 * 获取身高，单位为米。默认设置为1米7。
	 * 
	 * @return
	 */
	public float getBodyHigh() {
		return preference.getFloat("bodyHigh", 1.7f);
	}

	/**
	 * 设置身高。
	 * 
	 * @param bodyHigh
	 */
	public void setBodyHigh(float bodyHigh) {
		preference.setFloat("bodyHigh", bodyHigh);
	}
	
	@SuppressLint("SimpleDateFormat")
	public int getAge() {
		String birthday = getBirthday();
		if(birthday == null || "".equals(birthday)) {
			return 30;
		}
		int age = 0;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date birthDate = sdf.parse(birthday);
			Date now = new Date();
			long day = (now.getTime() - birthDate.getTime()) / 1000;
			age = (int) (day / (60*60*24*365));
		} catch (ParseException e) {
			age = 30;
		}
		return age;
	}

	public String getUserHeadImg() {
		return preference.getString("userHeadImg", "0");
	}

	public void setUserHeadImg(String img) {
		preference.setString("userHeadImg", img);
	}
}
