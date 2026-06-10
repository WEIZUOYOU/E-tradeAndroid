package com.example.e_tradeandroid.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.network.ApiClient;
import com.example.e_tradeandroid.ui.TradeInfoActivity;

import java.util.List;

/**
 * 订单列表适配器
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private List<TradeInfo> orderList;
    private boolean isActive; // 是否是进行中的订单

    public OrderAdapter(Context context, List<TradeInfo> orderList, boolean isActive) {
        this.context = context;
        this.orderList = orderList;
        this.isActive = isActive;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TradeInfo order = orderList.get(position);
        
        // 交易号
        holder.tvTradeNo.setText("交易号：" + order.getTradeNo());
        
        // 状态
        holder.tvStatus.setText(getStatusText(order.getTradeStatus()));
        holder.tvStatus.setTextColor(getStatusColor(order.getTradeStatus()));
        
        // 商品名称
        holder.tvProductName.setText(order.getProductName());
        
        // 价格
        holder.tvPrice.setText("¥" + order.getProductPrice());
        
        // 时间
        holder.tvTime.setText(formatTime(order.getMeetingTime()));
        
        // 商品图片
        if (order.getProductImage() != null && !order.getProductImage().isEmpty()) {
            String imageUrl = ApiClient.getImageUrl(order.getProductImage());
            Glide.with(context).load(imageUrl).into(holder.ivProduct);
        }
        
        // 操作按钮
        holder.btnAction.setOnClickListener(v -> {
            Intent intent = new Intent(context, TradeInfoActivity.class);
            intent.putExtra("tradeId", order.getId());
            intent.putExtra("productId", order.getProductId());
            intent.putExtra("sellerId", order.getSellerId());
            context.startActivity(intent);
        });
        
        // 根据状态设置按钮文字
        setButtonText(holder.btnAction, order.getTradeStatus());
    }

    /**
     * 获取状态文字
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待卖家确认";
            case 1: return "待线下交易";
            case 2: return "买家已确认完成";
            case 3: return "卖家已确认完成";
            case 4: return "交易已完成";
            case 5: return "交易已取消";
            case 6: return "买家已评价";
            case 7: return "卖家已评价";
            case 8: return "评价已完成";
            default: return "未知状态";
        }
    }

    /**
     * 获取状态颜色
     */
    private int getStatusColor(Integer status) {
        if (status == null) return context.getResources().getColor(R.color.text_secondary);
        
        if (isActive) {
            // 进行中的订单使用绿色
            return context.getResources().getColor(R.color.primary_green);
        } else {
            // 已完成的订单使用灰色
            return context.getResources().getColor(R.color.text_secondary);
        }
    }

    /**
     * 设置按钮文字
     */
    private void setButtonText(Button btn, Integer status) {
        if (status == null) {
            btn.setText("查看详情");
            return;
        }
        
        switch (status) {
            case 0:
                btn.setText("等待卖家确认");
                btn.setEnabled(false);
                break;
            case 1:
                btn.setText("确认完成");
                btn.setEnabled(true);
                break;
            case 2:
                btn.setText("确认完成");
                btn.setEnabled(true);
                break;
            case 3:
                btn.setText("确认完成");
                btn.setEnabled(true);
                break;
            case 4:
                btn.setText("去评价");
                btn.setEnabled(true);
                break;
            case 5:
                btn.setText("交易已取消");
                btn.setEnabled(false);
                break;
            case 6:
                btn.setText("待卖家评价");
                btn.setEnabled(false);
                break;
            case 7:
                btn.setText("待买家评价");
                btn.setEnabled(false);
                break;
            case 8:
                btn.setText("评价完成");
                btn.setEnabled(false);
                break;
            default:
                btn.setText("查看详情");
                btn.setEnabled(true);
        }
    }

    /**
     * 格式化时间
     */
    private String formatTime(String time) {
        if (time == null || time.isEmpty()) {
            return "";
        }
        try {
            long timestamp = Long.parseLong(time);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(new java.util.Date(timestamp));
        } catch (NumberFormatException e) {
            if (time.contains("T")) {
                return time.replace("T", " ").substring(0, 16);
            }
            return time;
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTradeNo, tvStatus, tvProductName, tvPrice, tvTime;
        ImageView ivProduct;
        Button btnAction;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTradeNo = itemView.findViewById(R.id.tv_trade_no);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivProduct = itemView.findViewById(R.id.iv_product);
            btnAction = itemView.findViewById(R.id.btn_action);
        }
    }
}