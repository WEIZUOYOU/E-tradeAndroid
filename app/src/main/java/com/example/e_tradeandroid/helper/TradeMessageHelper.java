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
     * 交易数据对象
     */
    private static class TradeData {
        private String tradeNo;
        private String productName;
        private double productPrice;
        private String productImage;
        private String meetingLocation;
        private String meetingTime;
        private int tradeStatus;
        private long buyerId;
        private long sellerId;
        private String buyerPhone;
        private String sellerPhone;
        
        // Getters and Setters
        public String getTradeNo() { return tradeNo; }
        public void setTradeNo(String tradeNo) { this.tradeNo = tradeNo; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public double getProductPrice() { return productPrice; }
        public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
        public String getProductImage() { return productImage; }
        public void setProductImage(String productImage) { this.productImage = productImage; }
        public String getMeetingLocation() { return meetingLocation; }
        public void setMeetingLocation(String meetingLocation) { this.meetingLocation = meetingLocation; }
        public String getMeetingTime() { return meetingTime; }
        public void setMeetingTime(String meetingTime) { this.meetingTime = meetingTime; }
        public int getTradeStatus() { return tradeStatus; }
        public void setTradeStatus(int tradeStatus) { this.tradeStatus = tradeStatus; }
        public long getBuyerId() { return buyerId; }
        public void setBuyerId(long buyerId) { this.buyerId = buyerId; }
        public long getSellerId() { return sellerId; }
        public void setSellerId(long sellerId) { this.sellerId = sellerId; }
        public String getBuyerPhone() { return buyerPhone; }
        public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }
        public String getSellerPhone() { return sellerPhone; }
        public void setSellerPhone(String sellerPhone) { this.sellerPhone = sellerPhone; }
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
        
        // 构建交易数据对象
        TradeData tradeDataObj = new TradeData();
        tradeDataObj.setTradeNo(tradeNo);
        tradeDataObj.setProductName(product != null ? product.getName() : "");
        tradeDataObj.setProductPrice(product != null ? product.getPrice() : 0);
        tradeDataObj.setProductImage(product != null && product.getImages() != null && !product.getImages().isEmpty()
                ? product.getImages().get(0) : "");
        tradeDataObj.setMeetingLocation(meetingLocation);
        tradeDataObj.setMeetingTime(meetingTime);
        tradeDataObj.setTradeStatus(tradeStatus);
        tradeDataObj.setBuyerId(buyerId);
        tradeDataObj.setSellerId(sellerId);
        tradeDataObj.setBuyerPhone(buyerPhone);
        tradeDataObj.setSellerPhone(sellerPhone);
        
        String tradeDataJson = gson.toJson(tradeDataObj);
        
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
     */
    public static void sendTradeCardMessage(TradeInfo tradeInfo, long receiverId, 
                                           Product product, SendCallback callback) {
        if (tradeInfo == null) {
            callback.onFailure("交易信息为空");
            return;
        }
        
        sendTradeCardMessage(
            tradeInfo.getId() != null ? tradeInfo.getId() : 0,
            tradeInfo.getTradeStatus() != null ? tradeInfo.getTradeStatus() : 0,
            tradeInfo.getTradeNo(),
            receiverId,
            tradeInfo.getBuyerId() != null ? tradeInfo.getBuyerId() : 0,
            tradeInfo.getSellerId() != null ? tradeInfo.getSellerId() : 0,
            product,
            tradeInfo.getMeetingLocation(),
            tradeInfo.getMeetingTime(),
            tradeInfo.getBuyerPhone(),
            tradeInfo.getSellerPhone(),
            callback
        );
    }
}