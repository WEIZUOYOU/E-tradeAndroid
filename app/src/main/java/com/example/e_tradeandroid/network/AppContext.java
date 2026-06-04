package com.example.e_tradeandroid.network;

import android.app.Application;
import android.content.Context;

public class AppContext extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        // 在这里初始化 ApiClient，传入 Application Context
        ApiClient.init(this);
    }

    public static Context getContext() {
        return context;
    }
}