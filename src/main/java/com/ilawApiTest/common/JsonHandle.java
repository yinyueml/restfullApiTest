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



    public static String getJsonValueByPath(String json,String path){
        String[] paths=path.split("\\.");
        String value=json;
        for(int i=0;i<paths.length;i++){
            if(!paths[i].contains("#")){
                value=getJsonValue(value,paths[i]);
            }else{
                String[] arrayPath=paths[i].split("#");
                try{
                    JSONArray jsonArray=new JSONObject(value).getJSONArray(arrayPath[0]);
                    value=jsonArray.getString(Integer.valueOf(arrayPath[1]));
                }catch(Exception e){
                    e.printStackTrace();
                }



            }

        }
        return value;
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
                System.out.println(orderJson);

                String pathValue=(String)jsonAction.getPathValue(orderJson,path);
//                String pathValue=JsonHandle.getJsonValueByPath(orderJson,path);
                if(key.equals("baselineBody")){
                    map.put(key,jsonValue.replaceAll("\"<<[\\s\\S]*>>\"",pathValue));
                }else{
                    map.put(key,jsonValue.replaceAll("<<[\\s\\S]*>>",pathValue));
                }

            }
        }
        return map;

    }
    public static String getJsonValue(String json, String jsonId) {

        String jsonValue = "";
        if (null == json || json.trim().length() == 0) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            jsonValue = jsonObject.getString(jsonId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonValue;
    }

    public static List<String> jsonCompare(String baselineJson, String responseJson)throws Exception {
        Map<String, String> baselineMap = new HashMap<String, String>();
        jsonAllToMap("",baselineJson,baselineMap);
        Map<String, String> responseMap = getJsonMap(responseJson);
        List<String> resList = new ArrayList<String>();
        //baselineMap包含字段匹配或者规则，其中字段数量小于等于responseMap


        return resList;
    }


    public static void jsonAllToMap(String key,String json,Map<String, String> baselineMap) throws Exception{
        if(getJSONType(json)==JSON_TYPE.JSON_TYPE_EMPTY){
            return;
        }
        if(getJSONType(json) == JSON_TYPE.JSON_TYPE_ARRAY){

            JSONArray jsonArray=new JSONArray(json);
            for(int i=0;i<jsonArray.length();i++){
                String value=jsonArray.getString(i);

                jsonAllToMap(key,value,baselineMap);
            }


        }else if(getJSONType(json) == JSON_TYPE.JSON_TYPE_VALUE){
            baselineMap.put(key,json);
            return;
        }else if(getJSONType(json) == JSON_TYPE.JSON_TYPE_OBJECT){
            JSONObject jsonObject=new JSONObject(json);
            Iterator<String> keys=jsonObject.keys();
            while(keys.hasNext()){

                String value=jsonObject.getString(keys.next());
                key=key+"."+keys.next();
                jsonAllToMap(key,value,baselineMap);
            }
        }



    }

    public static JSON_TYPE getJSONType(String json){
        if(TextUtils.isEmpty(json)){
            return JSON_TYPE.JSON_TYPE_EMPTY;
        }

        final char strChar=json.charAt(0);
        if(strChar=='{'){
            return JSON_TYPE.JSON_TYPE_OBJECT;
        }else if(strChar == '['){
            return JSON_TYPE.JSON_TYPE_ARRAY;
        }else{
            return JSON_TYPE.JSON_TYPE_VALUE;
        }
    }

    public enum JSON_TYPE {
        JSON_TYPE_OBJECT,
        JSON_TYPE_ARRAY,
        JSON_TYPE_EMPTY,
        JSON_TYPE_VALUE
    }

}


