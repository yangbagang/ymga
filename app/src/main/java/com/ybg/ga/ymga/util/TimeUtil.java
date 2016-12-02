/**
 * 
 */
package com.ybg.ga.ymga.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;

/**
 * @author 杨拔纲
 * 
 */
public final class TimeUtil {

	@SuppressLint("SimpleDateFormat")
	public final static String getButtyTime(String datetime) {
		String result = "未知";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			result = getButtyTime(sdf.parse(datetime));
		} catch (ParseException e) {
			// nothing
		}
		return result;
	}

	@SuppressLint("SimpleDateFormat")
	public final static String getButtyTime(Date datetime) {
		String result = "未知";
		if (datetime != null) {
			Date now = new Date();
			long diff = now.getTime() - datetime.getTime();
			if (diff < 0) {
				result = "穿越";
			} else if (diff < 5 * 60 * 1000) {
				result = "刚才";
			} else if (diff < 60 * 60 * 1000) {
				result = ((int) diff / (60 * 1000)) + "分钟前";
			} else if (diff < 24 * 60 * 60 * 1000) {
				result = ((int) diff / (60 * 60 * 1000)) + "小时前";
			} else if (diff < 10 * 24 * 60 * 60 * 1000) {
				result = ((int) diff / (24 * 60 * 60 * 1000)) + "天前";
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				result = sdf.format(datetime);
			}
		}
		return result;
	}
	
	/**
	 * 返回从当前日期偏移指定天数的日期
	 * @param offset
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public final static String getDateByOffset(int offset) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - offset);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(calendar.getTime());
	}
	
	@SuppressLint("SimpleDateFormat")
	public final static String getDateByOffset(String endDate, int offset) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// 分解时间
		int year = Integer.parseInt(endDate.split("-")[0]);
		int month = Integer.parseInt(endDate.split("-")[1]) - 1;
		int day = Integer.parseInt(endDate.split("-")[2]);
		// 设置时间
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day - offset + 1);
		// 返回数据
		return sdf.format(calendar.getTime());
	}
	
	@SuppressLint("DefaultLocale")
	public final static String getTimeString(int minutes) {
		if(minutes <= 0) return "0h0\'";
		int hours = minutes / 60;
		int minute = minutes % 60;
		return String.format("%dh%d\'", hours, minute);
	}
	
	public final static String getHourAndMinutes(long seconds) {
		if(seconds <=0) return "00:00";
		long hours = seconds / 60 / 60;
		long minute = (seconds - hours * 60 * 60) / 60;
		long second = seconds - hours * 60 * 60 - minute * 60;
		return String.format("%s:%s:%s", hours, minute, second);
	}
	
	public final static List<String> getDates(String endDate, int offsetDays, String format) {
		// 准备
		List<String> list = new ArrayList<String>();
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
		Calendar calendar = Calendar.getInstance();
		// 分解时间
		int year = Integer.parseInt(endDate.split("-")[0]);
		int month = Integer.parseInt(endDate.split("-")[1]) - 1;
		int day = Integer.parseInt(endDate.split("-")[2]);
		// 设置时间
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day - offsetDays);
		// 计算时间
		String timeString = null;
		for(int i = 0; i < offsetDays; i++) {
			calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1);
			timeString = sdf.format(calendar.getTime());
			if (!list.contains(timeString)) {
				list.add(timeString);
			}
		}
		return list;
	}
	
	public final static String getPartTimeFromDate(String date, String format) {
		String result = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			SimpleDateFormat sdf2 = new SimpleDateFormat(format, Locale.getDefault());
			Date dateDate = sdf.parse(date);
			result = sdf2.format(dateDate);
		} catch (ParseException e) {
			//nothing
		}
		return result;
	}
	
	public static List<String> get12Months(String date) {
		String[] months = new String[12];
		int endMonth = Integer.parseInt(date.split("-")[1]);
		for(int i = months.length - 1; i >= 0; i--) {
			months[i] = String.format("%02d", endMonth);
			endMonth--;
			if (endMonth < 1 ) {
				endMonth = 12;
			}
		}
		return Arrays.asList(months);
	}
	
}
