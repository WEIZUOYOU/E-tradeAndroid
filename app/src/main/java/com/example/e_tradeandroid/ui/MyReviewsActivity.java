package com.example.e_tradeandroid.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.ReviewAdapter;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Review;
import com.example.e_tradeandroid.model.ReviewListResponse;
import com.example.e_tradeandroid.network.ApiClient;
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
 * 我的评价页面（重构版）
 */
public class MyReviewsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tabReceived, tabGiven;
    private View tabIndicator;

    private SwipeRefreshLayout swipeRefreshReceived, swipeRefreshGiven;
    private RecyclerView rvReceived, rvGiven;
    private TextView tvEmptyReceived, tvEmptyGiven;

    private ReviewAdapter receivedAdapter, givenAdapter;
    private List<Review> receivedList = new ArrayList<>();
    private List<Review> givenList = new ArrayList<>();

    private Gson gson = new Gson();
    private boolean isReceivedTab = true; // 当前选中“收到的评价”

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        initViews();
        setupRecyclerViews();
        setupSwipeRefresh();
        setupTabSwitch();
        switchToReceived(); // 默认显示收到的评价
        loadReviews();

        ivBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tabReceived = findViewById(R.id.tab_received);
        tabGiven = findViewById(R.id.tab_given);
        tabIndicator = findViewById(R.id.tab_indicator);
        swipeRefreshReceived = findViewById(R.id.swipe_refresh_received);
        swipeRefreshGiven = findViewById(R.id.swipe_refresh_given);
        rvReceived = findViewById(R.id.rv_received);
        rvGiven = findViewById(R.id.rv_given);
        tvEmptyReceived = findViewById(R.id.tv_empty_received);
        tvEmptyGiven = findViewById(R.id.tv_empty_given);
    }

    private void setupRecyclerViews() {
        rvReceived.setLayoutManager(new LinearLayoutManager(this));
        receivedAdapter = new ReviewAdapter(this, receivedList, true);
        rvReceived.setAdapter(receivedAdapter);

        rvGiven.setLayoutManager(new LinearLayoutManager(this));
        givenAdapter = new ReviewAdapter(this, givenList, false);
        rvGiven.setAdapter(givenAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshReceived.setOnRefreshListener(this::loadReceivedReviews);
        swipeRefreshGiven.setOnRefreshListener(this::loadGivenReviews);
        swipeRefreshReceived.setColorSchemeResources(R.color.primary_green);
        swipeRefreshGiven.setColorSchemeResources(R.color.primary_green);
    }

    private void setupTabSwitch() {
        tabReceived.setOnClickListener(v -> switchToReceived());
        tabGiven.setOnClickListener(v -> switchToGiven());
    }

    private void switchToReceived() {
        if (isReceivedTab) return;
        isReceivedTab = true;

        tabReceived.setTextColor(getResources().getColor(R.color.primary_green));
        tabGiven.setTextColor(getResources().getColor(android.R.color.darker_gray));

        swipeRefreshReceived.setVisibility(View.VISIBLE);
        swipeRefreshGiven.setVisibility(View.GONE);
        updateEmptyState();
    }

    private void switchToGiven() {
        if (!isReceivedTab) return;
        isReceivedTab = false;

        tabGiven.setTextColor(getResources().getColor(R.color.primary_green));
        tabReceived.setTextColor(getResources().getColor(android.R.color.darker_gray));

        swipeRefreshGiven.setVisibility(View.VISIBLE);
        swipeRefreshReceived.setVisibility(View.GONE);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (isReceivedTab) {
            if (receivedList.isEmpty()) {
                rvReceived.setVisibility(View.GONE);
                tvEmptyReceived.setVisibility(View.VISIBLE);
            } else {
                rvReceived.setVisibility(View.VISIBLE);
                tvEmptyReceived.setVisibility(View.GONE);
            }
        } else {
            if (givenList.isEmpty()) {
                rvGiven.setVisibility(View.GONE);
                tvEmptyGiven.setVisibility(View.VISIBLE);
            } else {
                rvGiven.setVisibility(View.VISIBLE);
                tvEmptyGiven.setVisibility(View.GONE);
            }
        }
    }

    private void loadReviews() {
        loadReceivedReviews();
        loadGivenReviews();
    }

    private void loadReceivedReviews() {
        swipeRefreshReceived.setRefreshing(true);
        String url = ApiClient.BASE_URL + "/api/review/received";

        Request request = new Request.Builder().url(url).get().build();
        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefreshReceived.setRefreshing(false);
                    Toast.makeText(MyReviewsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Log.d("MyReviewsActivity", "收到评价响应: " + body);
                BaseResponse<ReviewListResponse> baseResp = gson.fromJson(body,
                        new TypeToken<BaseResponse<ReviewListResponse>>() {}.getType());

                runOnUiThread(() -> {
                    swipeRefreshReceived.setRefreshing(false);
                    if (baseResp.isSuccess() && baseResp.getData() != null) {
                        ReviewListResponse data = baseResp.getData();
                        receivedList.clear();
                        if (data.getReviews() != null) {
                            receivedList.addAll(data.getReviews());
                        }
                        receivedAdapter.notifyDataSetChanged();
                        
                        // ✅ 直接更新收到评价列表的可见性，不依赖当前Tab
                        if (receivedList.isEmpty()) {
                            rvReceived.setVisibility(View.GONE);
                            tvEmptyReceived.setVisibility(View.VISIBLE);
                        } else {
                            rvReceived.setVisibility(View.VISIBLE);
                            tvEmptyReceived.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(MyReviewsActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void loadGivenReviews() {
        swipeRefreshGiven.setRefreshing(true);
        String url = ApiClient.BASE_URL + "/api/review/given";

        Request request = new Request.Builder().url(url).get().build();
        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefreshGiven.setRefreshing(false);
                    Toast.makeText(MyReviewsActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                Log.d("MyReviewsActivity", "给出评价响应: " + body);
                BaseResponse<ReviewListResponse> baseResp = gson.fromJson(body,
                        new TypeToken<BaseResponse<ReviewListResponse>>() {}.getType());

                runOnUiThread(() -> {
                    swipeRefreshGiven.setRefreshing(false);
                    if (baseResp.isSuccess() && baseResp.getData() != null) {
                        ReviewListResponse data = baseResp.getData();
                        givenList.clear();
                        if (data.getReviews() != null) {
                            givenList.addAll(data.getReviews());
                        }
                        givenAdapter.notifyDataSetChanged();
                        
                        // ✅ 直接更新给出评价列表的可见性，不依赖当前Tab
                        if (givenList.isEmpty()) {
                            rvGiven.setVisibility(View.GONE);
                            tvEmptyGiven.setVisibility(View.VISIBLE);
                        } else {
                            rvGiven.setVisibility(View.VISIBLE);
                            tvEmptyGiven.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(MyReviewsActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}