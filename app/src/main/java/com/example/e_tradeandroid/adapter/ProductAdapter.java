package com.example.e_tradeandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.Product;
import com.example.e_tradeandroid.network.ApiClient;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final Context mContext;
    private final List<Product> mProductList;
    private OnProductClickListener mListener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public void setOnItemClickListener(OnProductClickListener listener) {
        this.mListener = listener;
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.mContext = context;
        this.mProductList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = mProductList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("¥" + product.getPrice());

        // 加载商品图片，处理中文文件名
        String imageUrl = null;
        if (product.getCoverImage() != null && !product.getCoverImage().isEmpty()) {
            imageUrl = product.getCoverImage();
        } else if (product.getMainImage() != null && !product.getMainImage().isEmpty()) {
            imageUrl = product.getMainImage();
        } else if (product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0);
        }
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // 如果已经是完整URL，直接使用；否则拼接BASE_URL
            String fullUrl = imageUrl.startsWith("http") ? imageUrl : ApiClient.BASE_URL + imageUrl;
            
            android.util.Log.d("ProductAdapter", "Loading image: " + fullUrl);
            
            Glide.with(mContext)
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.ivImage);
        } else {
            // 没有图片时显示默认占位图
            android.util.Log.w("ProductAdapter", "No image URL for product: " + product.getName());
            holder.ivImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProductList == null ? 0 : mProductList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }
}