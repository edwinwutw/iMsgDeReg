package com.edwin.imsgdereg.model;

import com.google.gson.annotations.SerializedName;

public class ResponseData {
    @SerializedName("messageCode")
    private String messageCode;
    @SerializedName("message")
    private String message;
    @SerializedName("status")
    private String status;

    public ResponseData(String messageCode, String message, String status) {
        this.messageCode = messageCode;
        this.message = message;
        this.status = status;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String name) {
        this.messageCode = messageCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}