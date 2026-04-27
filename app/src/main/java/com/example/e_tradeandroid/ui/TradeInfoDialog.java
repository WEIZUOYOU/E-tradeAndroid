package com.example.e_tradeandroid.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.network.ApiClient;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Callback;
import okhttp3.Response;

public class TradeInfoDialog extends Dialog {
    private EditText etLoc, etTime, etContact;
    private int productId, sellerId;
    private OnOrderCreate callback;

    public TradeInfoDialog(@NonNull Context c, int pid, int sid, OnOrderCreate cb) {
        super(c);productId=pid;sellerId=sid;callback=cb;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_trade_info);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);
        etLoc = findViewById(R.id.et_location);
        etTime = findViewById(R.id.et_time);
        etContact = findViewById(R.id.et_contact);
        findViewById(R.id.btn_submit).setOnClickListener(v->create());
    }

    public void create() {
        String loc = etLoc.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        if (loc.isEmpty()||time.isEmpty()||contact.isEmpty()) {
            Toast.makeText(getContext(),"请填写完整",Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject body = new JSONObject();
        try {
            body.put("productId",productId);
            body.put("sellerId",sellerId);
            body.put("buyerId",ApiClient.getCurrentUserId());
            body.put("meetingLocation",loc);
            body.put("meetingTime",time);
            body.put("contact",contact);
            body.put("tradeType",1);
        } catch (JSONException e) {e.printStackTrace();}

        ApiClient.post("/api/order/create", body.toString(), new Callback() {
            @Override public void onFailure(okhttp3.Call call, IOException e) {}
            @Override public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    if (obj.getInt("code")==200) {
                        int oid = obj.getJSONObject("data").getInt("id");
                        dismiss();
                        if (callback!=null) callback.onSuccess(oid);
                    }
                } catch (JSONException e) {e.printStackTrace();}
            }
        });
    }

    public interface OnOrderCreate {void onSuccess(int orderId);}
}