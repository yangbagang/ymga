/**
 * 
 */
package com.ybg.ga.ymga.util;

/**
 * @author 杨拔纲
 *
 */
public final class ByteUtil {

	public final static int byteToInt(byte b) {
		return 255 & b;
	}
	
	public final static int byteToInt(byte b, int level) {
		return (Double.valueOf((byteToInt(b) * Math.pow(256, level - 1)))).intValue();
	}
}
