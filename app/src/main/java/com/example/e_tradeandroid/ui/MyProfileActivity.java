package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.User;
import com.example.e_tradeandroid.model.VerifyRequest;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyProfileActivity extends AppCompatActivity {
    private TextView tvStudentId, tvUsername, tvPhone, tvCreditScore, tvStatus;
    private ImageView ivAvatar;
    private Button btnLogout, btnMyOrders, btnMyProducts, btnRealnameAuth;
    private BottomNavigationView bottomNavigation;
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
        btnRealnameAuth = findViewById(R.id.btn_realname_auth);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        loadUserProfile();

        // 我的订单按钮
        btnMyOrders.setOnClickListener(v -> {
            startActivity(new Intent(MyProfileActivity.this, OrderListActivity.class));
        });

        // 我发布的商品按钮
        btnMyProducts.setOnClickListener(v -> {
            Intent intent = new Intent(MyProfileActivity.this, MyProductsActivity.class);
            startActivity(intent);
        });

        // 退出登录按钮
        btnLogout.setOnClickListener(v -> {
            Request request = new Request.Builder()
                    .url(ApiClient.BASE_URL + "user/logout")
                    .post(RequestBody.create("", MediaType.parse("application/json; charset=utf-8")))
                    .build();

            ApiClient.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ApiClient.clearCookies();
                    goToLogin();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    ApiClient.clearCookies();
                    goToLogin();
                }
            });
        });

        // 实名认证按钮
        btnRealnameAuth.setOnClickListener(v -> showRealnameAuthDialog());
    }

    private void showRealnameAuthDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("实名认证");
        builder.setMessage("请输入学号和真实姓名进行认证");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etStudentId = new EditText(this);
        etStudentId.setHint("学号");
        layout.addView(etStudentId);

        final EditText etRealName = new EditText(this);
        etRealName.setHint("真实姓名");
        layout.addView(etRealName);

        builder.setView(layout);

        builder.setPositiveButton("提交", (dialog, which) -> {
            String studentId = etStudentId.getText().toString().trim();
            String realName = etRealName.getText().toString().trim();
            
            if (studentId.isEmpty() || realName.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }
            
            submitRealnameAuth(studentId, realName);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void submitRealnameAuth(String studentId, String realName) {
        VerifyRequest req = new VerifyRequest();
        req.setStudentId(studentId);
        req.setRealName(realName);
        
        String json = gson.toJson(req);
        RequestBody body = RequestBody.create(
            json, 
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "user/verify")
                .post(body)
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MyProfileActivity.this, "认证失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Void> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Void>>(){}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        Toast.makeText(MyProfileActivity.this, "认证申请已提交，等待审核", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MyProfileActivity.this, "认证失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MyProfileActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_category) {
                startActivity(new Intent(MyProfileActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_publish) {
                startActivity(new Intent(MyProfileActivity.this, PublishActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(MyProfileActivity.this, OrderListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(MyProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserProfile() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "user/current")
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