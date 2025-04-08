package com.asyncflow.log.util;

public class StringUtils {
    
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }
    
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
    
    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }
}