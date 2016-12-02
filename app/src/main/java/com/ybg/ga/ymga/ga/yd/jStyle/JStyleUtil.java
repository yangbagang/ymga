/**
 * 
 */
package com.ybg.ga.ymga.ga.yd.jStyle;

/**
 * @author 杨拔纲
 *
 */
public class JStyleUtil {

	/**
	 * 生成校验字节
	 * @param source
	 * @return
	 */
	public final static void genCRCByte(byte[] source) {
		int sum = 0;
		for(int i = 0; i < source.length - 1; i++) {
			sum += source[i];
		}
		source[source.length - 1] = (byte) (sum & 0xff);
	}
	
	/**
	 * 检查CRC码是否正确
	 * @param source
	 * @return
	 */
	public final static boolean verifyCRC(byte[] source) {
		if(source == null || source.length != 16) {
			return false;
		}
		int sum = 0;
		for(int i = 0; i < 15; i++) {
			sum += source[i];
		}
		return (byte)(sum & 0xff) == source[15];
	}
	
	public static void main() {
		byte[] uuid = {(byte) 0xff, (byte) 0xf6 ,0,0,0 ,0,0,0 ,0,0,0 ,0,0,0 ,0,0};
		JStyleUtil.genCRCByte(uuid);
		System.out.println(uuid[15]);
	}
}
