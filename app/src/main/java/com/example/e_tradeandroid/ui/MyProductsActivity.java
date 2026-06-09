package com.example.e_tradeandroid.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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

    private ImageView ivBack;
    private TextView tabActive, tabSoldOut;
    private View tabIndicator; // 保留但不再动态移动，仅用于视觉

    private SwipeRefreshLayout swipeRefreshActive, swipeRefreshSoldOut;
    private RecyclerView rvActive, rvSoldOut;
    private TextView tvEmptyActive, tvEmptySoldOut;
    private TextView tvLoadMoreActive, tvLoadMoreSoldOut;

    private ProductAdapter activeAdapter, soldOutAdapter;
    private List<Product> activeProductList = new ArrayList<>();
    private List<Product> soldOutProductList = new ArrayList<>();

    private Gson gson = new Gson();
    private boolean isActiveTab = true;

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
        setupBackButton();
        switchToActive();   // 默认显示上架中
        loadProducts();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tabActive = findViewById(R.id.tab_active);
        tabSoldOut = findViewById(R.id.tab_sold_out);
        tabIndicator = findViewById(R.id.tab_indicator);

        swipeRefreshActive = findViewById(R.id.swipe_refresh_active);
        swipeRefreshSoldOut = findViewById(R.id.swipe_refresh_sold_out);
        rvActive = findViewById(R.id.rv_active);
        rvSoldOut = findViewById(R.id.rv_sold_out);
        tvEmptyActive = findViewById(R.id.tv_empty_active);
        tvEmptySoldOut = findViewById(R.id.tv_empty_sold_out);
        tvLoadMoreActive = findViewById(R.id.tv_load_more_active);
        tvLoadMoreSoldOut = findViewById(R.id.tv_load_more_sold_out);
    }

    private void setupRecyclerViews() {
        rvActive.setLayoutManager(new LinearLayoutManager(this));
        activeAdapter = new ProductAdapter(this, activeProductList);
        rvActive.setAdapter(activeAdapter);

        rvSoldOut.setLayoutManager(new LinearLayoutManager(this));
        soldOutAdapter = new ProductAdapter(this, soldOutProductList);
        rvSoldOut.setAdapter(soldOutAdapter);

        // 下拉刷新
        swipeRefreshActive.setOnRefreshListener(() -> refreshActiveProducts());
        swipeRefreshSoldOut.setOnRefreshListener(() -> refreshSoldOutProducts());
        swipeRefreshActive.setColorSchemeResources(R.color.primary_green);
        swipeRefreshSoldOut.setColorSchemeResources(R.color.primary_green);

        // 加载更多点击
        tvLoadMoreActive.setOnClickListener(v -> loadMoreActiveProducts());
        tvLoadMoreSoldOut.setOnClickListener(v -> loadMoreSoldOutProducts());
    }

    private void setupTabSwitch() {
        tabActive.setOnClickListener(v -> switchToActive());
        tabSoldOut.setOnClickListener(v -> switchToSoldOut());
    }

    private void switchToActive() {
        if (isActiveTab) return;
        isActiveTab = true;

        tabActive.setTextColor(getResources().getColor(R.color.primary_green));
        tabSoldOut.setTextColor(getResources().getColor(android.R.color.darker_gray));

        swipeRefreshActive.setVisibility(View.VISIBLE);
        swipeRefreshSoldOut.setVisibility(View.GONE);
        updateEmptyState();
    }

    private void switchToSoldOut() {
        if (!isActiveTab) return;
        isActiveTab = false;

        tabSoldOut.setTextColor(getResources().getColor(R.color.primary_green));
        tabActive.setTextColor(getResources().getColor(android.R.color.darker_gray));

        swipeRefreshSoldOut.setVisibility(View.VISIBLE);
        swipeRefreshActive.setVisibility(View.GONE);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (isActiveTab) {
            if (activeProductList.isEmpty()) {
                rvActive.setVisibility(View.GONE);
                tvEmptyActive.setVisibility(View.VISIBLE);
                tvLoadMoreActive.setVisibility(View.GONE);
            } else {
                rvActive.setVisibility(View.VISIBLE);
                tvEmptyActive.setVisibility(View.GONE);
                // 加载更多按钮状态由数据加载时设置
            }
        } else {
            if (soldOutProductList.isEmpty()) {
                rvSoldOut.setVisibility(View.GONE);
                tvEmptySoldOut.setVisibility(View.VISIBLE);
                tvLoadMoreSoldOut.setVisibility(View.GONE);
            } else {
                rvSoldOut.setVisibility(View.VISIBLE);
                tvEmptySoldOut.setVisibility(View.GONE);
            }
        }
    }

    private void loadProducts() {
        loadActiveProducts();
        loadSoldOutProducts();
    }

    private void refreshActiveProducts() {
        activeCurrentPage = 1;
        activeTotalPages = 1;
        loadActiveProducts();
    }

    private void loadActiveProducts() {
        if (activeLoading) return;
        activeLoading = true;
        swipeRefreshActive.setRefreshing(true);

        String url = String.format("api/product/my?status=active&page=%d&size=10", activeCurrentPage);
        ApiClient.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    activeLoading = false;
                    swipeRefreshActive.setRefreshing(false);
                    Toast.makeText(MyProductsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    rvActive.setVisibility(View.GONE);
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

                        // 更新上架中列表的UI（不依赖当前Tab）
                        if (activeProductList.isEmpty()) {
                            rvActive.setVisibility(View.GONE);
                            tvEmptyActive.setVisibility(View.VISIBLE);
                            tvLoadMoreActive.setVisibility(View.GONE);
                            tvEmptyActive.setText("暂无上架中的商品");
                        } else {
                            rvActive.setVisibility(View.VISIBLE);
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
                        rvActive.setVisibility(View.GONE);
                        tvEmptyActive.setVisibility(View.VISIBLE);
                        tvLoadMoreActive.setVisibility(View.GONE);
                        tvEmptyActive.setText("暂无上架中的商品");
                    }
                });
            }
        });
    }

    private void loadMoreActiveProducts() {
        if (activeLoading || activeCurrentPage >= activeTotalPages) return;
        activeCurrentPage++;
        loadActiveProducts();
    }

    private void refreshSoldOutProducts() {
        soldOutCurrentPage = 1;
        soldOutTotalPages = 1;
        loadSoldOutProducts();
    }

    private void loadSoldOutProducts() {
        if (soldOutLoading) return;
        soldOutLoading = true;
        swipeRefreshSoldOut.setRefreshing(true);

        String url = String.format("api/product/my?status=sold_out&page=%d&size=10", soldOutCurrentPage);
        ApiClient.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    soldOutLoading = false;
                    swipeRefreshSoldOut.setRefreshing(false);
                    Toast.makeText(MyProductsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    rvSoldOut.setVisibility(View.GONE);
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

                        // 更新已售罄列表的UI（不依赖当前Tab）
                        if (soldOutProductList.isEmpty()) {
                            rvSoldOut.setVisibility(View.GONE);
                            tvEmptySoldOut.setVisibility(View.VISIBLE);
                            tvLoadMoreSoldOut.setVisibility(View.GONE);
                            tvEmptySoldOut.setText("暂无已售罄的商品");
                        } else {
                            rvSoldOut.setVisibility(View.VISIBLE);
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
                        rvSoldOut.setVisibility(View.GONE);
                        tvEmptySoldOut.setVisibility(View.VISIBLE);
                        tvLoadMoreSoldOut.setVisibility(View.GONE);
                        tvEmptySoldOut.setText("暂无已售罄的商品");
                    }
                });
            }
        });
    }

    private void loadMoreSoldOutProducts() {
        if (soldOutLoading || soldOutCurrentPage >= soldOutTotalPages) return;
        soldOutCurrentPage++;
        loadSoldOutProducts();
    }

    private void setupBackButton() {
        ivBack.setOnClickListener(v -> finish());
    }
}