package com.ybg.ga.ymga.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by yangbagang on 15/6/4.
 */
public class NoZeroLineChartView extends View {

    private float maxWidth = 0f;
    private float maxHeight = 0f;
    private float xOffset = 0f;
    private float yOffset = 0f;
    private float[] data = null;
    private Paint paint = new Paint();
    private float textSize = 0f;

    private float maxValue = 0f;
    private float minValue = 0f;
    private float xScale = 0f;
    private float yScale = 0f;

    public NoZeroLineChartView(Context context, float[] data) {
        super(context);
        this.data = data;
    }

    private void initXYValue() {
        maxWidth = getMeasuredWidth();
        maxHeight = getMeasuredHeight();
        textSize = Math.min(maxWidth, maxHeight) / 20f;
    }

    private void initPaint() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);// 去锯齿
        paint.setColor(0xff23ac38);// 颜色
        paint.setTextSize(textSize); // 设置轴文字大小
        //paint.setStrokeWidth(2);
    }

    private void initScaleLimit() {
        if (data != null) {
            for (float value : data) {
                if (value != 0) {
                    if (maxValue == 0 || value > maxValue) {
                        maxValue = value;
                    }
                    if (minValue == 0 || value < minValue) {
                        minValue = value;
                    }
                }
            }
            yScale = maxHeight * 0.9f / 4;
            if (data.length > 1) {
                xScale = maxWidth * 0.9f / (data.length - 1);
            } else {
                xScale = maxWidth * 0.9f / data.length;
            }
            xOffset = maxWidth * 0.05f;
            yOffset = maxHeight * 0.95f;
        }
    }

    public void onDraw(Canvas canvas) {
        // 计算必要的尺寸值
        initXYValue();
        // 初始化取值范围
        initScaleLimit();
        // 断路判断
        if (data == null || data.length == 0 || maxValue == 0) {
            paint.setTextSize(textSize * 1.3f);
            canvas.drawText("没有相关数据", maxWidth * 0.3f, maxHeight * 0.5f, paint);
            return;
        }
        if (data.length == 1) {
            maxValue = data[0] * 1.5f;
            minValue = data[0] * 0.5f;
        }
        // 初始化画笔
        initPaint();
        // 画第一个值
        canvas.drawCircle(xOffset, yOffset - getYValue(data[0]), 3, paint);
        canvas.drawText("" + data[0], xOffset - textSize, yOffset - getYValue(data[0]) - 5, paint);
        // 画其它值与线
        if (data.length > 1) {
            for (int i = 1; i < data.length; i++) {
                // 画线
                canvas.drawLine(xOffset + (i - 1) * xScale, yOffset - getYValue(data[i - 1]), xOffset + i * xScale, yOffset - getYValue(data[i]), paint);
                // 画第i个值
                canvas.drawCircle(xOffset + i * xScale, yOffset - getYValue(data[i]), 3, paint);
                canvas.drawText("" + data[i], xOffset + i * xScale - textSize, yOffset - getYValue(data[i]) - 5, paint);
            }
        }
    }

    private float getYValue(float value) {
        float validValue = value == 0 ? minValue : value;
        float yScaleMaxValue = maxValue - minValue;
        if (yScaleMaxValue == 0) yScaleMaxValue = 2;
        return (validValue - minValue) * maxHeight * 0.9f / yScaleMaxValue;
    }

}
