/**
 * 
 */
package com.ybg.ga.ymga.ui;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.ybg.ga.ymga.ga.xy.XYBean;

/**
 * @author 杨拔纲
 *
 */
public class XYHistoryScatterChart extends View {
	
	private int xPoint = 0; // 原点的X坐标
	private int yPoint = 0; // 原点的Y坐标
	private float xScale = 0; // X的刻度长度
	private float yScale = 0; // Y的刻度长度
	private int xLength = 0; // X轴的长度
	private int yLength = 0; // Y轴的长度
	private int maxWidth = 700; // 画布宽度
	private int maxHeight = 500; // 画布高度
	private float textSize = 0f; // 文字大小
	private List<XYBean> data = null;
	private Paint blackPaint = null;

	public XYHistoryScatterChart(Context context) {
		super(context);
	}
	
	/**
	 * 计算各项基础数据
	 */
	private void calculateScale() {
		textSize = maxHeight / 20f;
		xPoint = (int) (maxWidth * 0.02) + 30;
		yPoint = (int) (maxHeight * 0.98 - textSize);
		xLength = (int) ((maxWidth  - xPoint) * 0.9);
		yLength = (int) (maxHeight * 0.8 - textSize);
		yScale = yLength / 130f;
		xScale = xLength / 220f;
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);// 重写onDraw方法
		maxWidth = getWidth();
		maxHeight = getHeight();
		calculateScale();

