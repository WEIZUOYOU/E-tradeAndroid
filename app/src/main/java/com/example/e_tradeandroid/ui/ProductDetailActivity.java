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
import com.example.e_tradeandroid.model.CreateOrderRequest;
import com.example.e_tradeandroid.model.Order;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.model.User;
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

public class ProductDetailActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvStock, tvDescription, tvSeller, tvViewCount;
    private ImageView ivImage;
    private Button btnBuy;
    private Product product;
    private User seller;
    private BottomNavigationView bottomNavigation;
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
        tvViewCount = findViewById(R.id.tv_view_count);
        ivImage = findViewById(R.id.iv_image);
        btnBuy = findViewById(R.id.btn_buy);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupBottomNavigation();

        long productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            finish();
            return;
        }

        loadProductDetail(productId);

        btnBuy.setOnClickListener(v -> showTradeInfoDialog());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProductDetailActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_category) {
                startActivity(new Intent(ProductDetailActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_publish) {
                startActivity(new Intent(ProductDetailActivity.this, PublishActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(ProductDetailActivity.this, OrderListActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(ProductDetailActivity.this, MyProfileActivity.class));
                return true;
            }
            return false;
        });
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
                        tvViewCount.setText("浏览量: " + product.getViewCount());
                        
                        if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                            String firstUrl = product.getImageUrls().split(",")[0];
                            Glide.with(ProductDetailActivity.this)
                                    .load(ApiClient.BASE_URL + firstUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivImage);
                        }
                        
                        // 加载卖家信息
                        loadSellerInfo(product.getSellerId());
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ProductDetailActivity.this, "获取商品详情失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
    
    private void loadSellerInfo(Long sellerId) {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "user/info/" + sellerId)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> tvSeller.setText("卖家ID: " + sellerId));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<User> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<User>>(){}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    seller = baseResp.getData();
                    runOnUiThread(() -> {
                        String sellerInfo = "卖家: " + seller.getUsername() + " | 信用分: " + seller.getCreditScore();
                        tvSeller.setText(sellerInfo);
                    });
                } else {
                    runOnUiThread(() -> tvSeller.setText("卖家ID: " + sellerId));
                }
            }
        });
    }

    private void showTradeInfoDialog() {
        if (product == null) return;

        // 创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("填写交易信息");

        // 创建自定义布局
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText etLocation = new EditText(this);
        etLocation.setHint("交易地点（如：食堂门口）");
        layout.addView(etLocation);

        EditText etTime = new EditText(this);
        etTime.setHint("交易时间（如：明天下午3点）");
        layout.addView(etTime);

        EditText etContact = new EditText(this);
        etContact.setHint("联系方式（手机号）");
        layout.addView(etContact);

        builder.setView(layout);

        builder.setPositiveButton("确认购买", (dialog, which) -> {
            String location = etLocation.getText().toString().trim();
            String time = etTime.getText().toString().trim();
            String contact = etContact.getText().toString().trim();

            if (location.isEmpty() || time.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "请填写完整交易信息", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: 将交易信息传递给后端
            createOrder(location, time, contact);
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void createOrder(String location, String time, String contact) {
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