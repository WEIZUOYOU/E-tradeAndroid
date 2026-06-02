package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MessageListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private BottomNavigationView bottomNavigation;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        recyclerView = findViewById(R.id.recycler_view_messages);
        progressBar = findViewById(R.id.progress_bar_messages);
        tvEmpty = findViewById(R.id.tv_empty);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupBottomNav();
        loadSessions();
    }

    private void loadSessions() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Note: Backend doesn't have a sessions endpoint yet
        // For now, showing empty state - you may need to implement this on backend
        // or use a different approach to list conversations
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setText("暂无消息会话（后端暂未提供会话列表接口）");
            tvEmpty.setVisibility(View.VISIBLE);
        });

        /* Original code - endpoint doesn't exist on backend
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "api/v1/message/sessions")
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setText("加载失败，请稍后重试");
                    tvEmpty.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        JSONObject obj = new JSONObject(respBody);
                        if (obj.getInt("code") == 200) {
                            JSONArray arr = obj.optJSONArray("data");
                            if (arr != null && arr.length() > 0) {
                                recyclerView.setLayoutManager(new LinearLayoutManager(MessageListActivity.this));
                                recyclerView.setVisibility(View.VISIBLE);
                                tvEmpty.setVisibility(View.GONE);
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                tvEmpty.setText("暂无消息");
                                tvEmpty.setVisibility(View.VISIBLE);
                            }
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            tvEmpty.setText("暂无消息");
                            tvEmpty.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        recyclerView.setVisibility(View.GONE);
                        tvEmpty.setText("暂无消息");
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        */
    }

    private void setupBottomNav() {
        bottomNavigation.setSelectedItemId(R.id.nav_messages);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_publish) {
                startActivity(new Intent(this, PublishActivity.class));
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_messages) {
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, MyProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}
