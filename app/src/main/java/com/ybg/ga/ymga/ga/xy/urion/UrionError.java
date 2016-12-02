/**
 * 
 */
package com.ybg.ga.ymga.ga.xy.urion;

/**
 * @author 杨拔纲
 * 
 */
public class UrionError extends IBean {

	/**
	 * 连接失败
	 */
	public static final int ERROR_CONNECTION_FAILED = 0;
	/**
	 * 连接丢失
	 */
	public static final int ERROR_CONNECTION_LOST = 1;

	// 血压仪错误信息常量
	/**
	 * E-E EEPROM异常
	 */
	public static final int ERROR_EEPROM = 0x0E;
	/**
	 * E-1 人体心跳信号太小或压力突降
	 */
	public static final int ERROR_HEART = 0x01;
	/**
	 * E-2 杂讯干扰
	 */
	public static final int ERROR_DISTURB = 0x02;
	/**
	 * E-3 充气时间过长
	 */
	public static final int ERROR_GASING = 0x03;
	/**
	 * E-4 测得的结果异常
	 */
	public static final int ERROR_TEST = 0x05;
	/**
	 * E-C 校正异常
	 */
	public static final int ERROR_REVISE = 0x0C;
	/**
	 * E-B 电源低电压
	 */
	public static final int ERROR_POWER = 0x0B;

	/**
	 * 错误代码，该错误代码分为连接时的错误(int类型)和连接后血压仪发送的错误(float类型)
	 */
	private int error_code;

	private int error;

	public UrionError() {
		super();
	}

	public UrionError(int errorCode) {
		super();
		error_code = errorCode;
	}

	public int getError_code() {
		return error_code;
	}

	public void setError_code(int errorCode) {
		error_code = errorCode;
	}

	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}

	public void analysis(int[] f) {
		error = f[3];
	}

	public String getHumanErrorMsg() {
		String msg = "";
		switch (error) {
		case ERROR_EEPROM:
			msg = "EEPROM异常";
			break;
		case ERROR_HEART:
			msg = "人体心跳信号太小或压力突降";
			break;
		case ERROR_DISTURB:
			msg = "杂讯干扰";
			break;
		case ERROR_GASING:
			msg = "充气时间过长";
			break;
		case ERROR_TEST:
			msg = "测得的结果异常";
			break;
		case ERROR_REVISE:
			msg = "校正异常";
			break;
		case ERROR_POWER:
			msg = "电源低电压";
			break;
		}
		return msg;
	}
}
