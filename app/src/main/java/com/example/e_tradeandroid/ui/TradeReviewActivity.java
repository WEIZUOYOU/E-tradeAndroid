package com.example.e_tradeandroid.ui;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.model.ReviewRequest;
import com.example.e_tradeandroid.model.User;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TradeReviewActivity extends AppCompatActivity {
    private ImageView ivBack, ivUserAvatar;
    private TextView tvProductName, tvTradeTime, tvTradeLocation, tvUserName, tvUserRole;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmit;
    private LinearLayout llTags, llTags2;

    private long tradeId;
    private long toUserId;
    private boolean isSellerMode;
    private TradeInfo tradeInfo;
    private User targetUser;
    private final Gson gson = new Gson();

    private String[] positiveTags = {"守时守信", "态度友好", "商品相符", "沟通顺畅"};
    private String[] negativeTags = {"迟到", "商品不符", "态度差", "临时改价"};
    private List<String> selectedTags = new ArrayList<>();
    private List<TextView> tagViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_review);

        tradeId = getIntent().getLongExtra("tradeId", 0L);
        toUserId = getIntent().getLongExtra("toUserId", 0L);
        isSellerMode = getIntent().getBooleanExtra("isSellerMode", false);

        initViews();
        // 初始状态禁用提交按钮，等待数据加载完成
        btnSubmit.setEnabled(false);
        initListeners();
        loadTradeInfo();
        loadTargetUser();
        initTags();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivUserAvatar = findViewById(R.id.iv_user_avatar);
        tvProductName = findViewById(R.id.tv_product_name);
        tvTradeTime = findViewById(R.id.tv_trade_time);
        tvTradeLocation = findViewById(R.id.tv_trade_location);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserRole = findViewById(R.id.tv_user_role);
        ratingBar = findViewById(R.id.rating_bar);
        etComment = findViewById(R.id.et_comment);
        btnSubmit = findViewById(R.id.btn_submit);
        llTags = findViewById(R.id.ll_tags);
        llTags2 = findViewById(R.id.ll_tags2);
    }

    private void initListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void initTags() {
        // 第一行标签
        for (String tag : positiveTags) {
            TextView tv = createTagView(tag);
            llTags.addView(tv);
            tagViews.add(tv);
        }

        // 第二行标签
        for (String tag : negativeTags) {
            TextView tv = createTagView(tag);
            llTags2.addView(tv);
            tagViews.add(tv);
        }
    }

    private TextView createTagView(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(13);
        tv.setPadding(16, 8, 16, 8);
        tv.setBackgroundResource(R.drawable.bg_pill_button);
        tv.setTextColor(getResources().getColorStateList(R.color.selector_tag_text));
        tv.setClickable(true);
        tv.setFocusable(true);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 12, 12);
        tv.setLayoutParams(params);

        tv.setOnClickListener(v -> toggleTag(tv, text));

        return tv;
    }

    private void toggleTag(TextView tv, String tag) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag);
            tv.setBackgroundResource(R.drawable.bg_pill_button);
            tv.setTextColor(getResources().getColorStateList(R.color.selector_tag_text));
        } else {
            selectedTags.add(tag);
            tv.setBackgroundResource(R.drawable.bg_btn_primary);
            tv.setTextColor(getResources().getColor(R.color.white));
        }
    }

    private void loadTradeInfo() {
        ApiClient.get("api/trade/detail/" + tradeId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(TradeReviewActivity.this, "加载交易信息失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<TradeInfo> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<TradeInfo>>() {}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    tradeInfo = baseResp.getData();
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        tvProductName.setText(tradeInfo.getProductName());
                        tvTradeTime.setText("交易时间：" + tradeInfo.getMeetingTime());
                        tvTradeLocation.setText("交易地点：" + tradeInfo.getMeetingLocation());
                        // 数据加载成功，尝试启用提交按钮
                        checkAndEnableSubmit();
                    });
                }
            }
        });
    }

    private void loadTargetUser() {
        ApiClient.get("api/user/info/" + toUserId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    Toast.makeText(TradeReviewActivity.this, "加载用户信息失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<User> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<User>>() {}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    targetUser = baseResp.getData();
                    runOnUiThread(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        tvUserName.setText(targetUser.getUsername());
                        tvUserRole.setText(isSellerMode ? "买家" : "卖家");

                        if (targetUser.getAvatar() != null && !targetUser.getAvatar().isEmpty()) {
                            String avatarUrl = buildAvatarUrl(targetUser.getAvatar());
                            Glide.with(TradeReviewActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivUserAvatar);
                        }
                        // 数据加载成功，尝试启用提交按钮
                        checkAndEnableSubmit();
                    });
                }
            }
        });
    }
    
    /**
     * 构建正确的头像URL，避免双斜杠问题
     */
    private String buildAvatarUrl(String avatar) {
        if (avatar.startsWith("http")) {
            return avatar;
        }
        String base = ApiClient.BASE_URL;
        // 移除BASE_URL末尾的斜杠
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        // 确保avatar路径以斜杠开头
        String path = avatar.startsWith("/") ? avatar : "/" + avatar;
        return base + path;
    }
    
    /**
     * 检查所有必要数据是否已加载完成，完成后启用提交按钮
     */
    private void checkAndEnableSubmit() {
        if (tradeInfo != null && targetUser != null && toUserId > 0) {
            btnSubmit.setEnabled(true);
        }
    }

    private void submitReview() {
        // 1. 检查评分（最重要，放在前面）
        int rating = (int) ratingBar.getRating();
        if (rating == 0) {
            Toast.makeText(this, "请选择评分", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 检查评价目标用户ID是否有效
        if (toUserId <= 0) {
            Toast.makeText(this, "无法获取评价对象", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. 检查tradeId是否有效
        if (tradeId <= 0) {
            Toast.makeText(this, "交易ID无效", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. 防重复提交 - 点击后立即禁用按钮
        btnSubmit.setEnabled(false);

        String comment = etComment.getText().toString().trim();

        // 构建请求 - 匹配后端 API: POST /api/review
        ReviewRequest request = new ReviewRequest();
        request.setTradeId(tradeId);
        request.setRating(rating);
        request.setContent(comment);
        request.setTagsArray(selectedTags.toArray(new String[0]));

        String json = gson.toJson(request);
        // 添加调试日志
        Log.d("TradeReviewActivity", "提交评价请求:");
        Log.d("TradeReviewActivity", "tradeId: " + tradeId);
        Log.d("TradeReviewActivity", "rating: " + rating);
        Log.d("TradeReviewActivity", "content: " + comment);
        Log.d("TradeReviewActivity", "requestJson: " + json);
        
        // API路径: /api/review
        ApiClient.post("api/review", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    btnSubmit.setEnabled(true); // 失败后重新启用按钮
                    Toast.makeText(TradeReviewActivity.this, "提交失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<String>>() {}.getType());
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    if (baseResp.isSuccess()) {
                        Toast.makeText(TradeReviewActivity.this, "评价提交成功", Toast.LENGTH_SHORT).show();
                        
                        // ❌ 不再由前端发送消息，后端已实现完善的防重复机制
                        // sendReviewMessage();
                        
                        // ✅ 直接关闭页面，等待后端推送消息
                        safeFinish();
                    } else {
                        btnSubmit.setEnabled(true); // 失败后重新启用按钮
                        Toast.makeText(TradeReviewActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    /**
     * 发送评价消息通知对方 - 参考交易确认流程的卡片发送逻辑
     * 评价状态：6=买家已评价，7=卖家已评价，8=双方已评价
     */
    private void sendReviewMessage() {
        // 直接使用 Intent 传入的参数，不需要依赖 tradeInfo
        if (toUserId <= 0) {
            Log.e("TradeReviewActivity", "无法发送评价消息: toUserId 无效 = " + toUserId);
            safeFinish();
            return;
        }
        
        // 计算新的交易状态
        int newStatus = calculateReviewStatus();
        Log.d("TradeReviewActivity", "sendReviewMessage: newStatus=" + newStatus + ", toUserId=" + toUserId);
        
        // 发送评价卡片消息
        sendReviewCardMessage(tradeId, newStatus, toUserId, () -> {
            Log.d("TradeReviewActivity", "评价消息发送成功，关闭页面");
            // 发送广播通知聊天页面刷新
            sendBroadcastToRefreshChat(toUserId);
            safeFinish();
        });
    }
    
    /**
     * 计算评价后的交易状态
     * 注意：这个方法是临时的，最佳方案是在后端判断状态并发送消息
     */
    private int calculateReviewStatus() {
        // 使用 isSellerMode 判断当前用户身份
        // isSellerMode = true 表示当前用户是卖家
        // isSellerMode = false 表示当前用户是买家
        int newStatus;
        if (isSellerMode) {
            newStatus = 7; // 卖家已评价
            Log.d("TradeReviewActivity", "当前用户是卖家，评价后状态变为 7（卖家已评价）");
        } else {
            newStatus = 6; // 买家已评价
            Log.d("TradeReviewActivity", "当前用户是买家，评价后状态变为 6（买家已评价）");
        }
        
        // TODO: 这里应该从后端获取最新的交易状态，而不是本地计算
        // 临时方案：先返回基础状态，让后端决定最终状态和消息内容
        return newStatus;
    }
    
    /**
     * 发送评价卡片消息（参考交易确认流程）
     * 参数说明：tradeId, tradeStatus, receiverId(接收者), callback(发送成功后的回调)
     */
    private void sendReviewCardMessage(Long tradeId, int tradeStatus, long receiverId, Runnable callback) {
        Log.d("TradeReviewActivity", "sendReviewCardMessage 开始: tradeId=" + tradeId 
            + ", tradeStatus=" + tradeStatus + ", receiverId=" + receiverId);
        
        if (receiverId == 0) {
            Log.e("TradeReviewActivity", "receiverId 为 0，无法发送评价消息");
            if (callback != null) {
                runOnUiThread(callback);
            }
            return;
        }
        
        // 构建请求 - 参考交易确认流程的消息结构
        com.google.gson.JsonObject msgRequest = new com.google.gson.JsonObject();
        msgRequest.addProperty("receiverId", receiverId);
        msgRequest.addProperty("productId", 0); // productId 在卡片中可选
        msgRequest.addProperty("content", getReviewContent(tradeStatus));
        msgRequest.addProperty("type", 1); // 1 表示交易卡片类型
        msgRequest.addProperty("tradeId", tradeId);
        msgRequest.addProperty("tradeStatus", tradeStatus);
        
        String msgJson = gson.toJson(msgRequest);
        Log.d("TradeReviewActivity", "发送评价消息 JSON: " + msgJson);
        
        ApiClient.post("api/message/send", msgJson, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TradeReviewActivity", "发送评价消息失败: " + e.getMessage());
                // 失败时也执行回调，避免卡死
                if (callback != null) {
                    runOnUiThread(callback);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                Log.d("TradeReviewActivity", "发送评价消息响应: " + respBody);
                
                // 解析响应，检查是否成功
                try {
                    com.google.gson.JsonObject respObj = gson.fromJson(respBody, com.google.gson.JsonObject.class);
                    int code = respObj.has("code") ? respObj.get("code").getAsInt() : -1;
                    if (code == 200) {
                        Log.d("TradeReviewActivity", "评价消息发送成功");
                    } else {
                        String msg = respObj.has("msg") ? respObj.get("msg").getAsString() : "发送失败";
                        Log.w("TradeReviewActivity", "评价消息发送返回错误: " + msg);
                    }
                } catch (Exception e) {
                    Log.e("TradeReviewActivity", "解析评价消息响应失败: " + e.getMessage());
                }
                
                // 无论成功与否都执行回调
                if (callback != null) {
                    runOnUiThread(callback);
                }
            }
        });
    }
    
    /**
     * 根据评价状态获取消息内容
     */
    private String getReviewContent(int tradeStatus) {
        switch (tradeStatus) {
            case 6: return "买家已评价";
            case 7: return "卖家已评价";
            case 8: return "双方已评价";
            default: return "评价状态更新";
        }
    }
    
    /**
     * 发送广播通知聊天页面刷新消息列表
     */
    private void sendBroadcastToRefreshChat(long targetUserId) {
        Intent broadcastIntent = new Intent("com.example.e_tradeandroid.CHAT_REFRESH");
        broadcastIntent.putExtra("targetUserId", targetUserId);
        sendBroadcast(broadcastIntent);
        Log.d("TradeReviewActivity", "发送聊天刷新广播: targetUserId=" + targetUserId);
    }

    /**
     * 安全关闭页面 - 评价成功后通知 TradeInfoActivity 刷新
     */
    private void safeFinish() {
        runOnUiThread(() -> {
            if (!isFinishing() && !isDestroyed()) {
                // 传递评价后的交易状态，让 TradeInfoActivity 刷新
                int newStatus = calculateReviewStatus();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("tradeStatus", newStatus);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
