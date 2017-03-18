package com.ilawApiTest.common;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.zf.json.JsonAction;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import javax.crypto.MacSpi;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.restassured.RestAssured.given;

/**
 * Wrapper for RestAssured. Uses an HTTP request template and a single record housed in a RecordHandler object to
 * generate and perform an HTTP requests.
 */
public class HTTPReqGen {

    public enum HttpType {
        PUT, GET, DELETE, POST
    }
    public HTTPReqGen(){

    }

    public Map<String,ResultMessage> performRequestList(Map<String,String> header, Map<String, String> requestListMap,String host){
        Map<String,Response> responseMap=new HashMap<>();
        Map<String,ResultMessage> resultMap=new HashMap<>();
        Set<String> requestKeys=requestListMap.keySet();
        for(int i=0;i<requestKeys.size();i++){
            String key="order"+i;
            String value=requestListMap.get(key);
            Map<String,String> requestMap=JsonHandle.getJsonMap(value);



            Map<String,String> resReqMap=JsonHandle.replaceJsonByPath(requestMap,responseMap);

            resReqMap.put("Host",host);
            Response response=perform_request(header,requestMap);
            responseMap.put(key,response);

            String caseName=requestMap.get("case");
            String apiName=requestMap.get("apiPath");
            String messageHead="测试过程："+caseName+";测试接口："+apiName;
            ResultMessage resultMessage=null;
            if(response.statusCode()==200){
                if(requestMap.get("baselineBody")!=null){
                    String baselineBody=requestMap.get("baselineBody");
                    if(baselineBody.length()>2){
                        resultMessage=getResultByCompare(response.asString(),baselineBody);
                        System.out.println(baselineBody);
                        System.out.println(response.asString());
                    }
                }else if(requestMap.get("testScript")!=null){
                    String testScript=requestMap.get("testScript");
                    resultMessage=getResultByTestScript(response.asString(),testScript);

                }
            }else{
                resultMessage=new ResultMessage(false,"response返回响应码为：" + response.statusCode()+"\n");
            }
            resultMessage.setMessageHeader(messageHead);

            resultMap.put(key,resultMessage);
        }
        return resultMap;
    }

    //根据测试脚本查看response结果是否正确
    public ResultMessage getResultByTestScript(String responseJson,String testScript){
        JsonAction responseJA=new JsonAction();

        ResultMessage resultMessage=null;
        Map<String,String> testScriptMap=JsonHandle.getJsonMap(testScript);
        Set<String> testKeys=testScriptMap.keySet();
        StringBuffer messageSB=new StringBuffer("");
        for(String test:testKeys){

            String testValue=testScriptMap.get(test);
            if(responseJA.isExistPath(responseJson,test)){
                String responseValue=(String)responseJA.getPathValue(responseJson,test);
                Pattern pattern=Pattern.compile(testValue);
                Matcher matcher=pattern.matcher(responseValue);
                if(!matcher.matches()){

                    messageSB.append("response 路径"+test+"的规则为："+responseValue+";期望的值为："+testValue+"\n");
                }


            }else{
                messageSB.append("response 相应结果中没有该路径:"+test+"\n");
            }
        }
        if(messageSB.length()>0){
            resultMessage=new ResultMessage(false,messageSB.toString());
        }else{
            resultMessage=new ResultMessage(true,"test测试脚本运行后结果相同");
        }
        return resultMessage;
    }

    public ResultMessage getResultByCompare(String responseJson,String baselineJson){
        ResultMessage resultMessage=null;
        try{

            JSONCompareResult result = JSONCompare.compareJSON(StringUtil.removeSpaces(baselineJson), StringUtil.removeSpaces(responseJson), JSONCompareMode.NON_EXTENSIBLE);
            if(!result.passed()){
                resultMessage=new ResultMessage(false,result.getMessage());
            }else{
                resultMessage=new ResultMessage(true,result.getMessage());
            }
        }catch (JSONException e){
                e.printStackTrace();
                resultMessage=new ResultMessage(false,"baseline与response比较的时候出错"+e.getMessage()+"\n");
        }
        return resultMessage;
    }

    public Response perform_request(Map<String,String> header, Map<String, String> requestMap) {
        RequestSpecification reqSpec=given().relaxedHTTPSValidation();
        Set<String> heardKeys=header.keySet();
        for(String heardKey:heardKeys){
            String value=requestMap.get(heardKey);
            if(null!=value&&!"".equals(value)){
                reqSpec.header(heardKey,value);
            }else{
                reqSpec.header(heardKey,header.get(heardKey));
            }
        }

        String apiPath=requestMap.get("Host")+requestMap.get("apiPath");
        String requestBody=requestMap.get("requestBody");
        String body="";
        if(null!=requestBody){
        if(requestBody.substring(requestBody.indexOf("{")+1,requestBody.lastIndexOf("}")).trim().length()>0){
            body=requestBody;
        }}
        Response response = null;

        try {

             HttpType requestType= Enum.valueOf(HttpType.class,requestMap.get("requestType"));


            switch (requestType) {

                case GET: {
                    response = reqSpec.get(apiPath);
                    break;
                }
                case POST: {
                    response = reqSpec.body(body).post(apiPath);
                    break;
                }
                case PUT: {
                    response = reqSpec.body(body).put(apiPath);
                    break;
                }
                case DELETE: {
                    response = reqSpec.delete(apiPath);
                    break;
                }

                default: {
                    System.out.println("无该类型请求类型，请查看是否为GET、PUT、POTS、DELETE");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }


}