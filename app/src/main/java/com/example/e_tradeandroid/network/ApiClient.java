package com.example.e_tradeandroid.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiClient {
    public static final String BASE_URL = "http://10.0.2.2:8080/api/"; // 模拟器访问本机用10.0.2.2
    private static OkHttpClient client;
    private static SharedPreferences cookiePrefs;
    private static final String COOKIE_PREF_NAME = "cookies";
    private static final String COOKIE_KEY = "cookies_set";

    public static void init(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREF_NAME, Context.MODE_PRIVATE);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .cookieJar(new CookieJar() {
                    private final Set<Cookie> cookieStore = new HashSet<>();

                    @Override
                    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
                        // 保存Cookie到内存
                        cookieStore.addAll(cookies);
                        // 持久化到SharedPreferences（可选）
                        StringBuilder cookieStr = new StringBuilder();
                        for (Cookie cookie : cookies) {
                            cookieStr.append(cookie.toString()).append(";");
                        }
                        cookiePrefs.edit().putString(COOKIE_KEY, cookieStr.toString()).apply();
                    }

                    @Override
                    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                        // 从持久化恢复
                        String saved = cookiePrefs.getString(COOKIE_KEY, "");
                        if (!saved.isEmpty()) {
                            String[] parts = saved.split(";");
                            for (String part : parts) {
                                if (part.trim().isEmpty()) continue;
                                Cookie cookie = Cookie.parse(url, part);
                                if (cookie != null) cookieStore.add(cookie);
                            }
                        }
                        return new java.util.ArrayList<>(cookieStore);
                    }
                })
                .build();
    }

    public static OkHttpClient getClient() {
        if (client == null) {
            throw new IllegalStateException("ApiClient未初始化，请先调用init(Context)");
        }
        return client;
    }

    // 清除登录状态（退出登录时调用）
    public static void clearCookies() {
        cookiePrefs.edit().remove(COOKIE_KEY).apply();
        // 重置client中的CookieJar需要重建，简单起见直接置空再重新初始化，生产环境建议优雅处理
        client = null;
    }
}