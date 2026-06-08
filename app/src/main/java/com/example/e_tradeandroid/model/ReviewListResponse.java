package com.example.e_tradeandroid.model;

import java.util.List;

/**
 * 评价列表响应
 * 对应后端返回格式:
 * {
 *   "code": 200,
 *   "data": {
 *     "reviews": [...],
 *     "averageRating": 4.8,
 *     "reviewCount": 10
 *   }
 * }
 */
public class ReviewListResponse {
    private List<Review> reviews;
    private Double averageRating;
    private Integer reviewCount;

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }
}
