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
            Log.e("ChatAdapter", "bindTradeCard parse error: " + e.getMessage());
        }

        // ========== 第三步：如果解析失败或数据无效，显示兜底文案 ==========
        if (tradeInfo == null) {
            h.tvSenderRole.setText("系统");
            h.tvContent.setText("卡片数据异常");
            // 即使数据异常，也设置点击事件，让用户可以尝试查看详情
            h.cardRoot.setOnClickListener(v -> {
                if (tradeCardClickListener != null) {
                    tradeCardClickListener.onTradeCardClick(m);
                }
            });
            return;
        }

        // ========== 第四步：正常渲染逻辑 ==========
        long buyerId = tradeInfo.getBuyerId() != null ? tradeInfo.getBuyerId() : 0;
        long sellerId = tradeInfo.getSellerId() != null ? tradeInfo.getSellerId() : 0;
        boolean isCurrentUserBuyer = (buyerId == selfId);
        boolean isCurrentUserSeller = (sellerId == selfId);

        // 获取有效状态：优先用外层消息的 tradeStatus，其次 tradeData 内的
        int status = (m.getTradeStatus() != null && m.getTradeStatus() != 0)
                        ? m.getTradeStatus()
                        : (tradeInfo.getTradeStatus() != null ? tradeInfo.getTradeStatus() : 0);

        // 计算逻辑上的"发送方"角色
        boolean isLogicalSelfSent = isLogicalSelfSent(status, isCurrentUserBuyer, isCurrentUserSeller);

        // 设置背景和对齐
        params = (FrameLayout.LayoutParams) h.cardRoot.getLayoutParams();
        if (isLogicalSelfSent) {
            params.gravity = Gravity.END;
            h.cardRoot.setBackgroundResource(R.drawable.bg_trade_card_self_new);
            h.tvSenderRole.setTextColor(0xFF1565C0); // 蓝色
        } else {
            params.gravity = Gravity.START;
            h.cardRoot.setBackgroundResource(R.drawable.bg_trade_card_other_new);
            h.tvSenderRole.setTextColor(0xFFD84315); // 橙色
        }
        h.cardRoot.setLayoutParams(params);

        // 获取文案
        CardContent content = getCardContentStrict(status, isCurrentUserBuyer, isCurrentUserSeller, isLogicalSelfSent);
        h.tvSenderRole.setText(content.role);
        h.tvContent.setText(content.message);

        // 点击事件保持不变
        h.cardRoot.setOnClickListener(v -> {
            if (tradeCardClickListener != null) {
                tradeCardClickListener.onTradeCardClick(m);
            }
        });
    }
    
    /**
     * 根据交易状态和当前用户角色，计算这条卡片在逻辑上是否应该显示为"自己发出的"
     * （即：当前用户是这条卡片所代表操作的实际执行者）
     */
    private boolean isLogicalSelfSent(int status, boolean isCurrentUserBuyer, boolean isCurrentUserSeller) {
        switch (status) {
            case 0: return isCurrentUserBuyer;
            case 1: return isCurrentUserSeller;
            case 2: return isCurrentUserBuyer;
            case 3: return isCurrentUserSeller;
            case 4: return false;   // 双方都看到相同的"交易结束"
            case 6: return isCurrentUserBuyer;
            case 7: return isCurrentUserSeller;
            case 8: return false;   // 双方都看到"评价完结"
            case 5: return false;   // 取消消息统一居左
            default: return false;
        }
    }

    /**
     * 根据交易状态、用户角色、是否逻辑自己的卡片，获取对应的文案（角色名 + 消息内容）
     */
    private CardContent getCardContentStrict(int status, boolean isCurrentUserBuyer,
                                             boolean isCurrentUserSeller, boolean isLogicalSelfSent) {
        CardContent content = new CardContent();
        switch (status) {
            case 0: // 买家发起交易申请
                if (isLogicalSelfSent) {
                    content.role = "我（买家）";
                    content.message = "你已发起交易申请，等待卖家确认";
                } else {
                    content.role = "买家";
                    content.message = "买家发来新的交易申请，请确认";
                }
                break;
            case 1: // 卖家确认交易申请
                if (isLogicalSelfSent) {
                    content.role = "我（卖家）";
                    content.message = "你已确认申请，等待线下交易";
                } else {
                    content.role = "卖家";
                    content.message = "卖家已确认申请，交易进入待交易阶段";
                }
                break;
            case 2: // 买家已确认完成（等待卖家确认）
                if (isLogicalSelfSent) {
                    content.role = "我（买家）";
                    content.message = "你已确认交易完成，等待卖家确认";
                } else {
                    content.role = "买家";
                    content.message = "买家已确认交易完成，请你确认";
                }
                break;
            case 3: // 卖家已确认完成（等待买家确认）
                if (isLogicalSelfSent) {
                    content.role = "我（卖家）";
                    content.message = "你已确认交易完成，等待买家确认";
                } else {
                    content.role = "卖家";
                    content.message = "卖家已确认交易完成，请你确认";
                }
                break;
            case 4: // 双方均确认交易完成
                content.role = isCurrentUserBuyer ? "我（买家）" : "我（卖家）";
                content.message = "双方已确认，交易结束，请评价";
                break;
            case 6: // 买家提交评价
                if (isLogicalSelfSent) {
                    content.role = "我（买家）";
                    content.message = "你已评价，等待卖家评价";
                } else {
                    content.role = "买家";
                    content.message = "买家已评价，请你也评价";
                }
                break;
            case 7: // 卖家提交评价
                if (isLogicalSelfSent) {
                    content.role = "我（卖家）";
                    content.message = "你已评价，等待买家评价";
                } else {
                    content.role = "卖家";
                    content.message = "卖家已评价，请你也评价";
                }
                break;
            case 8: // 双方均完成评价
                content.role = isCurrentUserBuyer ? "我（买家）" : "我（卖家）";
                content.message = "双方已完成评价，交易完结";
                break;
            case 5: // 交易取消
                content.role = "系统";
                content.message = "交易已取消";
                break;
            default:
                content.role = "未知";
                content.message = "交易状态更新";
        }
        return content;
    }

    /**
     * 卡片内容数据类
     */
    private static class CardContent {
        String role = "";
        String message = "";
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
