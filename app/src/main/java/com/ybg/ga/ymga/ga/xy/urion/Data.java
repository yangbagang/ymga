/**
 * 
 */
package com.ybg.ga.ymga.ga.xy.urion;

/**
 * @author 杨拔纲
 *
 */
public class Data extends IBean {

	private String value;
	private int time;
	/**
	 * 收缩压
	 */
	private int sys;
	/**
	 * 舒张压
	 */
	private int dia;
	/**
	 * 心率
	 */
	private int pul;

	public Data() {
		super();
	}

	public Data(String value) {
		super();
		this.value = value;
	}

	public Data(String value, int time, int sys, int dia, int pul) {
		super();
		this.value = value;
		this.time = time;
		this.sys = sys;
		this.dia = dia;
		this.pul = pul;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getSys() {
		return sys;
	}

	public void setSys(int sys) {
		this.sys = sys;
	}

	public int getDia() {
		return dia;
	}

	public void setDia(int dia) {
		this.dia = dia;
	}

	public int getPul() {
		return pul;
	}

	public void setPul(int pul) {
		this.pul = pul;
	}

	public void analysis(int[] f) {
		sys = f[3];
		dia = f[4];
		pul = f[5];
		//System.out.println("SYS:" + sys + " DIA:" + dia + " PUL:" + pul);
	}
	
	public String getStringValue() {
		return sys + "," + dia + "," + pul;
	}
}
