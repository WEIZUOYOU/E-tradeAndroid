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
import com.example.e_tradeandroid.model.Order;
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
    private int orderId;
    private int status;
    private boolean isBuyer;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        orderId = getIntent().getIntExtra("orderId", 0);
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
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "order/detail/" + orderId)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderDetailActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Order> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Order>>(){}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    Order order = baseResp.getData();
                    status = order.getStatus() != null ? order.getStatus() : 0;
                    long currentUserId = ApiClient.getCurrentUserId();
                    isBuyer = order.getBuyerId() != null && order.getBuyerId() == currentUserId;

                    runOnUiThread(() -> {
                        tvName.setText(order.getProductName() != null ? order.getProductName() : "");
                        tvPrice.setText("¥" + (order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0"));
                        tvInfo.setText("地点：" + (order.getMeetingLocation() != null ? order.getMeetingLocation() : "")
                                + "\n时间：" + (order.getMeetingTime() != null ? order.getMeetingTime() : ""));
                        refreshStatusUI();
                    });
                }
            }
        });
    }

    private void refreshStatusUI() {
        String[] statusTexts = {"待确认", "交易中", "已交付", "已完成", "已取消"};
        tvStatus.setText(status < statusTexts.length ? statusTexts[status] : "未知");

        btnConfirm.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        if (isBuyer) {
            switch (status) {
                case 0:
                case 1:
                    btnCancel.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    btnComplete.setVisibility(View.VISIBLE);
                    btnComplete.setText("确认收货");
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
                    btnComplete.setText("已交付");
                    break;
            }
        }
    }

    private void updateOrder(String action) {
        String endpoint;
        switch (action) {
            case "confirm":
                endpoint = "order/" + orderId + "/confirm";
                break;
            case "complete":
                if (isBuyer && status == 2) {
                    endpoint = "order/" + orderId + "/receive";
                } else {
                    endpoint = "order/" + orderId + "/deliver";
                }
                break;
            default:
                endpoint = "order/" + orderId + "/cancel";
                break;
        }

        RequestBody body = RequestBody.create("", MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + endpoint)
                .post(body)
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
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
