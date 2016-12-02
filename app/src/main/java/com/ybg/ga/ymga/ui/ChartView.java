/**
 * 
 */
package com.ybg.ga.ymga.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * @author 杨拔纲
 * 
 */
public class ChartView extends View {

	private float xPoint = 40; // 原点的X坐标
	private float yPoint = 260; // 原点的Y坐标
	private float XScale = 60; // X的刻度长度
	private float YScale = 40; // Y的刻度长度
	private float XLength = 300; // X轴的长度
	private float YLength = 200; // Y轴的长度
	private String[] XLabel; // X的刻度
	private String[] YLabel; // Y的刻度
	private String[] Data; // 数据
	private float textSize = 0f; // 文字大小

	public ChartView(Context context, int xLength, int yLength, int xsize,
			int ysize) {
		super(context);
		YLength = yLength - 80;
		yPoint = yLength - 60;
		YScale = YLength / ysize;
		
		textSize = yLength / 25f;
		xPoint = xPoint + textSize * 2;
		XLength = xLength - xPoint - textSize;
		XScale = XLength / xsize;
	}

	public void setInfo(String[] XLabels, String[] YLabels, String[] AllData) {
		XLabel = XLabels;
		YLabel = YLabels;
		Data = AllData;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);// 重写onDraw方法

		// canvas.drawColor(Color.WHITE);// 设置背景颜色
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);// 去锯齿
		paint.setColor(0xff23ac38);// 颜色
		paint.setTextSize(textSize); // 设置轴文字大小
		paint.setStrokeWidth(2);
		// 设置Y轴
		canvas.drawLine(xPoint, yPoint - YLength, xPoint, yPoint, paint); // 轴线
		for (int i = 0; i * YScale < YLength; i++) {
			canvas.drawLine(xPoint, yPoint - i * YScale, xPoint + 5, yPoint - i
					* YScale, paint); // 刻度
			try {
				paint.setStrokeWidth(0);
				canvas.drawText(YLabel[i], xPoint - textSize * 2,
						yPoint - i * YScale + 5, paint); // 文字
			} catch (Exception e) {
			}
		}
		paint.setStrokeWidth(2);
		canvas.drawLine(xPoint, yPoint - YLength, xPoint - 3, yPoint - YLength
				+ 6, paint); // 箭头
		canvas.drawLine(xPoint, yPoint - YLength, xPoint + 3, yPoint - YLength
				+ 6, paint);
		// 设置X轴
		canvas.drawLine(xPoint, yPoint, xPoint + XLength, yPoint, paint); // 轴线
		int xsize = XLabel.length;
		for (int i = 0; i * XScale < XLength; i++) {
			canvas.drawLine(xPoint + i * XScale, yPoint, xPoint + i * XScale,
					yPoint - 5, paint); // 刻度
			try {
				if (xsize < 15 || i % 3 == 0 || i == xsize - 1) {
					paint.setStrokeWidth(0);
					canvas.drawText(XLabel[i], xPoint + i * XScale - 10,
						yPoint + 20, paint); // 文字
				}
				// 数据值
				paint.setStrokeWidth(2);
				if (i > 0 && YCoord(Data[i - 1]) != -999
						&& YCoord(Data[i]) != -999) // 保证有效数据
					canvas.drawLine(xPoint + (i - 1) * XScale,
							YCoord(Data[i - 1]), xPoint + i * XScale,
							YCoord(Data[i]), paint);
				canvas.drawCircle(xPoint + i * XScale, YCoord(Data[i]), 2,
						paint);
				if (XLabel.length < 10 && !"0".equals(Data[i]) && i > 0) {
					paint.setStrokeWidth(0);
					canvas.drawText(Data[i], xPoint + i * XScale,
							YCoord(Data[i]), paint);// 画出数值
				}
			} catch (Exception e) {
			}
		}
		paint.setStrokeWidth(2);
		canvas.drawLine(xPoint + XLength, yPoint, xPoint + XLength - 6,
				yPoint - 3, paint); // 箭头
		canvas.drawLine(xPoint + XLength, yPoint, xPoint + XLength - 6,
				yPoint + 3, paint);
	}

	// 计算绘制时的Y坐标，无数据时返回-999
	private float YCoord(String y0) {
		float y;
		try {
			y = Float.parseFloat(y0);
		} catch (Exception e) {
			return -999; // 出错则返回-999
		}
		try {
			return yPoint - y * YScale / Float.parseFloat(YLabel[1]);
		} catch (Exception e) {
		}
		return y;
	}

}
