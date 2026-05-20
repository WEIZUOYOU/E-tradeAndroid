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
import com.example.e_tradeandroid.adapter.ProductAdapter;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyProductsActivity extends AppCompatActivity {
    // 👇 完全匹配你 XML 里的 ID
    private RecyclerView recycler_view_my_products;
    private SwipeRefreshLayout swipe_refresh_my_products;
    private ProgressBar progress_bar_my_products;
    private BottomNavigationView bottom_navigation;

    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);

        // 👇 100% 匹配 XML ID
        recycler_view_my_products = findViewById(R.id.recycler_view_my_products);
        swipe_refresh_my_products = findViewById(R.id.swipe_refresh_my_products);
        progress_bar_my_products = findViewById(R.id.progress_bar_my_products);
        bottom_navigation = findViewById(R.id.bottom_navigation);

        recycler_view_my_products.setLayoutManager(new LinearLayoutManager(this));
        initNav();
        loadMyProducts();
    }

    // 底部导航
    private void initNav() {
        bottom_navigation.setSelectedItemId(R.id.nav_profile);
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
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    // 加载我的商品
    private void loadMyProducts() {
        swipe_refresh_my_products.setRefreshing(true);
        progress_bar_my_products.setVisibility(View.VISIBLE);

        ApiClient.get("product/my", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MyProductsActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                BaseResponse<List<Product>> base = gson.fromJson(res, new TypeToken<BaseResponse<List<Product>>>() {}.getType());
                runOnUiThread(() -> {
                    swipe_refresh_my_products.setRefreshing(false);
                    progress_bar_my_products.setVisibility(View.GONE);
                    if (base.isSuccess() && base.getData() != null) {
                        ProductAdapter adapter = new ProductAdapter(MyProductsActivity.this, base.getData());
                        recycler_view_my_products.setAdapter(adapter);
                    }
                });
            }
        });
    }
}