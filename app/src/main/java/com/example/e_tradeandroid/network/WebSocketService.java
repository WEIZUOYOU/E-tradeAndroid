package com.example.e_tradeandroid.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.e_tradeandroid.model.ChatMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import java.util.concurrent.TimeUnit;

/**
 * WebSocket 服务类
 * 用于实时接收消息推送
 */
public class WebSocketService {
    private static final String TAG = "WebSocketService";
    
    // WebSocket 服务器地址（从 ApiClient 获取，统一配置）
    private static String getWebSocketUrl() {
        return ApiClient.getWebSocketUrl();
    }
    
    private OkHttpClient client;
    private WebSocket webSocket;
    private Gson gson;
    private Handler mainHandler;
    
    private long currentUserId;
    private MessageListener messageListener;
    private ConnectionListener connectionListener;
    
    private boolean isConnected = false;
    private boolean shouldReconnect = true;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY = 3000; // 3秒
    
    /**
     * 消息监听器接口
     */
    public interface MessageListener {
        void onNewMessage(ChatMessage message);
        void onTradeCardMessage(ChatMessage message);
    }
    
    /**
     * 连接状态监听器接口
     */
    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }
    
    public WebSocketService(long userId) {
        this.currentUserId = userId;
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // 创建 OkHttpClient，设置超时时间
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .pingInterval(25, TimeUnit.SECONDS) // 心跳间隔
                .build();
    }
    
    /**
     * 设置消息监听器
     */
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }
    
    /**
     * 设置连接状态监听器
     */
    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }
    
    /**
     * 连接 WebSocket
     */
    public void connect() {
        if (isConnected && webSocket != null) {
            Log.d(TAG, "WebSocket 已连接，无需重复连接");
            return;
        }
        
        shouldReconnect = true;
        
        // 构建 WebSocket 请求，携带用户ID（从 ApiClient 获取统一的服务器地址）
        String url = getWebSocketUrl() + "?userId=" + currentUserId;
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        Log.d(TAG, "开始连接 WebSocket: " + url);
        
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket 连接成功");
                isConnected = true;
                reconnectAttempts = 0;
                
                mainHandler.post(() -> {
                    if (connectionListener != null) {
                        connectionListener.onConnected();
                    }
                });
            }
            
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "收到消息: " + text);
                handleMessage(text);
            }
            
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket 正在关闭: code=" + code + ", reason=" + reason);
                webSocket.close(1000, null);
                isConnected = false;
            }
            
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket 已关闭: code=" + code + ", reason=" + reason);
                isConnected = false;
                
                mainHandler.post(() -> {
                    if (connectionListener != null) {
                        connectionListener.onDisconnected();
                    }
                });
                
                // 自动重连
                if (shouldReconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    scheduleReconnect();
                }
            }
            
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket 连接失败: " + t.getMessage());
                isConnected = false;
                
                mainHandler.post(() -> {
                    if (connectionListener != null) {
                        connectionListener.onError(t.getMessage());
                    }
                });
                
                // 自动重连
                if (shouldReconnect && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    scheduleReconnect();
                }
            }
        });
    }
    
    /**
     * 处理收到的消息
     */
    private void handleMessage(String text) {
        try {
            JsonObject obj = gson.fromJson(text, JsonObject.class);
            
            // 判断消息类型（后端使用字符串类型）
            String messageTypeStr = obj.has("type") ? obj.get("type").getAsString() : "";
            
            ChatMessage message = new ChatMessage();
            message.setId(obj.has("id") ? obj.get("id").getAsLong() : 0);
            message.setSenderId(obj.has("senderId") ? obj.get("senderId").getAsLong() : 0);
            message.setReceiverId(obj.has("receiverId") ? obj.get("receiverId").getAsLong() : 0);
            message.setProductId(obj.has("productId") ? obj.get("productId").getAsLong() : 0);
            message.setContent(obj.has("content") ? obj.get("content").getAsString() : "");
            
            // 后端使用 messageType 字段表示消息类型（1=交易卡片）
            int messageType = obj.has("messageType") ? obj.get("messageType").getAsInt() : 0;
            message.setType(messageType);
            
            message.setIsRead(obj.has("isRead") ? obj.get("isRead").getAsInt() : 0);
            message.setCreateTime(obj.has("createTime") ? obj.get("createTime").getAsString() : "");
            
            // 交易卡片相关字段
            message.setTradeId(obj.has("tradeId") ? obj.get("tradeId").getAsLong() : 0);
            message.setTradeStatus(obj.has("tradeStatus") ? obj.get("tradeStatus").getAsInt() : 0);
            message.setTradeData(obj.has("tradeData") && !obj.get("tradeData").isJsonNull() 
                    ? obj.get("tradeData").getAsString() : "");
            
            mainHandler.post(() -> {
                if (messageListener != null) {
                    if (messageType == 1) { // 交易卡片消息
                        messageListener.onTradeCardMessage(message);
                    } else { // 普通消息
                        messageListener.onNewMessage(message);
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "解析消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 安排重连
     */
    private void scheduleReconnect() {
        reconnectAttempts++;
        Log.d(TAG, "安排重连，第 " + reconnectAttempts + " 次尝试");
        
        mainHandler.postDelayed(() -> {
            if (shouldReconnect) {
                connect();
            }
        }, RECONNECT_DELAY);
    }
    
    /**
     * 断开 WebSocket
     */
    public void disconnect() {
        shouldReconnect = false;
        if (webSocket != null) {
            webSocket.close(1000, "用户主动断开");
            webSocket = null;
        }
        isConnected = false;
        Log.d(TAG, "WebSocket 已断开");
    }
    
    /**
     * 发送消息
     */
    public boolean sendMessage(String message) {
        if (webSocket != null && isConnected) {
            return webSocket.send(message);
        }
        Log.w(TAG, "WebSocket 未连接，无法发送消息");
        return false;
    }
    
    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * 获取当前用户ID
     */
    public long getCurrentUserId() {
        return currentUserId;
    }
}