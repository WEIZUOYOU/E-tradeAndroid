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
import com.example.e_tradeandroid.model.CreditDetailResponse;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;
import com.example.e_tradeandroid.network.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private TextView tvName, tvPrice, tvStock, tvDescription, tvSeller, tvViewCount;
    private ImageView ivImage;
    private Button btnBuy;
    private Product product;
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
                        tvPrice.setText("¥" + (product.getPrice() != null ? product.getPrice().toString() : "0"));
                        tvStock.setText("库存：" + product.getStock());
                        tvDescription.setText(product.getDescription());
                        tvViewCount.setText("浏览量: " + (product.getViewCount() != null ? product.getViewCount() : 0));

                        if (product.getMainImage() != null && !product.getMainImage().isEmpty()) {
                            Glide.with(ProductDetailActivity.this)
                                    .load(ApiClient.BASE_URL + product.getMainImage())
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivImage);
                        } else if (product.getImages() != null && !product.getImages().isEmpty()) {
                            String firstUrl = product.getImages().get(0);
                            Glide.with(ProductDetailActivity.this)
                                    .load(ApiClient.BASE_URL + firstUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivImage);
                        }

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
                .url(ApiClient.BASE_URL + "v1/trade/credit/" + sellerId)
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
                BaseResponse<CreditDetailResponse> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<CreditDetailResponse>>(){}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    CreditDetailResponse credit = baseResp.getData();
                    runOnUiThread(() -> {
                        String sellerInfo = "卖家: " + credit.getUsername()
                                + "  信用分: " + (credit.getCreditScore() != null ? credit.getCreditScore() : 0);
                        tvSeller.setText(sellerInfo);
                    });
                } else {
                    runOnUiThread(() -> tvSeller.setText("卖家ID: " + sellerId));
                }
            }
        });
    }

    private void showNewTradeDialog() {
        if (product == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("校园二手下单");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText etMeetTime = new EditText(this);
        etMeetTime.setHint("约定时间 格式：2024-05-01 12:00:00");
        layout.addView(etMeetTime);

        EditText etMeetLoc = new EditText(this);
        etMeetLoc.setHint("约定地点 例：学校二食堂门口");
        layout.addView(etMeetLoc);

        builder.setView(layout);

        builder.setPositiveButton("确认下单", (dialog, which) -> {
            String meetTime = etMeetTime.getText().toString().trim();
            String meetLoc = etMeetLoc.getText().toString().trim();

            if (meetTime.isEmpty() || meetLoc.isEmpty()) {
                Toast.makeText(this, "请填写面交时间和地点", Toast.LENGTH_SHORT).show();
                return;
            }

            CreateOrderRequest req = new CreateOrderRequest();
            req.setProductId(product.getId());
            req.setQuantity(1);
            req.setTradeType(1);
            req.setMeetingTime(meetTime);
            req.setMeetingLocation(meetLoc);
            req.setPayType(3);
            req.setAddressId(null);

            submitNewOrder(req);
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void submitNewOrder(CreateOrderRequest req) {
        RetrofitClient.getInstance()
                .getTradeApi()
                .createTradeOrder(req)
                .enqueue(new retrofit2.Callback<BaseResponse<CreateOrderResponse>>() {
                    @Override
                    public void onResponse(retrofit2.Call<BaseResponse<CreateOrderResponse>> call, retrofit2.Response<BaseResponse<CreateOrderResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            CreateOrderResponse data = response.body().getData();
                            runOnUiThread(() -> {
                                Toast.makeText(ProductDetailActivity.this,
                                        "下单成功！订单号：" + data.getOrderNo(),
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ProductDetailActivity.this, OrderListActivity.class);
                                startActivity(intent);
                            });
                        } else {
                            String msg = "下单失败";
                            if (response.body() != null) {
                                msg = response.body().getMsg() != null ? response.body().getMsg() : "下单失败";
                            }
                            final String finalMsg = msg;
                            runOnUiThread(() -> Toast.makeText(ProductDetailActivity.this, finalMsg, Toast.LENGTH_SHORT).show());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<BaseResponse<CreateOrderResponse>> call, Throwable t) {
                        runOnUiThread(() -> Toast.makeText(ProductDetailActivity.this,
                                "网络错误：" + t.getMessage(),
                                Toast.LENGTH_SHORT).show());
                    }
                });
    }
}