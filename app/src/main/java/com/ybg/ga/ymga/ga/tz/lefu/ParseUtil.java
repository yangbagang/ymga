package com.ybg.ga.ymga.ga.tz.lefu;

import com.ybg.ga.ymga.ga.tz.TzBean;
import com.ybg.ga.ymga.user.UserPreferences;

/**
 * Created by yangbagang on 16/4/22.
 */
public class ParseUtil {

    private static java.text.DecimalFormat dfc = new java.text.DecimalFormat("#.#");

    public static String[] parse(String content) {
        byte[] data = hexStringToBytes(content);

        // 设别类型
        int v = data[0] & 0xFF;
        String typeRec = "脂肪秤";
        if (v == 0xcf) {
            typeRec = "脂肪秤";
        } else if (v == 0xce) {
            typeRec = "人体秤";
        } else if (v == 0xcb) {
            typeRec = "婴儿秤";
        } else if (v == 0xca) {
            typeRec = "厨房秤";
        }

        // 等级和组号
        int level = (data[1] >> 4) & 0xf;
        int group = data[1] & 0xf;

        String levelRec = "普通";
        if (level == 0) {
            levelRec = "普通";
        } else if (level == 1) {
            levelRec = "业余";
        } else if (level == 2) {
            levelRec = "专业";
        }

        // 性别
        int sex = (data[2] >> 7) & 0x1;
        String secRec = "";
        if (sex == 1) {
            secRec = "男";
        } else {
            secRec = "女";
        }
        // 年龄
        int age = data[2] & 0x7f;

        // 身高
        int height = data[3] & 0xFF;

        // 体重
        int weight = (data[4] << 8) | (data[5] & 0xff);
        float scale = (float) 0.1;
        if (v == 0xcf) {
            scale = (float) 0.1;
        } else if (v == 0xce) {
            scale = (float) 0.1;
        } else if (v == 0xcb) {
            scale = (float) 0.01;
        } else if (v == 0xca) {
            scale = (float) 0.001;
        }

        float weightRec = scale * weight;

        if (weightRec < 0) {
            weightRec *= -1;
        }

        // 脂肪
        int zhifang = (data[6] << 8) | (data[7] & 0xff);

        float zhifangRate = (float) (zhifang * 0.1);
        // 骨骼
        int guge = data[8] & 0xff;

        float gugeRate = (float) ((guge * 0.1) / weightRec) * 100;

        // 肌肉含量
        int jirou = (data[9] << 8) | (data[10] & 0xff);
        float jirouRate = (float) (jirou * 0.1);

        // 内脏脂肪等级
        int neizang = data[11] & 0xff;
        int neizanglevel = neizang * 1;

        // 水份含量
        int water = data[12] << 8 | data[13];
        float waterRate = (float) (water * 0.1);

        // 热量含量
        int hot = data[14] << 8 | (data[15] & 0xff);

        String[] rec = new String[]{"体重:" + dfc.format(weightRec < 0 ? -weightRec : weightRec) + "Kg", "骨骼:" + dfc.format(gugeRate < 0 ? -gugeRate : gugeRate) + "%", "脂肪:" + dfc.format(zhifangRate < 0 ? -zhifangRate : zhifangRate) + "%",
                "肌肉:" + dfc.format(jirouRate < 0 ? -jirouRate : jirouRate) + "%", "水分:" + dfc.format(waterRate < 0 ? -waterRate : waterRate) + "%", "内脏脂肪:" + dfc.format(neizanglevel < 0 ? -neizanglevel : neizanglevel), "BMR:" + dfc.format(hot < 0 ? -hot : hot) + "kcal"};

        return rec;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static TzBean getTzBeanFromHex(byte[] data) {
        if (data == null) {
            return null;
        }
        int length = data.length;
        if (length == 0) {
            return null;
        }
        // 设别类型
        int v = data[0] & 0xFF;
        // 体重
        int weight = (data[4] << 8) | (data[5] & 0xff);
        float scale = (float) 0.1;
        if (v == 0xcf) {
            scale = (float) 0.1;
        } else if (v == 0xce) {
            scale = (float) 0.1;
        } else if (v == 0xcb) {
            scale = (float) 0.01;
        } else if (v == 0xca) {
            scale = (float) 0.001;
        }

        float weightRec = scale * weight;

        if (weightRec < 0) {
            weightRec *= -1;
        }

        TzBean tzBean = new TzBean();
        tzBean.setTzValue(weightRec);
        // 脂肪
        int zhifang = (data[6] << 8) | (data[7] & 0xff);
        float zhifangRate = (float) (zhifang * 0.1);
        if (zhifangRate < 0) {
            zhifangRate *= -1;
        }
        tzBean.setTzZFValue(zhifangRate);
        // 肌肉含量
        int jirou = (data[9] << 8) | (data[10] & 0xff);
        float jirouRate = (float) (jirou * 0.1);
        if (jirouRate < 0) {
            jirouRate *= -1;
        }
        tzBean.setTzJRValue(jirouRate);
        // 水份含量
        int water = data[12] << 8 | (data[13] & 0xff);
        float waterRate = (float) (water * 0.1);
        if (waterRate < 0) {
            waterRate *= -1;
        }
        tzBean.setTzSFValue(waterRate);
        float bodyHigh = UserPreferences.getInstance().getBodyHigh();
        float bmi = weightRec / (bodyHigh * bodyHigh);
        tzBean.setTzBMIValue(bmi);
        float qz = (1 - zhifangRate / 100) * weightRec;
        tzBean.setTzQZValue(qz);
        // 骨骼
        int guge = data[8] & 0xff;
        float gugeRate = (float) ((guge * 0.1) / weightRec) * 100;
        if (gugeRate < 0) {
            gugeRate *= -1;
        }
        tzBean.setTzGGValue(gugeRate);
        // 内脏脂肪等级
        int neizang = data[11] & 0xff;
        if (neizang < 0) {
            neizang *= -1;
        }
        tzBean.setTzNZValue(neizang);
        // 热量含量
        int hot = data[14] << 8 | (data[15] & 0xff);
        if (hot < 0) {
            hot *= -1;
        }
        tzBean.setTzJCValue(hot);
        if (length > 16) {
            int st = data[16] & 0xff;
            if (st < 0) {
                st *= -1;
            }
            tzBean.setTzSTValue(st);
        }
        return tzBean;
    }
}
