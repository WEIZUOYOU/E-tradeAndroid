package com.example.e_tradeandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.MessageSession;
import com.example.e_tradeandroid.network.ApiClient;

import java.util.List;

public class MessageSessionAdapter extends RecyclerView.Adapter<MessageSessionAdapter.SessionViewHolder> {
    private List<MessageSession> sessionList;
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(MessageSession session);
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public MessageSessionAdapter(List<MessageSession> sessionList) {
        this.sessionList = sessionList;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        MessageSession session = sessionList.get(position);
        
        // 显示对方用户名
        holder.tvUserName.setText(session.getTargetUserName() != null ? session.getTargetUserName() : "未知用户");
        
        // 显示最后一条消息
        holder.tvLastMessage.setText(session.getLastMessage() != null ? session.getLastMessage() : "");
        
        // 显示最后消息时间
        if (session.getLastMessageTime() != null) {
            String timeStr = session.getLastMessageTime();
            if (timeStr.length() > 16) {
                timeStr = timeStr.substring(5, 16); // 截取 MM-DD HH:mm
            }
            holder.tvTime.setText(timeStr);
        }
        
        // 显示未读数量
        if (session.getUnreadCount() != null && session.getUnreadCount() > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(session.getUnreadCount()));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }
        
        // 加载对方头像
        if (session.getTargetUserAvatar() != null && !session.getTargetUserAvatar().isEmpty()) {
            String avatarUrl = ApiClient.getImageUrl(session.getTargetUserAvatar());
            Glide.with(holder.itemView.getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        }
        
        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSessionClick(session);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessionList == null ? 0 : sessionList.size();
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUserName, tvLastMessage, tvTime, tvUnreadCount;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvLastMessage = itemView.findViewById(R.id.tv_last_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
        }
    }
}
