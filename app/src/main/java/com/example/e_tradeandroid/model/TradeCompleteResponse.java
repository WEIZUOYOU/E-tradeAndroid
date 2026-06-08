package com.example.e_tradeandroid.model;

import com.google.gson.annotations.SerializedName;

public class TradeCompleteResponse {
    @SerializedName("tradeId")
    private Long tradeId;
    
    @SerializedName("tradeStatus")
    private Integer tradeStatus;
    
    @SerializedName("tradeNo")
    private String tradeNo;

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(Integer tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }
}
