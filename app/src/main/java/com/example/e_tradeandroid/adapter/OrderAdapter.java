package com.example.e_tradeandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.Order;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
    private List<Order> orderList;
    private OnItemClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    public OrderAdapter(List<Order> orderList, OnItemClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderNo.setText("订单号: " + order.getOrderNo());
        holder.tvProductName.setText("商品: " + order.getProductName());
        holder.tvQuantity.setText("数量: " + order.getQuantity());
        holder.tvTotalAmount.setText("总价: ¥" + order.getTotalAmount().toString());
        holder.tvStatus.setText("状态: " + getStatusText(order.getStatus()));
        holder.tvCreateTime.setText("时间: " + dateFormat.format(order.getCreateTime()));
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(order));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private String getStatusText(int status) {
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付待发货";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvProductName, tvQuantity, tvTotalAmount, tvStatus, tvCreateTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNo = itemView.findViewById(R.id.tv_order_no);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCreateTime = itemView.findViewById(R.id.tv_create_time);
        }
    }
}
