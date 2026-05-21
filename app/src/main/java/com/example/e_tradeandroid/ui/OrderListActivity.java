package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.OrderAdapter;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Order;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OrderListActivity extends AppCompatActivity {
    // 👇 完全匹配你的 XML ID
    private RecyclerView recycler_view_orders;
    private SwipeRefreshLayout swipe_refresh_orders;
    private ProgressBar progress_bar_orders;
    private BottomNavigationView bottom_navigation;

    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        // 👇 100% 匹配 XML 里的 id
        recycler_view_orders = findViewById(R.id.recycler_view_orders);
        swipe_refresh_orders = findViewById(R.id.swipe_refresh_orders);
        progress_bar_orders = findViewById(R.id.progress_bar_orders);
        bottom_navigation = findViewById(R.id.bottom_navigation);

        recycler_view_orders.setLayoutManager(new LinearLayoutManager(this));
        initNav();
        loadOrderData();
    }

    // 底部导航
    private void initNav() {
        bottom_navigation.setSelectedItemId(R.id.nav_orders);
        bottom_navigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_publish) {
                startActivity(new Intent(this, PublishActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, MyProfileActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                return true;
            }
            return false;
        });
    }

    // 加载订单
    private void loadOrderData() {
        swipe_refresh_orders.setRefreshing(true);
        progress_bar_orders.setVisibility(View.VISIBLE);

        ApiClient.get("order/list", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(OrderListActivity.this, "订单加载失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                BaseResponse<List<Order>> resp = gson.fromJson(body, new TypeToken<BaseResponse<List<Order>>>() {}.getType());
                runOnUiThread(() -> {
                    swipe_refresh_orders.setRefreshing(false);
                    progress_bar_orders.setVisibility(View.GONE);
                    if (resp.isSuccess() && resp.getData() != null) {
                        OrderAdapter adapter = new OrderAdapter(OrderListActivity.this, resp.getData(), order -> {
                            // 点击跳转订单详情，传递 orderId
                            Intent intent = new Intent(OrderListActivity.this, OrderDetailActivity.class);
                            intent.putExtra("orderId", order.getId());
                            startActivity(intent);
                        });
                        recycler_view_orders.setAdapter(adapter);
                    }
                });
            }
        });
    }
}