package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.e_tradeandroid.model.CreateOrderResponse;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;
import com.example.e_tradeandroid.model.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvStock, tvDescription, tvSeller, tvViewCount;
    private ImageView ivImage;
    private Button btnBuy;
    private Product product;
    private User seller;
    private BottomNavigationView bottomNavigation;
    private final Gson gson = new Gson();

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

        // 修复：直接用 long 接收，不转 int
        long productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            finish();
            return;
        }

        loadProductDetail(productId);
        btnBuy.setOnClickListener(v -> showNewTradeDialog());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_publish) {
                startActivity(new Intent(this, PublishActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, OrderListActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_messages) {
                startActivity(new Intent(this, MessageListActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, MyProfileActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadProductDetail(long productId) {
        ApiClient.get("product/detail/" + productId, new Callback() {
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
                        tvPrice.setText("¥" + product.getPrice());
                        tvStock.setText("库存：" + product.getStock());
                        tvDescription.setText(product.getDescription());
                        tvViewCount.setText("浏览量: " + (product.getViewCount() != null ? product.getViewCount() : 0));

                        if (product.getMainImage() != null && !product.getMainImage().isEmpty()) {
                            Glide.with(ProductDetailActivity.this)
                                    .load(ApiClient.BASE_URL + product.getMainImage())
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivImage);
                        } else if (product.getImages() != null && !product.getImages().isEmpty()) {
                            Glide.with(ProductDetailActivity.this)
                                    .load(ApiClient.BASE_URL + product.getImages().get(0))
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivImage);
                        }
                        loadSellerInfo(product.getSellerId());
                    });
                }
            }
        });
    }

    private void loadSellerInfo(Long sellerId) {
        ApiClient.get("user/info/" + sellerId, new Callback() {
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
                    runOnUiThread(() -> tvSeller.setText("卖家: " + seller.getUsername()));
                }
            }
        });
    }

    private void showNewTradeDialog() {
        if (product == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("填写交易信息");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50,40,50,10);

        EditText etTime = new EditText(this);
        etTime.setHint("交易时间 例：2025-05-20 15:00");
        layout.addView(etTime);

        EditText etLocation = new EditText(this);
        etLocation.setHint("交易地点 例：二食堂门口");
        layout.addView(etLocation);

        builder.setView(layout);

        builder.setPositiveButton("确认下单", (dialog, which) -> {
            String time = etTime.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            if (time.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "请填写完整", Toast.LENGTH_SHORT).show();
                return;
            }

            CreateOrderRequest req = new CreateOrderRequest();
            req.setProductId(product.getId());
            req.setQuantity(1);
            req.setTradeType(1);
            req.setMeetingTime(time);
            req.setMeetingLocation(location);
            req.setPayType(3);
            req.setAddressId(null);

            createOrder(req);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void createOrder(CreateOrderRequest req) {
        String json = gson.toJson(req);

        ApiClient.post("api/v1/trade/order", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ProductDetailActivity.this, "下单失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                BaseResponse<CreateOrderResponse> resp = gson.fromJson(body, new TypeToken<BaseResponse<CreateOrderResponse>>() {}.getType());

                runOnUiThread(() -> {
                    if (resp.isSuccess()) {
                        Toast.makeText(ProductDetailActivity.this, "下单成功！", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ProductDetailActivity.this, OrderListActivity.class));
                    } else {
                        Toast.makeText(ProductDetailActivity.this, resp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}