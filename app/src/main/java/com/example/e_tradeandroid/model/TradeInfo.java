package com.example.e_tradeandroid.model;

import com.google.gson.annotations.SerializedName;

public class TradeInfo {
    @SerializedName("id")
    private Long id;
    @SerializedName("tradeNo")
    private String tradeNo;
    @SerializedName("productId")
    private Long productId;
    @SerializedName("productName")
    private String productName;
    @SerializedName("productPrice")
    private Double productPrice;
    @SerializedName("productImage")
    private String productImage;
    @SerializedName("buyerId")
    private Long buyerId;
    @SerializedName("buyerName")
    private String buyerName;
    @SerializedName("buyerAvatar")
    private String buyerAvatar;
    @SerializedName("buyerCreditScore")
    private Integer buyerCreditScore;
    @SerializedName("buyerIsAuth")
    private Integer buyerIsAuth;  // ✅ 后端返回 0 或 1，不是布尔值
    @SerializedName("buyerPhone")
    private String buyerPhone;
    @SerializedName("sellerId")
    private Long sellerId;
    @SerializedName("sellerName")
    private String sellerName;
    @SerializedName("sellerAvatar")
    private String sellerAvatar;
    @SerializedName("sellerCreditScore")
    private Integer sellerCreditScore;
    @SerializedName("sellerIsAuth")
    private Integer sellerIsAuth;  // ✅ 后端返回 0 或 1，不是布尔值
    @SerializedName("sellerPhone")
    private String sellerPhone;
    @SerializedName("meetingLocation")
    private String meetingLocation;
    @SerializedName("meetingTime")
    private String meetingTime;
    @SerializedName("status")
    private Integer tradeStatus;
    @SerializedName("createTime")
    private String createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerAvatar() {
        return buyerAvatar;
    }

    public void setBuyerAvatar(String buyerAvatar) {
        this.buyerAvatar = buyerAvatar;
    }

    public Integer getBuyerCreditScore() {
        return buyerCreditScore;
    }

    public void setBuyerCreditScore(Integer buyerCreditScore) {
        this.buyerCreditScore = buyerCreditScore;
    }

    public Integer getBuyerIsAuth() {
        return buyerIsAuth;
    }

    public void setBuyerIsAuth(Integer buyerIsAuth) {
        this.buyerIsAuth = buyerIsAuth;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerAvatar() {
        return sellerAvatar;
    }

    public void setSellerAvatar(String sellerAvatar) {
        this.sellerAvatar = sellerAvatar;
    }

    public Integer getSellerCreditScore() {
        return sellerCreditScore;
    }

    public void setSellerCreditScore(Integer sellerCreditScore) {
        this.sellerCreditScore = sellerCreditScore;
    }

    public Integer getSellerIsAuth() {
        return sellerIsAuth;
    }

    public void setSellerIsAuth(Integer sellerIsAuth) {
        this.sellerIsAuth = sellerIsAuth;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }

    public void setSellerPhone(String sellerPhone) {
        this.sellerPhone = sellerPhone;
    }

    public String getMeetingLocation() {
        return meetingLocation;
    }

    public void setMeetingLocation(String meetingLocation) {
        this.meetingLocation = meetingLocation;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = meetingTime;
    }

    public Integer getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(Integer tradeStatus) {
        this.tradeStatus = tradeStatus;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
