package com.example.e_tradeandroid.model;

/**
 * 交易状态枚举
 */
public enum TradeStatus {
    PENDING_CONFIRM(0, "待卖家确认"),
    CONFIRMED(1, "待交易"),
    BUYER_CONFIRMED(2, "买家已确认"),   // 买家点击完成，等待卖家确认
    SELLER_CONFIRMED(3, "卖家已确认"),  // 卖家点击完成，等待买家确认
    COMPLETED(4, "已完成"),
    CANCELLED(5, "已取消"),
    BUYER_REVIEWED(6, "买家已评价"),    // 买家已提交评价，等待卖家评价
    SELLER_REVIEWED(7, "卖家已评价"),   // 卖家已提交评价，等待买家评价
    REVIEW_COMPLETED(8, "评价完成");    // 双方均已评价，交易完结

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
                return target == BUYER_CONFIRMED || target == SELLER_CONFIRMED || target == CANCELLED;
            case BUYER_CONFIRMED:
                return target == COMPLETED || target == CANCELLED; // 卖家确认后进入已完成
            case SELLER_CONFIRMED:
                return target == COMPLETED || target == CANCELLED; // 买家确认后进入已完成
            case COMPLETED:
                return target == BUYER_REVIEWED || target == SELLER_REVIEWED; // 交易完成后可以评价
            case CANCELLED:
                return false; // 已取消状态不可转换
            case BUYER_REVIEWED:
                return target == REVIEW_COMPLETED; // 买家评价后等待卖家评价
            case SELLER_REVIEWED:
                return target == REVIEW_COMPLETED; // 卖家评价后等待买家评价
            case REVIEW_COMPLETED:
                return false; // 评价完成，不可转换
            default:
                return false;
        }
    }
    
    /**
     * 判断是否为等待对方确认的状态
     */
    public boolean isWaitingForOther() {
        return this == SELLER_CONFIRMED || this == BUYER_CONFIRMED 
                || this == BUYER_REVIEWED || this == SELLER_REVIEWED;
    }
}