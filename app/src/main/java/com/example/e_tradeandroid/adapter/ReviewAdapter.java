package com.example.e_tradeandroid.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.Review;

import java.util.List;

/**
 * 评价列表适配器
 */
public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private Context context;
    private List<Review> reviewList;
    private boolean isReceived; // 是否是收到的评价

    public ReviewAdapter(Context context, List<Review> reviewList, boolean isReceived) {
        this.context = context;
        this.reviewList = reviewList;
        this.isReceived = isReceived;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Review review = reviewList.get(position);
        
        // 交易号
        holder.tvTradeNo.setText("交易号：" + review.getTradeNo());
        
        // 时间
        holder.tvTime.setText(formatTime(review.getCreateTime()));
        
        // 商品名称
        holder.tvProductName.setText(review.getProductName());
        
        // 评价者/被评价者信息
        if (isReceived) {
            holder.tvPersonInfo.setText("来自 " + review.getReviewerName());
        } else {
            holder.tvPersonInfo.setText("评价给 " + review.getRevieweeName());
        }
        
        // 评分星级
        setStarRating(holder, review.getRating());
        
        // 评价内容
        holder.tvComment.setText(review.getComment() != null ? review.getComment() : "暂无评价内容");
    }

    /**
     * 设置星级评分
     */
    private void setStarRating(ViewHolder holder, Integer rating) {
        ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};
        
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_empty);
            }
        }
    }

    /**
     * 格式化时间
     */
    private String formatTime(String time) {
        if (time == null || time.isEmpty()) {
            return "";
        }
        // 假设时间格式为 yyyy-MM-dd'T'HH:mm:ss 或时间戳
        try {
            // 如果是时间戳
            long timestamp = Long.parseLong(time);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
            return sdf.format(new java.util.Date(timestamp));
        } catch (NumberFormatException e) {
            // 如果是字符串格式
            if (time.contains("T")) {
                return time.replace("T", " ").substring(0, 16);
            }
            return time;
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTradeNo, tvTime, tvProductName, tvPersonInfo, tvComment;
        ImageView star1, star2, star3, star4, star5;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTradeNo = itemView.findViewById(R.id.tv_trade_no);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPersonInfo = itemView.findViewById(R.id.tv_person_info);
            tvComment = itemView.findViewById(R.id.tv_comment);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }
    }
}