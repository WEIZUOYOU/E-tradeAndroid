package com.example.e_tradeandroid.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.OrderAdapter;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.model.TradeListResponse;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 我的订单页面
 */
public class MyOrdersActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tabActive, tabCompleted;
    private View tabIndicator;
    private FrameLayout container;

    private RecyclerView rvActive, rvCompleted;
    private OrderAdapter activeAdapter, completedAdapter;
    private List<TradeInfo> activeList = new ArrayList<>();
    private List<TradeInfo> completedList = new ArrayList<>();
    
    // 下拉刷新
    private SwipeRefreshLayout swipeRefreshActive, swipeRefreshCompleted;
    // 空状态
    private TextView tvEmptyActive, tvEmptyCompleted;

    private Gson gson = new Gson();
    private boolean isActiveTab = true; // 当前选中的是进行中的订单标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initViews();
        setupRecyclerViews();
        setupSwipeRefresh();
        setupTabSwitch();
        switchToActive(); // 初始化默认显示进行中的订单
        loadOrders();

        ivBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tabActive = findViewById(R.id.tab_active);
        tabCompleted = findViewById(R.id.tab_completed);
        tabIndicator = findViewById(R.id.tab_indicator);
        container = findViewById(R.id.container);
        
        // 空状态
        tvEmptyActive = findViewById(R.id.tv_empty_active);
        tvEmptyCompleted = findViewById(R.id.tv_empty_completed);
    }

    private void setupRecyclerViews() {
        // 进行中的订单列表
        rvActive = findViewById(R.id.rv_active);
        rvActive.setLayoutManager(new LinearLayoutManager(this));
        activeAdapter = new OrderAdapter(this, activeList, true);
        rvActive.setAdapter(activeAdapter);

        // 已完成的订单列表
        rvCompleted = findViewById(R.id.rv_completed);
        rvCompleted.setLayoutManager(new LinearLayoutManager(this));
        completedAdapter = new OrderAdapter(this, completedList, false);
        rvCompleted.setAdapter(completedAdapter);
    }

    /**
     * 设置下拉刷新
     */
    private void setupSwipeRefresh() {
        swipeRefreshActive = findViewById(R.id.swipe_refresh_active);
        swipeRefreshCompleted = findViewById(R.id.swipe_refresh_completed);
        
        swipeRefreshActive.setOnRefreshListener(this::loadActiveOrders);
        swipeRefreshCompleted.setOnRefreshListener(this::loadCompletedOrders);
        
        // 设置刷新颜色
        swipeRefreshActive.setColorSchemeResources(R.color.primary_green);
        swipeRefreshCompleted.setColorSchemeResources(R.color.primary_green);
    }

    private void setupTabSwitch() {
        tabActive.setOnClickListener(v -> switchToActive());
        tabCompleted.setOnClickListener(v -> switchToCompleted());
    }

    /**
     * 切换到"进行中"订单
     */
    private void switchToActive() {
        if (isActiveTab) return;

        isActiveTab = true;
        
        // 更新 Tab 文字颜色
        tabActive.setTextColor(getResources().getColor(R.color.primary_green));
        tabCompleted.setTextColor(getResources().getColor(R.color.text_secondary));
        
        // 切换内容
        swipeRefreshActive.setVisibility(View.VISIBLE);
        swipeRefreshCompleted.setVisibility(View.GONE);
    }

    /**
     * 切换到"已完成"订单
     */
    private void switchToCompleted() {
        if (!isActiveTab) return;

        isActiveTab = false;
        
        // 更新 Tab 文字颜色
        tabCompleted.setTextColor(getResources().getColor(R.color.primary_green));
        tabActive.setTextColor(getResources().getColor(R.color.text_secondary));
        
        // 切换内容
        swipeRefreshCompleted.setVisibility(View.VISIBLE);
        swipeRefreshActive.setVisibility(View.GONE);
    }

    /**
     * 加载订单数据
     */
    private void loadOrders() {
        // 加载进行中的订单（状态 0,1,2,3）
        loadActiveOrders();
        
        // 加载已完成的订单（状态 4,5,6,7,8）
        loadCompletedOrders();
    }

    /**
     * 加载进行中的订单（状态 0-3）
     */
    private void loadActiveOrders() {
        // 开始刷新
        swipeRefreshActive.setRefreshing(true);
        
        long userId = ApiClient.getCurrentUserId();
        String url = ApiClient.BASE_URL + "api/trade/my/list?userId=" + userId + "&status=0,1,2,3";
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefreshActive.setRefreshing(false);
                    Toast.makeText(MyOrdersActivity.this, "加载订单失败", Toast.LENGTH_SHORT).show();
                    // 显示空状态
                    rvActive.setVisibility(View.GONE);
                    tvEmptyActive.setVisibility(View.VISIBLE);
                    tvEmptyActive.setText("加载失败，请下拉刷新重试");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                BaseResponse<TradeListResponse> baseResp = gson.fromJson(body, 
                            new TypeToken<BaseResponse<TradeListResponse>>() {}.getType());
                    
                    runOnUiThread(() -> {
                        swipeRefreshActive.setRefreshing(false);
                        
                        if (baseResp.isSuccess() && baseResp.getData() != null && baseResp.getData().getTrades() != null) {
                            activeList.clear();
                            activeList.addAll(baseResp.getData().getTrades());
                            activeAdapter.notifyDataSetChanged();
                            
                            // 处理空状态
                            if (activeList.isEmpty()) {
                                rvActive.setVisibility(View.GONE);
                                tvEmptyActive.setVisibility(View.VISIBLE);
                                tvEmptyActive.setText("暂无进行中的订单");
                            } else {
                                rvActive.setVisibility(View.VISIBLE);
                                tvEmptyActive.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(MyOrdersActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                            rvActive.setVisibility(View.GONE);
                            tvEmptyActive.setVisibility(View.VISIBLE);
                            tvEmptyActive.setText("暂无进行中的订单");
                        }
                    });
            }
        });
    }

    /**
     * 加载已完成的订单（状态 4,5,6,7,8）
     */
    private void loadCompletedOrders() {
        // 开始刷新
        swipeRefreshCompleted.setRefreshing(true);
        
        long userId = ApiClient.getCurrentUserId();
        String url = ApiClient.BASE_URL + "api/trade/my/list?userId=" + userId + "&status=4,5,6,7,8";
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefreshCompleted.setRefreshing(false);
                    Toast.makeText(MyOrdersActivity.this, "加载订单失败", Toast.LENGTH_SHORT).show();
                    // 显示空状态
                    rvCompleted.setVisibility(View.GONE);
                    tvEmptyCompleted.setVisibility(View.VISIBLE);
                    tvEmptyCompleted.setText("加载失败，请下拉刷新重试");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                BaseResponse<TradeListResponse> baseResp = gson.fromJson(body, 
                            new TypeToken<BaseResponse<TradeListResponse>>() {}.getType());
                    
                    runOnUiThread(() -> {
                        swipeRefreshCompleted.setRefreshing(false);
                        
                        if (baseResp.isSuccess() && baseResp.getData() != null && baseResp.getData().getTrades() != null) {
                            completedList.clear();
                            completedList.addAll(baseResp.getData().getTrades());
                            completedAdapter.notifyDataSetChanged();
                            
                            // 处理空状态
                            if (completedList.isEmpty()) {
                            rvCompleted.setVisibility(View.GONE);
                            tvEmptyCompleted.setVisibility(View.VISIBLE);
                            tvEmptyCompleted.setText("暂无已完成的订单");
                        } else {
                            rvCompleted.setVisibility(View.VISIBLE);
                            tvEmptyCompleted.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(MyOrdersActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                        rvCompleted.setVisibility(View.GONE);
                        tvEmptyCompleted.setVisibility(View.VISIBLE);
                        tvEmptyCompleted.setText("暂无已完成的订单");
                    }
                });
            }
        });
    }
}