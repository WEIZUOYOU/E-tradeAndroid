package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.net.Uri;
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

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyProfileActivity extends AppCompatActivity {
    private TextView tvStudentId, tvUsername, tvPhone, tvCreditScore, tvAuthStatus;
    private ImageView ivAvatar;
    private Button btnLogout, btnMyOrders, btnMyProducts, btnRealnameAuth, btnEditProfile;
    private BottomNavigationView bottomNavigation;
    private Gson gson = new Gson();
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        tvStudentId = findViewById(R.id.tv_student_id);
        tvUsername = findViewById(R.id.tv_username);
        tvPhone = findViewById(R.id.tv_phone);
        tvCreditScore = findViewById(R.id.tv_credit_score);
        tvAuthStatus = findViewById(R.id.tv_status);
        ivAvatar = findViewById(R.id.iv_avatar);
        btnLogout = findViewById(R.id.btn_logout);
        btnMyOrders = findViewById(R.id.btn_my_orders);
        btnMyProducts = findViewById(R.id.btn_my_products);
        btnRealnameAuth = findViewById(R.id.btn_realname_auth);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        loadUserProfile();

        btnMyOrders.setOnClickListener(v ->
            startActivity(new Intent(MyProfileActivity.this, OrderListActivity.class))
        );

        btnMyProducts.setOnClickListener(v ->
            startActivity(new Intent(MyProfileActivity.this, MyProductsActivity.class))
        );

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

        btnRealnameAuth.setOnClickListener(v -> showRealnameAuthDialog());

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        ivAvatar.setOnClickListener(v -> pickImage());
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("修改个人资料");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText etUsername = new EditText(this);
        etUsername.setHint("新用户名");
        layout.addView(etUsername);

        builder.setView(layout);

        builder.setPositiveButton("保存", (dialog, which) -> {
            String newUsername = etUsername.getText().toString().trim();
            if (newUsername.isEmpty()) {
                Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            updateProfile(newUsername);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void updateProfile(String newUsername) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", newUsername);
            ApiClient.put("user/profile", body.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MyProfileActivity.this, "修改失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respBody = response.body().string();
                    BaseResponse<Void> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Void>>(){}.getType());
                    runOnUiThread(() -> {
                        if (baseResp.isSuccess()) {
                            Toast.makeText(MyProfileActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                            loadUserProfile();
                        } else {
                            Toast.makeText(MyProfileActivity.this, "修改失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadAvatar(imageUri);
        }
    }

    private void uploadAvatar(Uri imageUri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] imageBytes = new byte[inputStream.available()];
            inputStream.read(imageBytes);
            inputStream.close();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "avatar.jpg",
                            RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder()
                    .url(ApiClient.BASE_URL + "user/avatar")
                    .post(requestBody)
                    .build();

            ApiClient.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MyProfileActivity.this, "上传失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respBody = response.body().string();
                    BaseResponse<String> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<String>>(){}.getType());
                    runOnUiThread(() -> {
                        if (baseResp.isSuccess()) {
                            Toast.makeText(MyProfileActivity.this, "头像上传成功", Toast.LENGTH_SHORT).show();
                            loadUserProfile();
                        } else {
                            Toast.makeText(MyProfileActivity.this, "上传失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "读取图片失败", Toast.LENGTH_SHORT).show());
        }
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
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

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
                        tvStudentId.setText("学号：" + (user.getStudentId() != null ? user.getStudentId() : ""));
                        tvUsername.setText("用户名：" + (user.getUsername() != null ? user.getUsername() : ""));
                        tvPhone.setText("手机：" + (user.getPhone() != null ? user.getPhone() : ""));
                        tvCreditScore.setText("信用分：" + (user.getCreditScore() != null ? user.getCreditScore() : 0));

                        if (user.getIsAuth() != null && user.getIsAuth() == 1) {
                            tvAuthStatus.setText("已认证 (" + (user.getRealName() != null ? user.getRealName() : "") + ")");
                        } else {
                            tvAuthStatus.setText("未认证");
                        }

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
