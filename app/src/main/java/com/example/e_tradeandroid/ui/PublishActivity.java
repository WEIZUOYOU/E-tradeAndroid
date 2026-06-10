package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Category;
import com.example.e_tradeandroid.network.ApiClient;
import com.example.e_tradeandroid.util.NavUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PublishActivity extends AppCompatActivity {
    // 图片上传区
    private FrameLayout fl_image_upload;
    private LinearLayout ll_upload_hint, ll_image_list;
    private HorizontalScrollView hsv_image_preview;
    
    // 表单字段
    private EditText et_name, et_price, et_stock, et_description;
    private TextView tv_title_counter;
    private Spinner spinner_category;
    
    // 底部按钮
    private Button btn_publish;
    private BottomNavigationView bottom_nav;

    private final Gson gson = new Gson();
    private List<android.net.Uri> selectedImageUris = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;
    
    // 分类相关
    private List<Category> categoryList = new ArrayList<>();
    private Long selectedCategoryId = 1L; // 默认分类ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        // 绑定所有控件
        initViews();
        
        // 设置点击事件
        initListeners();
    }
    
    private void initViews() {
        // 图片上传区
        fl_image_upload = findViewById(R.id.fl_image_upload);
        ll_upload_hint = findViewById(R.id.ll_upload_hint);
        ll_image_list = findViewById(R.id.ll_image_list);
        hsv_image_preview = findViewById(R.id.hsv_image_preview);
        
        // 表单字段
        et_name = findViewById(R.id.et_name);
        et_price = findViewById(R.id.et_price);
        et_stock = findViewById(R.id.et_stock);
        et_description = findViewById(R.id.et_description);
        tv_title_counter = findViewById(R.id.tv_title_counter);
        spinner_category = findViewById(R.id.spinner_category);
        
        // 底部按钮
        btn_publish = findViewById(R.id.btn_publish);
        bottom_nav = findViewById(R.id.bottom_nav);
    }
    
    private void initListeners() {
        // 加载分类列表
        loadCategories();
        
        // 图片上传区点击
        fl_image_upload.setOnClickListener(v -> selectImage());
        
        // 标题字符计数
        et_name.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tv_title_counter.setText(s.length() + "/50");
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        // 发布按钮
        btn_publish.setOnClickListener(v -> submitPublish());
        
        // 底部导航栏
        bottom_nav.setSelectedItemId(R.id.nav_publish);
        bottom_nav.setOnItemSelectedListener(item -> {
            return NavUtils.handleNavClick(this, item.getItemId());
        });
    }
    
    // 加载分类列表
    private void loadCategories() {
        // 调用后端接口获取一级分类列表
        ApiClient.get("api/category/list", new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(PublishActivity.this, "加载分类失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // 使用默认分类（降级方案）
                    setupDefaultCategory();
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                String res = response.body().string();
                BaseResponse<List<Category>> resp = gson.fromJson(res, new TypeToken<BaseResponse<List<Category>>>(){}.getType());
                
                runOnUiThread(() -> {
                    if (resp.isSuccess() && resp.getData() != null && !resp.getData().isEmpty()) {
                        categoryList = resp.getData();
                        setupCategorySpinner();
                    } else {
                        // 如果返回空列表，使用默认分类
                        setupDefaultCategory();
                    }
                });
            }
        });
    }
    
    // 设置默认分类（硬编码7个一级分类）
    private void setupDefaultCategory() {
        categoryList.clear();
        
        // 添加7个一级分类
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("教材与学习资料");
        categoryList.add(cat1);
        
        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("数码产品与配件");
        categoryList.add(cat2);
        
        Category cat3 = new Category();
        cat3.setId(3L);
        cat3.setName("生活电器与宿舍用品");
        categoryList.add(cat3);
        
        Category cat4 = new Category();
        cat4.setId(4L);
        cat4.setName("运动与户外");
        categoryList.add(cat4);
        
        Category cat5 = new Category();
        cat5.setId(5L);
        cat5.setName("服饰与配饰");
        categoryList.add(cat5);
        
        Category cat6 = new Category();
        cat6.setId(6L);
        cat6.setName("美妆与个护");
        categoryList.add(cat6);
        
        Category cat7 = new Category();
        cat7.setId(7L);
        cat7.setName("其他/闲置杂物");
        categoryList.add(cat7);
        
        setupCategorySpinner();
    }
    
    // 设置分类选择器
    private void setupCategorySpinner() {
        if (categoryList.isEmpty()) {
            return;
        }
        
        // 创建分类名称列表
        List<String> categoryNames = new ArrayList<>();
        for (Category category : categoryList) {
            categoryNames.add(category.getName());
        }
        
        // 创建 ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            categoryNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_category.setAdapter(adapter);
        
        // 设置默认选中第一个分类
        if (!categoryList.isEmpty()) {
            selectedCategoryId = categoryList.get(0).getId();
        }
        
        // 监听分类选择变化
        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < categoryList.size()) {
                    selectedCategoryId = categoryList.get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 不做处理
            }
        });
    }

    // 选择图片
    private void selectImage() {
        if (selectedImageUris.size() >= 6) {
            Toast.makeText(this, "最多只能上传6张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            android.net.Uri imageUri = data.getData();
            selectedImageUris.add(imageUri);
            
            // 显示图片预览
            updateImagePreview();
            
            Toast.makeText(this, "图片已添加（" + selectedImageUris.size() + "/6）", Toast.LENGTH_SHORT).show();
        }
    }
    
    // 更新图片预览显示
    private void updateImagePreview() {
        if (selectedImageUris.isEmpty()) {
            ll_upload_hint.setVisibility(View.VISIBLE);
            hsv_image_preview.setVisibility(View.GONE);
            return;
        }
        
        ll_upload_hint.setVisibility(View.GONE);
        hsv_image_preview.setVisibility(View.VISIBLE);
        
        // 清空现有图片
        ll_image_list.removeAllViews();
        
        // 添加所有图片缩略图
        for (int i = 0; i < selectedImageUris.size(); i++) {
            addImageThumbnail(i);
        }
        
        // 如果未达到上限，添加“+”按钮
        if (selectedImageUris.size() < 6) {
            addAddImageButton();
        }
    }
    
    // 添加图片缩略图
    private void addImageThumbnail(final int index) {
        FrameLayout imageContainer = new FrameLayout(this);
        int size = dpToPx(80);
        imageContainer.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        imageContainer.setPadding(dpToPx(4), 0, dpToPx(4), 0);
        
        // 图片
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackgroundResource(R.drawable.bg_card);
        
        // 设置圆角
        imageView.setClipToOutline(true);
        android.graphics.drawable.GradientDrawable background = new android.graphics.drawable.GradientDrawable();
        background.setColor(getResources().getColor(R.color.background_white));
        background.setCornerRadius(dpToPx(8));
        imageView.setBackground(background);
        
        // 加载图片
        try {
            Bitmap bitmap = decodeSampledBitmapFromUri(selectedImageUris.get(index), 200, 200);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 删除按钮
        ImageView deleteBtn = new ImageView(this);
        int deleteSize = dpToPx(24);
        FrameLayout.LayoutParams deleteParams = new FrameLayout.LayoutParams(deleteSize, deleteSize);
        deleteParams.gravity = android.view.Gravity.TOP | android.view.Gravity.END;
        deleteParams.setMargins(0, dpToPx(4), dpToPx(4), 0);
        deleteBtn.setLayoutParams(deleteParams);
        deleteBtn.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        deleteBtn.setBackgroundResource(R.drawable.bg_pill_button);
        deleteBtn.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        
        final int finalIndex = index;
        deleteBtn.setOnClickListener(v -> {
            selectedImageUris.remove(finalIndex);
            updateImagePreview();
        });
        
        imageContainer.addView(imageView);
        imageContainer.addView(deleteBtn);
        ll_image_list.addView(imageContainer);
    }
    
    // 添加“+”按钮
    private void addAddImageButton() {
        Button addBtn = new Button(this);
        int size = dpToPx(80);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        addBtn.setLayoutParams(params);
        addBtn.setText("+");
        addBtn.setTextSize(24);
        addBtn.setTextColor(getResources().getColor(R.color.primary_green));
        addBtn.setBackgroundResource(R.drawable.bg_image_upload);
        addBtn.setPadding(0, 0, 0, 0);
        
        addBtn.setOnClickListener(v -> selectImage());
        
        ll_image_list.addView(addBtn);
    }
    
    // dp转px
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    // 从Uri解码并压缩图片
    private android.graphics.Bitmap decodeSampledBitmapFromUri(android.net.Uri uri, int reqWidth, int reqHeight) {
        try {
            // 首先获取图片尺寸
            android.content.res.AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(uri, "r");
            if (afd == null) return null;
            
            java.io.InputStream inputStream = afd.createInputStream();
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            android.graphics.BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            afd.close();
            
            // 计算压缩比例
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            
            // 解码并返回压缩后的图片
            afd = getContentResolver().openAssetFileDescriptor(uri, "r");
            if (afd == null) return null;
            
            inputStream = afd.createInputStream();
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            afd.close();
            
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // 计算采样率
    private int calculateInSampleSize(android.graphics.BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }


    // 发布商品逻辑
    private void submitPublish() {
        String name = et_name.getText().toString().trim();
        String priceStr = et_price.getText().toString().trim();
        String stockStr = et_stock.getText().toString().trim();
        String desc = et_description.getText().toString().trim();

        // 表单验证
        if (name.isEmpty()) {
            Toast.makeText(this, "请输入商品名称", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (name.length() > 50) {
            Toast.makeText(this, "商品名称不能超过50个字符", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "请输入商品价格", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            double price = Double.parseDouble(priceStr);
            if (price <= 0) {
                Toast.makeText(this, "价格必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的价格", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (stockStr.isEmpty()) {
            Toast.makeText(this, "请输入库存数量", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            int stock = Integer.parseInt(stockStr);
            if (stock < 1) {
                Toast.makeText(this, "库存至少为1", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的库存数量", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (!desc.isEmpty() && desc.length() > 500) {
            Toast.makeText(this, "商品描述不能超过500个字符", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedImageUris.isEmpty()) {
            Toast.makeText(this, "请至少上传一张图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedImageUris.size() > 9) {
            Toast.makeText(this, "最多只能上传9张图片", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 根据API文档，需要添加 categoryId 字段（必填）
            // 使用用户选择的分类ID
            Long categoryId = selectedCategoryId;
            
            // 验证分类ID是否有效
            if (categoryId == null || categoryId <= 0) {
                Toast.makeText(this, "请选择商品分类", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 使用 multipart/form-data 格式提交
            okhttp3.MultipartBody.Builder bodyBuilder = new okhttp3.MultipartBody.Builder()
                    .setType(okhttp3.MultipartBody.FORM)
                    .addFormDataPart("categoryId", String.valueOf(categoryId))
                    .addFormDataPart("name", name)
                    .addFormDataPart("price", priceStr)
                    .addFormDataPart("stock", stockStr);
            
            if (!desc.isEmpty()) {
                bodyBuilder.addFormDataPart("description", desc);
            }
            
            // 添加所有图片
            for (int i = 0; i < selectedImageUris.size(); i++) {
                try {
                    android.net.Uri imageUri = selectedImageUris.get(i);
                    
                    // 获取文件名
                    String fileName = getFileName(imageUri);
                    
                    // 读取图片数据
                    java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    if (inputStream == null) {
                        continue;
                    }
                    
                    byte[] imageBytes = readInputStream(inputStream);
                    inputStream.close();
                    
                    // 确定MIME类型
                    String mimeType = getContentResolver().getType(imageUri);
                    if (mimeType == null) {
                        mimeType = "image/jpeg"; // 默认类型
                    }
                    
                    bodyBuilder.addFormDataPart("images", fileName,
                        okhttp3.RequestBody.create(imageBytes, okhttp3.MediaType.parse(mimeType)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            okhttp3.RequestBody requestBody = bodyBuilder.build();
            
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(ApiClient.BASE_URL + "/api/product/publish")
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
                    // 根据API文档，发布商品返回的是商品ID (Long类型)
                    BaseResponse<Long> resp = gson.fromJson(res, new TypeToken<BaseResponse<Long>>(){}.getType());
                    runOnUiThread(() -> {
                        if (resp.isSuccess() && resp.getData() != null) {
                            Long productId = resp.getData();
                            Toast.makeText(PublishActivity.this, "发布成功", Toast.LENGTH_SHORT).show();
                            
                            // 跳转到商品详情页
                            Intent intent = new Intent(PublishActivity.this, ProductDetailActivity.class);
                            intent.putExtra("product_id", productId);
                            startActivity(intent);
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
    
    // 从Uri获取文件名
    private String getFileName(android.net.Uri uri) {
        String fileName = "image.jpg";
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new java.io.File(uri.getPath()).getName();
        }
        return fileName;
    }
    
    // 读取输入流为字节数组
    private byte[] readInputStream(java.io.InputStream inputStream) throws java.io.IOException {
        java.io.ByteArrayOutputStream byteArrayOutputStream = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }
}