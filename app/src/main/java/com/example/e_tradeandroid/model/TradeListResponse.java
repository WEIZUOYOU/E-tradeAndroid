package com.example.e_tradeandroid.model;

import java.util.List;

/**
 * 订单列表响应
 * 对应后端返回格式:
 * {
 *   "code": 200,
 *   "data": {
 *     "trades": [...],
 *     "total": 10
 *   }
 * }
 */
public class TradeListResponse {
    private List<TradeInfo> trades;
    private Integer total;

    public List<TradeInfo> getTrades() {
        return trades;
    }

    public void setTrades(List<TradeInfo> trades) {
        this.trades = trades;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
