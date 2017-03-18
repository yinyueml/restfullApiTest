package com.ilawApiTest.oper;

import com.ilawApiTest.common.*;
import com.jayway.restassured.response.Response;
import junit.framework.Assert;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.io.*;
import java.util.*;

/**
 * Created by shaowei on 2017/3/15.
 */
public class HTTPReqResTest implements ITest {


    public String getTestName() {
        return "api-test";
    }

    String templatePath = System.getProperty("user.dir") + File.separator + "http_request_template.txt";
    Map<String, String> jsonMap = new HashMap<String, String>();

    String filePath = "";
    String testJson = "";
    String template = null;
    Map<String, String> requestHeader = new HashMap<String, String>();
    String caseName = "";
    List<File> testJsons = new ArrayList<File>();
    String host = "";

    @BeforeTest
    @Parameters("workJson")
    public void setup(String path) {

        File pathFile = new File(path);
        for (File json : pathFile.listFiles()) {
            if (json.getName().contains("test")) {
                testJsons.add(json);
            }
        }


        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(templatePath)));
            String line = "";
            while ((line = br.readLine()) != null) {
                if (line.contains(":") && !line.trim().startsWith("Host")) {
                    String[] items = line.split(":");
                    requestHeader.put(items[0].trim(), items[1].trim());
                } else if (line.trim().startsWith("Host")) {
                    host = line.substring(line.indexOf(":") + 1).trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @DataProvider(name = "requestJsonDate")
    protected Iterator<Object[]> testProvider(ITestContext context) {
        List<Object[]> caseList = new ArrayList<Object[]>();

        for (File jsonFile : testJsons) {

            try {
                testJson = TextFileParse.getJsonStringFromText(jsonFile);
                jsonMap = JsonHandle.getJsonMap(testJson);
                Map<String, String> requestListJsonMap = JsonHandle.getJsonMap(jsonMap.get("request"));
//                Map<String, String> baseLineJsonMap = JsonHandle.getJsonMap(jsonMap.get("baseline"));
                String caseName = jsonMap.get("caseGroup");
                caseList.add(new Object[]{host,caseName, requestListJsonMap});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return caseList.iterator();
    }


    @Test(dataProvider = "requestJsonDate", description = "HTTPReqResTest")
    public void api_test(String host,String caseName, Map<String, String> requestJsonMap) {
        StringBuffer outputSB=new StringBuffer("");
        boolean resultBoolean=true;
        HTTPReqGen httpReqGen = new HTTPReqGen();
        Map<String, ResultMessage> resMap = httpReqGen.performRequestList(requestHeader, requestJsonMap, host);
        Set<String> resKeys = resMap.keySet();
        for(int i=0;i<resKeys.size();i++){
            String key="order"+i;
            ResultMessage resultMessage = resMap.get(key);
            if (resultMessage != null) {
                if (resultMessage.isSuccess()) {
                    outputSB.append("\n============================\n");
                    outputSB.append(resultMessage.getMessageHeader()+"\n");
                    outputSB.append("<<<<<<<<<<<<\n");
                    outputSB.append(resultMessage.getResultMessage()+"\n");
                } else {
                    resultBoolean=false;
                    outputSB.append("\n============================\n");
                    outputSB.append(resultMessage.getMessageHeader()+"\n");
                    outputSB.append("<<<<<<<<<<<<\n");
                    outputSB.append(resultMessage.getResultMessage()+"\n");
                }
            }
        }
        Assert.assertTrue(outputSB.toString(),resultBoolean);
    }


    @AfterTest
    public void teardown() {

    }


}
