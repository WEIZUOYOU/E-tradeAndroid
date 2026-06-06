package com.example.e_tradeandroid.repository;

import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 交易数据仓库 - 统一处理交易相关的网络请求和数据缓存
 */
public class TradeRepository {
    private static final Gson gson = new Gson();
    
    // 回调接口
    public interface TradeCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }
    
    /**
     * 创建交易
     */
    public void createTrade(long productId, String meetingTime, String meetingLocation, 
                           TradeCallback<TradeInfo> callback) {
        String json = String.format(
            "{\"productId\":%d,\"meetingTime\":\"%s\",\"meetingLocation\":\"%s\"}",
            productId, meetingTime, meetingLocation
        );
        
        ApiClient.post("api/trade/create", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("创建交易失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<TradeInfo> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<TradeInfo>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(baseResp.getData());
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取交易详情
     */
    public void getTradeDetail(long tradeId, TradeCallback<TradeInfo> callback) {
        ApiClient.get("api/trade/detail/" + tradeId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("获取交易详情失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<TradeInfo> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<TradeInfo>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(baseResp.getData());
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
    
    /**
     * 卖家确认交易
     */
    public void confirmTrade(long tradeId, String sellerPhone, TradeCallback<Void> callback) {
        String json = String.format(
            "{\"tradeId\":%d,\"sellerPhone\":\"%s\"}",
            tradeId, sellerPhone
        );
        
        ApiClient.post("api/trade/confirm", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("确认交易失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<String>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
    
    /**
     * 完成交易
     */
    public void completeTrade(long tradeId, TradeCallback<Void> callback) {
        ApiClient.post("api/trade/complete", "{\"tradeId\":" + tradeId + "}", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("操作失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<String>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
    
    /**
     * 取消交易
     */
    public void cancelTrade(long tradeId, TradeCallback<Void> callback) {
        ApiClient.post("api/trade/cancel", "{\"tradeId\":" + tradeId + "}", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("取消交易失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<String>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
    
    /**
     * 更新交易信息
     */
    public void updateTrade(long tradeId, String meetingTime, String meetingLocation, 
                           TradeCallback<Void> callback) {
        String json = String.format(
            "{\"tradeId\":%d,\"meetingTime\":\"%s\",\"meetingLocation\":\"%s\"}",
            tradeId, meetingTime, meetingLocation
        );
        
        ApiClient.post("api/trade/update", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("修改失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<String>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
    
    /**
     * 确认修改
     */
    public void confirmUpdate(long tradeId, TradeCallback<Void> callback) {
        ApiClient.post("api/trade/confirmUpdate", "{\"tradeId\":" + tradeId + "}", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("确认失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<String>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(null);
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取我的交易列表
     */
    public void getMyTrades(Integer status, int page, int size, 
                           TradeCallback<List<TradeInfo>> callback) {
        StringBuilder url = new StringBuilder("api/trade/my/list?page=" + page + "&size=" + size);
        if (status != null) {
            url.append("&status=").append(status);
        }
        
        ApiClient.get(url.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("获取交易列表失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                try {
                    // 解析分页结果
                    com.google.gson.JsonObject obj = gson.fromJson(respBody, 
                        com.google.gson.JsonObject.class);
                    if (obj.get("code").getAsInt() == 200) {
                        com.google.gson.JsonArray list = obj.getAsJsonObject("data")
                            .getAsJsonArray("list");
                        List<TradeInfo> trades = gson.fromJson(list, 
                            new TypeToken<List<TradeInfo>>() {}.getType());
                        callback.onSuccess(trades);
                    } else {
                        callback.onFailure(obj.get("msg").getAsString());
                    }
                } catch (Exception e) {
                    callback.onFailure("解析失败");
                }
            }
        });
    }
}