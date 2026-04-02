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
import com.example.e_tradeandroid.model.CreateOrderRequest;
import com.example.e_tradeandroid.model.Order;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvStock, tvDescription, tvSeller;
    private ImageView ivImage;
    private Button btnBuy;
    private Product product;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        tvName = findViewById(R.id.tv_name);
        tvPrice = findViewById(R.id.tv_price);
        tvStock = findViewById(R.id.tv_stock);
        tvDescription = findViewById(R.id.tv_description);
        tvSeller = findViewById(R.id.tv_seller);
        ivImage = findViewById(R.id.iv_image);
        btnBuy = findViewById(R.id.btn_buy);

        long productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            finish();
            return;
        }

        loadProductDetail(productId);

        btnBuy.setOnClickListener(v -> createOrder());
    }

    private void loadProductDetail(long productId) {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "product/detail/" + productId)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProductDetailActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Product> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Product>>(){}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    product = baseResp.getData();
                    runOnUiThread(() -> {
                        tvName.setText(product.getName());
                        tvPrice.setText("¥" + product.getPrice().toString());
                        tvStock.setText("库存：" + product.getStock());
                        tvDescription.setText(product.getDescription());
                        tvSeller.setText("卖家ID：" + product.getSellerId());
                        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                            String firstUrl = product.getImageUrls().split(",")[0];
                            Glide.with(ProductDetailActivity.this)
                                    .load(ApiClient.BASE_URL + firstUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivImage);
                        }
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ProductDetailActivity.this, "获取商品详情失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void createOrder() {
        if (product == null) return;
        CreateOrderRequest req = new CreateOrderRequest();
        req.setProductId(product.getId());
        req.setQuantity(1);
        String json = gson.toJson(req);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "order/create")
                .post(body)
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProductDetailActivity.this, "下单失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Order> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Order>>(){}.getType());
                if (baseResp.isSuccess()) {
                    runOnUiThread(() -> {
                        Toast.makeText(ProductDetailActivity.this, "下单成功，订单号：" + baseResp.getData().getOrderNo(), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ProductDetailActivity.this, OrderActivity.class);
                        intent.putExtra("order_id", baseResp.getData().getId());
                        startActivity(intent);
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ProductDetailActivity.this, "下单失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}