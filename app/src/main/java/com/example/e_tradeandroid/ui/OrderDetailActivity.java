package com.example.e_tradeandroid.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView tvStatus, tvName, tvPrice, tvInfo;
    private Button btnConfirm, btnComplete, btnCancel;
    private long orderId;  // 改为 long 类型，与 TradeInfo 一致
    private int status;
    private boolean isBuyer;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        orderId = getIntent().getLongExtra("orderId", 0L);  // 改为 getLongExtra
        initView();
        loadDetail();
    }

    private void initView() {
        tvStatus = findViewById(R.id.tv_order_status);
        tvName = findViewById(R.id.tv_product_name);
        tvPrice = findViewById(R.id.tv_price);
        tvInfo = findViewById(R.id.tv_trade_info);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnComplete = findViewById(R.id.btn_complete);
        btnCancel = findViewById(R.id.btn_cancel);

        btnConfirm.setOnClickListener(v -> updateOrder("confirm"));
        btnComplete.setOnClickListener(v -> updateOrder("complete"));
        btnCancel.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("取消订单").setMessage("确定取消？")
                .setPositiveButton("确定", (d, w) -> updateOrder("cancel"))
                .setNegativeButton("取消", null).show());
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void loadDetail() {
        // 使用正确的交易详情API路径
        ApiClient.get("api/trade/detail/" + orderId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderDetailActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<TradeInfo> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<TradeInfo>>(){}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    TradeInfo trade = baseResp.getData();
                    status = trade.getTradeStatus() != null ? trade.getTradeStatus() : 0;
                    long currentUserId = ApiClient.getCurrentUserId();
                    isBuyer = trade.getBuyerId() != null && trade.getBuyerId() == currentUserId;

                    runOnUiThread(() -> {
                        tvName.setText(trade.getProductName() != null ? trade.getProductName() : "");
                        tvPrice.setText("¥" + (trade.getProductPrice() != null ? trade.getProductPrice().toString() : "0"));
                        tvInfo.setText("地点：" + (trade.getMeetingLocation() != null ? trade.getMeetingLocation() : "")
                                + "\n时间：" + (trade.getMeetingTime() != null ? trade.getMeetingTime() : ""));
                        refreshStatusUI();
                    });
                }
            }
        });
    }

    private void refreshStatusUI() {
        // 根据API文档：0-待卖家确认,1-待交易,4-已完成,5-已取消
        String[] statusTexts = {"待确认", "交易中", "未知", "未知", "已完成", "已取消"};
        tvStatus.setText(status >= 0 && status < statusTexts.length ? statusTexts[status] : "未知");

        btnConfirm.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        if (isBuyer) {
            switch (status) {
                case 0:
                    // 待确认状态下买家可以取消订单
                    btnCancel.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    // 待交易状态下买家可以取消订单或完成交易
                    btnCancel.setVisibility(View.VISIBLE);
                    btnComplete.setVisibility(View.VISIBLE);
                    btnComplete.setText("确认完成");
                    break;
            }
        } else {
            switch (status) {
                case 0:
                    btnConfirm.setVisibility(View.VISIBLE);
                    btnConfirm.setText("确认接单");
                    break;
                case 1:
                    btnComplete.setVisibility(View.VISIBLE);
                    btnComplete.setText("确认完成");
                    break;
            }
        }
    }

    private void updateOrder(String action) {
        String jsonBody = "{\"tradeId\":" + orderId + "}";
        
        String endpoint;
        switch (action) {
            case "confirm":
                // 卖家确认交易
                endpoint = "api/trade/confirm";
                jsonBody = "{\"tradeId\":" + orderId + ",\"sellerPhone\":\"\"}";  // 需要卖家电话
                break;
            case "complete":
                // 完成交易
                endpoint = "api/trade/complete";
                break;
            default:
                // 取消交易
                endpoint = "api/trade/cancel";
                break;
        }

        ApiClient.post(endpoint, jsonBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderDetailActivity.this, "操作失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Void> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Void>>(){}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        Toast.makeText(OrderDetailActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                        loadDetail();
                    } else {
                        Toast.makeText(OrderDetailActivity.this, "操作失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
