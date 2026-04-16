package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Order;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderActivity extends AppCompatActivity {
    private TextView tvOrderNo, tvStatus, tvProductName, tvQuantity, tvTotalAmount, tvCreateTime;
    private Button btnCancelOrder, btnConfirmReceive;
    private LinearLayout layoutActions;
    private BottomNavigationView bottomNavigation;
    private Order order;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        tvOrderNo = findViewById(R.id.tv_order_no);
        tvStatus = findViewById(R.id.tv_status);
        tvProductName = findViewById(R.id.tv_product_name);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvCreateTime = findViewById(R.id.tv_create_time);
        btnCancelOrder = findViewById(R.id.btn_cancel_order);
        btnConfirmReceive = findViewById(R.id.btn_confirm_receive);
        layoutActions = findViewById(R.id.layout_actions);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        long orderId = getIntent().getLongExtra("order_id", -1);
        if (orderId == -1) {
            Toast.makeText(this, "订单不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderDetail(orderId);
    }

    private void loadOrderDetail(long orderId) {
        // 注意：后端未提供获取单个订单的接口，这里假设有 /order/detail/{id}
        // 如果没有，需根据实际情况调整。示例代码假设有此接口。
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "order/detail/" + orderId)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderActivity.this, "加载订单失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Order> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Order>>(){}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    order = baseResp.getData();
                    runOnUiThread(() -> {
                        tvOrderNo.setText("订单号：" + order.getOrderNo());
                        tvStatus.setText("状态：" + getStatusText(order.getStatus()));
                        tvProductName.setText("商品：" + order.getProductName());
                        tvQuantity.setText("数量：" + order.getQuantity());
                        tvTotalAmount.setText("总价：¥" + order.getTotalAmount().toString());
                        tvCreateTime.setText("创建时间：" + order.getCreateTime().toString());
                        
                        // 根据订单状态显示操作按钮
                        updateActionButtons(order.getStatus());
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(OrderActivity.this, "获取订单详情失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String getStatusText(int status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付待发货";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }
    
    private void updateActionButtons(int status) {
        // 重置按钮可见性
        btnCancelOrder.setVisibility(View.GONE);
        btnConfirmReceive.setVisibility(View.GONE);
        
        switch (status) {
            case 0: // 待支付
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setOnClickListener(v -> showCancelOrderDialog());
                break;
            case 1: // 已支付待发货
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setOnClickListener(v -> showCancelOrderDialog());
                break;
            case 2: // 已发货
                btnConfirmReceive.setVisibility(View.VISIBLE);
                btnConfirmReceive.setOnClickListener(v -> confirmReceive());
                break;
            case 3: // 已完成
                // 无操作
                break;
            case 4: // 已取消
                // 无操作
                break;
        }
    }
    
    private void showCancelOrderDialog() {
        new AlertDialog.Builder(this)
            .setTitle("取消订单")
            .setMessage("确定要取消这个订单吗？")
            .setPositiveButton("确定", (dialog, which) -> cancelOrder())
            .setNegativeButton("取消", null)
            .show();
    }
    
    private void cancelOrder() {
        if (order == null) return;
        
        RequestBody body = RequestBody.create("", MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "order/cancel/" + order.getId())
                .post(body)
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderActivity.this, "取消失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Void> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Void>>(){}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        Toast.makeText(OrderActivity.this, "订单已取消", Toast.LENGTH_SHORT).show();
                        loadOrderDetail(order.getId()); // 重新加载订单详情
                    } else {
                        Toast.makeText(OrderActivity.this, "取消失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    private void confirmReceive() {
        if (order == null) return;
        
        RequestBody body = RequestBody.create("", MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "order/confirm/" + order.getId())
                .post(body)
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderActivity.this, "操作失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Void> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Void>>(){}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        Toast.makeText(OrderActivity.this, "确认收货成功", Toast.LENGTH_SHORT).show();
                        loadOrderDetail(order.getId()); // 重新加载订单详情
                    } else {
                        Toast.makeText(OrderActivity.this, "操作失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_orders);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(OrderActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_category) {
                startActivity(new Intent(OrderActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_publish) {
                startActivity(new Intent(OrderActivity.this, PublishActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(OrderActivity.this, MyProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}