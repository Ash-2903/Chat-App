package com.example.chatapplication.models;

public class Message {

    String uId, message, messageId, rMessageId;
    long timeStamp;

    public Message(String uId, String message, long timeStamp) {
        this.uId = uId;
        this.message = message;
        this.timeStamp = timeStamp;
    }



    public Message(String uId, String message) {
        this.uId = uId;
        this.message = message;
    }

    public Message() {

    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getrMessageId() {
        return rMessageId;
    }

    public void setrMessageId(String rMessageId) {
        this.rMessageId = rMessageId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
