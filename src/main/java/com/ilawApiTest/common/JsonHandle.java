package com.ilawApiTest.common;

import com.jayway.restassured.response.Response;
import com.zf.json.JsonAction;
import org.apache.http.util.TextUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by shaowei on 2017/3/16.
 * 解析json内容的操作类
 */
public class JsonHandle {

    public static Map<String, String> getJsonMap(String json) {
        Map<String, String> resMap = new HashMap<String, String>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            Iterator<Object> keys = jsonObject.keys();
            String key = null;
            while (keys.hasNext()) {
                key = (String) keys.next();
                resMap.put(key, jsonObject.getString(key));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resMap;
    }



    public static Map<String,String> replaceJsonByPath(Map<String,String> map, Map<String,Response> responseMap){
        Set<String> keys=map.keySet();
        for(String key:keys){
            String jsonValue = map.get(key);

            if(jsonValue.contains("<<")&&jsonValue.contains(">>")){
                String orderPath=jsonValue.substring(jsonValue.indexOf("<<")+2,jsonValue.indexOf(">>"));
                String order=orderPath.substring(0,orderPath.indexOf("."));
                String orderJson=responseMap.get(order).asString();
                String path=orderPath.substring(orderPath.indexOf(".")+1);
                JsonAction jsonAction=new JsonAction();

                String pathValue=(String)jsonAction.getPathValue(orderJson,path);
                if(key.equals("baselineBody")){
                    map.put(key,jsonValue.replaceAll("\"<<[\\s\\S]*>>\"",pathValue));
                }else{
                    map.put(key,jsonValue.replaceAll("<<[\\s\\S]*>>",pathValue));
                }

            }
        }
        return map;

    }

}


