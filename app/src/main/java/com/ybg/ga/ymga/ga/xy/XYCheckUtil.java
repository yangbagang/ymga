/**
 * 
 */
package com.ybg.ga.ymga.ga.xy;

import com.ybg.ga.ymga.R;

/**
 * @author 杨拔纲
 * 
 */
public class XYCheckUtil {

	public static int OVER_LOW = -1;

	public static int NORMAL = 0;

	public static int OVER_HIGH = 1;

	/**
	 * 16—20 115 7,3 110 70 21—25 115 73 110 71 26—30 115 7,5 112 73 31—35 117
	 * 76 114 74 36—40 120 80 116 77 41—45 120 8,1 120 78 46—50 120 8,2 120 79
	 * 51—55 120 8,4 120 80 56—60 120 84 120 82 61—65 120 8,6 120 83
	 */

	// private static int[][] xy = {
	// {115,73,110,70}
	// ,{115,73,110,70}
	// ,{115,73,110,71}
	// ,{115,75,112,73}
	// ,{117,76,114,74}
	// ,{120,80,116,77}
	// ,{120,81,120,78}
	// ,{120,82,120,79}
	// ,{120,84,120,80}
	// ,{120,84,120,82}
	// ,{120,86,120,83}
	// ,{120,86,120,83}
	// };
	//
	// public static int getXYNormalValue(float value, short sex, int age) {
	//
	// }

	public static int isHighCorrect(int value) {
		if (value < 90) {
			return OVER_LOW;
		} else if (value > 140) {
			return OVER_HIGH;
		} else {
			return NORMAL;
		}
	}

	public static int isLowCorrect(int value) {
		if (value < 60) {
			return OVER_LOW;
		} else if (value > 90) {
			return OVER_HIGH;
		} else {
			return NORMAL;
		}
	}

	public static int heartRate(int value) {
		if (value < 60) {
			return OVER_LOW;
		} else if (value > 100) {
			return OVER_HIGH;
		} else {
			return NORMAL;
		}
	}

	public static int getImageResourceId(int type, String strvValue) {
		int value = 0;
		try {
			value = Integer.valueOf(strvValue);
		} catch (NumberFormatException e) {
			value = 0;
		}
		int comp = 0;
		if (type == 0) {
			comp = isHighCorrect(value);
		} else if (type == 1) {
			comp = isLowCorrect(value);
		} else if (type == 2) {
			comp = heartRate(value);
		}
		switch (comp) {
		case -1:
			return R.mipmap.low;
		case 1:
			return R.mipmap.high;
		default:
			return R.mipmap.normal;
		}
	}
	
	public static String getNoticeMsg(String strHigh, String strLow, String strHeart) {
		try {
			int high = Integer.valueOf(strHigh);
			int low = Integer.valueOf(strLow);
			// 暂时不计较心跳
			//int heart = Integer.valueOf(strHeart);
			if (isHighCorrect(high) == 0 && isLowCorrect(low) == 0) {
				return "测试结果：您的血压灰常正常！请保持！";
			} else {
				return "测试结果：您的血压欠正常，需要留意。";
			}
		} catch (NumberFormatException e) {
			return "测试结果：未知。";
		}
	}
}
