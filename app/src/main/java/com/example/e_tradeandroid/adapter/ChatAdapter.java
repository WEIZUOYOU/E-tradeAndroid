package com.example.e_tradeandroid.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
    private final List<ChatMessage> list;
    private final int selfId;
    private static final int SELF = 1, OTHER = 2;

    public ChatAdapter(List<ChatMessage> l, int self) {list=l;selfId=self;}

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int type) {
        int layout = type==SELF ? R.layout.item_chat_self : R.layout.item_chat_other;
        View v = LayoutInflater.from(p.getContext()).inflate(layout,p,false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ChatMessage m = list.get(pos);
        h.content.setText(m.getContent());
        h.time.setText(m.getCreateTime());
    }

    @Override public int getItemCount() {return list.size();}
    @Override public int getItemViewType(int pos) {return list.get(pos).getSenderId()==selfId ? SELF : OTHER;}

    public static class VH extends RecyclerView.ViewHolder {
        TextView content, time;
        public VH(View v) {super(v);content=v.findViewById(R.id.tv_content);time=v.findViewById(R.id.tv_time);}
    }
}