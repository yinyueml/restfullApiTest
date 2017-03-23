package com.ilawApiTest.common;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage;
import com.zf.json.JsonAction;
import junit.framework.Assert;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.Reporter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.jayway.restassured.RestAssured.given;

/**
 * 该类为处理http请求和判断结果的主要类
 */
public class HTTPReqGen {
    //请求方式
    public enum HttpType {
        PUT, GET, DELETE, POST
    }

    //处理测试用例的所有测试过程。
    public Map<String,ResultMessage> performRequestList(Map<String,String> header, Map<String, String> requestListMap,String host){
        Map<String,Response> responseMap=new HashMap<>();
        Map<String,ResultMessage> resultMap=new HashMap<>();
        Set<String> requestKeys=requestListMap.keySet();
        //根据order的编号顺序执行测试顺序
        for(int i=0;i<requestKeys.size();i++){
            String key="order"+i;
            String value=requestListMap.get(key);
            Map<String,String> requestMap=JsonHandle.getJsonMap(value);
            //根据指定order的response的结果拼装request请求
            Map<String,String> resReqMap=JsonHandle.replaceJsonByPath(requestMap,responseMap);
            resReqMap.put("Host",host);
            //对http请求进行处理，返回response应答
            Response response=perform_request(header,requestMap);
            responseMap.put(key,response);
            //对该order的response进行测试。主要是json路径下value值的正确性判断

            ResultMessage resultMessage=resultMatching(requestMap,response,responseMap);
            resultMap.put(key,resultMessage);
        }
        return resultMap;
    }


    private ResultMessage resultMatching(Map<String,String> requestMap,Response response,Map<String,Response> resultResponseMap){

        ResultMessage resultMessage=null;
        String caseName=requestMap.get("case");
        String apiName=requestMap.get("apiPath");
        String messageHead="测试过程："+caseName+";测试接口："+apiName;
        if(response.statusCode()==200){
            if(requestMap.get("baselineBody")!=null){
                String baselineBody=requestMap.get("baselineBody");
                if(baselineBody.length()>2){
                    resultMessage=getResultByCompare(response.asString(),baselineBody);
                }
            }else if(requestMap.get("testScript")!=null){
                String testScript=requestMap.get("testScript");
                try{
                    resultMessage=getResultByTestScript(response.asString(),testScript,resultResponseMap);

                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }else{
            resultMessage=new ResultMessage(false,"response返回响应码为：" + response.statusCode()+"\n");
        }
        resultMessage.setMessageHeader(messageHead);
        return resultMessage;

    }
    //根据测试脚本查看response结果是否正确
    public ResultMessage getResultByTestScript(String responseJson,String testScript,Map<String,Response> resultResponseMap) throws Exception{
        JsonAction responseJA=new JsonAction();

        ResultMessage resultMessage=null;
        Map<String,String> testScriptMap=JsonHandle.getJsonMap(testScript);
        Set<String> testKeys=testScriptMap.keySet();
        StringBuffer messageSB=new StringBuffer("");
        for(String test:testKeys){
            String testValue=testScriptMap.get(test);
            if(testValue.startsWith("/.order")){
                //结果比对为当前请求的response与之前某个请求的response相关
                String testOrder=testValue.substring(testValue.indexOf("/.")+2,testValue.indexOf("./"));
                String testPath=testValue.substring(testValue.indexOf("./")+2);
                if(resultResponseMap.get(testOrder)!=null){
                    Response resResponse=resultResponseMap.get(testOrder);
                    if(responseJA.isExistPath(resResponse.asString(),testPath)&&responseJA.isExistPath(responseJson,test)){
                        String resResponseValue=(String) responseJA.getPathValue(resResponse.asString(),testPath);
                        String responseValue=(String)responseJA.getPathValue(responseJson,test);
                        if(!responseValue.equals(resResponseValue)){
                            messageSB.append("response 路径"+test+"的规则为："+responseValue+";期望的值为："+testValue+"\n");
                        }
                    }
                }

            }
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
                    Reporter.log("无该类型请求类型，请查看是否为GET、PUT、POTS、DELETE");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }


}