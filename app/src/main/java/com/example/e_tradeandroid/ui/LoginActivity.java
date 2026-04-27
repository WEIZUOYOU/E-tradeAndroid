package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.LoginRequest;
import com.example.e_tradeandroid.model.RegisterRequest;
import com.example.e_tradeandroid.model.User;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etStudentId, etPassword, etUsername, etPhone;
    private Button btnLogin, btnRegister;
    private boolean isLoginMode = true;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ApiClient.init(this);

        etStudentId = findViewById(R.id.et_student_id);
        etPassword = findViewById(R.id.et_password);
        etUsername = findViewById(R.id.et_username);
        etPhone = findViewById(R.id.et_phone);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        setLoginMode();

        btnLogin.setOnClickListener(v -> {
            if (isLoginMode) {
                doLogin();
            } else {
                doRegister();
            }
        });

        btnRegister.setOnClickListener(v -> {
            isLoginMode = !isLoginMode;
            if (isLoginMode) {
                setLoginMode();
            } else {
                setRegisterMode();
            }
        });
    }

    private void setLoginMode() {
        etUsername.setVisibility(View.GONE);
        etPhone.setVisibility(View.GONE);
        btnLogin.setText("登录");
        btnRegister.setText("去注册");
    }

    private void setRegisterMode() {
        etUsername.setVisibility(View.VISIBLE);
        etPhone.setVisibility(View.VISIBLE);
        btnLogin.setText("注册");
        btnRegister.setText("返回登录");
    }

    private void doLogin() {
        String phone = etStudentId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "手机号和密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        LoginRequest req = new LoginRequest();
        req.setPhone(phone);
        req.setPassword(password);
        String json = gson.toJson(req);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "user/login")
                .post(body)
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "网络错误：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<User> baseResp = gson.fromJson(respBody, new com.google.gson.reflect.TypeToken<BaseResponse<User>>(){}.getType());
                if (baseResp.isSuccess()) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "登录失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void doRegister() {
        String studentId = etStudentId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        if (studentId.isEmpty() || password.isEmpty() || username.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }
        RegisterRequest req = new RegisterRequest();
        req.setStudentId(studentId);
        req.setPassword(password);
        req.setUsername(username);
        req.setPhone(phone);
        String json = gson.toJson(req);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "user/register")
                .post(body)
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "网络错误：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Long> baseResp = gson.fromJson(respBody, new com.google.gson.reflect.TypeToken<BaseResponse<Long>>(){}.getType());
                if (baseResp.isSuccess()) {
                    runOnUiThread(() -> {
                        Toast.makeText(LoginActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                        isLoginMode = true;
                        setLoginMode();
                        etStudentId.setText("");
                        etPassword.setText("");
                        etUsername.setText("");
                        etPhone.setText("");
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "注册失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}