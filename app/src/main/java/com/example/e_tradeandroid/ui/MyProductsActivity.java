package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.ProductAdapter;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyProductsActivity extends AppCompatActivity {
    // Tab 相关
    private TextView tabActive, tabSoldOut;
    private View tabIndicator;
    private FrameLayout container;
    private LinearLayout layoutActive, layoutSoldOut;
    
    // 上架中的商品
    private RecyclerView recyclerViewActive;
    private SwipeRefreshLayout swipeRefreshActive;
    private ProgressBar progressBarActive;
    
    // 已售罄的商品
    private RecyclerView recyclerViewSoldOut;
    private SwipeRefreshLayout swipeRefreshSoldOut;
    private ProgressBar progressBarSoldOut;
    
    private ImageView ivBack;

    private final Gson gson = new Gson();
    private boolean isActiveTab = true; // 当前选中的是上架中的商品标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);

        initViews();
        setupRecyclerViews();
        setupTabSwitch();
        loadProducts();
        setupBackButton();
    }

    private void initViews() {
        tabActive = findViewById(R.id.tab_active);
        tabSoldOut = findViewById(R.id.tab_sold_out);
        tabIndicator = findViewById(R.id.tab_indicator);
        container = findViewById(R.id.container);
        layoutActive = findViewById(R.id.layout_active);
        layoutSoldOut = findViewById(R.id.layout_sold_out);
        
        recyclerViewActive = findViewById(R.id.recycler_view_active);
        swipeRefreshActive = findViewById(R.id.swipe_refresh_active);
        progressBarActive = findViewById(R.id.progress_bar_active);
        
        recyclerViewSoldOut = findViewById(R.id.recycler_view_sold_out);
        swipeRefreshSoldOut = findViewById(R.id.swipe_refresh_sold_out);
        progressBarSoldOut = findViewById(R.id.progress_bar_sold_out);
        
        ivBack = findViewById(R.id.iv_back);
    }

    private void setupRecyclerViews() {
        recyclerViewActive.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSoldOut.setLayoutManager(new LinearLayoutManager(this));
        
        // 设置下拉刷新
        swipeRefreshActive.setOnRefreshListener(this::loadActiveProducts);
        swipeRefreshSoldOut.setOnRefreshListener(this::loadSoldOutProducts);
    }

    private void setupTabSwitch() {
        tabActive.setOnClickListener(v -> switchToActive());
        tabSoldOut.setOnClickListener(v -> switchToSoldOut());
    }

    /**
     * 切换到"上架中"商品
     */
    private void switchToActive() {
        if (isActiveTab) return;

        isActiveTab = true;
        
        // 更新 Tab 文字颜色
        tabActive.setTextColor(getResources().getColor(R.color.primary_green));
        tabSoldOut.setTextColor(getResources().getColor(R.color.text_secondary));
        
        // 切换内容
        layoutActive.setVisibility(View.VISIBLE);
        layoutSoldOut.setVisibility(View.GONE);
    }

    /**
     * 切换到"已售罄"商品
     */
    private void switchToSoldOut() {
        if (!isActiveTab) return;

        isActiveTab = false;
        
        // 更新 Tab 文字颜色
        tabSoldOut.setTextColor(getResources().getColor(R.color.primary_green));
        tabActive.setTextColor(getResources().getColor(R.color.text_secondary));
        
        // 切换内容
        layoutSoldOut.setVisibility(View.VISIBLE);
        layoutActive.setVisibility(View.GONE);
    }

    /**
     * 加载商品数据
     */
    private void loadProducts() {
        loadActiveProducts();
        loadSoldOutProducts();
    }

    /**
     * 加载上架中的商品
     */
    private void loadActiveProducts() {
        swipeRefreshActive.setRefreshing(true);
        progressBarActive.setVisibility(View.VISIBLE);

        ApiClient.get("api/product/my?status=active", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefreshActive.setRefreshing(false);
                    progressBarActive.setVisibility(View.GONE);
                    Toast.makeText(MyProductsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                BaseResponse<List<Product>> base = gson.fromJson(res, new TypeToken<BaseResponse<List<Product>>>() {}.getType());
                runOnUiThread(() -> {
                    swipeRefreshActive.setRefreshing(false);
                    progressBarActive.setVisibility(View.GONE);
                    if (base.isSuccess() && base.getData() != null) {
                        ProductAdapter adapter = new ProductAdapter(MyProductsActivity.this, base.getData());
                        recyclerViewActive.setAdapter(adapter);
                    }
                });
            }
        });
    }

    /**
     * 加载已售罄的商品
     */
    private void loadSoldOutProducts() {
        swipeRefreshSoldOut.setRefreshing(true);
        progressBarSoldOut.setVisibility(View.VISIBLE);

        ApiClient.get("api/product/my?status=sold_out", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefreshSoldOut.setRefreshing(false);
                    progressBarSoldOut.setVisibility(View.GONE);
                    Toast.makeText(MyProductsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                BaseResponse<List<Product>> base = gson.fromJson(res, new TypeToken<BaseResponse<List<Product>>>() {}.getType());
                runOnUiThread(() -> {
                    swipeRefreshSoldOut.setRefreshing(false);
                    progressBarSoldOut.setVisibility(View.GONE);
                    if (base.isSuccess() && base.getData() != null) {
                        ProductAdapter adapter = new ProductAdapter(MyProductsActivity.this, base.getData());
                        recyclerViewSoldOut.setAdapter(adapter);
                    }
                });
            }
        });
    }

    // 返回按钮
    private void setupBackButton() {
        ivBack.setOnClickListener(v -> finish());
    }
}