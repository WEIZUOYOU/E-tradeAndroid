package com.example.e_tradeandroid.repository;

import com.example.e_tradeandroid.model.BaseResponse;
import com.example.e_tradeandroid.model.User;
import com.example.e_tradeandroid.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 用户数据仓库 - 统一处理用户相关的网络请求和数据缓存
 */
public class UserRepository {
    private static final Gson gson = new Gson();
    
    public interface UserCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }
    
    /**
     * 获取当前用户
     */
    public void getCurrentUser(UserCallback<User> callback) {
        ApiClient.get("api/user/current", new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("获取用户信息失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<User> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<User>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(baseResp.getData());
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取用户公开信息
     */
    public void getUserInfo(long userId, UserCallback<User> callback) {
        ApiClient.get("api/user/info/" + userId, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("获取用户信息失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<User> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<User>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(baseResp.getData());
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
    
    /**
     * 批量获取用户信息
     */
    public void getBatchUserInfo(List<Long> userIds, UserCallback<List<User>> callback) {
        StringBuilder json = new StringBuilder("{\"userIds\":[");
        for (int i = 0; i < userIds.size(); i++) {
            if (i > 0) json.append(",");
            json.append(userIds.get(i));
        }
        json.append("]}");
        
        ApiClient.post("api/user/batch-info", json.toString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure("获取用户信息失败");
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respBody = response.body().string();
                BaseResponse<List<User>> baseResp = gson.fromJson(respBody, 
                    new TypeToken<BaseResponse<List<User>>>() {}.getType());
                
                if (baseResp.isSuccess()) {
                    callback.onSuccess(baseResp.getData());
                } else {
                    callback.onFailure(baseResp.getMessage());
                }
            }
        });
    }
}