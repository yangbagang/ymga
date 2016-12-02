/**
 * 
 */
package com.ybg.ga.ymga.ui;

import java.util.List;

import com.ybg.ga.ymga.ga.xy.XYBean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * @author 杨拔纲
 * 
 */
public class XYHistoryMultiLineChart extends View {

	private int xPoint = 0; // 原点的X坐标
	private int yPoint = 0; // 原点的Y坐标
	private float xScale = 0; // X的刻度长度
	private float yScale = 0; // Y的刻度长度
	private int xLength = 0; // X轴的长度
	private int yLength = 0; // Y轴的长度
	private int maxWidth = 700; // 画布宽度
	private int maxHeight = 500; // 画布高度
	private float textSize = 0f; // 文字大小
	private float radius = 0f;
	private List<String> xValues = null;
	private List<XYBean> data = null;
	private Paint paint = null;// 全局画笔
	private Paint blackPaint = null;
	private Paint netPaint = null;// 格子画笔
	private Paint sysPaint = null;// 高压画笔
	private Paint diaPaint = null;// 低压画笔
	private Paint pulPaint = null;// 心率画笔

	public XYHistoryMultiLineChart(Context context) {
		super(context);
	}

	/**
	 * 计算各项基础数据
	 */
	private void calculateScale() {
		textSize = maxHeight / 20f;
		radius = textSize / 3f;
		xPoint = (int) (maxWidth * 0.02) + 30;
		yPoint = (int) (maxHeight * 0.98 - textSize);
		xLength = (int) ((maxWidth - xPoint) * 0.9);
		yLength = (int) (maxHeight * 0.9 - textSize);
		yScale = yLength / 260f;
		if (xValues != null && xValues.size() > 0) {
			xScale = xLength / xValues.size();
		}
	}

	public void setXYData(List<XYBean> data) {
		this.data = data;
	}

	public void setXValues(List<String> xValues) {
		this.xValues = xValues;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 获取宽高
		maxWidth = getWidth();
		maxHeight = getHeight();
		calculateScale();
		// 画XY轴
		drawXYLine(canvas);

		// 画数据
		drawXYData(canvas);
	}

	private void drawXYLine(Canvas canvas) {
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);// 去锯齿
		paint.setColor(0xff23ac38);// 颜色
		paint.setTextSize(textSize); // 设置轴文字大小
		blackPaint = new Paint();
		blackPaint.setStyle(Paint.Style.STROKE);
		blackPaint.setAntiAlias(true);// 去锯齿
		blackPaint.setColor(0xff000000);// 颜色
		blackPaint.setTextSize(textSize); // 设置轴文字大小
		netPaint = new Paint();
		netPaint.setStyle(Paint.Style.STROKE);
		netPaint.setAntiAlias(true);// 去锯齿
		netPaint.setColor(0xffd1d1d1);// 颜色
		// 设置Y轴
		canvas.drawLine(xPoint, yPoint - yLength, xPoint, yPoint, paint); // 轴线
		canvas.drawLine(xPoint, yPoint - yLength, xPoint - 3, yPoint - yLength
				+ 6, paint); // 箭头
		canvas.drawLine(xPoint, yPoint - yLength, xPoint + 3, yPoint - yLength
				+ 6, paint);
		// 画Y轴刻度
		int i = 20;
		while (i < 220) {
			canvas.drawText("" + i, xPoint - 42, yPoint - i * yScale + textSize
					/ 2, blackPaint);
			canvas.drawLine(xPoint, yPoint - i * yScale, xPoint - 3, yPoint - i
					* yScale, paint);
			canvas.drawLine(xPoint, yPoint - i * yScale,
					xPoint + (xValues.size() - 1) * xScale,
					yPoint - i * yScale, netPaint);
			i += 20;
		}
		// 设置X轴
		canvas.drawLine(xPoint, yPoint, xPoint + xLength, yPoint, paint);
		canvas.drawLine(xPoint + xLength, yPoint, xPoint + xLength - 6,
				yPoint - 3, paint); // 箭头
		canvas.drawLine(xPoint + xLength, yPoint, xPoint + xLength - 6,
				yPoint + 3, paint);
		// 画X轴刻度
		int xsize = xValues.size();
		for (int j = 0; j < xsize; j++) {
			canvas.drawLine(xPoint + j * xScale, yPoint, xPoint + j * xScale,
					yPoint - 3, paint);
			if (xsize < 15 || j % 3 == 0 || j == xsize - 1) {
				canvas.drawText(xValues.get(j), xPoint + j * xScale - textSize
						/ 2, yPoint + textSize, blackPaint);
			}
			if (j != 0) {
				canvas.drawLine(xPoint + j * xScale, yPoint, xPoint + j
						* xScale, yPoint - 200 * yScale, netPaint);
			}
		}

		// 画图例
		sysPaint = new Paint();
		sysPaint.setStyle(Paint.Style.FILL);
		sysPaint.setAntiAlias(true);// 去锯齿
		sysPaint.setColor(0xff138e49);// 颜色
		canvas.drawCircle(xPoint + 30, 50, radius, sysPaint);
		canvas.drawText("高压(收缩压)", xPoint + 35 + 2 * radius, 50 + radius, paint);
		diaPaint = new Paint();
		diaPaint.setStyle(Paint.Style.FILL);
		diaPaint.setAntiAlias(true);// 去锯齿
		diaPaint.setColor(0xff1c6eb7);// 颜色
		canvas.drawCircle(xPoint + 30 + xLength / 3, 50, radius, diaPaint);
		canvas.drawText("低压(舒张压)", xPoint + 35 + xLength / 3 + 2 * radius,
				50 + radius, paint);
		pulPaint = new Paint();
		pulPaint.setStyle(Paint.Style.FILL);
		pulPaint.setAntiAlias(true);// 去锯齿
		pulPaint.setColor(0xffd4181a);// 颜色
		canvas.drawCircle(xPoint + 30 + xLength / 3 * 2, 50, radius, pulPaint);
		canvas.drawText("心率(脉搏)", xPoint + 35 + xLength / 3 * 2 + 2 * radius,
				50 + radius, paint);
	}

	private void drawXYData(Canvas canvas) {
		if (data == null || data.size() == 0) {
			canvas.drawText("没有数据", xPoint + 80, yPoint - 80, paint);
		} else {
			for (XYBean bean : data) {
				canvas.drawCircle(xPoint + getIndex(bean.getDate()) * xScale,
						yPoint - bean.getSys() * yScale, radius/2, sysPaint);
				canvas.drawCircle(xPoint + getIndex(bean.getDate()) * xScale,
						yPoint - bean.getDia() * yScale, radius/2, diaPaint);
				canvas.drawCircle(xPoint + getIndex(bean.getDate()) * xScale,
						yPoint - bean.getPul() * yScale, radius/2, pulPaint);
			}
		}
	}

	private int getIndex(String date) {
		if (xValues == null || xValues.size() == 0)
			return 0;
		if (xValues.contains(date)) {
			for (int i = 0; i < xValues.size(); i++) {
				if (xValues.get(i).equals(date))
					return i;
			}
		}
		return 0;
	}
}
