/**
 *
 */
package com.ybg.ga.ymga.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 杨拔纲
 */
public class StringUtil {

    public static List<String[]> parseStringToList(String response) {
        List<String[]> list = new ArrayList<String[]>();
        if (response != null) {
            int begin = response.indexOf("[");
            int end = response.lastIndexOf("]");
            if (begin != -1 && end != -1 && end > 4) {
                // 去除[]
                response = response.substring(1, end);
                // 分pdkj
                String[] resp = response.split("],\\[");
                for (int i = 0; i < resp.length; i++) {
                    list.add(parseStringToArray(resp[i]));
                }
            }
        }
        return list;
    }

    public static String[] parseStringToArray(String source) {
        if (source == null || "".equals(source))
            return new String[]{};
        int begin = source.indexOf("[");
        int end = source.lastIndexOf("]");
        if (begin != -1) {
            begin = 1;
        } else {
            begin = 0;
        }
        if (end == -1) {
            end = source.length();
        }
        // 去除[]
        source = source.substring(begin, end);
        String[] result = source.split(",");
        String s = null;
        for (int i = 0; i < result.length; i++) {
            s = result[i];
            if (s.startsWith("\""))
                s = s.substring(1);
            if (s.endsWith("\""))
                s = s.substring(0, s.length() - 1);
            result[i] = s;
        }
        return result;
    }

    public static void main(String[] args) {
        String response = "[[2,\"aaa\",null],[3,\"bbb\",null]]";
        List<String[]> list = parseStringToList(response);
        if (list == null || list.size() == 0) {
            System.out.println("list is empty.");
        }
        for (String[] arr : list) {
            for (String a : arr) {
                System.out.print(a + ",");
            }
            System.out.println();
        }
    }

    public static int getIntFromString(String value, int defaultValue) {
        int result = defaultValue;
        try {
            result = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            // nothing
        }
        return result;
    }

    public static float getFloatFromString(String value, float defaultValue) {
        float result = defaultValue;
        try {
            result = Float.valueOf(value);
        } catch (NumberFormatException e) {
            // nothing
        }
        return result;
    }

    public static String cutNumberLastStr(String source, int ws) {
        if (source == null || "".equals(source))
            return source;
        int index = source.indexOf(".");
        if (index < 0)
            return source;
        if (index + ws >= source.length())
            return source;
        return source.substring(0, index + ws + 1);
    }

    public static String formatFloat(float num, String format) {
        return new DecimalFormat(format).format(num);
    }
}
