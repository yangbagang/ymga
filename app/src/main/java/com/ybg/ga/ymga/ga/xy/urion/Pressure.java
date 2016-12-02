/**
 * 
 */
package com.ybg.ga.ymga.ga.xy.urion;

/**
 * @author 杨拔纲
 * 
 */
public class Pressure extends IBean {

	private int PressureH;
	private int PressureL;

	public Pressure() {
		super();
	}

	public int getPressure() {
		return PressureH * 256 + PressureL;
	}

	public int getPressureHL() {
		return PressureH + PressureL;
	}

	public int getPressureH() {
		return PressureH;
	}

	public void setPressureH(int pressureH) {
		PressureH = pressureH;
	}

	public int getPressureL() {
		return PressureL;
	}

	public void setPressureL(int pressureL) {
		PressureL = pressureL;
	}

	public void analysis(int[] f) {
		PressureH = f[3];
		PressureL = f[4];
	}

	public String getStringValue() {
		return PressureH + "," + PressureL;
	}
}
