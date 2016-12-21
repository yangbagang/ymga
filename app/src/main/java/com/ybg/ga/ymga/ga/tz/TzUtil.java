package com.ybg.ga.ymga.ga.tz;

import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * Created by yangbagang on 15/8/6.
 */
public class TzUtil {

    //此方法计算得到的值不准，因为热量的值太高。
    public static int getCalAge(float bodyWeight, int jcValue) {
        UserPreferences userPreferences = UserPreferences.getInstance();
        int sex = userPreferences.getUserSex();
        int height = (int) (userPreferences.getBodyHigh() * 100);//convert to cm
        int calJc = (int) (jcValue / 1.35);//由于基础热量过高，除以系数进行平衡。
        //BMR(male)=(13.7*bodyWeight)+(5.0*bodyHeight)-(6.8*age)+66
        //BMR(female)=(9.6*bodyWeight)+(1.8*bodyHeight)-(4.7*age)+655
        //男性：BMR = 10 * 体重(KG)+ 6.25 * 身高(CM)- 5 * 年龄 + 5
        //女性：BMR = 10 * 体重(KG)+ 6.25 * 身高(CM)- 5 * 年龄 – 161
        if (sex == AppConstat.SEX_MALE) {
            return (int) ((13.7 * bodyWeight + 5.0 * height + 66 - calJc) / 6.8);
        }
        return (int) ((9.6 * bodyWeight + 1.8 * height + 655 - calJc) / 4.7);
    }

    public static void main(String[] args) {

    }

    public final static String TZ_DEVICE_LEFU_BT = "lefu_bt";
    public final static String TZ_DEVICE_LEFU_BLE = "lefu_ble";
    public final static String TZ_DEVICE_FURUIK = "furuik1";
}
