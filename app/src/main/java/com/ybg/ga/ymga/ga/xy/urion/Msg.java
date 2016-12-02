/**
 * 
 */
package com.ybg.ga.ymga.ga.xy.urion;

/**
 * @author 杨拔纲
 *
 */
public class Msg extends IBean {

	public static final int MESSAGE_STATE_CONNECTING = 0;
	public static final int MESSAGE_STATE_NONE = 1;
	public static final int MESSAGE_STATE_CONNECTED = 2;

	public static final int START = 0x06;

	private int msg_code;
	private String device_name;

	public Msg() {
		super();
	}

	public Msg(int msgCode, String deviceName) {
		super();
		msg_code = msgCode;
		device_name = deviceName;
	}

	public int getMsg_code() {
		return msg_code;
	}

	public void setMsg_code(int msgCode) {
		msg_code = msgCode;
	}

	public String getDevice_name() {
		return device_name;
	}

	public void setDevice_name(String deviceName) {
		device_name = deviceName;
	}
	
	public void analysis(int[] i) {
		msg_code = i[2];
	}
	
	public String getStringMsg() {
		return "" + msg_code;
	}
}
