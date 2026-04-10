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
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OrderListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        recyclerView = findViewById(R.id.recycler_view_orders);
        swipeRefresh = findViewById(R.id.swipe_refresh_orders);
        progressBar = findViewById(R.id.progress_bar_orders);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList, order -> {
            Intent intent = new Intent(OrderListActivity.this, OrderActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadOrders);

        loadOrders();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_orders);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(OrderListActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_category) {
                startActivity(new Intent(OrderListActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_publish) {
                startActivity(new Intent(OrderListActivity.this, PublishActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(OrderListActivity.this, MyProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadOrders() {
        swipeRefresh.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "order/my-orders?page=1&size=20")
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(OrderListActivity.this, "加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<List<Order>> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<List<Order>>>(){}.getType());
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    if (baseResp.isSuccess() && baseResp.getData() != null) {
                        orderList.clear();
                        orderList.addAll(baseResp.getData());
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(OrderListActivity.this, "获取订单列表失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
