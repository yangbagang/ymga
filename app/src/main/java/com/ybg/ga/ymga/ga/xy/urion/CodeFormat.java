package com.ybg.ga.ymga.ga.xy.urion;

public final class CodeFormat {

	/**
	 * 把字节数组转换成16进制字符串
	 * 
	 * @param bArray
	 * @return
	 */
	public static final int[] bytesToHexStringTwo(byte[] bArray, int count) {
		int[] fs = new int[count];
		for (int i = 0; i < count; i++) {
			fs[i] = (0xFF & bArray[i]);
		}
		return fs;
	}

}
