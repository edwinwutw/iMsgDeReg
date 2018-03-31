package com.edwin.imsgdereg.network;

/**
 * Created by edwinwu on 2018/3/9.
 */

public class ResponseData {
    private int    actionId;
    private String url;
    private String status;
    private String messageCode;
    private String message;

    public ResponseData() {
    }

    public ResponseData(int actionId, String url, String status, String messageCode, String message) {
        this.actionId = actionId;
        this.url = url;
        this.status = status;
        this.messageCode = messageCode;
        this.message = message;
    }

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return "actionId = " + actionId + "\r\n" +
                "url = " + url + "\r\n" +
                "status = " + status + "\r\n" +
                "messageCode = " + messageCode + "\n" +
                "message = " + message;
    }
}
