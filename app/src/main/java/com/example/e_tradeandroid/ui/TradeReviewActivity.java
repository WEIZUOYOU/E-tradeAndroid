package com.example.e_tradeandroid.ui;

import android.os.Bundle;
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
                        // 发送评价消息通知对方
                        sendReviewMessage();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        btnSubmit.setEnabled(true); // 失败后重新启用按钮
                        Toast.makeText(TradeReviewActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    /**
     * 发送评价消息通知对方 - 前端简化，只需传递 tradeId，后端自动生成快照
     * 评价状态：6=买家已评价，7=卖家已评价，8=双方已评价
     */
    private void sendReviewMessage() {
        if (tradeInfo == null || toUserId <= 0) {
            Log.e("TradeReviewActivity", "无法发送评价消息: tradeInfo=" + (tradeInfo != null) + ", toUserId=" + toUserId);
            return;
        }
        
        // 计算新的交易状态
        int newStatus;
        long currentUserId = ApiClient.getCurrentUserId();
        boolean isCurrentUserBuyer = (tradeInfo.getBuyerId() != null && tradeInfo.getBuyerId() == currentUserId);
        
        if (isCurrentUserBuyer) {
            newStatus = 6; // 买家已评价
        } else {
            newStatus = 7; // 卖家已评价
        }
        
        // 【简化】前端只需传递基本信息，后端自动生成完整的交易快照
        com.google.gson.JsonObject msgRequest = new com.google.gson.JsonObject();
        msgRequest.addProperty("receiverId", toUserId);
        msgRequest.addProperty("productId", tradeInfo.getProductId() != null ? tradeInfo.getProductId() : 0);
        msgRequest.addProperty("content", "提交了评价");
        msgRequest.addProperty("type", 1); // 交易卡片类型
        msgRequest.addProperty("tradeId", tradeId);
        msgRequest.addProperty("tradeStatus", newStatus);
        // msgRequest.addProperty("tradeData", null); // 不设置 tradeData，让后端自动生成
        
        String msgJson = gson.toJson(msgRequest);
        Log.d("TradeReviewActivity", "发送评价消息: " + msgJson);
        
        ApiClient.post("api/message/send", msgJson, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TradeReviewActivity", "发送评价消息失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                Log.d("TradeReviewActivity", "发送评价消息响应: " + resp);
            }
        });
    }
}
