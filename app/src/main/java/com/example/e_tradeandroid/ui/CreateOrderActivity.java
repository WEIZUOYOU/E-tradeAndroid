package com.example.e_tradeandroid.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.CreateOrderRequest;
import com.example.e_tradeandroid.model.CreateOrderResponse;
import com.example.e_tradeandroid.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateOrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);
    }

    private void submitOrder(CreateOrderRequest req) {
        RetrofitClient.getInstance()
                .getTradeApi()
                .createTradeOrder(req)
                .enqueue(new Callback<BaseResponse<CreateOrderResponse>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<CreateOrderResponse>> call, Response<BaseResponse<CreateOrderResponse>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            CreateOrderResponse data = response.body().getData();
                            Toast.makeText(CreateOrderActivity.this,
                                    "下单成功！订单号：" + data.getOrderNo(),
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msg = "下单失败";
                            if (response.body() != null) {
                                msg = response.body().getMsg() != null ? response.body().getMsg() : "下单失败";
                            }
                            Toast.makeText(CreateOrderActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse<CreateOrderResponse>> call, Throwable t) {
                        Toast.makeText(CreateOrderActivity.this,
                                "网络错误：" + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}