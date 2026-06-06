package com.example.e_tradeandroid.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易状态机 - 集中管理状态转换逻辑
 */
public class TradeStateMachine {
    private static final String TAG = "TradeStateMachine";
    
    private TradeStatus currentStatus;
    private final List<StateChangeLog> changeLogs = new ArrayList<>();
    
    /**
     * 状态变更日志
     */
    public static class StateChangeLog {
        private final TradeStatus fromStatus;
        private final TradeStatus toStatus;
        private final long timestamp;
        private final String operator;
        
        public StateChangeLog(TradeStatus fromStatus, TradeStatus toStatus, String operator) {
            this.fromStatus = fromStatus;
            this.toStatus = toStatus;
            this.timestamp = System.currentTimeMillis();
            this.operator = operator;
        }
        
        public TradeStatus getFromStatus() { return fromStatus; }
        public TradeStatus getToStatus() { return toStatus; }
        public long getTimestamp() { return timestamp; }
        public String getOperator() { return operator; }
    }
    
    /**
     * 创建状态机
     * @param initialStatus 初始状态
     */
    public TradeStateMachine(TradeStatus initialStatus) {
        this.currentStatus = initialStatus;
        changeLogs.add(new StateChangeLog(null, initialStatus, "SYSTEM"));
        Log.d(TAG, "状态机初始化: " + initialStatus);
    }
    
    /**
     * 创建状态机（从状态码）
     */
    public TradeStateMachine(int statusCode) {
        this(TradeStatus.fromCode(statusCode));
    }
    
    /**
     * 获取当前状态
     */
    public TradeStatus getCurrentStatus() {
        return currentStatus;
    }
    
    /**
     * 获取当前状态码
     */
    public int getCurrentStatusCode() {
        return currentStatus.getCode();
    }
    
    /**
     * 尝试转换到目标状态
     * @param targetStatus 目标状态
     * @param operator 操作人标识
     * @return 是否转换成功
     */
    public boolean transitionTo(TradeStatus targetStatus, String operator) {
        if (currentStatus.canTransitionTo(targetStatus)) {
            StateChangeLog log = new StateChangeLog(currentStatus, targetStatus, operator);
            changeLogs.add(log);
            currentStatus = targetStatus;
            
            Log.d(TAG, "状态转换成功: " + log.getFromStatus() + " -> " + log.getToStatus() + 
                    " by " + log.getOperator());
            return true;
        } else {
            Log.e(TAG, "状态转换失败: 不允许从 " + currentStatus + " 转换到 " + targetStatus);
            return false;
        }
    }
    
    /**
     * 尝试转换到目标状态（使用状态码）
     */
    public boolean transitionTo(int targetStatusCode, String operator) {
        return transitionTo(TradeStatus.fromCode(targetStatusCode), operator);
    }
    
    /**
     * 判断是否可以转换到目标状态
     */
    public boolean canTransitionTo(TradeStatus targetStatus) {
        return currentStatus.canTransitionTo(targetStatus);
    }
    
    /**
     * 判断是否可以转换到目标状态（使用状态码）
     */
    public boolean canTransitionTo(int targetStatusCode) {
        return canTransitionTo(TradeStatus.fromCode(targetStatusCode));
    }
    
    /**
     * 获取状态变更历史
     */
    public List<StateChangeLog> getChangeLogs() {
        return new ArrayList<>(changeLogs);
    }
    
    /**
     * 判断当前是否为待确认状态
     */
    public boolean isPendingConfirm() {
        return currentStatus == TradeStatus.PENDING_CONFIRM;
    }
    
    /**
     * 判断当前是否为待交易状态
     */
    public boolean isConfirmed() {
        return currentStatus == TradeStatus.CONFIRMED;
    }
    
    /**
     * 判断当前是否为卖家已确认状态
     */
    public boolean isSellerCompleted() {
        return currentStatus == TradeStatus.SELLER_COMPLETED;
    }
    
    /**
     * 判断当前是否为买家已确认状态
     */
    public boolean isBuyerCompleted() {
        return currentStatus == TradeStatus.BUYER_COMPLETED;
    }
    
    /**
     * 判断当前是否为等待对方确认状态
     */
    public boolean isWaitingForOther() {
        return currentStatus.isWaitingForOther();
    }
    
    /**
     * 判断当前是否为待确认修改状态
     */
    public boolean isPendingUpdate() {
        return currentStatus == TradeStatus.PENDING_UPDATE;
    }
    
    /**
     * 判断当前是否为已完成状态
     */
    public boolean isCompleted() {
        return currentStatus == TradeStatus.COMPLETED;
    }
    
    /**
     * 判断当前是否为已取消状态
     */
    public boolean isCancelled() {
        return currentStatus == TradeStatus.CANCELLED;
    }
    
    /**
     * 判断交易是否已结束（已完成或已取消）
     */
    public boolean isTerminated() {
        return isCompleted() || isCancelled();
    }
}