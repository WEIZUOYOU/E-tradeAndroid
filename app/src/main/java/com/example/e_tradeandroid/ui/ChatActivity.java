package com.example.e_tradeandroid.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.adapter.ChatAdapter;
import com.example.e_tradeandroid.model.ChatMessage;
import com.example.e_tradeandroid.network.ApiClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvChat;
    private EditText etInput;
    private Button btnSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private int productId;
    private int sellerId;
    private int currentUserId;
    private final Handler handler = new Handler();
    private Runnable pollTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        productId = getIntent().getIntExtra("productId", 0);
        sellerId = getIntent().getIntExtra("sellerId", 0);
        currentUserId = ApiClient.getCurrentUserId();

        initView();
        loadMessage();
        startPoll();
    }

    private void initView() {
        rvChat = findViewById(R.id.rv_chat);
        etInput = findViewById(R.id.et_input);
        btnSend = findViewById(R.id.btn_send);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        btnSend.setOnClickListener(v -> sendMsg());
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
    }

    private void sendMsg() {
        String content = etInput.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "输入不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("productId", productId);
            body.put("senderId", currentUserId);
            body.put("receiverId", sellerId);
            body.put("content", content);
        } catch (JSONException e) {e.printStackTrace();}

        ApiClient.post("/api/chat/send", body.toString(), new Callback() {
            @Override public void onFailure(Call call, IOException e) {runOnUiThread(()->Toast.makeText(ChatActivity.this,"发送失败",Toast.LENGTH_SHORT).show());}
            @Override public void onResponse(Call call, Response response) {runOnUiThread(()->{etInput.setText("");loadMessage();});}
        });
    }

    private void loadMessage() {
        String url = ApiClient.BASE_URL + "/api/chat/list?productId="+productId+"&sellerId="+sellerId+"&buyerId="+currentUserId;
        Request request = new Request.Builder().url(url).build();
        ApiClient.getHttpClient().newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {}
            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    if (obj.getInt("code")==200) {
                        JSONArray arr = obj.getJSONArray("data");
                        messageList.clear();
                        for (int i=0;i<arr.length();i++) {
                            JSONObject o = arr.getJSONObject(i);
                            ChatMessage m = new ChatMessage();
                            m.setSenderId(o.getInt("senderId"));
                            m.setContent(o.getString("content"));
                            m.setCreateTime(o.getString("createTime"));
                            messageList.add(m);
                        }
                        runOnUiThread(()->{chatAdapter.notifyDataSetChanged();rvChat.scrollToPosition(messageList.size()-1);});
                    }
                } catch (JSONException e) {e.printStackTrace();}
            }
        });
    }

    private void startPoll() {
        pollTask = () -> {loadMessage();handler.postDelayed(pollTask,3000);};
        handler.post(pollTask);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (handler!=null&&pollTask!=null) handler.removeCallbacks(pollTask);
    }
}