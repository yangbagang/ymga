/**
 * 
 */
package com.ybg.ga.ymga.ga.tz.lefu;

import com.ybg.ga.ymga.ga.tz.TzUtil;

/**
 * @author 杨拔纲
 * 
 */
public class TZCFRecoder {

	private String scaleType;// CF=脂肪秤，CE=人体秤，CB=婴儿秤
	private int group;//用户组号

	private float weight;// 体重
	private float jirou;// 肌肉 
	private int neiZhang;// 内脏
	private int calorie;//卡路里 热量
	private float bone; // 骨骼 kg
	private float bodyWater; // 水份 百分比%

	private String level;//用户级别
	private String sex;//性别
	private float bodyFat; // 脂肪 百分比 %
	private int height; // 身高
	private int age; // 年龄
	public String getScaleType() {
		return scaleType;
	}
	public void setScaleType(String scaleType) {
		this.scaleType = scaleType;
	}
	public int getGroup() {
		return group;
	}
	public void setGroup(int group) {
		this.group = group;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public float getJirou() {
		return jirou;
	}
	public void setJirou(float jirou) {
		this.jirou = jirou;
	}
	public int getNeiZhang() {
		return neiZhang;
	}
	public void setNeiZhang(int neiZhang) {
		this.neiZhang = neiZhang;
	}
	public int getCalorie() {
		return calorie;
	}
	public void setCalorie(int calorie) {
		this.calorie = calorie;
	}
	public float getBone() {
		return bone;
	}
	public void setBone(float bone) {
		this.bone = bone;
	}
	public float getBodyWater() {
		return bodyWater;
	}
	public void setBodyWater(float bodyWater) {
		this.bodyWater = bodyWater;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public float getBodyFat() {
		return bodyFat;
	}
	public void setBodyFat(float bodyFat) {
		this.bodyFat = bodyFat;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public int getCalAge() {
		return TzUtil.getCalAge(weight, calorie);
	}

	public float getBMI() {
		return weight/(height * height);
	}

	public float getQZValue() {
		return (1 - bodyFat/100) * weight;
	}

	@Override
	public String toString() {
		return "TZCFRecoder [scaleType=" + scaleType + ", group=" + group
				+ ", weight=" + weight + ", jirou=" + jirou + ", neiZhang="
				+ neiZhang + ", calorie=" + calorie + ", bone=" + bone
				+ ", bodyWater=" + bodyWater + ", level=" + level + ", sex="
				+ sex + ", bodyFat=" + bodyFat + ", height=" + height
				+ ", age=" + age + "]";
	}
	
}
