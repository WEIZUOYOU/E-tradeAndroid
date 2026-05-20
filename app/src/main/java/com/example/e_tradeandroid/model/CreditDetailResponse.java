package com.example.e_tradeandroid.model;

public class CreditDetailResponse {
    private Long userId;
    private String username;
    private String avatar;
    private Integer creditScore;
    private Integer tradeCount;
    private Double goodReviewRate;
    private Integer totalReviews;
    private Integer goodReviews;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }

    public Integer getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(Integer tradeCount) {
        this.tradeCount = tradeCount;
    }

    public Double getGoodReviewRate() {
        return goodReviewRate;
    }

    public void setGoodReviewRate(Double goodReviewRate) {
        this.goodReviewRate = goodReviewRate;
    }

    public Integer getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(Integer totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Integer getGoodReviews() {
        return goodReviews;
    }

    public void setGoodReviews(Integer goodReviews) {
        this.goodReviews = goodReviews;
    }
}
