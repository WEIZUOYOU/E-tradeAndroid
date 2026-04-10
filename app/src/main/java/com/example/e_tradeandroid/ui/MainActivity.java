package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.ProductAdapter;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> filteredList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private SearchView searchView;
    private LinearLayout layoutCategories;
    private BottomNavigationView bottomNavigation;
    private Gson gson = new Gson();
    private String searchKeyword = "";
    private String selectedCategory = "";
    
    // 分类列表（从商品标签中提取）
    private List<String> categories = Arrays.asList(
        "全部", "教材", "电子产品", "生活用品", "运动器材", 
        "服装", "其他"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化ApiClient
        ApiClient.init(this);
        
        // 检查登录状态（可选）
        // 如果需要强制登录，可以取消下面的注释
        /*
        if (!isLoggedIn()) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }
        */
        
        setContentView(R.layout.activity_main);

        initViews();
        setupCategories();
        setupSearchView();
        setupBottomNavigation();
        loadProducts();
    }
    
    private boolean isLoggedIn() {
        // 简单检查：尝试获取client，如果未初始化则未登录
        try {
            ApiClient.getClient();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        searchView = findViewById(R.id.search_view);
        layoutCategories = findViewById(R.id.layout_categories);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(filteredList, product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadProducts);
    }

    private void setupCategories() {
        for (String category : categories) {
            TextView tvCategory = (TextView) LayoutInflater.from(this)
                    .inflate(R.layout.item_category, layoutCategories, false);
            tvCategory.setText(category);
            
            // 点击分类进行搜索
            tvCategory.setOnClickListener(v -> {
                selectedCategory = category.equals("全部") ? "" : category;
                searchKeyword = selectedCategory;
                if (!selectedCategory.isEmpty()) {
                    searchView.setQuery(selectedCategory, false);
                } else {
                    searchView.setQuery("", false);
                }
                filterProducts();
            });
            
            layoutCategories.addView(tvCategory);
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchKeyword = query;
                filterProducts();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchKeyword = newText;
                filterProducts();
                return true;
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // 已经在首页，不做操作
                return true;
            } else if (itemId == R.id.nav_category) {
                // 滚动到分类模块
                layoutCategories.getParent().requestChildFocus(
                    (View) layoutCategories.getParent(), null);
                return true;
            } else if (itemId == R.id.nav_publish) {
                startActivity(new Intent(MainActivity.this, PublishActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(MainActivity.this, OrderListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadProducts() {
        swipeRefresh.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        
        String url = ApiClient.BASE_URL + "product/list?page=1&size=20";
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<List<Product>> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<List<Product>>>(){}.getType());
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    if (baseResp.isSuccess() && baseResp.getData() != null) {
                        productList.clear();
                        productList.addAll(baseResp.getData());
                        filterProducts();
                    } else {
                        Toast.makeText(MainActivity.this, "获取商品列表失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void filterProducts() {
        filteredList.clear();
        
        for (Product product : productList) {
            boolean matchesSearch = true;
            boolean matchesCategory = true;
            
            // 搜索过滤
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                matchesSearch = product.getName().toLowerCase().contains(searchKeyword.toLowerCase()) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchKeyword.toLowerCase()));
            }
            
            // 分类过滤
            if (selectedCategory != null && !selectedCategory.isEmpty()) {
                matchesCategory = (product.getDescription() != null && 
                    product.getDescription().contains(selectedCategory)) ||
                    product.getName().contains(selectedCategory);
            }
            
            if (matchesSearch && matchesCategory) {
                filteredList.add(product);
            }
        }
        
        adapter.notifyDataSetChanged();
    }
}