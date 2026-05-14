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
import com.example.e_tradeandroid.model.Category;
import com.example.e_tradeandroid.model.Product;
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

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private SearchView searchView;
    private LinearLayout layoutCategories;
    private BottomNavigationView bottomNavigation;
    private Gson gson = new Gson();
    private String searchKeyword = "";
    private Long selectedCategoryId = null;
    private List<Category> categories = new ArrayList<>();
    private boolean isSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ApiClient.init(this);

        setContentView(R.layout.activity_main);

        initViews();
        setupSearchView();
        setupBottomNavigation();
        loadCategories();
        loadProducts();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        searchView = findViewById(R.id.search_view);
        layoutCategories = findViewById(R.id.layout_categories);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(productList, product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> {
            if (isSearchMode) {
                searchProducts();
            } else {
                loadProducts();
            }
        });
    }

    private void loadCategories() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "category/list")
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> setupDefaultCategories());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<List<Category>> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<List<Category>>>(){}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess() && baseResp.getData() != null && !baseResp.getData().isEmpty()) {
                        categories.clear();
                        categories.addAll(baseResp.getData());
                        setupCategoriesView();
                    } else {
                        setupDefaultCategories();
                    }
                });
            }
        });
    }

    private void setupDefaultCategories() {
        categories.clear();
        String[] defaultNames = {"全部", "教材", "电子产品", "生活用品", "运动器材", "服装", "其他"};
        for (int i = 0; i < defaultNames.length; i++) {
            Category c = new Category();
            c.setId(i == 0 ? 0L : (long) i);
            c.setName(defaultNames[i]);
            categories.add(c);
        }
        setupCategoriesView();
    }

    private void setupCategoriesView() {
        layoutCategories.removeAllViews();
        for (Category category : categories) {
            TextView tvCategory = (TextView) LayoutInflater.from(this)
                    .inflate(R.layout.item_category, layoutCategories, false);
            tvCategory.setText(category.getName());

            tvCategory.setOnClickListener(v -> {
                if (category.getId() != null && category.getId() == 0L) {
                    selectedCategoryId = null;
                } else {
                    selectedCategoryId = category.getId();
                }
                searchKeyword = "";
                searchView.setQuery("", false);
                searchProducts();
            });

            layoutCategories.addView(tvCategory);
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchKeyword = query;
                selectedCategoryId = null;
                searchProducts();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_category) {
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
        isSearchMode = false;
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
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "获取商品列表失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void searchProducts() {
        isSearchMode = true;
        swipeRefresh.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);

        StringBuilder url = new StringBuilder(ApiClient.BASE_URL + "product/search?page=1&size=20");
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            url.append("&keyword=").append(searchKeyword);
        }
        if (selectedCategoryId != null) {
            url.append("&categoryId=").append(selectedCategoryId);
        }

        Request request = new Request.Builder()
                .url(url.toString())
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "搜索失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MainActivity.this, "搜索无结果", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