		//canvas.drawColor(Color.BLUE);// 设置背景颜色
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);// 去锯齿
		paint.setColor(0xff23ac38);// 颜色
		paint.setTextSize(textSize); // 设置轴文字大小
		Paint tlPaint = new Paint();
		tlPaint.setStyle(Paint.Style.STROKE);
		tlPaint.setAntiAlias(true);// 去锯齿
		tlPaint.setColor(0xff23ac38);// 颜色
		tlPaint.setTextSize(textSize*0.8f); // 设置轴文字大小
		blackPaint = new Paint();
		blackPaint.setStyle(Paint.Style.STROKE);
		blackPaint.setAntiAlias(true);// 去锯齿
		blackPaint.setColor(0xff000000);// 颜色
		blackPaint.setTextSize(textSize); // 设置轴文字大小
		// 设置Y轴
		canvas.drawLine(xPoint, yPoint - yLength, xPoint, yPoint, paint); // 轴线
		canvas.drawLine(xPoint, yPoint - yLength, xPoint - 3, yPoint - yLength
				+ 6, paint); // 箭头
		canvas.drawLine(xPoint, yPoint - yLength, xPoint + 3, yPoint - yLength
				+ 6, paint);
		canvas.drawText("(舒张压)", xPoint - textSize*2, yPoint - yLength - 3, paint);
		canvas.drawText("低压", xPoint - textSize, yPoint - yLength - 3 - textSize, paint);
		// 设置X轴
		canvas.drawLine(xPoint, yPoint, xPoint + xLength, yPoint, paint); // 轴线
		canvas.drawLine(xPoint + xLength, yPoint, xPoint + xLength - 6,
				yPoint - 3, paint); // 箭头
		canvas.drawLine(xPoint + xLength, yPoint, xPoint + xLength - 6,
				yPoint + 3, paint);
		canvas.drawText("高压", xPoint + 200 * xScale + textSize, yPoint - 6 - textSize, paint);
		canvas.drawText("(收缩压)", xPoint + 200 * xScale, yPoint - 6, paint);
		
		// 画矩形
		drawRectBG(canvas, tlPaint);
		
		// 画数据
		drawXYData(canvas);
	}
	
	public void setXYData(List<XYBean> data) {
		this.data = data;
	}
	
	private void drawXYData(Canvas canvas) {
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);// 去锯齿
		paint.setTextSize(textSize);
		paint.setColor(0xffffffff);
		if (data == null || data.size() == 0) {
			canvas.drawText("没有数据", xPoint + 80, yPoint - 80, paint);
		} else {
			for(XYBean bean : data) {
				canvas.drawCircle(xPoint +  bean.getSys() * xScale, yPoint - bean.getDia() * yScale, textSize/4, paint);
			}
		}
	}

	private void drawRectBG(Canvas canvas, Paint paint) {
		float y = maxHeight * 0.04f;
		Paint paint2 = new Paint();
		paint2.setStyle(Paint.Style.FILL);
		paint2.setColor(0xffe50012);
		canvas.drawRect(xPoint, yPoint - 120 * yScale, xPoint + 200 * xScale, yPoint, paint2);
		canvas.drawRect(xPoint + 130 * xScale, 4*y, xPoint + 130 * xScale + 60, 5*y, paint2);
		canvas.drawText("重度高血压", xPoint + 130 * xScale + 65, 5*y, paint);
		
		Paint paint3 = new Paint();
		paint3.setStyle(Paint.Style.FILL);
		paint3.setColor(0xffea6208);
		canvas.drawRect(xPoint, yPoint - 110 * yScale, xPoint + 180 * xScale, yPoint, paint3);
		canvas.drawRect(xPoint + 130 * xScale, 2*y, xPoint + 130 * xScale + 60, 3*y, paint3);
		canvas.drawText("中等高血压", xPoint + 130 * xScale + 65, 3*y, paint);
		canvas.drawText(""+110, xPoint - 42, yPoint - 110 * yScale, blackPaint);
		canvas.drawText(""+180, xPoint + 180 * xScale, yPoint + textSize, blackPaint);
		
		Paint paint4 = new Paint();
		paint4.setStyle(Paint.Style.FILL);
		paint4.setColor(0xfff19c00);
		canvas.drawRect(xPoint, yPoint - 100 * yScale, xPoint + 160 * xScale, yPoint, paint4);
		canvas.drawRect(xPoint + 130 * xScale, 0, xPoint + 130 * xScale + 60, y, paint4);
		canvas.drawText("轻微高血压", xPoint + 130 * xScale + 65, y, paint);
		canvas.drawText(""+100, xPoint - 42, yPoint - 100 * yScale, blackPaint);
		canvas.drawText(""+160, xPoint + 160 * xScale, yPoint + textSize, blackPaint);
		
		Paint paint5 = new Paint();
		paint5.setStyle(Paint.Style.FILL);
		paint5.setColor(0xfffff000);
		canvas.drawRect(xPoint, yPoint - 90 * yScale, xPoint + 140 * xScale, yPoint, paint5);
		canvas.drawRect(xPoint + 60, 4*y, xPoint + 120, 5*y, paint5);
		canvas.drawText("正常高压", xPoint + 125, 5*y, paint);
		canvas.drawText(""+90, xPoint - 42, yPoint - 90 * yScale, blackPaint);
		canvas.drawText(""+140, xPoint + 140 * xScale, yPoint + textSize, blackPaint);
		
		Paint paint6 = new Paint();
		paint6.setStyle(Paint.Style.FILL);
		paint6.setColor(0xff90c41b);
		canvas.drawRect(xPoint, yPoint - 85 * yScale, xPoint + 130 * xScale, yPoint, paint6);
		canvas.drawRect(xPoint + 60, 2*y, xPoint + 120, 3*y, paint6);
		canvas.drawText("正常血压", xPoint + 125, 3*y, paint);
		canvas.drawText(""+85, xPoint - 42, yPoint - 80 * yScale, blackPaint);
		canvas.drawText(""+130, xPoint + 120 * xScale, yPoint + textSize, blackPaint);
		
		Paint paint7 = new Paint();
		paint7.setStyle(Paint.Style.FILL);
		paint7.setColor(0xff0a7c24);
		canvas.drawRect(xPoint, yPoint - 80 * yScale, xPoint + 120 * xScale, yPoint, paint7);
		canvas.drawRect(xPoint + 60, 0, xPoint + 120, y, paint7);
		canvas.drawText("理想血压", xPoint + 125, y, paint);
		canvas.drawText(""+80, xPoint - 42, yPoint - 80 * yScale + textSize, blackPaint);
		canvas.drawText(""+120, xPoint + 120 * xScale - 2*textSize, yPoint + textSize, blackPaint);
	}
	
}
