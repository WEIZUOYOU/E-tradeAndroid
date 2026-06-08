package com.example.e_tradeandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.MessageSessionAdapter;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.MessageSession;
import com.example.e_tradeandroid.network.ApiClient;
import com.example.e_tradeandroid.util.NavUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class MessageListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private BottomNavigationView bottomNavigation;
    private Gson gson = new Gson();
    private List<MessageSession> sessionList = new ArrayList<>();
    private MessageSessionAdapter sessionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);

        recyclerView = findViewById(R.id.recycler_view_messages);
        progressBar = findViewById(R.id.progress_bar_messages);
        tvEmpty = findViewById(R.id.tv_empty);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        setupBottomNav();
        initRecyclerView();
        loadSessions();
    }
    
    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        sessionAdapter = new MessageSessionAdapter(sessionList);
        sessionAdapter.setOnSessionClickListener(session -> {
            // 点击会话，跳转到聊天界面
            Intent intent = new Intent(MessageListActivity.this, ChatActivity.class);
            intent.putExtra("sellerId", session.getTargetUserId());
            intent.putExtra("productId", session.getProductId() != null ? session.getProductId() : 0L);
            startActivity(intent);
            
            // 标记该会话为已读
            markSessionAsRead(session.getTargetUserId());
        });
        recyclerView.setAdapter(sessionAdapter);
    }

    private void loadSessions() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // 调用后端接口获取会话列表
        String url = ApiClient.BASE_URL + "api/message/sessions?page=1&size=20";
        Request request = new Request.Builder().url(url).build();
        
        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MessageListActivity.this, "加载失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(respBody);
                    if (jsonObject.getInt("code") == 200) {
                        JSONObject dataObj = jsonObject.getJSONObject("data");
                        // 直接获取sessions数组
                        org.json.JSONArray sessionsArray = dataObj.getJSONArray("sessions");

                        // 构建去重 Map（按 targetUserId 去重，保留最新的一条消息）
                        Map<Long, MessageSession> uniqueMap = new LinkedHashMap<>();
                        for (int i = 0; i < sessionsArray.length(); i++) {
                            JSONObject sessionObj = sessionsArray.getJSONObject(i);
                            MessageSession session = new MessageSession();
                            session.setTargetUserId(sessionObj.optLong("targetUserId"));
                            session.setTargetUserName(sessionObj.optString("targetUserName"));
                            session.setTargetUserAvatar(sessionObj.optString("targetUserAvatar"));
                            session.setLastMessage(sessionObj.optString("lastMessage"));
                            session.setLastMessageTime(sessionObj.optString("lastMessageTime"));
                            session.setUnreadCount(sessionObj.optInt("unreadCount", 0));
                            session.setProductId(sessionObj.optLong("productId"));
                            session.setProductName(sessionObj.optString("productName"));

                            Long userId = session.getTargetUserId();
                            // 若已存在，比较最后消息时间，保留较新的
                            if (uniqueMap.containsKey(userId)) {
                                MessageSession existing = uniqueMap.get(userId);
                                String existingTime = existing.getLastMessageTime();
                                String newTime = session.getLastMessageTime();
                                if (newTime != null && (existingTime == null || newTime.compareTo(existingTime) > 0)) {
                                    uniqueMap.put(userId, session);
                                }
                            } else {
                                uniqueMap.put(userId, session);
                            }
                        }

                        List<MessageSession> uniqueSessions = new ArrayList<>(uniqueMap.values());

                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            sessionList.clear();
                            if (uniqueSessions != null && !uniqueSessions.isEmpty()) {
                                sessionList.addAll(uniqueSessions);
                                sessionAdapter.notifyDataSetChanged();
                                recyclerView.setVisibility(View.VISIBLE);
                                tvEmpty.setVisibility(View.GONE);
                            } else {
                                showEmptyState();
                            }
                        });
                    } else {
                        String errorMsg = jsonObject.optString("msg", "请求失败");
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(MessageListActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            showEmptyState();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(MessageListActivity.this, "数据解析失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        showEmptyState();
                    });
                }
            }
        });
    }
    
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setText("暂无消息会话\n请从商品详情页点击“聊一聊”开始聊天");
        tvEmpty.setVisibility(View.VISIBLE);
    }
    
    private void markSessionAsRead(Long targetUserId) {
        // 调用后端接口标记会话已读
        String url = ApiClient.BASE_URL + "api/message/read-session/" + targetUserId;
        okhttp3.RequestBody body = okhttp3.RequestBody.create(null, new byte[0]);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .put(body)
                .build();
        
        ApiClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 静默失败，不影响用户体验
            }

            @Override
            public void onResponse(Call call, Response response) {
                // 成功后刷新列表
                runOnUiThread(() -> loadSessions());
            }
        });
    }

    private void setupBottomNav() {
        bottomNavigation.setSelectedItemId(R.id.nav_messages);
        bottomNavigation.setOnItemSelectedListener(item -> {
            return NavUtils.handleNavClick(this, item.getItemId());
        });
    }
}
