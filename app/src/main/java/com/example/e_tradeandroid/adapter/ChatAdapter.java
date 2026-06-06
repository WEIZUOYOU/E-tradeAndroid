package com.example.e_tradeandroid.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.ChatMessage;
import com.example.e_tradeandroid.model.TradeInfo;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;

import java.util.List;

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
                v = LayoutInflater.from(p.getContext()).inflate(R.layout.item_trade_card, p, false);
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
            h.content.setText(m.getContent());
            h.time.setText(m.getCreateTime());
        }
    }

    private void bindTradeCard(TradeCardVH h, ChatMessage m) {
        try {
            TradeInfo tradeInfo = gson.fromJson(m.getTradeData(), TradeInfo.class);

            // 设置商品信息
            if (tradeInfo.getProductImage() != null && !tradeInfo.getProductImage().isEmpty()) {
                String imageUrl = tradeInfo.getProductImage().startsWith("http")
                        ? tradeInfo.getProductImage()
                        : ApiClient.BASE_URL + tradeInfo.getProductImage();
                Glide.with(h.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .into(h.ivProduct);
            }

            h.tvProductName.setText(tradeInfo.getProductName() != null ? tradeInfo.getProductName() : "");
            h.tvProductPrice.setText("￥" + (tradeInfo.getProductPrice() != null ? tradeInfo.getProductPrice() : "0.00"));

            // 设置交易信息
            h.tvLocation.setText(tradeInfo.getMeetingLocation() != null ? tradeInfo.getMeetingLocation() : "");
            h.tvTime.setText(tradeInfo.getMeetingTime() != null ? tradeInfo.getMeetingTime() : "");

            // 设置联系电话（始终显示对方的电话）
            String phone = "";
            long buyerId = tradeInfo.getBuyerId() != null ? tradeInfo.getBuyerId() : 0;
            long sellerId = tradeInfo.getSellerId() != null ? tradeInfo.getSellerId() : 0;
            
            if (buyerId == selfId) {
                // 当前用户是买家，显示卖家电话
                phone = tradeInfo.getSellerPhone();
            } else if (sellerId == selfId) {
                // 当前用户是卖家，显示买家电话
                phone = tradeInfo.getBuyerPhone();
            }
            
            if (phone != null && phone.length() >= 11) {
                h.tvPhone.setText(phone.substring(0, 3) + "****" + phone.substring(7));
            } else {
                h.tvPhone.setText("未填写");
            }

            // 设置交易状态
            h.tvTradeStatus.setText(getTradeStatusText(tradeInfo.getTradeStatus()));
            h.tvTradeStatus.setBackgroundResource(getTradeStatusBg(tradeInfo.getTradeStatus()));

            // 设置操作按钮
            int status = tradeInfo.getTradeStatus() != null ? tradeInfo.getTradeStatus() : 0;
            setupTradeActionButton(h, m, status);

            // 设置卡片点击事件
            h.llTradeCard.setOnClickListener(v -> {
                if (tradeCardClickListener != null) {
                    tradeCardClickListener.onTradeCardClick(m);
                }
            });

            // 设置操作按钮点击事件
            h.btnAction.setOnClickListener(v -> {
                if (tradeCardClickListener != null) {
                    tradeCardClickListener.onTradeActionClick(m);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTradeActionButton(TradeCardVH h, ChatMessage m, int status) {
        boolean isSelf = m.getSenderId() != null && m.getSenderId().intValue() == selfId;
        
        // 尝试从交易数据中解析买家和卖家ID
        long buyerId = 0;
        long sellerId = 0;
        try {
            if (m.getTradeData() != null && !m.getTradeData().isEmpty()) {
                com.google.gson.JsonObject tradeObj = new com.google.gson.Gson()
                    .fromJson(m.getTradeData(), com.google.gson.JsonObject.class);
                if (tradeObj.has("buyerId")) {
                    buyerId = tradeObj.get("buyerId").getAsLong();
                }
                if (tradeObj.has("sellerId")) {
                    sellerId = tradeObj.get("sellerId").getAsLong();
                }
            }
        } catch (Exception e) {
            // 解析失败，使用默认值
        }
        
        // 判断当前用户是买家还是卖家
        boolean isCurrentUserBuyer = buyerId != 0 && buyerId == selfId;
        boolean isCurrentUserSeller = sellerId != 0 && sellerId == selfId;
        
        Log.d("ChatAdapter", "setupTradeActionButton: status=" + status + 
                ", isSelf=" + isSelf + ", isBuyer=" + isCurrentUserBuyer + 
                ", isSeller=" + isCurrentUserSeller);

        switch (status) {
            case 0: // 待卖家确认
                if (isCurrentUserSeller) {
                    // 卖家看到待确认卡片，显示确认按钮
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("去确认");
                    h.btnAction.setBackgroundResource(R.drawable.bg_btn_primary);
                    h.btnAction.setEnabled(true);
                } else {
                    // 买家看到待确认卡片（包括自己发的），显示等待状态
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("待卖家确认");
                    h.btnAction.setBackgroundResource(R.drawable.bg_status_tag);
                    h.btnAction.setEnabled(false);
                }
                break;
            case 1: // 待交易
                // 判断当前用户是否可以操作（根据角色而非发送者）
                boolean canComplete = false;
                if (isCurrentUserBuyer) {
                    // 买家可以确认完成
                    canComplete = true;
                } else if (isCurrentUserSeller) {
                    // 卖家可以确认完成
                    canComplete = true;
                }
                
                if (canComplete) {
                    // 当前用户可以操作，显示确认完成按钮
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("确认完成");
                    h.btnAction.setBackgroundResource(R.drawable.bg_btn_primary);
                    h.btnAction.setEnabled(true);
                } else {
                    // 当前用户不能操作，显示待交易状态
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("待交易");
                    h.btnAction.setBackgroundResource(R.drawable.bg_status_tag);
                    h.btnAction.setEnabled(false);
                }
                break;
            case 2: // 卖家已确认，等待买家
                if (isCurrentUserBuyer) {
                    // 买家看到，需要确认
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("确认完成");
                    h.btnAction.setBackgroundResource(R.drawable.bg_btn_primary);
                    h.btnAction.setEnabled(true);
                } else {
                    // 卖家看到，等待买家确认
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("待买家确认");
                    h.btnAction.setBackgroundResource(R.drawable.bg_status_tag);
                    h.btnAction.setEnabled(false);
                }
                break;
            case 3: // 买家已确认，等待卖家
                if (isCurrentUserSeller) {
                    // 卖家看到，需要确认
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("确认完成");
                    h.btnAction.setBackgroundResource(R.drawable.bg_btn_primary);
                    h.btnAction.setEnabled(true);
                } else {
                    // 买家看到，等待卖家确认
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("待卖家确认");
                    h.btnAction.setBackgroundResource(R.drawable.bg_status_tag);
                    h.btnAction.setEnabled(false);
                }
                break;
            case 4: // 待确认修改
                if (!isSelf) {
                    // 对方发起的修改，显示确认修改按钮
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("确认修改");
                    h.btnAction.setBackgroundResource(R.drawable.bg_btn_primary);
                    h.btnAction.setEnabled(true);
                } else {
                    // 自己发起的修改，显示等待确认
                    h.btnAction.setVisibility(View.VISIBLE);
                    h.btnAction.setText("等待确认");
                    h.btnAction.setBackgroundResource(R.drawable.bg_status_tag);
                    h.btnAction.setEnabled(false);
                }
                break;
            case 5: // 已完成
                h.btnAction.setVisibility(View.VISIBLE);
                h.btnAction.setText("已完成");
                h.btnAction.setBackgroundResource(R.drawable.bg_btn_success);
                h.btnAction.setEnabled(false);
                break;
            case 6: // 已取消
                h.btnAction.setVisibility(View.VISIBLE);
                h.btnAction.setText("已取消");
                h.btnAction.setBackgroundResource(R.drawable.bg_btn_danger);
                h.btnAction.setEnabled(false);
                break;
            default:
                h.btnAction.setVisibility(View.GONE);
        }
    }

    private String getTradeStatusText(Integer status) {
        if (status == null) return "未知状态";
        switch (status) {
            case 0: return "待卖家确认";
            case 1: return "待交易";
            case 2: return "卖家已确认";
            case 3: return "买家已确认";
            case 4: return "待确认修改";
            case 5: return "已完成";
            case 6: return "已取消";
            default: return "未知状态";
        }
    }

    private int getTradeStatusBg(Integer status) {
        if (status == null) return R.drawable.bg_status_tag;
        switch (status) {
            case 0: return R.drawable.bg_status_tag; // 橙色
            case 1: return R.drawable.bg_btn_success; // 绿色
            case 2: return R.drawable.bg_btn_primary; // 蓝色（等待买家）
            case 3: return R.drawable.bg_btn_primary; // 蓝色（等待卖家）
            case 4: return R.drawable.bg_status_tag; // 橙色（待确认修改）
            case 5: return R.drawable.bg_btn_success; // 绿色
            case 6: return R.drawable.bg_btn_danger; // 红色（已取消）
            default: return R.drawable.bg_status_tag;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int pos) {
        ChatMessage msg = list.get(pos);
        // type=1表示交易卡片消息
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

    public static class TradeCardVH extends RecyclerView.ViewHolder {
        LinearLayout llTradeCard;
        TextView tvTradeStatus, tvProductName, tvProductPrice;
        TextView tvLocation, tvTime, tvPhone;
        ImageView ivProduct;
        Button btnAction;

        public TradeCardVH(View v) {
            super(v);
            llTradeCard = v.findViewById(R.id.ll_trade_card);
            tvTradeStatus = v.findViewById(R.id.tv_trade_status);
            tvProductName = v.findViewById(R.id.tv_product_name);
            tvProductPrice = v.findViewById(R.id.tv_product_price);
            tvLocation = v.findViewById(R.id.tv_location);
            tvTime = v.findViewById(R.id.tv_time);
            tvPhone = v.findViewById(R.id.tv_phone);
            ivProduct = v.findViewById(R.id.iv_product);
            btnAction = v.findViewById(R.id.btn_action);
        }
    }
}
