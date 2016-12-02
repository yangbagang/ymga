/**
 * 
 */
package com.ybg.ga.ymga.ga.xy;

/**
 * @author 杨拔纲
 *
 */
public class XYBean {

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
	
	private String date;
	
	public XYBean(){}
	
	public XYBean(String date, int sys, int dia, int pul) {
		this.date = date;
		this.sys = sys;
		this.dia = dia;
		this.pul = pul;
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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "XYBean [date=" + date + ", sys=" + sys + ", dia=" + dia + ", pul=" + pul + "]";
	}
		
}
