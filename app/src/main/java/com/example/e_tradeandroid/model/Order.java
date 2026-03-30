package com.example.e_tradeandroid.model;

import java.math.BigDecimal;
import java.util.Date;

public class Order {
    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String productName;
    private BigDecimal productPriceAtOrder;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Integer status; // 0-待支付 1-已支付待发货 2-已发货 3-已完成 4-已取消
    private Date createTime;
    // getters & setters...
}