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
import com.example.e_tradeandroid.model.ProductPageResponse;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
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
    private TextView tvEmptyActive, tvLoadMoreActive; // 空状态和加载更多
    
    // 已售罄的商品
    private RecyclerView recyclerViewSoldOut;
    private SwipeRefreshLayout swipeRefreshSoldOut;
    private ProgressBar progressBarSoldOut;
    private TextView tvEmptySoldOut, tvLoadMoreSoldOut; // 空状态和加载更多
    
    private ImageView ivBack;

    private final Gson gson = new Gson();
    private boolean isActiveTab = true; // 当前选中的是上架中的商品标签
    
    // 商品列表数据
    private List<Product> activeProductList = new ArrayList<>();
    private List<Product> soldOutProductList = new ArrayList<>();
    private ProductAdapter activeAdapter, soldOutAdapter;
    
    // 分页参数
    private int activeCurrentPage = 1;
    private int activeTotalPages = 1;
    private boolean activeLoading = false;
    
    private int soldOutCurrentPage = 1;
    private int soldOutTotalPages = 1;
    private boolean soldOutLoading = false;

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
        tvEmptyActive = findViewById(R.id.tv_empty_active); // 空状态
        tvLoadMoreActive = findViewById(R.id.tv_load_more_active); // 加载更多
        
        recyclerViewSoldOut = findViewById(R.id.recycler_view_sold_out);
        swipeRefreshSoldOut = findViewById(R.id.swipe_refresh_sold_out);
        progressBarSoldOut = findViewById(R.id.progress_bar_sold_out);
        tvEmptySoldOut = findViewById(R.id.tv_empty_sold_out); // 空状态
        tvLoadMoreSoldOut = findViewById(R.id.tv_load_more_sold_out); // 加载更多
        
        ivBack = findViewById(R.id.iv_back);
    }

    private void setupRecyclerViews() {
        recyclerViewActive.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSoldOut.setLayoutManager(new LinearLayoutManager(this));
        
        // 设置适配器
        activeAdapter = new ProductAdapter(this, activeProductList);
        soldOutAdapter = new ProductAdapter(this, soldOutProductList);
        recyclerViewActive.setAdapter(activeAdapter);
        recyclerViewSoldOut.setAdapter(soldOutAdapter);
        
        // 设置下拉刷新
        swipeRefreshActive.setOnRefreshListener(this::refreshActiveProducts);
        swipeRefreshSoldOut.setOnRefreshListener(this::refreshSoldOutProducts);
        
        // 设置加载更多点击事件
        tvLoadMoreActive.setOnClickListener(v -> loadMoreActiveProducts());
        tvLoadMoreSoldOut.setOnClickListener(v -> loadMoreSoldOutProducts());
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
     * 刷新上架中的商品（下拉刷新）
     */
    private void refreshActiveProducts() {
        activeCurrentPage = 1;
        activeTotalPages = 1;
        loadActiveProducts();
    }

    /**
     * 加载上架中的商品
     */
    private void loadActiveProducts() {
        if (activeLoading) return;
        activeLoading = true;
        
        swipeRefreshActive.setRefreshing(true);
        progressBarActive.setVisibility(View.VISIBLE);

        String url = String.format("api/product/my?status=active&page=%d&size=10", activeCurrentPage);
        ApiClient.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    activeLoading = false;
                    swipeRefreshActive.setRefreshing(false);
                    progressBarActive.setVisibility(View.GONE);
                    Toast.makeText(MyProductsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    // 显示空状态
                    recyclerViewActive.setVisibility(View.GONE);
                    tvEmptyActive.setVisibility(View.VISIBLE);
                    tvLoadMoreActive.setVisibility(View.GONE);
                    tvEmptyActive.setText("加载失败，请下拉刷新重试");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                BaseResponse<ProductPageResponse> base = gson.fromJson(res, 
                    new TypeToken<BaseResponse<ProductPageResponse>>() {}.getType());
                
                runOnUiThread(() -> {
                    activeLoading = false;
                    swipeRefreshActive.setRefreshing(false);
                    progressBarActive.setVisibility(View.GONE);
                    
                    if (base.isSuccess() && base.getData() != null) {
                        ProductPageResponse pageData = base.getData();
                        
                        if (activeCurrentPage == 1) {
                            activeProductList.clear();
                        }
                        
                        if (pageData.getProducts() != null) {
                            activeProductList.addAll(pageData.getProducts());
                        }
                        
                        activeTotalPages = pageData.getTotalPages() != null ? pageData.getTotalPages() : 1;
                        activeAdapter.notifyDataSetChanged();
                        
                        // 处理空状态和加载更多
                        if (activeProductList.isEmpty()) {
                            recyclerViewActive.setVisibility(View.GONE);
                            tvEmptyActive.setVisibility(View.VISIBLE);
                            tvLoadMoreActive.setVisibility(View.GONE);
                            tvEmptyActive.setText("暂无上架中的商品");
                        } else {
                            recyclerViewActive.setVisibility(View.VISIBLE);
                            tvEmptyActive.setVisibility(View.GONE);
                            
                            if (activeCurrentPage < activeTotalPages) {
                                tvLoadMoreActive.setVisibility(View.VISIBLE);
                                tvLoadMoreActive.setText("点击加载更多");
                            } else {
                                tvLoadMoreActive.setVisibility(View.VISIBLE);
                                tvLoadMoreActive.setText("已加载全部");
                            }
                        }
                    } else {
                        Toast.makeText(MyProductsActivity.this, base.getMessage(), Toast.LENGTH_SHORT).show();
                        recyclerViewActive.setVisibility(View.GONE);
                        tvEmptyActive.setVisibility(View.VISIBLE);
                        tvLoadMoreActive.setVisibility(View.GONE);
                        tvEmptyActive.setText("暂无上架中的商品");
                    }
                });
            }
        });
    }

    /**
     * 加载更多上架中的商品
     */
    private void loadMoreActiveProducts() {
        if (activeLoading || activeCurrentPage >= activeTotalPages) return;
        
        activeCurrentPage++;
        loadActiveProducts();
    }

    /**
     * 刷新已售罄的商品（下拉刷新）
     */
    private void refreshSoldOutProducts() {
        soldOutCurrentPage = 1;
        soldOutTotalPages = 1;
        loadSoldOutProducts();
    }

    /**
     * 加载已售罄的商品
     */
    private void loadSoldOutProducts() {
        if (soldOutLoading) return;
        soldOutLoading = true;
        
        swipeRefreshSoldOut.setRefreshing(true);
        progressBarSoldOut.setVisibility(View.VISIBLE);

        String url = String.format("api/product/my?status=sold_out&page=%d&size=10", soldOutCurrentPage);
        ApiClient.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    soldOutLoading = false;
                    swipeRefreshSoldOut.setRefreshing(false);
                    progressBarSoldOut.setVisibility(View.GONE);
                    Toast.makeText(MyProductsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    // 显示空状态
                    recyclerViewSoldOut.setVisibility(View.GONE);
                    tvEmptySoldOut.setVisibility(View.VISIBLE);
                    tvLoadMoreSoldOut.setVisibility(View.GONE);
                    tvEmptySoldOut.setText("加载失败，请下拉刷新重试");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                BaseResponse<ProductPageResponse> base = gson.fromJson(res, 
                    new TypeToken<BaseResponse<ProductPageResponse>>() {}.getType());
                
                runOnUiThread(() -> {
                    soldOutLoading = false;
                    swipeRefreshSoldOut.setRefreshing(false);
                    progressBarSoldOut.setVisibility(View.GONE);
                    
                    if (base.isSuccess() && base.getData() != null) {
                        ProductPageResponse pageData = base.getData();
                        
                        if (soldOutCurrentPage == 1) {
                            soldOutProductList.clear();
                        }
                        
                        if (pageData.getProducts() != null) {
                            soldOutProductList.addAll(pageData.getProducts());
                        }
                        
                        soldOutTotalPages = pageData.getTotalPages() != null ? pageData.getTotalPages() : 1;
                        soldOutAdapter.notifyDataSetChanged();
                        
                        // 处理空状态和加载更多
                        if (soldOutProductList.isEmpty()) {
                            recyclerViewSoldOut.setVisibility(View.GONE);
                            tvEmptySoldOut.setVisibility(View.VISIBLE);
                            tvLoadMoreSoldOut.setVisibility(View.GONE);
                            tvEmptySoldOut.setText("暂无已售罄的商品");
                        } else {
                            recyclerViewSoldOut.setVisibility(View.VISIBLE);
                            tvEmptySoldOut.setVisibility(View.GONE);
                            
                            if (soldOutCurrentPage < soldOutTotalPages) {
                                tvLoadMoreSoldOut.setVisibility(View.VISIBLE);
                                tvLoadMoreSoldOut.setText("点击加载更多");
                            } else {
                                tvLoadMoreSoldOut.setVisibility(View.VISIBLE);
                                tvLoadMoreSoldOut.setText("已加载全部");
                            }
                        }
                    } else {
                        Toast.makeText(MyProductsActivity.this, base.getMessage(), Toast.LENGTH_SHORT).show();
                        recyclerViewSoldOut.setVisibility(View.GONE);
                        tvEmptySoldOut.setVisibility(View.VISIBLE);
                        tvLoadMoreSoldOut.setVisibility(View.GONE);
                        tvEmptySoldOut.setText("暂无已售罄的商品");
                    }
                });
            }
        });
    }

    /**
     * 加载更多已售罄的商品
     */
    private void loadMoreSoldOutProducts() {
        if (soldOutLoading || soldOutCurrentPage >= soldOutTotalPages) return;
        
        soldOutCurrentPage++;
        loadSoldOutProducts();
    }

    // 返回按钮
    private void setupBackButton() {
        ivBack.setOnClickListener(v -> finish());
    }
}