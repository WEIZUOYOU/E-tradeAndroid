package com.example.e_tradeandroid.model;

import java.math.BigDecimal;

public class Product {
    private Long id;
    private Long sellerId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String imageUrls; // 后端返回字符串，可能多个用逗号分隔，此处简化为单图
    private Integer status;   // 0-在售 1-已下架
    private Integer viewCount;

    // getters & setters...
}