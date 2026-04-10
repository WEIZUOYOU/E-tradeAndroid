package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.User;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MyProfileActivity extends AppCompatActivity {
    private TextView tvStudentId, tvUsername, tvPhone, tvCreditScore, tvStatus;
    private ImageView ivAvatar;
    private Button btnLogout, btnMyOrders, btnMyProducts;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        tvStudentId = findViewById(R.id.tv_student_id);
        tvUsername = findViewById(R.id.tv_username);
        tvPhone = findViewById(R.id.tv_phone);
        tvCreditScore = findViewById(R.id.tv_credit_score);
        tvStatus = findViewById(R.id.tv_status);
        ivAvatar = findViewById(R.id.iv_avatar);
        btnLogout = findViewById(R.id.btn_logout);
        btnMyOrders = findViewById(R.id.btn_my_orders);
        btnMyProducts = findViewById(R.id.btn_my_products);

        loadUserProfile();

        // 我的订单按钮
        btnMyOrders.setOnClickListener(v -> {
            startActivity(new Intent(MyProfileActivity.this, OrderListActivity.class));
        });

        // 我发布的商品按钮（可以跳转到MainActivity并过滤）
        btnMyProducts.setOnClickListener(v -> {
            Toast.makeText(this, "功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 退出登录按钮
        btnLogout.setOnClickListener(v -> {
            ApiClient.clearCookies();
            Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserProfile() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "user/profile")
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MyProfileActivity.this, "加载用户信息失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<User> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<User>>(){}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    User user = baseResp.getData();
                    runOnUiThread(() -> {
                        tvStudentId.setText(user.getStudentId());
                        tvUsername.setText(user.getUsername());
                        tvPhone.setText(user.getPhone());
                        tvCreditScore.setText(String.valueOf(user.getCreditScore()));
                        tvStatus.setText(user.getStatus() == 1 ? "正常" : "异常");
                        
                        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            Glide.with(MyProfileActivity.this)
                                    .load(ApiClient.BASE_URL + user.getAvatar())
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivAvatar);
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(MyProfileActivity.this, "获取用户信息失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}