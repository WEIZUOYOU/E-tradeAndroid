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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private long productId;
    private long targetUserId;
    private long currentUserId;
    private final Handler handler = new Handler();
    private Runnable pollTask;
    private final Gson gson = new Gson();
    private Product product;

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
        startPoll();
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
                        
                        // 根据商品卖家ID判断是否显示去下单按钮
                        // 只有当前用户不是商品卖家时，才显示按钮
                        long productSellerId = product.getSellerId();
                        Log.d("ChatActivity", "=== 去下单按钮显示判断 ===");
                        Log.d("ChatActivity", "商品卖家ID: " + productSellerId);
                        Log.d("ChatActivity", "当前用户ID: " + currentUserId);
                        Log.d("ChatActivity", "聊天对象ID: " + targetUserId);
                        
                        if (currentUserId == productSellerId) {
                            // 当前用户是商品卖家，隐藏按钮
                            Log.d("ChatActivity", "当前用户是卖家，隐藏按钮");
                            btnOrder.setVisibility(View.GONE);
                        } else {
                            // 当前用户是买家，显示按钮
                            Log.d("ChatActivity", "当前用户是买家，显示按钮");
                            btnOrder.setVisibility(View.VISIBLE);
                        }
                        
                        // 更新 targetUserId 为商品卖家ID，确保后续逻辑正确
                        targetUserId = productSellerId;
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
        
        Intent intent = new Intent(this, TradeInfoActivity.class);
        intent.putExtra("productId", productId);
        intent.putExtra("sellerId", targetUserId);
        // 判断当前用户是否是卖家（通过比较目标用户ID和当前用户ID）
        // 如果 targetUserId 等于 currentUserId，说明当前用户是卖家
        boolean isSeller = (targetUserId == currentUserId);
        Log.d("ChatActivity", "isSeller: " + isSeller);
        intent.putExtra("isSellerMode", isSeller);
        startActivityForResult(intent, REQUEST_TRADE_INFO);
        
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
                            m.setTradeStatus(o.optInt("tradeStatus", 0));
                            m.setTradeData(o.optString("tradeData", ""));
                            m.setTradeId(o.optLong("tradeId", 0));

                            tempList.add(m);
                        }
                        
                        // 先清空再添加，避免闪烁
                        messageList.clear();
                        messageList.addAll(tempList);
                        
                        runOnUiThread(() -> {
                            if (isFinishing() || isDestroyed()) return;
                            chatAdapter.notifyDataSetChanged();
                            if (messageList.size() > 0) {
                                rvChat.scrollToPosition(messageList.size() - 1);
                            }
                            Log.d("ChatActivity", "Messages loaded successfully, count: " + messageList.size());
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

    private void startPoll() {
        pollTask = () -> {
            loadMessage();
            handler.postDelayed(pollTask, 3000);
        };
        handler.post(pollTask);
    }

    private void openTradeDetail(ChatMessage message) {
        // 检查是否存在有效的交易数据
        if (message.getTradeData() == null || message.getTradeData().isEmpty()) {
            return;
        }

        try {
            TradeInfo tradeInfo = gson.fromJson(message.getTradeData(), TradeInfo.class);

            // 判断当前用户是买家还是卖家
            boolean isBuyer = currentUserId == tradeInfo.getBuyerId();

            Intent intent = new Intent(this, TradeInfoActivity.class);
            intent.putExtra("productId", productId);
            intent.putExtra("sellerId", targetUserId);
            intent.putExtra("tradeId", message.getTradeId());
            intent.putExtra("isSellerMode", !isBuyer);
            startActivityForResult(intent, REQUEST_TRADE_CONFIRM);
        } catch (Exception e) {
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
                case 4: // 待确认修改
                    // 对方发起的修改需要确认
                    openTradeDetail(message);
                    break;
                case 5: // 已完成 - 可查看详情或评价
                    openTradeDetail(message);
                    break;
                case 6: // 已取消
                    openTradeDetail(message);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmReceive(Long tradeId) {
        // 买家确认收到商品 - 使用统一的完成交易接口
        ApiClient.post("api/trade/complete", "{\"tradeId\":" + tradeId + "}", new Callback() {
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
        ApiClient.post("api/trade/complete", "{\"tradeId\":" + tradeId + "}", new Callback() {
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
        
        // 重新启动轮询
        if (handler != null && pollTask != null) {
            // 先移除可能存在的任务，避免重复
            handler.removeCallbacks(pollTask);
        }
        startPoll();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ChatActivity", "onPause called");
        
        // 暂停时停止轮询，避免后台消耗资源
        if (handler != null && pollTask != null) {
            handler.removeCallbacks(pollTask);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && pollTask != null) {
            handler.removeCallbacks(pollTask);
        }
    }
}
