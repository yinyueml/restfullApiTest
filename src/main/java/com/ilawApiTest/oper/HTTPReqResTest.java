package com.ilawApiTest.oper;

import com.ilawApiTest.common.*;
import junit.framework.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.*;

import java.io.*;
import java.util.*;

/**
 * Created by shaowei on 2017/3/15.
 * 每个测试的json文件为一个测试用例，里面可能包括多个测试过程，分别为order0、order1...
 */
public class HTTPReqResTest implements ITest {


    public String getTestName() {
        return "api-test";
    }

    String templatePath = System.getProperty("user.dir") + File.separator + "http_request_template.txt";
    Map<String, String> jsonMap = new HashMap<String, String>();

    String testJson = "";
    Map<String, String> requestHeader = new HashMap<String, String>();
    List<File> testJsons = new ArrayList<File>();
    String host = "";

    @BeforeTest
    @Parameters("workJson")
    public void setup(String path) {

        File pathFile = new File(path);
        for (File json : pathFile.listFiles()) {
            //防止读取非测试文件
            if (json.getName().contains("test")) {
                testJsons.add(json);
            }
        }

        //读取模板文件，获取请求头的通用信息

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(templatePath)));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(":") && !line.trim().startsWith("Host")) {
                    String[] items = line.split(":");
                    requestHeader.put(items[0].trim(), items[1].trim());
                } else if (line.trim().startsWith("Host")) {
                    //获取请求ip的地址
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
                //解析测试文档json串
                jsonMap = JsonHandle.getJsonMap(testJson);
                //获取request相关信息（"request"）
                Map<String, String> requestListJsonMap = JsonHandle.getJsonMap(jsonMap.get("request"));
                String caseName = jsonMap.get("caseGroup");
                //将所有的测试用例发送给api_test方法
                caseList.add(new Object[]{host, caseName, requestListJsonMap});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return caseList.iterator();
    }


    @Test(dataProvider = "requestJsonDate", description = "HTTPReqResTest")
    public void api_test(String host, String caseName, Map<String, String> requestJsonMap) {
        boolean resultBoolean = true;
        HTTPReqGen httpReqGen = new HTTPReqGen();
        Map<String, ResultMessage> resMap = httpReqGen.performRequestList(requestHeader, requestJsonMap, host);
        Set<String> resKeys = resMap.keySet();
        for (int i = 0; i < resKeys.size(); i++) {
            String key = "order" + i;
            ResultMessage resultMessage = resMap.get(key);
            if (resultMessage != null) {
                if (!resultMessage.isSuccess()) {
                    resultBoolean = false;
                }
                Reporter.log(resultMessage.getMessageHeader() +"\n开始测试：");
                Reporter.log(resultMessage.getResultMessage());
                Reporter.log("结束测试");
            }
        }
        Reporter.getCurrentTestResult().setAttribute("caseName",caseName);
        Assert.assertTrue(resultBoolean);
    }


    @AfterTest
    public void teardown() {

    }


}
