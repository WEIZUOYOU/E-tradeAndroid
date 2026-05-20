package com.example.e_tradeandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private final Context mContext;
    private final List<Order> mOrderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.mContext = context;
        this.mOrderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = mOrderList.get(position);
        holder.tvOrderNo.setText("订单号：" + order.getOrderNo());
        holder.tvProductName.setText(order.getProductName());
        holder.tvPrice.setText("¥" + order.getTotalAmount());

        String statusStr;
        switch (order.getStatus()) {
            case 1: statusStr = "待交易"; break;
            case 2: statusStr = "已完成"; break;
            case 3: statusStr = "已取消"; break;
            default: statusStr = "未知状态";
        }
        holder.tvStatus.setText(statusStr);
    }

    @Override
    public int getItemCount() {
        return mOrderList == null ? 0 : mOrderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNo, tvProductName, tvPrice, tvStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNo = itemView.findViewById(R.id.tv_order_no);
            tvProductName = itemView.findViewById(R.id.tv_order_name);
            tvPrice = itemView.findViewById(R.id.tv_order_price);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
        }
    }
}