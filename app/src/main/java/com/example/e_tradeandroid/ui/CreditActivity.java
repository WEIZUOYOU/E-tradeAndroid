package com.example.e_tradeandroid.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.UserCredit;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreditActivity extends AppCompatActivity {
    private TextView tvCreditScore, tvTradeCount, tvGoodRate;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit);

        tvCreditScore = findViewById(R.id.tvCreditScore);
        tvTradeCount = findViewById(R.id.tvTradeCount);
        tvGoodRate = findViewById(R.id.tvGoodRate);

        getUserCredit();
    }

    private void getUserCredit() {
        ApiClient.get("api/user/current", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(CreditActivity.this, "获取失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                BaseResponse<com.example.e_tradeandroid.model.User> resp = gson.fromJson(json, new TypeToken<BaseResponse<com.example.e_tradeandroid.model.User>>() {}.getType());
                if (resp.isSuccess() && resp.getData() != null) {
                    runOnUiThread(() -> {
                        com.example.e_tradeandroid.model.User user = resp.getData();
                        // 从 User 对象中获取信用分等信息
                        tvCreditScore.setText("信用分：" + (user.getCreditScore() != null ? user.getCreditScore() : 100));
                        // TODO: 交易次数和好评率需要从订单统计中获取，这里暂时显示默认值
                        tvTradeCount.setText("交易次数：0");
                        tvGoodRate.setText("好评率：100%");
                    });
                }
            }
        });
    }
}