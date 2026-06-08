package com.example.e_tradeandroid.model;

/**
 * 提交评价请求
 * 对应后端 API: POST /api/review
 */
public class ReviewRequest {
    private Long tradeId;      // 交易ID
    private Integer rating;    // 评分 1-5
    private String content;    // 评价内容
    private String tags;       // 评价标签，逗号分隔

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
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

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
    
    /**
     * 从数组转换为逗号分隔的字符串
     */
    public void setTagsArray(String[] tagArray) {
        if (tagArray != null && tagArray.length > 0) {
            this.tags = String.join(",", tagArray);
        }
    }
}
