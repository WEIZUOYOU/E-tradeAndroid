package com.example.e_tradeandroid.ui;

import android.os.Bundle;
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
import okhttp3.Request;
import okhttp3.Response;

public class OrderActivity extends AppCompatActivity {
    private TextView tvOrderNo, tvStatus, tvProductName, tvQuantity, tvTotalAmount, tvCreateTime;
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
                    Order order = baseResp.getData();
                    runOnUiThread(() -> {
                        tvOrderNo.setText("订单号：" + order.getOrderNo());
                        tvStatus.setText("状态：" + getStatusText(order.getStatus()));
                        tvProductName.setText("商品：" + order.getProductName());
                        tvQuantity.setText("数量：" + order.getQuantity());
                        tvTotalAmount.setText("总价：¥" + order.getTotalAmount().toString());
                        tvCreateTime.setText("创建时间：" + order.getCreateTime().toString());
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
}