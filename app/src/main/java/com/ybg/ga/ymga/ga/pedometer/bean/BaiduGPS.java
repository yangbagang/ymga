/**
 * 
 */
package com.ybg.ga.ymga.ga.pedometer.bean;

import com.baidu.mapapi.model.LatLng;

/**
 * @author 杨拔纲
 * 
 */
public class BaiduGPS {

	private int retType;

	private double latitude;

	private double longitude;

	public BaiduGPS(int retType, double latitude, double longitude) {
		super();
		this.retType = retType;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public int getRetType() {
		return retType;
	}

	public void setRetType(int retType) {
		this.retType = retType;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public LatLng getLatLng() {
		return new LatLng(latitude, longitude);
	}

	public boolean isGPSLocation() {
		return retType == 61;
	}
}
