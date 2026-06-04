package com.example.e_tradeandroid.model;

import java.util.List;

public class Product {
    private Long id;
    private Long sellerId;
    private String sellerName; // 卖家名称
    private String sellerAvatar; // 卖家头像
    private Integer sellerIsAuth; // 卖家认证状态 0-未认证 1-已认证
    private Integer categoryId;
    private String categoryName; // 分类名称
    private String name;
    // 把类型改成和变量一致的 double
    private double price;
    private Integer stock;
    private Integer soldCount; // 已售数量
    private Integer viewCount;
    private String description;
    private String mainImage;
    private String coverImage; // 封面图
    private List<String> images;
    private String imageUrls;
    private Integer status; // 0-待审核 1-在售 2-已下架 3-已售出 4-审核不通过
    private Integer isRecommend; // 0-否 1-是
    private String createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    
    public Integer getSellerIsAuth() {
        return sellerIsAuth;
    }
    
    public void setSellerIsAuth(Integer sellerIsAuth) {
        this.sellerIsAuth = sellerIsAuth;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 修正 getPrice 返回类型
    public double getPrice() {
        return price;
    }

    // 修正 setPrice 参数类型
    public void setPrice(double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public Integer getSoldCount() {
        return soldCount;
    }
    
    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getIsRecommend() {
        return isRecommend;
    }
    
    public void setIsRecommend(Integer isRecommend) {
        this.isRecommend = isRecommend;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    // 把字段改成成员变量
    public boolean isCollected; // 是否收藏
}