package com.example.e_tradeandroid.model;
public class ChatMessage {
    private long senderId;
    private String content;
    private String createTime;

    public long getSenderId() {return senderId;}
    public void setSenderId(long senderId) {this.senderId = senderId;}
    public String getContent() {return content;}
    public void setContent(String content) {this.content = content;}
    public String getCreateTime() {return createTime;}
    public void setCreateTime(String createTime) {this.createTime = createTime;}
}