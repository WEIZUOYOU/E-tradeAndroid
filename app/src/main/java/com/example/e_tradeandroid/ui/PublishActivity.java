package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PublishActivity extends AppCompatActivity {
    // 👇 完全匹配你 XML 里的 id 名称
    private EditText et_name, et_price, et_stock, et_description;
    private Button btn_select_image, btn_publish;
    private ImageView iv_preview;
    private BottomNavigationView bottom_navigation;

    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        // 👇 绑定 XML 里的所有控件（100% 匹配）
        et_name = findViewById(R.id.et_name);
        et_price = findViewById(R.id.et_price);
        et_stock = findViewById(R.id.et_stock);
        et_description = findViewById(R.id.et_description);
        btn_select_image = findViewById(R.id.btn_select_image);
        iv_preview = findViewById(R.id.iv_preview);
        btn_publish = findViewById(R.id.btn_publish);
        bottom_navigation = findViewById(R.id.bottom_navigation);

        initNav();
        btn_publish.setOnClickListener(v -> submitPublish());
    }

    // 底部导航
    private void initNav() {
        bottom_navigation.setSelectedItemId(R.id.nav_publish);
        bottom_navigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_messages) {
                startActivity(new Intent(this, MessageListActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, MyProfileActivity.class));
                finish();
                return true;
            }
            return true;
        });
    }

    // 发布商品逻辑
    private void submitPublish() {
        String name = et_name.getText().toString().trim();
        String priceStr = et_price.getText().toString().trim();
        String stockStr = et_stock.getText().toString().trim();
        String desc = et_description.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product();
        product.setName(name);
        product.setPrice(Double.parseDouble(priceStr));
        product.setStock(Integer.parseInt(stockStr));
        product.setDescription(desc);

        String json = gson.toJson(product);
        ApiClient.post("product/publish", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(PublishActivity.this, "发布失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                BaseResponse<Object> resp = gson.fromJson(res, BaseResponse.class);
                runOnUiThread(() -> {
                    if (resp.isSuccess()) {
                        Toast.makeText(PublishActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PublishActivity.this, resp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}