/**
 * 
 */
package com.ybg.ga.ymga.ga.yd.jStyle;

import java.util.Calendar;

import com.ybg.ga.ymga.util.BCDUtil;

/**
 * @author 杨拔纲
 * 
 */
public final class JStyleCmd {

	/**
	 * 读取某天数据详情
	 * 
	 * @param dayIndex
	 * @return
	 */
	public static byte[] getReadDetailCmd(byte dayIndex) {
		byte[] cmd = { 0x43, dayIndex, 00, 00, 00, 00, 00, 00, 00, 00, 00, 00,
				00, 00, 00, 0x43 };
		cmd[15] = (byte) ((0x43 + dayIndex) & 0xff);
		return cmd;
	}

	public static byte[] getSyncTimeCmd() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR) - 2000;// 两位年分
		int month = calendar.get(Calendar.MONTH) + 1;// 月份
		int day = calendar.get(Calendar.DAY_OF_MONTH);// 日期
		int hour = calendar.get(Calendar.HOUR_OF_DAY);// 24小时制的小时
		int minute = calendar.get(Calendar.MINUTE);// 分钟
		int second = calendar.get(Calendar.SECOND);// 秒
		byte[] cmd = new byte[16];
		cmd[0] = 0x01;
		cmd[1] = BCDUtil.int2byte(year);
		cmd[2] = BCDUtil.int2byte(month);
		cmd[3] = BCDUtil.int2byte(day);
		cmd[4] = BCDUtil.int2byte(hour);
		cmd[5] = BCDUtil.int2byte(minute);
		cmd[6] = BCDUtil.int2byte(second);
		cmd[15] = (byte) ((cmd[0] + cmd[1] + cmd[2] + cmd[3] + cmd[4] + cmd[5] + cmd[6]) & 0xff);
		return cmd;
	}

	public static byte[] getPersonInfoCmd(int sex, int age, float bodyHight,
			float bodyWeight, float stepLength) {
		byte[] cmd = new byte[16];
		cmd[0] = 0x02;
		cmd[1] = (byte) sex;
		cmd[2] = (byte) age;
		cmd[3] = (byte) (bodyHight * 100);
		cmd[4] = (byte) bodyWeight;
		cmd[5] = (byte) (stepLength * 100);
		cmd[15] = (byte) ((cmd[0] + cmd[1] + cmd[2] + cmd[3] + cmd[4] + cmd[5]) & 0xff);
		return cmd;
	}

	public static byte[] getAimCmd(int aim, int type) {
		byte[] cmd = new byte[16];
		int s1 = aim / (256 * 256);
		int s2 = (aim - s1 * 256 * 256) / 256;
		int s3 = aim - s1 * 256 * 256 - s2 * 256;
		cmd[0] = 0x0B;
		cmd[1] = (byte) s1;
		cmd[2] = (byte) s2;
		cmd[3] = (byte) s3;
		cmd[4] = (byte) 1;
		cmd[5] = (byte) 224;
		cmd[15] = (byte) ((cmd[0] + cmd[1] + cmd[2] + cmd[3] + cmd[4] + cmd[5]) & 0xff);
		return cmd;
	}
}
