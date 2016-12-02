/**
 * 
 */
package com.ybg.ga.ymga.ga.tz.lefu;

/**
 * @author 杨拔纲
 * 
 */
public class CRUtil {

	public final static TZCFRecoder parseMessage(String msg) {
		if (msg == null || msg.length() != 32) {
			return null;
		}
		TZCFRecoder recoder = new TZCFRecoder();
		int[] tz = StringUtil.hexStringToIntArray(msg);

		// Byte1: 设备类型：CF 表示脂肪秤，CE表示人体秤，CB 表示婴儿秤，CA 表示厨房秤
		float jingdu = 0.1f;// 精度

		if (tz[0] == 0xcf) {
			recoder.setScaleType("脂肪秤");
			jingdu = 0.1f;
		} else if (tz[0] == 0xce) {
			recoder.setScaleType("人体秤");
			jingdu = 0.1f;
		} else if (tz[0] == 0xcb) {
			recoder.setScaleType("婴儿秤");
			jingdu = 0.01f;
		} else if (tz[0] == 0xca) {
			recoder.setScaleType("厨房秤");
			jingdu = 0.001f;
		}

		// Byte2： 此字节高4位表示运动员级别：bit7-bit4运动员级别表示位：=0普通，=1业余，=2专业)
		// 此字节低4位是表示用户组号：Bit3-bit0 ，从P0-P9
		int level = tz[1] >> 4;
		if (level == 0) {
			recoder.setLevel("普通");
		} else if (level == 1) {
			recoder.setLevel("业余");
		} else if (level == 2) {
			recoder.setLevel("专业");
		}

		recoder.setGroup(tz[1] & 0x0f);

		// Byte3: Bit7=1为男性，=0为女性，Bit6-Bit0为年龄
		recoder.setAge(tz[2] & 0x7f);
		if ((tz[2] >> 7) == 1) {
			recoder.setSex("男");
		} else {
			recoder.setSex("女");
		}

		// Byte4： 身高，分辨率为1CM
		recoder.setHeight(tz[3]);

		// Byte5： 体重高字节,Byte6： 体重低字节
		recoder.setWeight((tz[4] * 256 + tz[5]) * jingdu);

		// Byte7： 脂肪高字节,Byte8： 脂肪低字节，分辨率是0.1%
		recoder.setBodyFat((tz[6] * 256 + tz[7]) * 0.1f);

		// Byte9： 骨骼，分辨率是0.1%
		recoder.setBone((tz[8] * 0.1f) / recoder.getWeight() * 100);

		// Byte10：肌肉高字节,Byte11：肌肉低字节，分辨率是0.1%
		recoder.setJirou((tz[9] * 256 + tz[10]) * 0.1f);

		// Byte12：内脏脂肪等级，分辨率是1
		recoder.setNeiZhang(tz[11]);

		// Byte13 水份高字节，Byte14 水份低字节，分辨率是0.1%
		recoder.setBodyWater((tz[12] * 256 + tz[13]) * 0.1f);

		// Byte15：热量高字节，Byte16：热量低字节，分辨率是1
		recoder.setCalorie(tz[14] * 256 + tz[15]);
		return recoder;
	}
}
