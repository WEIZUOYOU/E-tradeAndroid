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
    private android.net.Uri selectedImageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

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
        btn_select_image.setOnClickListener(v -> selectImage());
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

    // 选择图片
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            iv_preview.setImageURI(selectedImageUri);
            iv_preview.setVisibility(android.view.View.VISIBLE);
            Toast.makeText(this, "图片已选择", Toast.LENGTH_SHORT).show();
        }
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

        try {
            // 使用 multipart/form-data 格式提交
            okhttp3.MultipartBody.Builder bodyBuilder = new okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart("name", name)
                    .addFormDataPart("price", priceStr)
                    .addFormDataPart("stock", stockStr);
            
            if (!desc.isEmpty()) {
                bodyBuilder.addFormDataPart("description", desc);
            }
            
            // 如果有图片，添加图片文件
            if (selectedImageUri != null) {
                try {
                    java.io.InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    byte[] imageBytes = new byte[inputStream.available()];
                    inputStream.read(imageBytes);
                    inputStream.close();
                    
                    bodyBuilder.addFormDataPart("images", "image.jpg",
                        okhttp3.RequestBody.create(imageBytes, okhttp3.MediaType.parse("image/jpeg")));
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "图片读取失败", Toast.LENGTH_SHORT).show());
                    return;
                }
            }
            
            okhttp3.RequestBody requestBody = bodyBuilder.build();
            
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(ApiClient.BASE_URL + "api/product/publish")
                    .post(requestBody)
                    .build();
            
            ApiClient.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(PublishActivity.this, "发布失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "发布失败：" + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}