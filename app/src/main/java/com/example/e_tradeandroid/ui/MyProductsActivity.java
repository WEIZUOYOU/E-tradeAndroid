package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 我发布的商品页面
 */
public class MyProductsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private BottomNavigationView bottomNavigation;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);

        recyclerView = findViewById(R.id.recycler_view_my_products);
        swipeRefresh = findViewById(R.id.swipe_refresh_my_products);
        progressBar = findViewById(R.id.progress_bar_my_products);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();
        setupRecyclerView();
        loadMyProducts();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MyProductsActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_category) {
                startActivity(new Intent(MyProductsActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_publish) {
                startActivity(new Intent(MyProductsActivity.this, PublishActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(MyProductsActivity.this, OrderListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(MyProductsActivity.this, MyProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(productList, product -> {
            // 点击商品进入编辑页面
            Intent intent = new Intent(MyProductsActivity.this, EditProductActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadMyProducts);
    }

    private void loadMyProducts() {
        swipeRefresh.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);

        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "product/my")
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MyProductsActivity.this, "加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MyProductsActivity.this, "获取商品列表失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
