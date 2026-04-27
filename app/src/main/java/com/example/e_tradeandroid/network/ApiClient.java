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

// 新增导入
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ApiClient {
    public static final String BASE_URL = "http://10.0.2.2:4523/m1/8086391-7842204-default/api/";
    private static OkHttpClient client;
    private static SharedPreferences cookiePrefs;
    private static final String COOKIE_PREF_NAME = "cookies";
    private static final String COOKIE_KEY = "cookies_set";

    // ==================== 新增用户存储 ====================
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String SP_USER = "user_info";
    private static final String KEY_USER_ID = "user_id";
    private static SharedPreferences userSp;

    public static void init(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREF_NAME, Context.MODE_PRIVATE);
        userSp = context.getSharedPreferences(SP_USER, Context.MODE_PRIVATE);

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
        client = null;
    }

    // ==================== 兼容页面的核心方法 ====================
    public static int getCurrentUserId() {
        if (userSp == null) return 1;
        return userSp.getInt(KEY_USER_ID, 1);
    }

    public static void saveUserId(int userId) {
        if (userSp != null) {
            userSp.edit().putInt(KEY_USER_ID, userId).apply();
        }
    }

    public static OkHttpClient getHttpClient() {
        return getClient();
    }

    public static void get(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + url)
                .build();
        getClient().newCall(request).enqueue(callback);
    }

    public static void post(String url, String jsonBody, Callback callback) {
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + url)
                .post(body)
                .build();
        getClient().newCall(request).enqueue(callback);
    }
}