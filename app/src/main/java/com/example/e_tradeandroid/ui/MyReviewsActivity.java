package com.example.e_tradeandroid.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
 * 我的评价页面
 */
public class MyReviewsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tabReceived, tabGiven;
    private View tabIndicator;
    private FrameLayout container;

    private RecyclerView rvReceived, rvGiven;
    private ReviewAdapter receivedAdapter, givenAdapter;
    private List<Review> receivedList = new ArrayList<>();
    private List<Review> givenList = new ArrayList<>();

    private Gson gson = new Gson();
    private boolean isReceivedTab = true; // 当前选中的是收到的评价标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reviews);

        initViews();
        setupRecyclerViews();
        setupTabSwitch();
        loadReviews();

        ivBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tabReceived = findViewById(R.id.tab_received);
        tabGiven = findViewById(R.id.tab_given);
        tabIndicator = findViewById(R.id.tab_indicator);
        container = findViewById(R.id.container);
    }

    private void setupRecyclerViews() {
        // 我收到的评价列表
        rvReceived = new RecyclerView(this);
        rvReceived.setLayoutManager(new LinearLayoutManager(this));
        receivedAdapter = new ReviewAdapter(this, receivedList, true);
        rvReceived.setAdapter(receivedAdapter);

        // 我给出的评价列表
        rvGiven = new RecyclerView(this);
        rvGiven.setLayoutManager(new LinearLayoutManager(this));
        givenAdapter = new ReviewAdapter(this, givenList, false);
        rvGiven.setAdapter(givenAdapter);
    }

    private void setupTabSwitch() {
        tabReceived.setOnClickListener(v -> switchToReceived());
        tabGiven.setOnClickListener(v -> switchToGiven());
    }

    /**
     * 切换到"我收到的评价"
     */
    private void switchToReceived() {
        if (isReceivedTab) return;

        isReceivedTab = true;
        
        // 更新 Tab 文字颜色
        tabReceived.setTextColor(getResources().getColor(R.color.primary_green));
        tabGiven.setTextColor(getResources().getColor(R.color.text_secondary));
        
        // 切换内容
        container.removeAllViews();
        container.addView(rvReceived);
        
        // 更新指示器位置
        tabIndicator.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                0, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 
                1f
        ));
    }

    /**
     * 切换到"我给出的评价"
     */
    private void switchToGiven() {
        if (!isReceivedTab) return;

        isReceivedTab = false;
        
        // 更新 Tab 文字颜色
        tabGiven.setTextColor(getResources().getColor(R.color.primary_green));
        tabReceived.setTextColor(getResources().getColor(R.color.text_secondary));
        
        // 切换内容
        container.removeAllViews();
        container.addView(rvGiven);
        
        // 更新指示器位置
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                0, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 
                1f
        );
        params.weight = 0;
        tabIndicator.setLayoutParams(params);
        
        // 重新设置布局参数，将指示器移到右侧
        tabIndicator.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                0, 
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 
                1f
        ));
        // 通过设置 margin 来移动指示器位置
        params.setMarginStart(getResources().getDisplayMetrics().widthPixels / 2);
        tabIndicator.setLayoutParams(params);
    }

    /**
     * 加载评价数据
     */
    private void loadReviews() {
        // 加载我收到的评价
        loadReceivedReviews();
        
        // 加载我给出的评价
        loadGivenReviews();
    }

    /**
     * 加载我收到的评价
     * 后端返回格式: { "code": 200, "data": { "reviews": [...], "averageRating": 4.8, "reviewCount": 10 } }
     */
    private void loadReceivedReviews() {
        long userId = ApiClient.getCurrentUserId();
        String url = ApiClient.BASE_URL + "api/review/received?userId=" + userId;
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MyReviewsActivity.this, "加载评价失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                BaseResponse<ReviewListResponse> baseResp = gson.fromJson(body, 
                        new TypeToken<BaseResponse<ReviewListResponse>>() {}.getType());
                
                runOnUiThread(() -> {
                    if (baseResp.isSuccess() && baseResp.getData() != null) {
                        ReviewListResponse data = baseResp.getData();
                        receivedList.clear();
                        if (data.getReviews() != null) {
                            receivedList.addAll(data.getReviews());
                        }
                        receivedAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MyReviewsActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 加载我给出的评价
     * 后端返回格式: { "code": 200, "data": { "reviews": [...], "averageRating": 4.8, "reviewCount": 10 } }
     */
    private void loadGivenReviews() {
        long userId = ApiClient.getCurrentUserId();
        String url = ApiClient.BASE_URL + "api/review/given?userId=" + userId;
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(MyReviewsActivity.this, "加载评价失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                BaseResponse<ReviewListResponse> baseResp = gson.fromJson(body, 
                        new TypeToken<BaseResponse<ReviewListResponse>>() {}.getType());
                
                runOnUiThread(() -> {
                    if (baseResp.isSuccess() && baseResp.getData() != null) {
                        ReviewListResponse data = baseResp.getData();
                        givenList.clear();
                        if (data.getReviews() != null) {
                            givenList.addAll(data.getReviews());
                        }
                        givenAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(MyReviewsActivity.this, baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}