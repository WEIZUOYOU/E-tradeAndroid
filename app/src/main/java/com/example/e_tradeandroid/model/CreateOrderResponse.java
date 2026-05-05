package com.example.e_tradeandroid.model;

import java.math.BigDecimal;

public class CreateOrderResponse {
    // 后端生成的订单号
    private String orderNo;
    // 后端计算的总价（元，保留两位小数）
    private BigDecimal totalAmount;
    // 可选：商品名称，方便展示
    private String productName;

    // ===== getter/setter =====
    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}