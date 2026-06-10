package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.ProductAdapter;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.Category;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;
import com.example.e_tradeandroid.util.NavUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private SearchView searchView;
    private RecyclerView layoutCategories; // 改为RecyclerView
    private BottomNavigationView bottomNavigation;
    private Gson gson = new Gson();
    private List<Product> productList = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private String searchKeyword = "";
    private Long selectedCategoryId = null;
    private boolean isSearchMode = false;
    
    // 分页相关
    private int currentPage = 1;
    private int pageSize = 20;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ApiClient 已在 AppContext（Application）中初始化，无需重复初始化
        // 重复初始化会导致 OkHttpClient 被重建，Cookie 可能丢失

        setContentView(R.layout.activity_main);

        initViews();
        setupSearchView();
        setupBottomNav();
        setupDefaultCategories();
        loadProducts();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        searchView = findViewById(R.id.search_view);
        layoutCategories = findViewById(R.id.layout_categories);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        // 分类使用 FlexboxLayoutManager，自动换行，标签完整显示
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(this);
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);
        flexboxLayoutManager.setJustifyContent(JustifyContent.FLEX_START);
        layoutCategories.setLayoutManager(flexboxLayoutManager);

        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1;
            hasMoreData = true;
            if (isSearchMode) {
                searchProducts();
            } else {
                loadProducts();
            }
        });
        
        // 添加滚动监听，实现上拉加载更多
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                if (dy > 0) { // 向下滚动
                    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                        
                        // 判断是否滑动到底部
                        if (!isLoading && hasMoreData && 
                            (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                            firstVisibleItemPosition >= 0) {
                            loadMoreProducts();
                        }
                    }
                }
            }
        });
    }

    private void loadCategories() {
        Request request = new Request.Builder()
                .url(ApiClient.BASE_URL + "/api/category/list")
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> setupDefaultCategories());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    try {
                        BaseResponse<List<Category>> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<List<Category>>>(){}.getType());
                        if (baseResp.isSuccess() && baseResp.getData() != null && !baseResp.getData().isEmpty()) {
                            categories.clear();
                            categories.addAll(baseResp.getData());
                            setupCategoriesView();
                        } else {
                            setupDefaultCategories();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        setupDefaultCategories();
                    }
                });
            }
        });
    }

    private void setupDefaultCategories() {
        categories.clear();
        String[] defaultNames = {"全部", "教材", "电子产品", "生活用品", "运动器材", "服装", "其他"};
        for (int i = 0; i < defaultNames.length; i++) {
            Category c = new Category();
            c.setId(i == 0 ? 0L : (long) i);
            c.setName(defaultNames[i]);
            categories.add(c);
        }
        setupCategoriesView();
    }

    private void setupCategoriesView() {
        // 创建分类Adapter
        CategoryAdapter adapter = new CategoryAdapter(categories);
        layoutCategories.setAdapter(adapter);
    }
    
    // 分类Adapter内部类 - 使用 Material Chip
    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
        private List<Category> categories;
        
        public CategoryAdapter(List<Category> categories) {
            this.categories = categories;
        }
        
        @NonNull
        @Override
        public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Chip chip = (Chip) LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(chip);
        }
        
        @Override
        public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.chip.setText(category.getName());
            
            // 设置选中状态（selector 会自动处理背景和文字颜色）
            boolean isSelected = (selectedCategoryId != null && selectedCategoryId.equals(category.getId()))
                    || (selectedCategoryId == null && category.getId() != null && category.getId() == 0L);
            holder.chip.setChecked(isSelected);
            
            holder.chip.setOnClickListener(v -> {
                // 取消其他选中状态
                notifyDataSetChanged();
                
                if (category.getId() != null && category.getId() == 0L) {
                    // "全部"分类，清空筛选
                    selectedCategoryId = null;
                    searchKeyword = "";
                    searchView.setQuery("", false);
                    currentPage = 1;
                    hasMoreData = true;
                    loadProducts(); // 使用普通列表接口
                } else {
                    // 选择了具体分类
                    selectedCategoryId = category.getId();
                    searchKeyword = "";
                    searchView.setQuery("", false);
                    currentPage = 1;
                    hasMoreData = true;
                    searchProducts(); // 使用搜索接口，带categoryId参数
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return categories == null ? 0 : categories.size();
        }
        
        class CategoryViewHolder extends RecyclerView.ViewHolder {
            Chip chip;
            
            public CategoryViewHolder(@NonNull View itemView) {
                super(itemView);
                this.chip = (Chip) itemView;
            }
        }
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchKeyword = query;
                selectedCategoryId = null;
                // 重置分页
                currentPage = 1;
                hasMoreData = true;
                searchProducts();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void setupBottomNav() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            return NavUtils.handleNavClick(this, item.getItemId());
        });
    }

    private void loadProducts() {
        isSearchMode = false;
        isLoading = true;
        
        if (currentPage == 1) {
            swipeRefresh.setRefreshing(true);
            progressBar.setVisibility(View.VISIBLE);
        }

        String url = ApiClient.BASE_URL + "/api/product/list?page=" + currentPage + "&size=" + pageSize;
        
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    Toast.makeText(MainActivity.this, "加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    
                    try {
                        BaseResponse<List<Product>> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<List<Product>>>(){}.getType());
                        if (baseResp.isSuccess() && baseResp.getData() != null) {
                            List<Product> newProducts = baseResp.getData();
                            
                            if (currentPage == 1) {
                                // 第一页，清空列表
                                productList.clear();
                                productList.addAll(newProducts);
                            } else {
                                // 加载更多，追加到列表
                                productList.addAll(newProducts);
                            }
                            
                            // 判断是否还有更多数据
                            hasMoreData = newProducts.size() >= pageSize;
                            
                            updateAdapter();
                            
                            if (!hasMoreData && !productList.isEmpty()) {
                                Toast.makeText(MainActivity.this, "没有更多商品了", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "获取商品列表失败", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "数据解析失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    
    // 加载更多商品
    private void loadMoreProducts() {
        if (isLoading || !hasMoreData) {
            return;
        }
        
        currentPage++;
        
        if (isSearchMode) {
            searchProducts();
        } else {
            loadProducts();
        }
    }

    private void searchProducts() {
        isSearchMode = true;
        isLoading = true;
        
        if (currentPage == 1) {
            swipeRefresh.setRefreshing(true);
            progressBar.setVisibility(View.VISIBLE);
        }

        StringBuilder url = new StringBuilder(ApiClient.BASE_URL + "/api/product/search?page=" + currentPage + "&size=" + pageSize);
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            url.append("&keyword=").append(searchKeyword);
        }
        if (selectedCategoryId != null) {
            url.append("&categoryId=").append(selectedCategoryId);
        }

        Request request = new Request.Builder()
                .url(url.toString())
                .get()
                .build();

        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    Toast.makeText(MainActivity.this, "搜索失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                    
                    try {
                        BaseResponse<List<Product>> baseResp = gson.fromJson(respBody, new TypeToken<BaseResponse<List<Product>>>(){}.getType());
                        if (baseResp.isSuccess() && baseResp.getData() != null) {
                            List<Product> newProducts = baseResp.getData();
                            
                            if (currentPage == 1) {
                                // 第一页，清空列表
                                productList.clear();
                                productList.addAll(newProducts);
                            } else {
                                // 加载更多，追加到列表
                                productList.addAll(newProducts);
                            }
                            
                            // 判断是否还有更多数据
                            hasMoreData = newProducts.size() >= pageSize;
                            
                            updateAdapter();
                            
                            if (currentPage == 1 && productList.isEmpty()) {
                                Toast.makeText(MainActivity.this, "搜索无结果", Toast.LENGTH_SHORT).show();
                            } else if (!hasMoreData && !productList.isEmpty()) {
                                Toast.makeText(MainActivity.this, "没有更多商品了", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "搜索无结果", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "数据解析失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateAdapter() {
        ProductAdapter adapter = new ProductAdapter(this, productList);
        adapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }
}
