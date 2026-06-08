package com.example.e_tradeandroid.adapter;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.ChatMessage;
import com.example.e_tradeandroid.model.TradeInfo;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> list;
    private final int selfId;
    private static final int SELF = 1, OTHER = 2, TRADE_CARD = 3;
    private final Gson gson = new Gson();
    private OnTradeCardClickListener tradeCardClickListener;

    public interface OnTradeCardClickListener {
        void onTradeCardClick(ChatMessage message);
        void onTradeActionClick(ChatMessage message);
    }

    public ChatAdapter(List<ChatMessage> l, int self) {
        list = l;
        selfId = self;
    }

    public void setOnTradeCardClickListener(OnTradeCardClickListener listener) {
        this.tradeCardClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int type) {
        View v;
        switch (type) {
            case SELF:
                v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_chat_self, p, false);
                return new SelfVH(v);
            case OTHER:
                v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_chat_other, p, false);
                return new OtherVH(v);
            case TRADE_CARD:
                v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_trade_card_compact_v2, p, false);
                return new TradeCardVH(v);
            default:
                v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_chat_other, p, false);
                return new OtherVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        ChatMessage m = list.get(pos);
        int type = getItemViewType(pos);

        if (type == TRADE_CARD) {
            TradeCardVH h = (TradeCardVH) holder;
            bindTradeCard(h, m);
        } else {
            BaseVH h = (BaseVH) holder;
            h.content.setText(m.getContent() != null ? m.getContent() : "");
            String timeStr = formatTime(m.getCreateTime());
            h.time.setText(timeStr);
        }
    }

    /**
     * 交易卡片绑定逻辑 - 强制重置+安全解析+异常兜底
     */
    private void bindTradeCard(TradeCardVH h, ChatMessage m) {
        // ========== 第一步：强制重置所有可能残留的 UI ==========
        h.tvSenderRole.setText("");
        h.tvContent.setText("");
        h.tvTime.setText("");

        // 重置背景和对齐为默认值（接收方样式）
        h.cardRoot.setBackgroundResource(R.drawable.bg_trade_card_other_new);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) h.cardRoot.getLayoutParams();
        params.gravity = Gravity.START;
        h.cardRoot.setLayoutParams(params);

        // 时间兜底
        String timeStr = formatTime(m.getCreateTime());
        h.tvTime.setText(timeStr);

        // ========== 第二步：安全解析 tradeData ==========
        TradeInfo tradeInfo = null;
        try {
            if (m.getTradeData() != null && !m.getTradeData().isEmpty()) {
                tradeInfo = gson.fromJson(m.getTradeData(), TradeInfo.class);
            }
        } catch (Exception e) {
            Log.e("ChatAdapter", "解析 tradeData 失败: " + e.getMessage());
        }

        if (tradeInfo == null) {
            h.tvSenderRole.setText("系统");
            h.tvContent.setText("卡片数据异常");
            setCardAlignment(h, false); // 异常消息默认居左
            h.cardRoot.setOnClickListener(v -> {
                if (tradeCardClickListener != null) {
                    tradeCardClickListener.onTradeCardClick(m);
                }
            });
            return;
        }

        // 2. 决定对齐方式：发送者是自己则居右，否则居左
        boolean isSelfSent = (m.getSenderId() != null && m.getSenderId().intValue() == selfId);
        setCardAlignment(h, isSelfSent);

        // 3. 生成卡片内的角色和动作文案（使用发送者判断，不再依赖状态码）
        int status = (m.getTradeStatus() != null) ? m.getTradeStatus() : 0;
        String roleText = getRoleText(m, tradeInfo);
        String actionText = getActionText(status, m, tradeInfo);

        h.tvSenderRole.setText(roleText);
        h.tvContent.setText(actionText);

        // 4. 点击事件
        h.cardRoot.setOnClickListener(v -> {
            if (tradeCardClickListener != null) {
                tradeCardClickListener.onTradeCardClick(m);
            }
        });
    }

    /**
     * 设置卡片对齐和背景
     */
    private void setCardAlignment(TradeCardVH h, boolean isSelfSent) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) h.cardRoot.getLayoutParams();
        if (isSelfSent) {
            params.gravity = Gravity.END;
            h.cardRoot.setBackgroundResource(R.drawable.bg_trade_card_self_new);
            h.tvSenderRole.setTextColor(0xFF1565C0); // 蓝色
        } else {
            params.gravity = Gravity.START;
            h.cardRoot.setBackgroundResource(R.drawable.bg_trade_card_other_new);
            h.tvSenderRole.setTextColor(0xFFD84315); // 橙色
        }
        h.cardRoot.setLayoutParams(params);
    }

    /**
     * 获取角色文本（直接使用消息发送者）
     * 不再依赖状态码判断操作方，而是根据实际发送者显示
     */
    private String getRoleText(ChatMessage msg, TradeInfo tradeInfo) {
        Long senderId = msg.getSenderId();
        boolean isSelf = (senderId != null && senderId.intValue() == selfId);
        boolean isSenderBuyer = (senderId != null && senderId.equals(tradeInfo.getBuyerId()));
        
        if (isSelf) {
            return isSenderBuyer ? "我（买家）" : "我（卖家）";
        } else {
            return isSenderBuyer ? "买家" : "卖家";
        }
    }

    /**
     * 获取动作描述（根据状态和发送者角色）
     * 根据发送者角色动态生成文案，而不是依赖当前用户角色
     */
    private String getActionText(int status, ChatMessage msg, TradeInfo tradeInfo) {
        Long senderId = msg.getSenderId();
        boolean isSenderBuyer = (senderId != null && senderId.equals(tradeInfo.getBuyerId()));
        
        switch (status) {
            case 0:
                return isSenderBuyer ? "发起了交易申请，等待卖家确认" : "发起了交易申请，请确认";
            case 1:
                return isSenderBuyer ? "已确认申请，等待线下交易" : "已确认申请，交易进入待交易阶段";
            case 2:
                // 发送者已确认，等待对方确认（兼容后端状态码定义）
                return isSenderBuyer ? "已确认交易完成，等待卖家确认" : "已确认交易完成，等待买家确认";
            case 3:
                // 对方已确认，请当前用户确认
                return isSenderBuyer ? "已确认交易完成，请你确认" : "已确认交易完成，请你确认";
            case 4:
                return "双方已确认，交易结束，请评价";
            case 5:
                return "交易已取消";
            case 6:
                return isSenderBuyer ? "已评价，等待卖家评价" : "已评价，请你也评价";
            case 7:
                return isSenderBuyer ? "已评价，请你也评价" : "已评价，等待买家评价";
            case 8:
                return "双方已完成评价，交易完结";
            default:
                return "交易状态更新";
        }
    }



    /**
     * 查找交易的最新状态（备用方案）
     */
    private int findLatestTradeStatus(long tradeId) {
        int latestStatus = 0;
        for (ChatMessage msg : list) {
            if (msg.getType() != null && msg.getType() == 1) {
                try {
                    TradeInfo tradeInfo = gson.fromJson(msg.getTradeData(), TradeInfo.class);
                    if (tradeInfo.getId() != null && tradeInfo.getId() == tradeId) {
                        int status = tradeInfo.getTradeStatus() != null ? tradeInfo.getTradeStatus() : 0;
                        if (status > latestStatus) {
                            latestStatus = status;
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return latestStatus;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int pos) {
        ChatMessage msg = list.get(pos);
        if (msg.getType() != null && msg.getType() == 1) {
            return TRADE_CARD;
        }
        return msg.getSenderId() != null && msg.getSenderId().intValue() == selfId ? SELF : OTHER;
    }

    // 基础ViewHolder
    public static class BaseVH extends RecyclerView.ViewHolder {
        TextView content, time;
        public BaseVH(View v) {
            super(v);
            content = v.findViewById(R.id.tv_content);
            time = v.findViewById(R.id.tv_time);
        }
    }

    public static class SelfVH extends BaseVH {
        public SelfVH(View v) {
            super(v);
        }
    }

    public static class OtherVH extends BaseVH {
        public OtherVH(View v) {
            super(v);
        }
    }

    /**
     * 极简交易卡片ViewHolder
     */
    public static class TradeCardVH extends RecyclerView.ViewHolder {
        LinearLayout cardRoot;
        TextView tvSenderRole;
        TextView tvContent;
        TextView tvTime;

        public TradeCardVH(View v) {
            super(v);
            cardRoot = v.findViewById(R.id.card_root);
            tvSenderRole = v.findViewById(R.id.tv_sender_role);
            tvContent = v.findViewById(R.id.tv_content);
            tvTime = v.findViewById(R.id.tv_time);
        }
    }
    
    /**
     * 格式化时间显示
     * @param timestamp 时间戳字符串（支持时间戳、"yyyy-MM-dd HH:mm:ss"格式）
     * @return 格式化的时间字符串（HH:mm格式）
     */
    private String formatTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            // 尝试解析为时间戳（秒/毫秒）
            long ts = Long.parseLong(timestamp);
            if (ts < 10000000000L) ts *= 1000;
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            return sdf.format(new java.util.Date(ts));
        } catch (NumberFormatException e) {
            // 不是纯数字，尝试解析为 "yyyy-MM-dd HH:mm:ss" 格式
            try {
                java.text.SimpleDateFormat sdfInput = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                java.util.Date date = sdfInput.parse(timestamp);
                java.text.SimpleDateFormat sdfOutput = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                return sdfOutput.format(date);
            } catch (Exception ex) {
                return timestamp; // 返回原始字符串
            }
        }
    }
}
