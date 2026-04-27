package com.example.e_tradeandroid.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.network.ApiClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Callback;
import okhttp3.Response;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView tvStatus, tvName, tvPrice, tvInfo;
    private Button btnConfirm, btnComplete, btnCancel;
    private int orderId;
    private int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        orderId = getIntent().getIntExtra("orderId",0);
        initView();
        loadDetail();
    }

    private void initView() {
        tvStatus = findViewById(R.id.tv_order_status);
        tvName = findViewById(R.id.tv_product_name);
        tvPrice = findViewById(R.id.tv_price);
        tvInfo = findViewById(R.id.tv_trade_info);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnComplete = findViewById(R.id.btn_complete);
        btnCancel = findViewById(R.id.btn_cancel);

        btnConfirm.setOnClickListener(v->update(1));
        btnComplete.setOnClickListener(v->update(2));
        btnCancel.setOnClickListener(v->new AlertDialog.Builder(this)
                .setTitle("取消订单").setMessage("确定取消？")
                .setPositiveButton("确定",(d,w)->update(-1))
                .setNegativeButton("取消",null).show());
        findViewById(R.id.iv_back).setOnClickListener(v->finish());
    }

    private void loadDetail() {
        ApiClient.get("/api/order/detail/"+orderId, new Callback() {
            @Override public void onFailure(okhttp3.Call call, IOException e) {}
            @Override public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    if (obj.getInt("code")==200) {
                        JSONObject data = obj.getJSONObject("data");
                        status = data.getInt("status");
                        runOnUiThread(()->{
                            try {
                                tvName.setText(data.getString("productName"));
                                tvPrice.setText("¥"+data.getDouble("price"));
                                tvInfo.setText("地点："+data.getString("meetingLocation")
                                        +"\n时间："+data.getString("meetingTime"));
                                refreshStatusUI();
                            } catch (JSONException e) {e.printStackTrace();}
                        });
                    }
                } catch (JSONException e) {e.printStackTrace();}
            }
        });
    }

    private void update(int s) {
        JSONObject body = new JSONObject();
        try {body.put("orderId",orderId);body.put("status",s);} catch (JSONException e) {e.printStackTrace();}
        ApiClient.post("/api/order/updateStatus", body.toString(), new Callback() {
            @Override public void onFailure(okhttp3.Call call, IOException e) {}
            @Override public void onResponse(okhttp3.Call call, Response response) {
                runOnUiThread(()->{Toast.makeText(OrderDetailActivity.this,"操作成功",Toast.LENGTH_SHORT).show();loadDetail();});
            }
        });
    }

    private void refreshStatusUI() {
        switch (status) {
            case 0: tvStatus.setText("待卖家确认");btnConfirm.setVisibility(View.VISIBLE);btnComplete.setVisibility(View.GONE);break;
            case 1: tvStatus.setText("交易中");btnConfirm.setVisibility(View.GONE);btnComplete.setVisibility(View.VISIBLE);break;
            case 2: tvStatus.setText("已完成");btnConfirm.setVisibility(View.GONE);btnComplete.setVisibility(View.GONE);break;
            case -1: tvStatus.setText("已取消");break;
        }
    }
}