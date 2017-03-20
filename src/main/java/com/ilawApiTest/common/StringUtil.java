package com.ilawApiTest.common;

/**
* 处理String串
*/
public class StringUtil {
    public static String removeSpaces(String str){
    	return str.replaceAll("[\\s]+", "");
    }
}
