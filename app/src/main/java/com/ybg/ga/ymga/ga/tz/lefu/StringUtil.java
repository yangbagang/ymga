/**
 * 
 */
package com.ybg.ga.ymga.ga.tz.lefu;

/**
 * @author 杨拔纲
 *
 */
public class StringUtil {

	public static int[] hexStringToIntArray(String digits) {
		int[] bb = new int[digits.length()/2];
		try {
			for (int i = 0; i < digits.length(); i += 2) {
				char c1 = digits.charAt(i);
				if (i + 1 >= digits.length()) {
					throw new IllegalArgumentException("hexUtil.odd");
				}
				char c2 = digits.charAt(i + 1);
				int b = 0;
				if ((c1 >= '0') && (c1 <= '9'))
					b = b + (c1 - '0') * 16;
				else if ((c1 >= 'a') && (c1 <= 'f'))
					b = b + (c1 - 'a' + 10) * 16;
				else if ((c1 >= 'A') && (c1 <= 'F'))
					b = b + (c1 - 'A' + 10) * 16;
				else {
					throw new IllegalArgumentException("hexUtil.bad");
				}
				if ((c2 >= '0') && (c2 <= '9'))
					b = b + (c2 - '0');
				else if ((c2 >= 'a') && (c2 <= 'f'))
					b = b + (c2 - 'a' + 10);
				else if ((c2 >= 'A') && (c2 <= 'F'))
					b = b + (c2 - 'A' + 10);
				else
					throw new IllegalArgumentException("hexUtil.bad");
				bb[i/2] = b;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		return bb;
	}
	
}
