package com.ybg.ga.ymga.ga.tw;

/**
 * 温度转换工具
 * Created by yangbagang on 2015/5/29.
 */
public class WenduTool {

    /**
     * 摄氏度转华氏度
     *
     * @param c 摄氏度
     * @return 华氏度
     */
    public static float c2f(float c) {
        float a = c * 1.8f + 32;
        int b = (int) (a * 10);
        return b / 10f;
    }

    /**
     * 华氏度转摄氏度
     *
     * @param f 华氏度
     * @return 摄氏度
     */
    public static float f2c(float f) {
        float a = (f - 32) / 1.8f;
        int b = (int) (a * 10);
        return b / 10f;
    }

}
