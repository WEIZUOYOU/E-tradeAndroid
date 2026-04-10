package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private List<Product> filteredList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private Gson gson = new Gson();
    private String searchKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("校园交易平台");
        }

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        FloatingActionButton fab = findViewById(R.id.fab_publish);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(filteredList, product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadProducts);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PublishActivity.class)));

        loadProducts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("搜索商品...");
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
        
        // 添加订单列表按钮
        MenuItem orderItem = menu.findItem(R.id.action_orders);
        orderItem.setOnMenuItemClickListener(item -> {
            startActivity(new Intent(MainActivity.this, OrderListActivity.class));
            return true;
        });
        
        // 添加个人资料按钮
        MenuItem profileItem = menu.findItem(R.id.action_profile);
        profileItem.setOnMenuItemClickListener(item -> {
            startActivity(new Intent(MainActivity.this, MyProfileActivity.class));
            return true;
        });
        
        return true;
    }

    private void loadProducts() {
        swipeRefresh.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        
        String url = ApiClient.BASE_URL + "product/list?page=1&size=20";
        if (!searchKeyword.isEmpty()) {
            url += "&keyword=" + searchKeyword;
        }
        
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
        if (searchKeyword == null || searchKeyword.isEmpty()) {
            filteredList.addAll(productList);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(searchKeyword.toLowerCase()) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(searchKeyword.toLowerCase()))) {
                    filteredList.add(product);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}