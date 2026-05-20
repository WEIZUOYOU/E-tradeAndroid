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

public class MainActivity extends AppCompatActivity {
    private RecyclerView recycler_view;
    private SwipeRefreshLayout swipe_refresh;
    private ProgressBar progress_bar;
    private BottomNavigationView bottom_navigation;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recycler_view = findViewById(R.id.recycler_view);
        swipe_refresh = findViewById(R.id.swipe_refresh);
        progress_bar = findViewById(R.id.progress_bar);
        bottom_navigation = findViewById(R.id.bottom_navigation);

        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        loadProductList();
        setupBottomNav();

        swipe_refresh.setOnRefreshListener(this::loadProductList);
    }

    private void loadProductList() {
        swipe_refresh.setRefreshing(true);
        progress_bar.setVisibility(View.VISIBLE);

        ApiClient.get("product/list", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    swipe_refresh.setRefreshing(false);
                    progress_bar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                BaseResponse<List<Product>> resp = gson.fromJson(json, new TypeToken<BaseResponse<List<Product>>>() {}.getType());

                runOnUiThread(() -> {
                    swipe_refresh.setRefreshing(false);
                    progress_bar.setVisibility(View.GONE);

                    if (resp.isSuccess() && resp.getData() != null) {
                        ProductAdapter adapter = new ProductAdapter(MainActivity.this, resp.getData());
                        recycler_view.setAdapter(adapter);

                        adapter.setOnItemClickListener(product -> {
                            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                            // 直接用 long 传递，不强制转换
                            intent.putExtra("product_id", product.getId());
                            startActivity(intent);
                        });
                    }
                });
            }
        });
    }

    private void setupBottomNav() {
        bottom_navigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_publish) {
                startActivity(new Intent(this, PublishActivity.class));
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderListActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, MyProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}