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
    // 顶部导航
    private ImageView iv_back;
    
    // 图片轮播
    private androidx.viewpager2.widget.ViewPager2 view_pager_images;
    private LinearLayout ll_page_indicator;
    
    // 商品信息
    private TextView tv_name, tv_price, tv_stock, tv_description, tv_view_count, tv_publish_time;
    private TextView tv_bargain_tag;
    private LinearLayout ll_tags;
    
    // 卖家信息
    private ImageView iv_seller_avatar;
    private TextView tv_seller_name, tv_seller_credit;
    private Button btn_chat;
    
    // 底部操作
    private ImageView iv_favorite, iv_share;
    private Button btn_buy;
    
    private Product product;
    private User seller;
    private final Gson gson = new Gson();
    private boolean isFavorite = false; // 收藏状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // 绑定所有控件
        initViews();
        
        // 设置点击事件
        initListeners();
        
        // 获取商品ID并加载详情
        long productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            finish();
            return;
        }

        loadProductDetail(productId);
    }
    
    private void initViews() {
        // 顶部导航
        iv_back = findViewById(R.id.iv_back);
        
        // 图片轮播
        view_pager_images = findViewById(R.id.view_pager_images);
        ll_page_indicator = findViewById(R.id.ll_page_indicator);
        
        // 商品信息
        tv_name = findViewById(R.id.tv_name);
        tv_price = findViewById(R.id.tv_price);
        tv_stock = findViewById(R.id.tv_stock);
        tv_description = findViewById(R.id.tv_description);
        tv_view_count = findViewById(R.id.tv_view_count);
        tv_publish_time = findViewById(R.id.tv_publish_time);
        tv_bargain_tag = findViewById(R.id.tv_bargain_tag);
        ll_tags = findViewById(R.id.ll_tags);
        
        // 卖家信息
        iv_seller_avatar = findViewById(R.id.iv_seller_avatar);
        tv_seller_name = findViewById(R.id.tv_seller_name);
        tv_seller_credit = findViewById(R.id.tv_seller_credit);
        btn_chat = findViewById(R.id.btn_chat);
        
        // 底部操作
        iv_favorite = findViewById(R.id.iv_favorite);
        iv_share = findViewById(R.id.iv_share);
        btn_buy = findViewById(R.id.btn_buy);
    }
    
    private void initListeners() {
        // 返回按钮
        iv_back.setOnClickListener(v -> finish());
        
        // 聊一聊按钮
        btn_chat.setOnClickListener(v -> startChat());
        
        // 收藏按钮
        iv_favorite.setOnClickListener(v -> toggleFavorite());
        
        // 分享按钮
        iv_share.setOnClickListener(v -> shareProduct());
        
        // 购买按钮
        btn_buy.setOnClickListener(v -> showNewTradeDialog());
    }

    private void loadProductDetail(long productId) {
        ApiClient.get("api/product/detail/" + productId, new Callback() {
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
                        // 设置商品信息
                        tv_name.setText(product.getName());
                        tv_price.setText("￥" + product.getPrice());
                        tv_stock.setText("库存：" + product.getStock());
                        tv_description.setText(product.getDescription() != null ? product.getDescription() : "暂无描述");
                        tv_view_count.setText("浏览量: " + (product.getViewCount() != null ? product.getViewCount() : 0));
                        
                        // 设置发布时间
                        if (product.getCreateTime() != null) {
                            String timeStr = product.getCreateTime().toString();
                            if (timeStr.length() > 10) {
                                timeStr = timeStr.substring(0, 10);
                            }
                            tv_publish_time.setText("发布于 " + timeStr);
                        }
                        
                        // 加载图片轮播
                        loadImages(product.getImages());
                        
                        // 加载卖家信息
                        loadSellerInfo(product.getSellerId());
                    });
                }
            }
        });
    }

    private void loadSellerInfo(Long sellerId) {
        // 后端商品详情接口已返回sellerName、sellerAvatar、sellerIsAuth
        runOnUiThread(() -> {
            // 显示卖家名称
            if (product.getSellerName() != null && !product.getSellerName().isEmpty()) {
                tv_seller_name.setText(product.getSellerName());
            } else {
                tv_seller_name.setText("卖家ID: " + sellerId);
            }
            
            // 显示认证状态
            if (product.getSellerIsAuth() != null && product.getSellerIsAuth() == 1) {
                tv_seller_credit.setText("已实名认证");
            } else {
                tv_seller_credit.setText("未实名认证");
            }
            
            // 加载卖家头像
            if (product.getSellerAvatar() != null && !product.getSellerAvatar().isEmpty()) {
                String avatarUrl = product.getSellerAvatar().startsWith("http") 
                    ? product.getSellerAvatar() 
                    : ApiClient.BASE_URL + product.getSellerAvatar();
                Glide.with(ProductDetailActivity.this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(iv_seller_avatar);
            }
        });
    }
    
    // 加载图片轮播
    private void loadImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        
        // TODO: 实现ViewPager2图片轮播
        // 这里简化处理，只显示第一张图片
        if (!images.isEmpty()) {
            Glide.with(this)
                    .load(ApiClient.BASE_URL + images.get(0))
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(new android.widget.ImageView(this)); // 临时处理
        }
    }
    
    // 开始聊天
    private void startChat() {
        if (product == null) {
            Toast.makeText(this, "商品加载中，请稍后", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 检查是否是自己的商品
        long currentUserId = ApiClient.getCurrentUserId();
        if (product.getSellerId() != null && product.getSellerId() == currentUserId) {
            Toast.makeText(this, "不能和自己聊天", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 跳转到聊天界面，使用商品的sellerId
        Intent intent = new Intent(ProductDetailActivity.this, ChatActivity.class);
        intent.putExtra("productId", product.getId());
        intent.putExtra("sellerId", product.getSellerId());
        startActivity(intent);
    }
    
    // 切换收藏状态
    private void toggleFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            iv_favorite.setImageResource(android.R.drawable.btn_star_big_on);
            Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
        } else {
            iv_favorite.setImageResource(android.R.drawable.btn_star_big_off);
            Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
        }
        // TODO: 调用API保存收藏状态
    }
    
    // 分享商品
    private void shareProduct() {
        if (product == null) return;
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享商品");
        shareIntent.putExtra(Intent.EXTRA_TEXT, 
            "推荐一个商品：" + product.getName() + "\n价格：￥" + product.getPrice() + 
            "\n详情：" + (product.getDescription() != null ? product.getDescription() : ""));
        startActivity(Intent.createChooser(shareIntent, "分享到"));
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