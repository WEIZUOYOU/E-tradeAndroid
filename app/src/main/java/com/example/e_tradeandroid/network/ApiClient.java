package com.example.e_tradeandroid.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiClient {
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";
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
                        cookieStore.addAll(cookies);
                        // 持久化：将Cookie集合转为字符串存储
                        StringBuilder sb = new StringBuilder();
                        for (Cookie cookie : cookies) {
                            sb.append(cookie.toString()).append(";");
                        }
                        cookiePrefs.edit().putString(COOKIE_KEY, sb.toString()).apply();
                    }

                    @Override
                    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                        String saved = cookiePrefs.getString(COOKIE_KEY, "");
                        if (!saved.isEmpty()) {
                            String[] parts = saved.split(";");
                            for (String part : parts) {
                                if (part.trim().isEmpty()) continue;
                                Cookie cookie = Cookie.parse(url, part);
                                if (cookie != null) cookieStore.add(cookie);
                            }
                        }
                        return new ArrayList<>(cookieStore);
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

    public static void clearCookies() {
        cookiePrefs.edit().remove(COOKIE_KEY).apply();
        // 重新创建client使cookieStore重置（简单方法：置null后下次getClient会抛异常，需重新init）
        // 更好的做法是重建client，但为了简单，调用方需重新init
        client = null;
    }
}