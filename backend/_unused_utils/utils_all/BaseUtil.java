package cn.nodesoft.utils;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseUtil {

    public static boolean isEmpty(@Nullable Object str) {
        if ((str == null || "".equals(str))
                || "null".equals(str)
                || "undefined".equals(str)) {
            return true;
        }
        return false;
    }

    public Class<?> getValueClass(Object obj){
        if (obj instanceof Boolean) {
            return Boolean.class;
        } else if(obj instanceof Integer){
            return Integer.class;
        } else if(obj instanceof String){
            return String.class;
        } else if(obj instanceof Long){
            return Long.class;
        } else if(obj instanceof Map){
            return Map.class;
        } else if(obj instanceof Collection){
            return Collection.class;
        } else if(obj instanceof java.util.List){
            return java.util.List.class;
        }else {
            return String.class;
        }
    }

    /**
     * 驼峰转下划线
     * @param str 目标字符串
     * @return: java.lang.String
     */
    public static String toUnderline(String str) {
        String regex = "([A-Z])";
        Matcher matcher = Pattern.compile(regex).matcher(str);
        while (matcher.find()) {
            String target = matcher.group();
            str = str.replaceAll(target, "_" + target.toLowerCase());
        }
        return str;
    }

}
