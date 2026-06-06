package com.example.e_tradeandroid.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.model.SendMessageRequest;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.model.User;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TradeInfoActivity extends AppCompatActivity {
    // 交易创建响应
    private static class TradeCreateResponse {
        Long tradeId;
        String tradeNo;
    }
    
    private ImageView ivBack, ivProduct, ivBuyerAvatar, ivSellerAvatar;
    private TextView tvProductName, tvProductPrice, tvBuyerName, tvBuyerCredit, tvBuyerAuth;
    private TextView tvSellerName, tvSellerCredit, tvSellerAuth;
    private EditText etBuyerPhone, etSellerPhone, etLocation;
    private EditText etYear, etMonth, etDay, etHour, etMinute;
    private Button btnConfirm;
    private Button btnCancel;

    private long productId;
    private long sellerId;
    private long currentUserId;
    private Product product;
    private User seller;
    private User buyer;
    private TradeInfo existingTrade;
    private final Gson gson = new Gson();
    private boolean isSellerMode = false;
    private Long tradeId = null;
    private boolean isSubmitting = false; // 防重复提交标志
    private boolean modifyButtonAdded = false; // 修改按钮是否已添加

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_info);

        productId = getIntent().getLongExtra("productId", 0L);
        sellerId = getIntent().getLongExtra("sellerId", 0L);
        currentUserId = ApiClient.getCurrentUserId();
        tradeId = getIntent().getLongExtra("tradeId", 0L);
        isSellerMode = getIntent().getBooleanExtra("isSellerMode", false);

        initViews();
        initListeners();

        loadProductDetail();
        loadUserInfo();
        
        // 如果有交易ID，加载现有交易信息
        if (tradeId != 0) {
            loadTradeInfo();
        }
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivProduct = findViewById(R.id.iv_product);
        ivBuyerAvatar = findViewById(R.id.iv_buyer_avatar);
        ivSellerAvatar = findViewById(R.id.iv_seller_avatar);

        tvProductName = findViewById(R.id.tv_product_name);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvBuyerName = findViewById(R.id.tv_buyer_name);
        tvBuyerCredit = findViewById(R.id.tv_buyer_credit);
        tvBuyerAuth = findViewById(R.id.tv_buyer_auth);
        tvSellerName = findViewById(R.id.tv_seller_name);
        tvSellerCredit = findViewById(R.id.tv_seller_credit);
        tvSellerAuth = findViewById(R.id.tv_seller_auth);

        etBuyerPhone = findViewById(R.id.et_buyer_phone);
        etSellerPhone = findViewById(R.id.et_seller_phone);
        etLocation = findViewById(R.id.et_location);
        etYear = findViewById(R.id.et_year);
        etMonth = findViewById(R.id.et_month);
        etDay = findViewById(R.id.et_day);
        etHour = findViewById(R.id.et_hour);
        etMinute = findViewById(R.id.et_minute);

        btnConfirm = findViewById(R.id.btn_confirm);
        btnCancel = findViewById(R.id.btn_cancel);
        
        // 设置初始按钮状态
        initButtonState();
        
        // 加载商品详情并检查库存
        loadProductDetail();
    }
    
    private void initButtonState() {
        if (tradeId == 0) {
            // 新建交易
            if (isSellerMode) {
                // 卖家不能发起交易，只能确认已有交易
                btnConfirm.setText("等待买家发起交易");
                btnConfirm.setEnabled(false);
                // 卖家模式下显示卖家电话输入框，隐藏买家电话输入框
                etBuyerPhone.setEnabled(false);
                etBuyerPhone.setVisibility(View.GONE);
                etSellerPhone.setEnabled(true);
                etSellerPhone.setVisibility(View.VISIBLE);
                // 隐藏其他输入框（时间和地点由买家填写）
                etLocation.setEnabled(false);
                etYear.setEnabled(false);
                etMonth.setEnabled(false);
                etDay.setEnabled(false);
                etHour.setEnabled(false);
                etMinute.setEnabled(false);
            } else {
                // 买家可以发起交易，但先禁用，等库存检查完成
                btnConfirm.setText("提交交易");
                btnConfirm.setEnabled(false);
                // 买家模式下显示买家电话输入框，隐藏卖家电话输入框
                etBuyerPhone.setEnabled(true);
                etSellerPhone.setEnabled(false);
                etSellerPhone.setVisibility(View.GONE);
            }
            // 隐藏取消按钮
            btnCancel.setVisibility(View.GONE);
        } else {
            // 已有交易 - 先设置输入框的可见性和可用性
            // 具体状态由 updateButtonByStatus() 在加载交易详情后设置
            if (isSellerMode) {
                // 卖家查看：显示卖家电话输入框，隐藏买家电话输入框
                etSellerPhone.setVisibility(View.VISIBLE);
                etSellerPhone.setEnabled(true);
                etBuyerPhone.setVisibility(View.GONE);
            } else {
                // 买家查看：显示买家电话输入框，隐藏卖家电话输入框
                etBuyerPhone.setVisibility(View.VISIBLE);
                etBuyerPhone.setEnabled(false);
                etSellerPhone.setVisibility(View.GONE);
            }
        }
    }

    private void initListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> handleConfirm());
        
        btnCancel.setOnClickListener(v -> cancelTrade());
        
        // 卖家电话输入框监听，当输入变化时更新按钮状态
        etSellerPhone.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 如果是卖家模式且有待确认的交易，检查电话是否有效
                // 使用 tradeId != 0 而不是 existingTrade != null，因为 existingTrade 可能还未加载
                if (isSellerMode && tradeId != 0) {
                    String phone = s.toString().trim();
                    boolean isValid = phone.length() == 11;
                    btnConfirm.setEnabled(isValid);
                    btnConfirm.setText(isValid ? "确认交易" : "请先填写联系电话");
                }
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void initModifyButton() {
        // 检查是否已添加，避免重复
        if (modifyButtonAdded) {
            return;
        }
        
        // 待交易状态添加修改按钮
        Button btnModify = new Button(this);
        btnModify.setText("修改信息");
        btnModify.setOnClickListener(v -> showModifyDialog());
        btnModify.setBackgroundResource(R.drawable.bg_btn_primary);
        btnModify.setTextColor(getResources().getColor(R.color.white));
        
        LinearLayout layout = findViewById(R.id.ll_bottom_buttons);
        if (layout != null) {
            layout.addView(btnModify);
            modifyButtonAdded = true; // 设置标志位
        }
    }

    private void loadProductDetail() {
        ApiClient.get("api/product/detail/" + productId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TradeInfoActivity.this, "加载商品失败", Toast.LENGTH_SHORT).show());
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

                        if (product.getImages() != null && !product.getImages().isEmpty()) {
                            String imageUrl = product.getImages().get(0).startsWith("http")
                                    ? product.getImages().get(0)
                                    : ApiClient.BASE_URL + product.getImages().get(0);
                            Glide.with(TradeInfoActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivProduct);
                        }

                        if (product.getSellerName() != null) {
                            tvSellerName.setText(product.getSellerName());
                        }
                        if (product.getSellerAvatar() != null) {
                            String avatarUrl = product.getSellerAvatar().startsWith("http")
                                    ? product.getSellerAvatar()
                                    : ApiClient.BASE_URL + product.getSellerAvatar();
                            Glide.with(TradeInfoActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivSellerAvatar);
                        }
                        
                        // 检查库存并更新按钮状态（仅对买家有效）
                        checkStockAndUpdateButton();
                    });
                }
            }
        });
    }

    /**
     * 检查库存并更新按钮状态
     */
    private void checkStockAndUpdateButton() {
        if (tradeId != 0 || isSellerMode) {
            return; // 只有买家新建交易时才检查库存
        }
        
        if (product != null && product.getStock() != null) {
            int stock = product.getStock();
            if (stock > 0) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("提交交易");
            } else {
                btnConfirm.setEnabled(false);
                btnConfirm.setText("商品已售罄");
            }
        }
    }

    private void loadUserInfo() {
        if (tradeId != 0) {
            // 查看已有交易 - 从交易详情中获取买家和卖家信息
            // loadTradeInfo() 已经会加载交易详情，这里不需要重复加载
            // 买家和卖家信息会在 loadTradeInfo() 的回调中通过 existingTrade 获取
            return;
        }
        
        // 新建交易 - 买家发起交易
        // 当前用户是买家，sellerId 是商品卖家
        ApiClient.get("api/user/current", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TradeInfoActivity.this, "加载买家信息失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<User> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<User>>() {}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    buyer = baseResp.getData();
                    runOnUiThread(() -> updateBuyerInfo());
                }
            }
        });

        // 加载卖家信息
        ApiClient.get("api/user/info/" + sellerId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TradeInfoActivity.this, "加载卖家信息失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<User> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<User>>() {}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    seller = baseResp.getData();
                    runOnUiThread(() -> updateSellerInfo());
                }
            }
        });
    }

    private void loadTradeInfo() {
        ApiClient.get("api/trade/detail/" + tradeId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TradeInfoActivity.this, "加载交易信息失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<TradeInfo> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<TradeInfo>>() {}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    existingTrade = baseResp.getData();
                    runOnUiThread(() -> {
                        // 填充交易信息
                        etLocation.setText(existingTrade.getMeetingLocation());
                        
                        // 解析时间字符串并填充到拆分的字段中
                        String meetingTime = existingTrade.getMeetingTime();
                        if (meetingTime != null && !meetingTime.isEmpty()) {
                            // 格式：2024-01-01T12:00:00
                            String[] dateTime = meetingTime.split("T");
                            if (dateTime.length > 0) {
                                String[] dateParts = dateTime[0].split("-");
                                if (dateParts.length == 3) {
                                    etYear.setText(dateParts[0]);
                                    etMonth.setText(dateParts[1]);
                                    etDay.setText(dateParts[2]);
                                }
                                if (dateTime.length > 1) {
                                    String[] timeParts = dateTime[1].split(":");
                                    if (timeParts.length >= 2) {
                                        etHour.setText(timeParts[0]);
                                        etMinute.setText(timeParts[1]);
                                    }
                                }
                            }
                        }
                        
                        if (existingTrade.getBuyerPhone() != null) {
                            etBuyerPhone.setText(existingTrade.getBuyerPhone());
                        }
                        if (existingTrade.getSellerPhone() != null) {
                            etSellerPhone.setText(existingTrade.getSellerPhone());
                        }

                        // 从交易详情中获取买家和卖家信息并显示
                        updateBuyerInfoFromTrade();
                        updateSellerInfoFromTrade();

                        // 更新按钮状态
                        updateButtonByStatus(existingTrade.getTradeStatus());
                        
                        // 显示评价按钮（如果交易已完成）
                        showReviewButton();
                    });
                }
            }
        });
    }
    
    /**
     * 从交易详情中更新买家信息显示
     */
    private void updateBuyerInfoFromTrade() {
        if (existingTrade == null) return;
        
        tvBuyerName.setText(existingTrade.getBuyerName() != null ? existingTrade.getBuyerName() : "买家");
        tvBuyerCredit.setText("信誉分: " + (existingTrade.getBuyerCreditScore() != null ? existingTrade.getBuyerCreditScore() : 100));
        tvBuyerAuth.setText(existingTrade.getBuyerIsAuth() != null && existingTrade.getBuyerIsAuth() == 1 ? "已实名" : "未实名");
        tvBuyerAuth.setTextColor(existingTrade.getBuyerIsAuth() != null && existingTrade.getBuyerIsAuth() == 1
                ? getResources().getColor(R.color.success_green)
                : getResources().getColor(R.color.warning_orange));

        if (existingTrade.getBuyerAvatar() != null && !existingTrade.getBuyerAvatar().isEmpty()) {
            String avatarUrl = existingTrade.getBuyerAvatar().startsWith("http")
                    ? existingTrade.getBuyerAvatar()
                    : ApiClient.BASE_URL + existingTrade.getBuyerAvatar();
            Glide.with(TradeInfoActivity.this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivBuyerAvatar);
        }
    }
    
    /**
     * 从交易详情中更新卖家信息显示
     */
    private void updateSellerInfoFromTrade() {
        if (existingTrade == null) return;
        
        tvSellerName.setText(existingTrade.getSellerName() != null ? existingTrade.getSellerName() : "卖家");
        tvSellerCredit.setText("信誉分: " + (existingTrade.getSellerCreditScore() != null ? existingTrade.getSellerCreditScore() : 100));
        tvSellerAuth.setText(existingTrade.getSellerIsAuth() != null && existingTrade.getSellerIsAuth() == 1 ? "已实名" : "未实名");
        tvSellerAuth.setTextColor(existingTrade.getSellerIsAuth() != null && existingTrade.getSellerIsAuth() == 1
                ? getResources().getColor(R.color.success_green)
                : getResources().getColor(R.color.warning_orange));

        if (existingTrade.getSellerAvatar() != null && !existingTrade.getSellerAvatar().isEmpty()) {
            String avatarUrl = existingTrade.getSellerAvatar().startsWith("http")
                    ? existingTrade.getSellerAvatar()
                    : ApiClient.BASE_URL + existingTrade.getSellerAvatar();
            Glide.with(TradeInfoActivity.this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivSellerAvatar);
        }
    }

    private void updateBuyerInfo() {
        tvBuyerName.setText(buyer.getUsername());
        tvBuyerCredit.setText("信誉分: " + (buyer.getCreditScore() != null ? buyer.getCreditScore() : 100));
        tvBuyerAuth.setText(buyer.getIsAuth() != null && buyer.getIsAuth() == 1 ? "已实名" : "未实名");
        tvBuyerAuth.setTextColor(buyer.getIsAuth() != null && buyer.getIsAuth() == 1
                ? getResources().getColor(R.color.success_green)
                : getResources().getColor(R.color.warning_orange));

        if (buyer.getAvatar() != null && !buyer.getAvatar().isEmpty()) {
            String avatarUrl = buyer.getAvatar().startsWith("http")
                    ? buyer.getAvatar()
                    : ApiClient.BASE_URL + buyer.getAvatar();
            Glide.with(TradeInfoActivity.this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivBuyerAvatar);
        }

        if (buyer.getPhone() != null && etBuyerPhone.getText().isEmpty()) {
            etBuyerPhone.setText(buyer.getPhone());
        }
    }

    private void updateSellerInfo() {
        tvSellerName.setText(seller.getUsername());
        tvSellerCredit.setText("信誉分: " + (seller.getCreditScore() != null ? seller.getCreditScore() : 100));
        tvSellerAuth.setText(seller.getIsAuth() != null && seller.getIsAuth() == 1 ? "已实名" : "未实名");
        tvSellerAuth.setTextColor(seller.getIsAuth() != null && seller.getIsAuth() == 1
                ? getResources().getColor(R.color.success_green)
                : getResources().getColor(R.color.warning_orange));

        if (seller.getAvatar() != null && !seller.getAvatar().isEmpty()) {
            String avatarUrl = seller.getAvatar().startsWith("http")
                    ? seller.getAvatar()
                    : ApiClient.BASE_URL + seller.getAvatar();
            Glide.with(TradeInfoActivity.this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(ivSellerAvatar);
        }

        if (isSellerMode && seller.getPhone() != null && etSellerPhone.getText().isEmpty()) {
            etSellerPhone.setText(seller.getPhone());
        }
    }

    private void handleConfirm() {
        if (tradeId == 0) {
            // 新建交易 - 买家提交交易信息
            submitTradeInfo();
        } else {
            // 根据状态执行不同操作
            handleTradeAction();
        }
    }

    private void handleTradeAction() {
        if (existingTrade == null) return;

        int status = existingTrade.getTradeStatus() != null ? existingTrade.getTradeStatus() : 0;
        
        switch (status) {
            case 0: // 待卖家确认 - 卖家点击确认
                if (isSellerMode) {
                    confirmTradeBySeller();
                }
                break;
            case 1: // 待交易 - 双方可修改信息，点击完成交易
                completeTrade();
                break;
            case 4: // 已完成 - 显示评价按钮
                showReviewButton();
                break;
        }
    }

    private void submitTradeInfo() {
        // 防重复提交检查
        if (isSubmitting) {
            Toast.makeText(this, "正在提交中，请稍候", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String buyerPhone = etBuyerPhone.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        
        // 获取拆分的时间字段
        String year = etYear.getText().toString().trim();
        String month = etMonth.getText().toString().trim();
        String day = etDay.getText().toString().trim();
        String hour = etHour.getText().toString().trim();
        String minute = etMinute.getText().toString().trim();

        if (buyerPhone.isEmpty()) {
            Toast.makeText(this, "请输入联系电话", Toast.LENGTH_SHORT).show();
            return;
        }
        if (location.isEmpty()) {
            Toast.makeText(this, "请输入交易地点", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证时间字段
        if (year.isEmpty() || month.isEmpty() || day.isEmpty() || hour.isEmpty() || minute.isEmpty()) {
            Toast.makeText(this, "请完整填写交易时间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证年份（4位数字）
        if (!year.matches("\\d{4}")) {
            Toast.makeText(this, "年份格式不正确，应为4位数字", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证月份（1-12）
        int monthInt = Integer.parseInt(month);
        if (monthInt < 1 || monthInt > 12) {
            Toast.makeText(this, "月份应在1-12之间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证日期（1-31）
        int dayInt = Integer.parseInt(day);
        if (dayInt < 1 || dayInt > 31) {
            Toast.makeText(this, "日期应在1-31之间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证小时（0-23）
        int hourInt = Integer.parseInt(hour);
        if (hourInt < 0 || hourInt > 23) {
            Toast.makeText(this, "小时应在0-23之间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证分钟（0-59）
        int minuteInt = Integer.parseInt(minute);
        if (minuteInt < 0 || minuteInt > 59) {
            Toast.makeText(this, "分钟应在0-59之间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 生成 ISO 标准格式时间（2024-01-01T12:00:00）
        String isoTime = String.format("%s-%02d-%02dT%02d:%02d:00", 
            year, monthInt, dayInt, hourInt, minuteInt);

        // 根据后端API格式构建请求
        String json = String.format(
            "{\"productId\":%d,\"meetingTime\":\"%s\",\"meetingLocation\":\"%s\"}",
            productId, isoTime, location
        );
        
        // 设置提交状态
        isSubmitting = true;
        
        ApiClient.post("api/trade/create", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isSubmitting = false; // 重置提交状态
                runOnUiThread(() -> Toast.makeText(TradeInfoActivity.this, "提交失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                isSubmitting = false; // 重置提交状态
                String respBody = response.body().string();
                runOnUiThread(() -> {
                    try {
                        // 解析交易创建响应
                        BaseResponse<TradeCreateResponse> baseResp = gson.fromJson(
                            respBody, 
                            new TypeToken<BaseResponse<TradeCreateResponse>>() {}.getType()
                        );
                        if (baseResp.isSuccess() && baseResp.getData() != null) {
                            TradeCreateResponse tradeResp = baseResp.getData();
                            
                            // 检查 sellerId 是否有效
                            if (sellerId == 0) {
                                Log.e("TradeInfoActivity", "sellerId 为 0，无法发送交易卡片消息");
                                Toast.makeText(TradeInfoActivity.this, "交易信息已提交，等待卖家确认", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                                return;
                            }
                            
                            // 发送交易卡片消息给卖家（买家创建交易，buyerId = 当前用户ID）
                            Log.d("TradeInfoActivity", "发送交易卡片: tradeId=" + tradeResp.tradeId + ", sellerId=" + sellerId + ", buyerId=" + currentUserId);
                            sendTradeCardMessage(tradeResp.tradeId, 0, tradeResp.tradeNo, sellerId, currentUserId);
                            
                            Toast.makeText(TradeInfoActivity.this, "交易信息已提交，等待卖家确认", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(TradeInfoActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(TradeInfoActivity.this, "解析响应失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    // 发送交易卡片消息
    private void sendTradeCardMessage(Long tradeId, int tradeStatus, String tradeNo, long receiverId, long buyerId) {
        Log.d("TradeInfoActivity", "sendTradeCardMessage 开始: tradeId=" + tradeId + ", tradeStatus=" + tradeStatus + ", tradeNo=" + tradeNo + ", receiverId=" + receiverId + ", buyerId=" + buyerId);
        
        if (receiverId == 0) {
            Log.e("TradeInfoActivity", "receiverId 为 0，无法发送消息");
            return;
        }
        
        // 构建交易数据对象
        TradeData tradeDataObj = new TradeData();
        tradeDataObj.setTradeNo(tradeNo);
        tradeDataObj.setProductName(product != null ? product.getName() : "");
        tradeDataObj.setProductPrice(product != null ? product.getPrice() : 0);
        tradeDataObj.setProductImage(product != null ? product.getCoverImage() : "");
        tradeDataObj.setMeetingLocation(etLocation.getText().toString().trim());
        // 使用用户输入的时间（已经是 ISO 格式）
        tradeDataObj.setMeetingTime(etYear.getText().toString() + "-" + 
            String.format("%02d", Integer.parseInt(etMonth.getText().toString())) + "-" + 
            String.format("%02d", Integer.parseInt(etDay.getText().toString())) + "T" + 
            String.format("%02d", Integer.parseInt(etHour.getText().toString())) + ":" + 
            String.format("%02d", Integer.parseInt(etMinute.getText().toString())) + ":00");
        tradeDataObj.setTradeStatus(tradeStatus);  // 设置交易状态
        tradeDataObj.setBuyerId(buyerId);
        tradeDataObj.setSellerId(sellerId);
        tradeDataObj.setBuyerPhone(etBuyerPhone.getText().toString().trim());  // 设置买家电话
        tradeDataObj.setSellerPhone(etSellerPhone.getText().toString().trim());  // 设置卖家电话
        
        // 将 tradeData 对象转换为 JSON 字符串（后端期望 String 类型）
        String tradeDataJson = gson.toJson(tradeDataObj);
        Log.d("TradeInfoActivity", "tradeData JSON 字符串: " + tradeDataJson);
        
        // 构建消息请求对象
        SendMessageRequest request = new SendMessageRequest();
        request.setReceiverId(receiverId);
        request.setProductId(productId);
        request.setContent("发起交易请求");
        request.setType(1); // 1 表示交易卡片
        request.setTradeId(tradeId);
        request.setTradeStatus(tradeStatus);
        request.setTradeData(tradeDataJson); // 设置为 JSON 字符串
        
        // 将整个请求对象转换为 JSON
        String json = gson.toJson(request);
        Log.d("TradeInfoActivity", "发送消息 JSON: " + json);
        
        ApiClient.post("api/message/send", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TradeInfoActivity", "发送交易卡片失败: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                Log.d("TradeInfoActivity", "发送交易卡片响应: " + respBody);
            }
        });
    }
    
    /**
     * 交易数据对象，用于构建 tradeData JSON
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

    private void updateOrConfirmTrade() {
        if (existingTrade == null) return;

        int status = existingTrade.getTradeStatus() != null ? existingTrade.getTradeStatus() : 0;
        
        if (status == 0 && isSellerMode) {
            // 卖家确认交易
            confirmTradeBySeller();
        } else if (status == 1) {
            // 待交易状态 - 完成交易
            completeTrade();
        } else if (status == 4) {
            // 已完成 - 显示评价
            showReviewButton();
        }
    }

    private void confirmTradeBySeller() {
        String sellerPhone = etSellerPhone.getText().toString().trim();
        if (sellerPhone.isEmpty() || sellerPhone.length() != 11) {
            Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
            return;
        }

        // 根据后端API格式构建请求
        String json = String.format(
            "{\"tradeId\":%d,\"sellerPhone\":\"%s\"}",
            tradeId, sellerPhone
        );
        
        ApiClient.post("api/trade/confirm", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TradeInfoActivity.this, "确认失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<String>>() {}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        // 发送交易确认消息给买家（sellerId = 当前用户是卖家）
                        if (existingTrade != null && existingTrade.getBuyerId() != null) {
                            sendTradeCardMessage(existingTrade.getId(), 1, existingTrade.getTradeNo(), existingTrade.getBuyerId(), existingTrade.getBuyerId());
                        }
                        Toast.makeText(TradeInfoActivity.this, "交易已确认", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(TradeInfoActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void completeTrade() {
        if (isSubmitting) return;
        isSubmitting = true;
        
        int currentStatus = existingTrade != null && existingTrade.getTradeStatus() != null 
                ? existingTrade.getTradeStatus() 
                : 0;
        
        // 判断当前用户角色
        boolean isSeller = isSellerMode || (existingTrade != null && existingTrade.getSellerId() != null 
                && existingTrade.getSellerId() == currentUserId);
        
        Log.d("TradeInfoActivity", "completeTrade: isSeller=" + isSeller + ", currentStatus=" + currentStatus);
        
        // 构建请求体，包含操作方信息
        String body = String.format("{\"tradeId\":%d,\"operatorType\":\"%s\"}", 
                tradeId, isSeller ? "SELLER" : "BUYER");
        
        ApiClient.post("api/trade/complete", body, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(TradeInfoActivity.this, "操作失败", Toast.LENGTH_SHORT).show();
                    isSubmitting = false;
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<String>>() {}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        // 获取后端返回的新状态
                        int newStatus = 0;
                        try {
                            com.google.gson.JsonObject obj = gson.fromJson(respBody, com.google.gson.JsonObject.class);
                            if (obj.has("data")) {
                                com.google.gson.JsonObject data = obj.getAsJsonObject("data");
                                if (data.has("tradeStatus")) {
                                    newStatus = data.get("tradeStatus").getAsInt();
                                }
                            }
                        } catch (Exception e) {
                            // 解析失败，使用默认逻辑
                        }
                        
                        // 根据新状态发送不同消息
                        String toastMsg;
                        long receiverId = existingTrade != null && existingTrade.getBuyerId() != null 
                                ? (isSeller ? existingTrade.getBuyerId() : existingTrade.getSellerId()) 
                                : 0;
                        
                        // 如果后端未返回状态，根据当前状态推断新状态
                        if (newStatus == 0) {
                            int currentStatus = existingTrade != null && existingTrade.getTradeStatus() != null 
                                    ? existingTrade.getTradeStatus() : 0;
                            if (currentStatus == 1) { // 待交易 -> 一方确认
                                newStatus = isSeller ? 2 : 3;
                            } else if (currentStatus == 2 || currentStatus == 3) { // 一方已确认 -> 完成
                                newStatus = 5;
                            }
                        }
                        
                        switch (newStatus) {
                            case 2: // 卖家已确认，等待买家
                                toastMsg = "已确认交易，等待买家确认";
                                sendTradeCardMessage(tradeId, 2, existingTrade.getTradeNo(), receiverId, 
                                        existingTrade.getBuyerId() != null ? existingTrade.getBuyerId() : currentUserId);
                                break;
                            case 3: // 买家已确认，等待卖家
                                toastMsg = "已确认交易，等待卖家确认";
                                sendTradeCardMessage(tradeId, 3, existingTrade.getTradeNo(), receiverId, 
                                        existingTrade.getBuyerId() != null ? existingTrade.getBuyerId() : currentUserId);
                                break;
                            case 5: // 双方都已确认，交易完成
                                toastMsg = "交易完成！进入互评阶段";
                                sendTradeCardMessage(tradeId, 5, existingTrade.getTradeNo(), receiverId, 
                                        existingTrade.getBuyerId() != null ? existingTrade.getBuyerId() : currentUserId);
                                break;
                            default:
                                toastMsg = "操作成功";
                                sendTradeCardMessage(tradeId, newStatus, existingTrade.getTradeNo(), receiverId, 
                                        existingTrade.getBuyerId() != null ? existingTrade.getBuyerId() : currentUserId);
                        }
                        
                        Toast.makeText(TradeInfoActivity.this, toastMsg, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(TradeInfoActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                        isSubmitting = false;
                    }
                });
            }
        });
    }

    private void cancelTrade() {
        // 取消交易 - 仅待卖家确认状态可取消，买卖双方均可取消
        if (tradeId == null || tradeId == 0L) return;
        if (existingTrade != null && existingTrade.getTradeStatus() != null 
            && existingTrade.getTradeStatus() != 0) {
            Toast.makeText(this, "当前状态不可取消", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new AlertDialog.Builder(this)
                .setTitle("取消交易")
                .setMessage("确定要取消这笔交易吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    ApiClient.post("api/trade/cancel", "{\"tradeId\":" + tradeId + "}", new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(() -> Toast.makeText(TradeInfoActivity.this, "取消失败", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String respBody = response.body().string();
                            BaseResponse<String> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<String>>() {}.getType());
                            runOnUiThread(() -> {
                                if (baseResp.isSuccess()) {
                                    Toast.makeText(TradeInfoActivity.this, "交易已取消", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);
                                    finish();
                                } else {
                                    Toast.makeText(TradeInfoActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showModifyDialog() {
        // 显示修改确认对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改交易信息");
        builder.setMessage("修改后需要对方确认才能生效，确定要修改吗？");
        
        builder.setPositiveButton("确定", (dialog, which) -> submitModifyRequest());
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void submitModifyRequest() {
        String location = etLocation.getText().toString().trim();
        String phone = isSellerMode ? etSellerPhone.getText().toString().trim() : etBuyerPhone.getText().toString().trim();
        
        // 获取拆分的时间字段
        String year = etYear.getText().toString().trim();
        String month = etMonth.getText().toString().trim();
        String day = etDay.getText().toString().trim();
        String hour = etHour.getText().toString().trim();
        String minute = etMinute.getText().toString().trim();

        if (location.isEmpty()) {
            Toast.makeText(this, "请填写交易地点", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证时间字段
        if (year.isEmpty() || month.isEmpty() || day.isEmpty() || hour.isEmpty() || minute.isEmpty()) {
            Toast.makeText(this, "请完整填写交易时间", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 验证时间格式
        int monthInt = Integer.parseInt(month);
        int dayInt = Integer.parseInt(day);
        int hourInt = Integer.parseInt(hour);
        int minuteInt = Integer.parseInt(minute);
        
        if (!year.matches("\\d{4}") || monthInt < 1 || monthInt > 12 || 
            dayInt < 1 || dayInt > 31 || hourInt < 0 || hourInt > 23 || 
            minuteInt < 0 || minuteInt > 59) {
            Toast.makeText(this, "时间格式不正确", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 生成 ISO 标准格式时间
        String isoTime = String.format("%s-%02d-%02dT%02d:%02d:00", 
            year, monthInt, dayInt, hourInt, minuteInt);

        // 根据后端API格式构建请求（只包含必要字段）
        String json = String.format(
            "{\"tradeId\":%d,\"meetingTime\":\"%s\",\"meetingLocation\":\"%s\"}",
            tradeId, isoTime, location
        );
        
        ApiClient.post("api/trade/update", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TradeInfoActivity.this, "修改失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<String>>() {}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        // 发送修改请求消息给对方
                        if (existingTrade != null) {
                            long receiverId = isSellerMode ? existingTrade.getBuyerId() : existingTrade.getSellerId();
                            long buyerId = existingTrade.getBuyerId() != null ? existingTrade.getBuyerId() : currentUserId;
                            sendTradeCardMessage(existingTrade.getId(), 1, existingTrade.getTradeNo(), receiverId, buyerId);
                        }
                        Toast.makeText(TradeInfoActivity.this, "已发送修改请求，等待对方确认", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(TradeInfoActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void confirmModify() {
        // 确认对方的修改请求
        ApiClient.post("api/trade/confirmUpdate", "{\"tradeId\":" + tradeId + "}", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(TradeInfoActivity.this, "确认失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<String> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<String>>() {}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        Toast.makeText(TradeInfoActivity.this, "已确认修改", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(TradeInfoActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private TradeInfo createTradeInfo(String buyerPhone, String location, String time) {
        TradeInfo tradeInfo = new TradeInfo();
        tradeInfo.setProductId(productId);
        tradeInfo.setProductName(product != null ? product.getName() : "");
        tradeInfo.setProductPrice(product != null ? product.getPrice() : 0.0);
        tradeInfo.setProductImage(product != null && product.getImages() != null && !product.getImages().isEmpty()
                ? product.getImages().get(0) : "");

        tradeInfo.setBuyerId(currentUserId);
        tradeInfo.setBuyerName(buyer != null ? buyer.getUsername() : "");
        tradeInfo.setBuyerAvatar(buyer != null ? buyer.getAvatar() : "");
        tradeInfo.setBuyerCreditScore(buyer != null ? buyer.getCreditScore() : 100);
        tradeInfo.setBuyerIsAuth(buyer != null ? buyer.getIsAuth() : 0);
        tradeInfo.setBuyerPhone(buyerPhone);

        tradeInfo.setSellerId(sellerId);
        tradeInfo.setSellerName(seller != null ? seller.getUsername() : "");
        tradeInfo.setSellerAvatar(seller != null ? seller.getAvatar() : "");
        tradeInfo.setSellerCreditScore(seller != null ? seller.getCreditScore() : 100);
        tradeInfo.setSellerIsAuth(seller != null ? seller.getIsAuth() : 0);

        tradeInfo.setMeetingLocation(location);
        tradeInfo.setMeetingTime(time);

        return tradeInfo;
    }

    private boolean validateInput(String buyerPhone, String location, String time) {
        if (buyerPhone.isEmpty()) {
            Toast.makeText(this, "请输入联系电话", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (buyerPhone.length() != 11) {
            Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (location.isEmpty()) {
            Toast.makeText(this, "请输入面交地点", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (time.isEmpty()) {
            Toast.makeText(this, "请输入交易时间", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void updateButtonByStatus(Integer status) {
        if (status == null) status = 0;
        
        // 默认隐藏取消按钮
        if (btnCancel != null) {
            btnCancel.setVisibility(View.GONE);
        }
        
        switch (status) {
            case 0: // 待卖家确认
                btnConfirm.setText(isSellerMode ? "确认交易" : "等待卖家确认");
                
                if (isSellerMode) {
                    // 卖家模式：显示买家电话（只读）和卖家电话输入框（可编辑）
                    etBuyerPhone.setVisibility(View.VISIBLE);
                    etBuyerPhone.setEnabled(false);  // 买家电话只读
                    etSellerPhone.setVisibility(View.VISIBLE);
                    etSellerPhone.setEnabled(true);  // 卖家电话可编辑
                    
                    String phone = etSellerPhone.getText().toString().trim();
                    btnConfirm.setEnabled(phone.length() == 11);
                    if (!btnConfirm.isEnabled()) {
                        btnConfirm.setText("请先填写联系电话");
                    }
                } else {
                    // 买家模式：显示买家电话输入框（只读），隐藏卖家电话输入框
                    etBuyerPhone.setVisibility(View.VISIBLE);
                    etBuyerPhone.setEnabled(false);
                    etSellerPhone.setVisibility(View.GONE);
                    btnConfirm.setEnabled(false);
                }
                
                // 待卖家确认状态，买卖双方都可取消
                if (btnCancel != null) {
                    btnCancel.setVisibility(View.VISIBLE);
                    btnCancel.setText("取消交易");
                }
                break;
                
            case 1: // 待交易
                btnConfirm.setText("完成交易");
                btnConfirm.setEnabled(true);
                
                // 双方都能看到彼此的电话（只读）
                etBuyerPhone.setVisibility(View.VISIBLE);
                etBuyerPhone.setEnabled(false);
                etSellerPhone.setVisibility(View.VISIBLE);
                etSellerPhone.setEnabled(false);
                
                // 待交易状态显示修改按钮
                initModifyButton();
                break;
                
            case 2: // 待确认收货
                if (isSellerMode) {
                    // 卖家视角：等待买家确认收货
                    btnConfirm.setText("等待买家确认");
                    btnConfirm.setEnabled(false);
                } else {
                    // 买家视角：确认收货
                    btnConfirm.setText("确认收货");
                    btnConfirm.setEnabled(true);
                }
                
                // 双方都能看到彼此的电话（只读）
                etBuyerPhone.setVisibility(View.VISIBLE);
                etBuyerPhone.setEnabled(false);
                etSellerPhone.setVisibility(View.VISIBLE);
                etSellerPhone.setEnabled(false);
                break;
                
            case 4: // 已完成
                btnConfirm.setText("评价对方");
                btnConfirm.setEnabled(true);
                
                // 双方都能看到彼此的电话（只读）
                etBuyerPhone.setVisibility(View.VISIBLE);
                etBuyerPhone.setEnabled(false);
                etSellerPhone.setVisibility(View.VISIBLE);
                etSellerPhone.setEnabled(false);
                
                btnConfirm.setOnClickListener(v -> {
                    // 跳转到评价界面
                    Intent intent = new Intent(TradeInfoActivity.this, TradeReviewActivity.class);
                    intent.putExtra("tradeId", tradeId);
                    intent.putExtra("toUserId", isSellerMode ? existingTrade.getBuyerId() : existingTrade.getSellerId());
                    intent.putExtra("isSellerMode", isSellerMode);
                    startActivityForResult(intent, 1003);
                });
                break;
        }
    }

    private void showReviewButton() {
        // 交易完成后显示评价按钮
        if (existingTrade != null && existingTrade.getTradeStatus() != null 
            && existingTrade.getTradeStatus() == 4) {
            
            btnConfirm.setText("评价对方");
            btnConfirm.setEnabled(true);
            btnConfirm.setOnClickListener(v -> {
                // 跳转到评价界面
                Intent intent = new Intent(TradeInfoActivity.this, TradeReviewActivity.class);
                intent.putExtra("tradeId", tradeId);
                intent.putExtra("toUserId", isSellerMode ? existingTrade.getBuyerId() : existingTrade.getSellerId());
                intent.putExtra("isSellerMode", isSellerMode);
                startActivityForResult(intent, 1003);
            });
        }
    }
}
