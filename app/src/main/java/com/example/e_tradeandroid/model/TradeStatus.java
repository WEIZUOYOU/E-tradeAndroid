package com.example.e_tradeandroid.model;

/**
 * 交易状态枚举
 */
public enum TradeStatus {
    PENDING_CONFIRM(0, "待卖家确认"),
    CONFIRMED(1, "待交易"),
    SELLER_COMPLETED(2, "卖家已确认"),  // 卖家点击完成，等待买家确认
    BUYER_COMPLETED(3, "买家已确认"),   // 买家点击完成，等待卖家确认
    PENDING_UPDATE(4, "待确认修改"),
    COMPLETED(5, "已完成"),
    CANCELLED(6, "已取消");

    private final int code;
    private final String description;

    TradeStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取枚举值
     */
    public static TradeStatus fromCode(int code) {
        for (TradeStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return PENDING_CONFIRM; // 默认值
    }

    /**
     * 判断是否可以转换到目标状态
     */
    public boolean canTransitionTo(TradeStatus target) {
        switch (this) {
            case PENDING_CONFIRM:
                return target == CONFIRMED || target == CANCELLED;
            case CONFIRMED:
                return target == SELLER_COMPLETED || target == BUYER_COMPLETED || target == PENDING_UPDATE || target == CANCELLED;
            case SELLER_COMPLETED:
                return target == BUYER_COMPLETED || target == CANCELLED; // 买家确认后进入已完成
            case BUYER_COMPLETED:
                return target == SELLER_COMPLETED || target == CANCELLED; // 卖家确认后进入已完成
            case PENDING_UPDATE:
                return target == CONFIRMED || target == CANCELLED;
            case COMPLETED:
                return false; // 已完成状态不可转换
            case CANCELLED:
                return false; // 已取消状态不可转换
            default:
                return false;
        }
    }
    
    /**
     * 判断是否为等待对方确认的状态
     */
    public boolean isWaitingForOther() {
        return this == SELLER_COMPLETED || this == BUYER_COMPLETED;
    }
}