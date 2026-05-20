package com.example.e_tradeandroid.ui;

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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MyProductsActivity extends AppCompatActivity {
    private RecyclerView recycler_view;
    private SwipeRefreshLayout swipe_refresh;
    private ProgressBar progress_bar;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);
        recycler_view = findViewById(R.id.recycler_view);
        swipe_refresh = findViewById(R.id.swipe_refresh);
        progress_bar = findViewById(R.id.progress_bar);
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        loadMyProducts();
    }

    private void loadMyProducts() {
        swipe_refresh.setRefreshing(true);
        progress_bar.setVisibility(View.VISIBLE);

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
                    swipe_refresh.setRefreshing(false);
                    progress_bar.setVisibility(View.GONE);
                    if (base.isSuccess() && base.getData() != null) {
                        ProductAdapter adapter = new ProductAdapter(MyProductsActivity.this, base.getData());
                        recycler_view.setAdapter(adapter);
                    }
                });
            }
        });
    }
}