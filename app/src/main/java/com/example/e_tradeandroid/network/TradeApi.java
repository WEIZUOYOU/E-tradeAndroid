package com.example.e_tradeandroid.network;

import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.CreateOrderRequest;
import com.example.e_tradeandroid.model.CreateOrderResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TradeApi {
    @POST("/api/v1/trade/order")
    Call<BaseResponse<CreateOrderResponse>> createTradeOrder(@Body CreateOrderRequest request);
}