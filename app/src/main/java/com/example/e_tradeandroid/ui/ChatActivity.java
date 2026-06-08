package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.ChatAdapter;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.ChatMessage;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.network.ApiClient;
import com.example.e_tradeandroid.network.WebSocketService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvChat;
    private EditText etInput;
    private Button btnSend, btnOrder;
    private ImageView ivProductMini;
    private TextView tvProductName, tvProductPrice;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    // WebSocket 服务
    private WebSocketService webSocketService;

    private long productId;
    private long targetUserId;
    private long currentUserId;
    private final Handler handler = new Handler();
    private Runnable pollTask;
    private final Gson gson = new Gson();
    private Product product;
    private TradeInfo latestTradeInfo; // 最新的交易信息

    private static final int REQUEST_TRADE_INFO = 1001;
    private static final int REQUEST_TRADE_CONFIRM = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        productId = getIntent().getLongExtra("productId", 0L);
        targetUserId = getIntent().getLongExtra("sellerId", 0L);
        currentUserId = ApiClient.getCurrentUserId();

        initView();
        loadProductInfo();
        loadMessage();
        
        // 初始化 WebSocket（优先使用实时推送）
        initWebSocket();

        // 延迟启动轮询作为备用方案（给 WebSocket 3秒时间连接）
        // 如果 WebSocket 连接成功，会在 onConnected 中停止轮询
        handler.postDelayed(() -> {
            if (!isFinishing()) {
                startPoll();
            }
        }, 3000);
    }
    
    /**
     * 初始化 WebSocket 服务
     */
    private void initWebSocket() {
        webSocketService = new WebSocketService(currentUserId);
        
        // 设置消息监听器
        webSocketService.setMessageListener(new WebSocketService.MessageListener() {
            @Override
            public void onNewMessage(ChatMessage message) {
                Log.d("ChatActivity", "WebSocket 收到新消息: " + message.getContent());
                // 添加消息到列表并刷新
                addMessageToList(message);
            }
            
            @Override
            public void onTradeCardMessage(ChatMessage message) {
                Log.d("ChatActivity", "WebSocket 收到交易卡片: tradeStatus=" + message.getTradeStatus());
                // 添加交易卡片到列表并刷新
                addMessageToList(message);
                
                // 更新顶部按钮状态
                updateTopButton();
            }
        });
        
        // 设置连接状态监听器
        webSocketService.setConnectionListener(new WebSocketService.ConnectionListener() {
            @Override
            public void onConnected() {
                Log.d("ChatActivity", "WebSocket 已连接");
                // WebSocket 连接成功后，可以停止轮询
                stopPoll();
            }
            
            @Override
            public void onDisconnected() {
                Log.d("ChatActivity", "WebSocket 已断开，启用轮询备用方案");
                // WebSocket 断开后，启用轮询
                startPoll();
            }
            
            @Override
            public void onError(String error) {
                Log.e("ChatActivity", "WebSocket 错误: " + error);
                Toast.makeText(ChatActivity.this, "实时推送连接失败，使用轮询模式", Toast.LENGTH_SHORT).show();
            }
        });
        
        // 连接 WebSocket
        webSocketService.connect();
    }
    
    /**
     * 添加消息到列表并刷新UI（带去重）
     */
    private void addMessageToList(ChatMessage message) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) return;

            // 根据消息 ID 去重
            for (ChatMessage existing : messageList) {
                if (existing.getId() != null && existing.getId().equals(message.getId())) {
                    Log.d("ChatActivity", "消息已存在，跳过添加: " + message.getId());
                    return; // 已存在，不添加
                }
            }

            messageList.add(message);
            chatAdapter.notifyDataSetChanged();

            // 滚动到最后一条消息
            if (messageList.size() > 0) {
                rvChat.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void initView() {
        rvChat = findViewById(R.id.rv_chat);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);
        btnOrder = findViewById(R.id.btn_order);
        ivProductMini = findViewById(R.id.iv_product_mini);
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductPrice = findViewById(R.id.tv_product_price);

        // 初始化按钮状态 - 默认隐藏，等商品信息加载后再判断
        btnOrder.setVisibility(View.GONE);
        
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, (int) currentUserId);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        // 设置交易卡片点击监听
        chatAdapter.setOnTradeCardClickListener(new ChatAdapter.OnTradeCardClickListener() {
            @Override
            public void onTradeCardClick(ChatMessage message) {
                // 点击交易卡片查看详情
                openTradeDetail(message);
            }

            @Override
            public void onTradeActionClick(ChatMessage message) {
                // 点击操作按钮
                handleTradeAction(message);
            }
        });

        btnSend.setOnClickListener(v -> sendMsg());
        btnOrder.setOnClickListener(v -> goToTradeInfo());
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void loadProductInfo() {
        if (productId == 0) return;

        ApiClient.get("api/product/detail/" + productId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "加载商品信息失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Product> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Product>>() {}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    product = baseResp.getData();
                    runOnUiThread(() -> {
                        tvProductName.setText(product.getName());
                        tvProductPrice.setText("￥" + product.getPrice());

                        // 加载商品缩略图 - 添加空值检查
                        if (product.getImages() != null && !product.getImages().isEmpty()) {
                            String firstImage = product.getImages().get(0);
                            if (firstImage != null && !firstImage.isEmpty() && !firstImage.equals("null")) {
                                String imageUrl = firstImage.startsWith("http")
                                        ? firstImage
                                        : ApiClient.BASE_URL + firstImage;
                                Glide.with(ChatActivity.this)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .into(ivProductMini);
                            }
                        }
                        
                        // 只有当 targetUserId 为 0 时才更新为商品卖家ID
                        // 避免覆盖从 Intent 传入的正确聊天对象ID
                        if (targetUserId == 0 && product != null) {
                            targetUserId = product.getSellerId();
                            Log.d("ChatActivity", "targetUserId was 0, updated from product: " + targetUserId);
                            // 重新加载消息
                            loadMessage();
                        }

                        // 更新顶部按钮状态
                        updateTopButton();
                    });
                }
            }
        });
    }

    private void goToTradeInfo() {
        // 调试日志
        Log.d("ChatActivity", "goToTradeInfo called");
        Log.d("ChatActivity", "productId: " + productId);
        Log.d("ChatActivity", "sellerId: " + targetUserId);
        Log.d("ChatActivity", "currentUserId: " + currentUserId);
        
        if (latestTradeInfo != null && latestTradeInfo.getId() != null && latestTradeInfo.getId() > 0) {
            // 已有交易信息，跳转到交易详情
            Log.d("ChatActivity", "已有交易信息，跳转到交易详情");
            Intent intent = new Intent(this, TradeInfoActivity.class);
            intent.putExtra("productId", productId);
            intent.putExtra("sellerId", targetUserId);
            intent.putExtra("tradeId", latestTradeInfo.getId());
            
            boolean isBuyer = currentUserId == latestTradeInfo.getBuyerId();
            intent.putExtra("isSellerMode", !isBuyer);
            
            startActivityForResult(intent, REQUEST_TRADE_CONFIRM);
        } else {
            // 无交易信息，跳转到创建交易页面
            Log.d("ChatActivity", "无交易信息，跳转到创建交易页面");
            Intent intent = new Intent(this, TradeInfoActivity.class);
            intent.putExtra("productId", productId);
            intent.putExtra("sellerId", targetUserId);
            
            // 判断当前用户是否是卖家（通过比较目标用户ID和当前用户ID）
            // 如果 targetUserId 等于 currentUserId，说明当前用户是卖家
            boolean isSeller = (targetUserId == currentUserId);
            Log.d("ChatActivity", "isSeller: " + isSeller);
            intent.putExtra("isSellerMode", isSeller);
            
            startActivityForResult(intent, REQUEST_TRADE_INFO);
        }
        
        Log.d("ChatActivity", "startActivityForResult called, should navigate to TradeInfoActivity");
    }

    private void sendMsg() {
        String content = etInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "输入不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("receiverId", targetUserId);
            body.put("productId", productId);
            body.put("content", content);
            body.put("type", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiClient.post("api/message/send", body.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(ChatActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    etInput.setText("");
                    loadMessage();
                });
            }
        });
    }

    private void loadMessage() {
        Log.d("ChatActivity", "loadMessage called, targetUserId: " + targetUserId);
        
        if (targetUserId == 0) {
            Log.e("ChatActivity", "targetUserId is 0, cannot load messages");
            return;
        }
        
        String url = ApiClient.BASE_URL + "api/message/history?targetUserId=" + targetUserId;
        Request request = new Request.Builder().url(url).build();
        ApiClient.getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ChatActivity", "Message load failed: " + e.getMessage());
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    // 添加错误提示，方便调试
                    Toast.makeText(ChatActivity.this, "消息加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    Log.d("ChatActivity", "Message response: " + responseBody);
                    
                    JSONObject obj = new JSONObject(responseBody);
                    if (obj.getInt("code") == 200) {
                        JSONArray arr = obj.getJSONArray("data");
                        Log.d("ChatActivity", "Message count: " + arr.length());
                        
                        // 创建临时列表，避免在加载过程中UI显示空列表
                        List<ChatMessage> tempList = new ArrayList<>();
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            ChatMessage m = new ChatMessage();
                            m.setId(o.optLong("id"));
                            m.setSenderId(o.optLong("senderId"));
                            m.setReceiverId(o.optLong("receiverId"));
                            m.setProductId(o.optLong("productId"));
                            m.setContent(o.optString("content", ""));
                            m.setType(o.optInt("type", 0));
                            m.setIsRead(o.optInt("isRead", 0));
                            m.setCreateTime(o.optString("createTime", ""));

                            // 交易卡片相关字段
                            int msgType = o.optInt("type", 0);
                            m.setType(msgType);
                            
                            if (msgType == 1) {
                                String tradeDataStr = o.optString("tradeData", "");
                                if (tradeDataStr.isEmpty() || tradeDataStr.equals("null")) {
                                    Log.w("ChatActivity", "跳过无效交易卡片: id=" + m.getId());
                                    continue; // 直接丢弃
                                }
                                m.setTradeData(tradeDataStr);
                                m.setTradeId(o.optLong("tradeId", 0));
                                m.setTradeStatus(o.optInt("tradeStatus", 0));
                            } else {
                                m.setTradeStatus(o.optInt("tradeStatus", 0));
                                m.setTradeData(o.optString("tradeData", ""));
                                m.setTradeId(o.optLong("tradeId", 0));
                            }

                            tempList.add(m);
                        }
                        
                        // 先清空再添加，避免闪烁
                        messageList.clear();
                        messageList.addAll(tempList);
                        
                        // 查找最新的交易信息
                        latestTradeInfo = null;
                        for (ChatMessage m : messageList) {
                            if (m.getTradeData() != null && !m.getTradeData().isEmpty()) {
                                latestTradeInfo = gson.fromJson(m.getTradeData(), TradeInfo.class);
                            }
                        }
                        
                        // 同步交易卡片最新状态（解决消息中状态过时的问题）
                        syncTradeCardStatus();
                        
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            chatAdapter.notifyDataSetChanged();
                            if (messageList.size() > 0) {
                                rvChat.scrollToPosition(messageList.size() - 1);
                            }
                            Log.d("ChatActivity", "Messages loaded successfully, count: " + messageList.size());
                            
                            // 更新顶部按钮状态
                            updateTopButton();
                        });
                    } else {
                        String errorMsg = obj.optString("msg", "未知错误");
                        Log.e("ChatActivity", "Load failed: " + errorMsg);
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            Toast.makeText(ChatActivity.this, "加载失败: " + errorMsg, Toast.LENGTH_SHORT).show();
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("ChatActivity", "JSON parse error: " + e.getMessage());
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(ChatActivity.this, "消息解析失败", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    /**
     * 同步交易卡片状态（修复评价状态覆盖问题 + 避免状态跳动）
     * 问题1：后端返回的 TradeInfo.status 始终为 4（交易已完成），会覆盖评价状态 6/7/8
     * 解决方案：只同步交易状态（0-5）的卡片，评价卡片（6-8）不同步
     * 问题2：异步拉取状态后直接 notifyDataSetChanged 导致卡片跳动
     * 解决方案：只在状态真正变化时才刷新
     */
    private void syncTradeCardStatus() {
        // 收集所有需要同步的交易ID（避免重复）
        java.util.Set<Long> tradeIdsToSync = new java.util.HashSet<>();
        for (ChatMessage m : messageList) {
            if (m.getType() != null && m.getType() == 1 && m.getTradeId() != null && m.getTradeId() > 0) {
                // 只同步交易状态（0-5）的卡片，评价卡片（6-8）不同步
                Integer outerStatus = m.getTradeStatus();
                if (outerStatus == null || outerStatus <= 5) {
                    tradeIdsToSync.add(m.getTradeId());
                }
            }
        }
        
        if (tradeIdsToSync.isEmpty()) {
            return;
        }
        
        Log.d("ChatActivity", "syncTradeCardStatus: 需要同步的交易数 = " + tradeIdsToSync.size());
        
        // 对每个交易ID，调用API获取最新状态
        for (Long tradeId : tradeIdsToSync) {
            ApiClient.get("api/trade/detail/" + tradeId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("ChatActivity", "获取交易详情失败: tradeId=" + tradeId + ", error=" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String respBody = response.body().string();
                        BaseResponse<TradeInfo> baseResp = gson.fromJson(respBody, 
                            new TypeToken<BaseResponse<TradeInfo>>() {}.getType());
                        
                        if (baseResp.isSuccess() && baseResp.getData() != null) {
                            TradeInfo latestTradeInfo = baseResp.getData();
                            int latestStatus = latestTradeInfo.getTradeStatus() != null ? latestTradeInfo.getTradeStatus() : 0;
                            
                            Log.d("ChatActivity", "获取交易最新状态: tradeId=" + tradeId + ", status=" + latestStatus);
                            
                            // 更新消息列表中所有该交易的卡片状态
                            runOnUiThread(() -> {
                                if (isFinishing() || isDestroyed()) return;
                                
                                // 【注释开始】tradeStatusMap 已在 ChatAdapter 中移除
                                /*
                                boolean changed = false;
                                
                                // 更新tradeStatusMap
                                Integer oldStatus = tradeStatusMap.get(tradeId);
                                if (oldStatus == null || oldStatus != latestStatus) {
                                    tradeStatusMap.put(tradeId, latestStatus);
                                    chatAdapter.setTradeStatusMap(tradeStatusMap);
                                    changed = true;
                                }
                                */
                                // 【注释结束】
                                
                                // 【注释开始】不再修改已有的聊天消息，保持发送时的状态快照
                                /*
                                for (ChatMessage m : messageList) {
                                    if (m.getTradeId() != null && m.getTradeId().equals(tradeId)) {
                                        Integer msgOldStatus = m.getTradeStatus();
                                        if (msgOldStatus == null || msgOldStatus != latestStatus) {
                                            // 更新 tradeData 中的状态
                                            try {
                                                com.google.gson.JsonObject tradeDataObj = gson.fromJson(m.getTradeData(), com.google.gson.JsonObject.class);
                                                tradeDataObj.addProperty("tradeStatus", latestStatus);
                                                
                                                // 同时更新电话信息（如果有的话）
                                                if (latestTradeInfo.getSellerPhone() != null) {
                                                    tradeDataObj.addProperty("sellerPhone", latestTradeInfo.getSellerPhone());
                                                }
                                                if (latestTradeInfo.getBuyerPhone() != null) {
                                                    tradeDataObj.addProperty("buyerPhone", latestTradeInfo.getBuyerPhone());
                                                }
                                                
                                                m.setTradeData(gson.toJson(tradeDataObj));
                                                m.setTradeStatus(latestStatus);
                                                
                                                changed = true;
                                                Log.d("ChatActivity", "已更新交易卡片状态: tradeId=" + tradeId + ", status=" + latestStatus);
                                            } catch (Exception e) {
                                                Log.e("ChatActivity", "更新交易卡片状态失败: " + e.getMessage());
                                            }
                                        }
                                    }
                                }
                                */
                                // 【注释结束】
                                
                                // 更新 latestTradeInfo（用于顶部按钮）
                                ChatActivity.this.latestTradeInfo = latestTradeInfo;
                                
                                // 不再刷新消息列表适配器，只刷新按钮状态
                                // if (changed) {
                                //     chatAdapter.notifyDataSetChanged();
                                //     updateTopButton();
                                // }
                                updateTopButton();  // 只刷新顶部按钮
                            });
                        }
                    } catch (Exception e) {
                        Log.e("ChatActivity", "解析交易详情失败: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void startPoll() {
        // ★ 确保只有一个轮询任务
        stopPoll();
        
        // 如果 WebSocket 已连接，不需要轮询
        if (webSocketService != null && webSocketService.isConnected()) {
            Log.d("ChatActivity", "WebSocket 已连接，跳过轮询");
            return;
        }
        
        pollTask = () -> {
            loadMessage();
            handler.postDelayed(pollTask, 3000);
        };
        handler.post(pollTask);
    }
    
    /**
     * 停止轮询
     */
    private void stopPoll() {
        if (handler != null && pollTask != null) {
            handler.removeCallbacks(pollTask);
            Log.d("ChatActivity", "轮询已停止");
        }
    }

    private void openTradeDetail(ChatMessage message) {
        // 检查是否存在有效的交易数据
        if (message.getTradeData() == null || message.getTradeData().isEmpty()) {
            Log.e("ChatActivity", "openTradeDetail: tradeData 为空");
            return;
        }

        try {
            TradeInfo tradeInfo = gson.fromJson(message.getTradeData(), TradeInfo.class);

            // 判断当前用户是买家还是卖家
            boolean isBuyer = currentUserId == tradeInfo.getBuyerId();
            int status = tradeInfo.getTradeStatus() != null ? tradeInfo.getTradeStatus() : 0;
            
            Log.d("ChatActivity", "=== openTradeDetail ===");
            Log.d("ChatActivity", "tradeId=" + message.getTradeId());
            Log.d("ChatActivity", "currentUserId=" + currentUserId);
            Log.d("ChatActivity", "buyerId=" + tradeInfo.getBuyerId());
            Log.d("ChatActivity", "sellerId=" + tradeInfo.getSellerId());
            Log.d("ChatActivity", "status=" + status);
            Log.d("ChatActivity", "isBuyer=" + isBuyer);
            Log.d("ChatActivity", "isSellerMode=" + !isBuyer);

            Intent intent = new Intent(this, TradeInfoActivity.class);
            // 从 tradeInfo 中获取正确的商品 ID 和卖家 ID
            intent.putExtra("productId", tradeInfo.getProductId() != null ? tradeInfo.getProductId() : productId);
            intent.putExtra("sellerId", tradeInfo.getSellerId() != null ? tradeInfo.getSellerId() : targetUserId);
            intent.putExtra("tradeId", message.getTradeId());
            intent.putExtra("isSellerMode", !isBuyer);
            startActivityForResult(intent, REQUEST_TRADE_CONFIRM);
        } catch (Exception e) {
            Log.e("ChatActivity", "openTradeDetail 异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleTradeAction(ChatMessage message) {
        if (message.getTradeData() == null || message.getTradeData().isEmpty()) {
            return;
        }

        try {
            TradeInfo tradeInfo = gson.fromJson(message.getTradeData(), TradeInfo.class);
            int status = tradeInfo.getTradeStatus() != null ? tradeInfo.getTradeStatus() : 0;
            boolean isSelf = message.getSenderId() != null && message.getSenderId() == currentUserId;
            boolean isBuyer = currentUserId == tradeInfo.getBuyerId();
            boolean isSeller = currentUserId == tradeInfo.getSellerId();

            Log.d("ChatActivity", "handleTradeAction: status=" + status + ", isSelf=" + isSelf + 
                    ", isBuyer=" + isBuyer + ", isSeller=" + isSeller);

            switch (status) {
                case 0: // 待卖家确认
                    // 双方都可以查看详情，但只有卖家可以操作
                    openTradeDetail(message);
                    break;
                case 1: // 待交易 - 双方都可以点击查看详情或确认完成
                    openTradeDetail(message);
                    break;
                case 2: // 卖家已确认，等待买家确认
                    if (isBuyer) {
                        // 买家可以确认完成
                        openTradeDetail(message);
                    } else {
                        // 卖家只能查看详情
                        openTradeDetail(message);
                    }
                    break;
                case 3: // 买家已确认，等待卖家确认
                    if (isSeller) {
                        // 卖家可以确认完成
                        openTradeDetail(message);
                    } else {
                        // 买家只能查看详情
                        openTradeDetail(message);
                    }
                    break;
                case 4: // 已完成 - 可查看详情或评价
                    openTradeDetail(message);
                    break;
                case 5: // 已取消
                    openTradeDetail(message);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmReceive(Long tradeId) {
        // 买家确认收到商品 - 使用统一的完成交易接口
        ApiClient.post("api/trade/complete", "{\"tradeId\":" + tradeId + ",\"operatorType\":\"BUYER\"}", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(ChatActivity.this, "确认失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<String>>() {}.getType());
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (baseResp.isSuccess()) {
                        Toast.makeText(ChatActivity.this, "已确认收到商品", Toast.LENGTH_SHORT).show();
                        loadMessage();
                    } else {
                        Toast.makeText(ChatActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void confirmComplete(Long tradeId) {
        // 卖家确认交易完成 - 使用统一的完成交易接口
        ApiClient.post("api/trade/complete", "{\"tradeId\":" + tradeId + ",\"operatorType\":\"SELLER\"}", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(ChatActivity.this, "确认失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<String>>() {}.getType());
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (baseResp.isSuccess()) {
                        Toast.makeText(ChatActivity.this, "交易已完成", Toast.LENGTH_SHORT).show();
                        loadMessage();
                    } else {
                        Toast.makeText(ChatActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TRADE_INFO:
                    // 买家提交交易信息成功，刷新消息列表
                    Log.d("ChatActivity", "onActivityResult: REQUEST_TRADE_INFO");
                    loadMessage();
                    break;
                case REQUEST_TRADE_CONFIRM:
                    // 交易确认成功，刷新消息列表
                    Log.d("ChatActivity", "onActivityResult: REQUEST_TRADE_CONFIRM");
                    loadMessage();
                    break;
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ChatActivity", "onResume called");
        
        // 优先使用 WebSocket
        if (webSocketService != null && !webSocketService.isConnected()) {
            webSocketService.connect();
        }
        
        // WebSocket 未连接时启用轮询
        if (webSocketService == null || !webSocketService.isConnected()) {
            if (handler != null && pollTask != null) {
                // 先移除可能存在的任务，避免重复
                handler.removeCallbacks(pollTask);
            }
            startPoll();
        }
        
        // 更新顶部按钮状态
        updateTopButton();
    }
    
    /**
     * 根据交易状态更新顶部按钮文本
     */
    private void updateTopButton() {
        if (product == null) return;
        
        Log.d("ChatActivity", "updateTopButton called");
        Log.d("ChatActivity", "currentUserId: " + currentUserId);
        Log.d("ChatActivity", "product.getSellerId(): " + product.getSellerId());
        
        if (currentUserId == product.getSellerId()) {
            // 当前用户是商品卖家，隐藏按钮
            Log.d("ChatActivity", "当前用户是卖家，隐藏按钮");
            btnOrder.setVisibility(View.GONE);
            return;
        }
        
        // 当前用户是买家，显示按钮
        btnOrder.setVisibility(View.VISIBLE);
        
        if (latestTradeInfo == null || latestTradeInfo.getTradeStatus() == null) {
            // 没有交易信息，显示"去下单"
            btnOrder.setText("去下单");
            Log.d("ChatActivity", "无交易信息，显示：去下单");
            return;
        }
        
        int status = latestTradeInfo.getTradeStatus();
        Log.d("ChatActivity", "交易状态: " + status);
        
        boolean isBuyer = currentUserId == latestTradeInfo.getBuyerId();
        boolean isSeller = currentUserId == latestTradeInfo.getSellerId();
        
        switch (status) {
            case 0: // 待卖家确认
                if (isBuyer) {
                    btnOrder.setText("等待确认");
                } else {
                    btnOrder.setText("去确认");
                }
                break;
            case 1: // 待交易
                btnOrder.setText("确认完成");
                break;
            case 2: // 卖家已确认
                if (isBuyer) {
                    btnOrder.setText("确认完成");
                } else {
                    btnOrder.setText("等待对方");
                }
                break;
            case 3: // 买家已确认
                if (isBuyer) {
                    btnOrder.setText("等待对方");
                } else {
                    btnOrder.setText("确认完成");
                }
                break;
            case 4: // 已完成
                btnOrder.setText("已完成");
                btnOrder.setEnabled(false);
                break;
            case 5: // 已取消
                btnOrder.setText("已取消");
                btnOrder.setEnabled(false);
                break;
            default:
                btnOrder.setText("去下单");
                btnOrder.setEnabled(true);
                break;
        }
        
        Log.d("ChatActivity", "按钮文本设置为: " + btnOrder.getText());
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ChatActivity", "onPause called");
        
        // 暂停时停止轮询，避免后台消耗资源
        if (handler != null && pollTask != null) {
            handler.removeCallbacks(pollTask);
        }
        
        // 暂停时断开 WebSocket（可选，根据需求决定）
        // 如果希望后台也能接收消息，可以不断开
        // if (webSocketService != null) {
        //     webSocketService.disconnect();
        // }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // 停止轮询
        if (handler != null && pollTask != null) {
            handler.removeCallbacks(pollTask);
        }
        
        // 断开 WebSocket
        if (webSocketService != null) {
            webSocketService.disconnect();
        }
    }
}
