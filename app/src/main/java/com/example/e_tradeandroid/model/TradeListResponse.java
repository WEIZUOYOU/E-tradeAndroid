package com.example.e_tradeandroid.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 订单列表响应
 * 对应后端返回格式:
 * {
 *   "code": 200,
 *   "data": {
 *     "list": [...],  // 注意：后端返回的是 list 而不是 trades
 *     "page": 1,
 *     "size": 10
 *   }
 * }
 */
public class TradeListResponse {
    @SerializedName("list")  // ✅ 映射后端的 list 字段
    private List<TradeInfo> trades;
    
    @SerializedName("page")
    private Integer page;
    
    @SerializedName("size")
    private Integer size;
    
    private Integer total;

    public List<TradeInfo> getTrades() {
        return trades;
    }

    public void setTrades(List<TradeInfo> trades) {
        this.trades = trades;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
