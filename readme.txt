使用testNG的测试框架，展示使用reportNG包，其中1.1.6是自己修改了内容并打包，结果展示优化，1.1.5是使用其他人的修改的包。

1.http_request_template.txt 为request头的信息，主要为host的配置。

2.testng.xml为该应用配置。
其中<parameter name="workJson" value="./testSpringBootJsons" />表示读取文件夹中所有文件。
 <class name="com.ilawApiTest.oper.HTTPReqResTest"/>为主方法。


3.2testJson.txt 等文件为一个用例的相关配置。可能会包括多个测试过程。如testJsons文件夹下文件2testJson.txt中。
caseGroup 为该用例的名字。
order0 为第一个测试流程的名字。
order1 为第二个测试流程的名字。
testScript 为结果匹配的判断。

之后的order可以依附之前order结果进行相关request请求。

4.主类方法说明

@BeforeTest
@Parameters("workJson")
public void setup(String path)

该方法为测试前准备，通过@Parameters注解将testng.xml配置文件中的文件夹写入，并且读取所有文件。

@DataProvider(name = "requestJsonDate")
protected Iterator<Object[]> testProvider(ITestContext context)

该方法为下面的测试类参数的前期处理

@Test(dataProvider = "requestJsonDate", description = "HTTPReqResTest")
public void api_test(String host, String caseName, Map<String, String> requestJsonMap)

该方法为具体测试类，由于testProvider方法返回的为集合，因此该方法可以自动轮训，并将结果输出。

