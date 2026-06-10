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
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.network.ApiClient;

import java.util.List;

public class TradeAdapter extends RecyclerView.Adapter<TradeAdapter.TradeViewHolder> {

    private Context context;
    private List<TradeInfo> tradeList;
    private OnTradeClickListener listener;

    public interface OnTradeClickListener {
        void onTradeClick(TradeInfo trade);
    }

    public TradeAdapter(Context context, List<TradeInfo> tradeList, OnTradeClickListener listener) {
        this.context = context;
        this.tradeList = tradeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trade_list, parent, false);
        return new TradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TradeViewHolder holder, int position) {
        TradeInfo trade = tradeList.get(position);
        holder.bind(trade);
    }

    @Override
    public int getItemCount() {
        return tradeList.size();
    }

    class TradeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvProductName, tvProductPrice, tvStatus, tvLocation, tvTime, tvOtherParty;

        public TradeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvLocation = itemView.findViewById(R.id.tv_location);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvOtherParty = itemView.findViewById(R.id.tv_other_party);
        }

        public void bind(TradeInfo trade) {
            // 商品信息
            tvProductName.setText(trade.getProductName());
            tvProductPrice.setText("￥" + trade.getProductPrice());

            // 商品图片
            if (trade.getProductImage() != null && !trade.getProductImage().isEmpty()) {
                String imageUrl = ApiClient.getImageUrl(trade.getProductImage());
                Glide.with(context).load(imageUrl).into(ivProduct);
            }

            // 状态
            tvStatus.setText(getStatusText(trade.getTradeStatus()));
            tvStatus.setBackgroundResource(getStatusBg(trade.getTradeStatus()));

            // 交易信息
            tvLocation.setText("交易地点: " + trade.getMeetingLocation());
            tvTime.setText("交易时间: " + trade.getMeetingTime());

            // 对方信息
            boolean isBuyer = ApiClient.getCurrentUserId() == trade.getBuyerId();
            String otherParty = isBuyer ? "卖家: " + trade.getSellerName() : "买家: " + trade.getBuyerName();
            tvOtherParty.setText(otherParty);

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTradeClick(trade);
                }
            });
        }

        private String getStatusText(Integer status) {
            if (status == null) return "未知";
            switch (status) {
                case 0: return "待卖家确认";
                case 1: return "待交易";
                case 2: return "卖家已确认";
                case 3: return "买家已确认";
                case 4: return "已完成";
                case 5: return "已取消";
                case 6: return "买家已评价";
                case 7: return "卖家已评价";
                case 8: return "双方已评价";
                default: return "未知";
            }
        }

        private int getStatusBg(Integer status) {
            if (status == null) return R.drawable.bg_btn_primary;
            switch (status) {
                case 0: return R.drawable.bg_status_tag;      // 橙色边框
                case 1: return R.drawable.bg_btn_success;      // 绿色
                case 2: return R.drawable.bg_btn_success;      // 绿色
                case 3: return R.drawable.bg_btn_success;      // 绿色
                case 4: return R.drawable.bg_btn_success;      // 绿色
                case 5: return R.drawable.bg_btn_danger;       // 红色
                case 6: return R.drawable.bg_btn_primary;      // 蓝色
                case 7: return R.drawable.bg_btn_primary;      // 蓝色
                case 8: return R.drawable.bg_btn_success;      // 绿色
                default: return R.drawable.bg_btn_primary;     // 蓝色（未知状态用蓝色）
            }
        }
    }
}