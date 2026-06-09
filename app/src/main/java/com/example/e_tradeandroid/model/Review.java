package com.example.e_tradeandroid.model;

import com.google.gson.annotations.SerializedName;

/**
 * 评价数据模型
 */
public class Review {
    private Long id;
    private Long tradeId;
    private String tradeNo;
    private Long reviewerId;      // 评价者ID
    private String reviewerName;  // 评价者姓名
    private Long revieweeId;      // 被评价者ID
    private String revieweeName;  // 被评价者姓名
    private Integer rating;       // 评分（1-5）
    
    @SerializedName("content")    // ✅ 后端返回的字段名
    private String content;       // 评价内容
    
    private String comment;       // 兼容旧字段
    private String tags;          // 评价标签，逗号分隔
    private String productName;   // 商品名称
    private String productImage;  // 商品图片路径
    private String createTime;    // 创建时间
    private Boolean isReceived;   // 是否是收到的评价（true=收到的，false=给出的）

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Long getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Long reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Long getRevieweeId() {
        return revieweeId;
    }

    public void setRevieweeId(Long revieweeId) {
        this.revieweeId = revieweeId;
    }

    public String getRevieweeName() {
        return revieweeName;
    }

    public void setRevieweeName(String revieweeName) {
        this.revieweeName = revieweeName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getComment() {
        // 兼容：优先返回 content，其次返回 comment
        return content != null ? content : comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Boolean getIsReceived() {
        return isReceived;
    }

    public void setIsReceived(Boolean isReceived) {
        this.isReceived = isReceived;
    }
}