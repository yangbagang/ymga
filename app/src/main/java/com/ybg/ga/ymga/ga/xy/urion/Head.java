/**
 * 
 */
package com.ybg.ga.ymga.ga.xy.urion;


/**
 * @author 杨拔纲
 *
 */
public class Head {

	/**
	 * 测量结果
	 */
	public final static int TYPE_RESULT = 0xFC;
	/**
	 * 错误信息
	 */
	public final static int TYPE_ERROR = 0xFD;
	/**
	 * 测量开始
	 */
	public final static int TYPE_MESSAGE = 0x06;
	/**
	 * 压力数据 测量过程信息
	 */
	public final static int TYPE_PRESSURE = 0xFB;

	private int head1;

	private int head2;

	private int type;

	public Head() {

	}

	public int getHead1() {
		return head1;
	}

	public void setHead1(int head1) {
		this.head1 = head1;
	}

	public int getHead2() {
		return head2;
	}

	public void setHead2(int head2) {
		this.head2 = head2;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void analysis(int[] i) {
		head1 = i[1];
		head2 = i[1];
		type = i[2];
	}
	
}
