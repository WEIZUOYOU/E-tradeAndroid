package com.example.e_tradeandroid.helper;

import android.util.Log;

import com.example.e_tradeandroid.model.ChatMessage;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 交易消息助手 - 封装交易卡片消息的发送逻辑
 * 统一使用 TradeInfo 作为交易数据模型
 */
public class TradeMessageHelper {
    private static final String TAG = "TradeMessageHelper";
    private static final Gson gson = new Gson();
    
    /**
     * 发送交易卡片消息的回调接口
     */
    public interface SendCallback {
        void onSuccess();
        void onFailure(String error);
    }
    
    /**
     * 发送交易卡片消息
     * @param tradeId 交易ID
     * @param tradeStatus 交易状态
     * @param tradeNo 交易编号
     * @param receiverId 接收者ID
     * @param buyerId 买家ID
     * @param sellerId 卖家ID
     * @param product 商品信息
     * @param meetingLocation 交易地点
     * @param meetingTime 交易时间
     * @param buyerPhone 买家电话
     * @param sellerPhone 卖家电话
     * @param callback 回调
     */
    public static void sendTradeCardMessage(long tradeId, int tradeStatus, String tradeNo,
                                           long receiverId, long buyerId, long sellerId,
                                           Product product, String meetingLocation, 
                                           String meetingTime, String buyerPhone, 
                                           String sellerPhone, SendCallback callback) {
        Log.d(TAG, "sendTradeCardMessage: tradeId=" + tradeId + ", status=" + tradeStatus + 
                ", receiverId=" + receiverId);
        
        if (receiverId == 0) {
            callback.onFailure("接收者ID无效");
            return;
        }
        
        // 直接使用 TradeInfo 作为交易数据对象
        TradeInfo tradeInfo = new TradeInfo();
        tradeInfo.setId(tradeId);
        tradeInfo.setTradeNo(tradeNo);
        tradeInfo.setTradeStatus(tradeStatus);
        tradeInfo.setBuyerId(buyerId);
        tradeInfo.setSellerId(sellerId);
        tradeInfo.setProductName(product != null ? product.getName() : "");
        tradeInfo.setProductPrice(product != null ? product.getPrice() : 0);
        tradeInfo.setProductImage(product != null && product.getImages() != null && !product.getImages().isEmpty()
                ? product.getImages().get(0) : "");
        tradeInfo.setMeetingLocation(meetingLocation);
        tradeInfo.setMeetingTime(meetingTime);
        tradeInfo.setBuyerPhone(buyerPhone);
        tradeInfo.setSellerPhone(sellerPhone);
        
        String tradeDataJson = gson.toJson(tradeInfo);
        
        // 构建消息请求体
        ChatMessage message = new ChatMessage();
        message.setReceiverId(receiverId);
        message.setContent("交易卡片");
        message.setType(1); // 交易消息类型
        message.setTradeId(tradeId);
        message.setTradeStatus(tradeStatus);
        message.setTradeData(tradeDataJson);
        
        String body = gson.toJson(message);
        
        ApiClient.post("api/message/send", body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "发送交易卡片失败: " + e.getMessage());
                callback.onFailure("发送失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                try {
                    com.google.gson.JsonObject obj = gson.fromJson(respBody, 
                        com.google.gson.JsonObject.class);
                    if (obj.get("code").getAsInt() == 200) {
                        Log.d(TAG, "交易卡片发送成功");
                        callback.onSuccess();
                    } else {
                        String errorMsg = obj.get("msg").getAsString();
                        Log.e(TAG, "发送失败: " + errorMsg);
                        callback.onFailure(errorMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析响应失败: " + e.getMessage());
                    callback.onFailure("发送失败");
                }
            }
        });
    }
    
    /**
     * 简化版本 - 从 TradeInfo 对象发送交易卡片
     * 直接序列化传入的 TradeInfo 对象
     */
    public static void sendTradeCardMessage(TradeInfo tradeInfo, long receiverId, 
                                           Product product, SendCallback callback) {
        if (tradeInfo == null) {
            callback.onFailure("交易信息为空");
            return;
        }
        
        // 补充商品信息（如果 TradeInfo 中缺失）
        if (product != null) {
            if (tradeInfo.getProductName() == null || tradeInfo.getProductName().isEmpty()) {
                tradeInfo.setProductName(product.getName());
            }
            if (tradeInfo.getProductPrice() == null || tradeInfo.getProductPrice() == 0) {
                tradeInfo.setProductPrice(product.getPrice());
            }
            if (tradeInfo.getProductImage() == null || tradeInfo.getProductImage().isEmpty()) {
                if (product.getImages() != null && !product.getImages().isEmpty()) {
                    tradeInfo.setProductImage(product.getImages().get(0));
                }
            }
        }
        
        // 直接序列化 TradeInfo
        String tradeDataJson = gson.toJson(tradeInfo);
        
        long tradeId = tradeInfo.getId() != null ? tradeInfo.getId() : 0;
        int tradeStatus = tradeInfo.getTradeStatus() != null ? tradeInfo.getTradeStatus() : 0;
        
        // 构建消息请求体
        ChatMessage message = new ChatMessage();
        message.setReceiverId(receiverId);
        message.setContent("交易卡片");
        message.setType(1); // 交易消息类型
        message.setTradeId(tradeId);
        message.setTradeStatus(tradeStatus);
        message.setTradeData(tradeDataJson);
        
        String body = gson.toJson(message);
        
        ApiClient.post("api/message/send", body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "发送交易卡片失败: " + e.getMessage());
                callback.onFailure("发送失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                try {
                    com.google.gson.JsonObject obj = gson.fromJson(respBody, 
                        com.google.gson.JsonObject.class);
                    if (obj.get("code").getAsInt() == 200) {
                        Log.d(TAG, "交易卡片发送成功");
                        callback.onSuccess();
                    } else {
                        String errorMsg = obj.get("msg").getAsString();
                        Log.e(TAG, "发送失败: " + errorMsg);
                        callback.onFailure(errorMsg);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "解析响应失败: " + e.getMessage());
                    callback.onFailure("发送失败");
                }
            }
        });
    }
}