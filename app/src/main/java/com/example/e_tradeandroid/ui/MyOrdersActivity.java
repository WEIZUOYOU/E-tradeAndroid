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

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.OrderAdapter;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.TradeInfo;
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

    private Gson gson = new Gson();
    private boolean isActiveTab = true; // 当前选中的是进行中的订单标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initViews();
        setupRecyclerViews();
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
    }

    private void setupRecyclerViews() {
        // 进行中的订单列表
        rvActive = new RecyclerView(this);
        rvActive.setLayoutManager(new LinearLayoutManager(this));
        activeAdapter = new OrderAdapter(this, activeList, true);
        rvActive.setAdapter(activeAdapter);

        // 已完成的订单列表
        rvCompleted = new RecyclerView(this);
        rvCompleted.setLayoutManager(new LinearLayoutManager(this));
        completedAdapter = new OrderAdapter(this, completedList, false);
        rvCompleted.setAdapter(completedAdapter);
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
        container.removeAllViews();
        container.addView(rvActive);
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
        container.removeAllViews();
        container.addView(rvCompleted);
    }

    /**
     * 加载订单数据
     */
    private void loadOrders() {
        // 加载进行中的订单（状态 0,1,2,3）
        loadActiveOrders();
        
        // 加载已完成的订单（状态 4,5）
        loadCompletedOrders();
    }

    /**
     * 加载进行中的订单
     */
    private void loadActiveOrders() {
        long userId = ApiClient.getCurrentUserId();
        String url = ApiClient.BASE_URL + "api/trade/active?userId=" + userId;
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MyOrdersActivity.this, "加载订单失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                BaseResponse<List<TradeInfo>> baseResp = gson.fromJson(body, 
                        new TypeToken<BaseResponse<List<TradeInfo>>>() {}.getType());
                
                runOnUiThread(() -> {
                    if (baseResp.isSuccess() && baseResp.getData() != null) {
                        activeList.clear();
                        activeList.addAll(baseResp.getData());
                        activeAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MyOrdersActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 加载已完成的订单
     */
    private void loadCompletedOrders() {
        long userId = ApiClient.getCurrentUserId();
        String url = ApiClient.BASE_URL + "api/trade/completed?userId=" + userId;
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MyOrdersActivity.this, "加载订单失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                BaseResponse<List<TradeInfo>> baseResp = gson.fromJson(body, 
                        new TypeToken<BaseResponse<List<TradeInfo>>>() {}.getType());
                
                runOnUiThread(() -> {
                    if (baseResp.isSuccess() && baseResp.getData() != null) {
                        completedList.clear();
                        completedList.addAll(baseResp.getData());
                        completedAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MyOrdersActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}