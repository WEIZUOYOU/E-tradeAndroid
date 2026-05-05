package com.example.e_tradeandroid.model;

public class CreateOrderRequest {
    // 原有字段
    private Long productId;
    private Integer quantity;

    // 新增字段（和后端接口完全对齐）
    private Integer tradeType;      // 0-快递 1-面交
    private String meetingTime;    // 面交时间
    private String meetingLocation; // 面交地点
    private Long addressId;         // 快递地址ID
    private Integer payType;        // 1-微信 2-支付宝 3-当面付

    // ===== 原有 getter/setter =====
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // ===== 新增字段 getter/setter =====
    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getMeetingLocation() {
        return meetingLocation;
    }

    public void setMeetingLocation(String meetingLocation) {
        this.meetingLocation = meetingLocation;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Integer getPayType() {
        return payType;
    }

    public void setPayType(Integer payType) {
        this.payType = payType;
    }
}