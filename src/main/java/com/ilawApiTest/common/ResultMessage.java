package com.ilawApiTest.common;

/**
 * Created by shaowei on 2017/3/17.
 */
public class ResultMessage {

    public ResultMessage(boolean success,String resultMessage){
        this.success=success;
        this.resultMessage=resultMessage;
    }

    private String messageHeader;
    private String caseName;
    private String resultMessage;
    private boolean success;

    public String getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(String messageHeader) {
        this.messageHeader = messageHeader;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
