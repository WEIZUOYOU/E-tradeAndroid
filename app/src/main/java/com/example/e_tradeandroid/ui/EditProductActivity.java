package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 编辑商品页面
 */
public class EditProductActivity extends AppCompatActivity {
    private EditText etName, etPrice, etStock, etDescription;
    private Button btnSelectImage, btnUpdate, btnOffshelf, btnDelete;
    private ImageView ivPreview, ivBack;
    private Uri selectedImageUri;
    private long productId;
    private Product product;
    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        productId = getIntent().getLongExtra("product_id", -1);
        if (productId == -1) {
            Toast.makeText(this, "商品不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadProductDetail();

        btnSelectImage.setOnClickListener(v -> selectImage());
        btnUpdate.setOnClickListener(v -> updateProduct());
        btnOffshelf.setOnClickListener(v -> showOffshelfDialog());
        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etPrice = findViewById(R.id.et_price);
        etStock = findViewById(R.id.et_stock);
        etDescription = findViewById(R.id.et_description);
        btnSelectImage = findViewById(R.id.btn_select_image);
        btnUpdate = findViewById(R.id.btn_update);
        btnOffshelf = findViewById(R.id.btn_offshelf);
        btnDelete = findViewById(R.id.btn_delete);
        ivPreview = findViewById(R.id.iv_preview);
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());
    }

    private void loadProductDetail() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/api/product/detail/" + productId)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(EditProductActivity.this, "加载失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Product> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Product>>(){}.getType());
                if (baseResp.isSuccess() && baseResp.getData() != null) {
                    product = baseResp.getData();
                    runOnUiThread(() -> {
                        etName.setText(product.getName());
                        // 修复：double 类型直接转字符串，不用判空
                        etPrice.setText(String.valueOf(product.getPrice()));
                        etStock.setText(String.valueOf(product.getStock()));
                        etDescription.setText(product.getDescription());

                        if (product.getMainImage() != null && !product.getMainImage().isEmpty()) {
                            String imageUrl = ApiClient.getImageUrl(product.getMainImage());
                            Glide.with(EditProductActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivPreview);
                            ivPreview.setVisibility(View.VISIBLE);
                        } else if (product.getImages() != null && !product.getImages().isEmpty()) {
                            String imageUrl = ApiClient.getImageUrl(product.getImages().get(0));
                            Glide.with(EditProductActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(ivPreview);
                            ivPreview.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });
    }

    private void selectImage() {
        // 简化：提示用户暂不支持更换图片
        Toast.makeText(this, "暂不支持更换图片", Toast.LENGTH_SHORT).show();
    }

    private void updateProduct() {
        String name = etName.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String stock = etStock.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || price.isEmpty() || stock.isEmpty()) {
            Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject updateData = new JSONObject();
            updateData.put("name", name);
            updateData.put("price", Double.parseDouble(price));
            updateData.put("stock", Integer.parseInt(stock));
            updateData.put("description", description);
            updateData.put("categoryId", product.getCategoryId() != null ? product.getCategoryId() : 0);

            RequestBody body = RequestBody.create(
                    updateData.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(ApiClient.BASE_URL + "/api/product/update/" + productId)
                    .post(body)
                    .build();

            ApiClient.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(EditProductActivity.this, "更新失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String respBody = response.body().string();
                    BaseResponse<Void> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Void>>(){}.getType());
                    runOnUiThread(() -> {
                        if (baseResp.isSuccess()) {
                            Toast.makeText(EditProductActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(EditProductActivity.this, "更新失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "数据格式错误", Toast.LENGTH_SHORT).show());
        }
    }

    private void showOffshelfDialog() {
        new AlertDialog.Builder(this)
                .setTitle("下架商品")
                .setMessage("确定要下架这个商品吗？")
                .setPositiveButton("确定", (dialog, which) -> offshelfProduct())
                .setNegativeButton("取消", null)
                .show();
    }

    private void offshelfProduct() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/api/product/offline/" + productId)
                .put(RequestBody.create("", MediaType.parse("application/json; charset=utf-8")))
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(EditProductActivity.this, "下架失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Void> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Void>>(){}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        Toast.makeText(EditProductActivity.this, "商品已下架", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProductActivity.this, "下架失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("删除商品")
                .setMessage("确定要删除这个商品吗？此操作不可恢复！")
                .setPositiveButton("确定", (dialog, which) -> deleteProduct())
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteProduct() {
        ApiClient.delete("api/product/delete/" + productId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(EditProductActivity.this, "删除失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<Void> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<Void>>(){}.getType());
                runOnUiThread(() -> {
                    if (baseResp.isSuccess()) {
                        Toast.makeText(EditProductActivity.this, "商品已删除", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProductActivity.this, "删除失败：" + baseResp.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}