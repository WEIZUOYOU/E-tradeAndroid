package com.example.e_tradeandroid.network;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;

public class ApiClient {
    public static final String BASE_URL = "http://10.0.2.2:8080/api/";
    private static OkHttpClient client;
    private static SharedPreferences cookiePrefs;
    private static final String COOKIE_PREF_NAME = "cookies";
    private static final String COOKIE_KEY = "cookies_set";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static SharedPreferences userSp;
    private static final String SP_USER = "user_info";
    private static final String KEY_USER_ID = "user_id";

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
                        for (Cookie c : cookies) sb.append(c.toString()).append(";");
                        cookiePrefs.edit().putString(COOKIE_KEY, sb.toString()).apply();
                    }

                    @Override
                    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                        String s = cookiePrefs.getString(COOKIE_KEY, "");
                        if (!s.isEmpty()) {
                            for (String part : s.split(";")) {
                                Cookie c = Cookie.parse(url, part);
                                if (c != null) cookieStore.add(c);
                            }
                        }
                        return new ArrayList<>(cookieStore);
                    }
                })
                .build();
    }

    public static OkHttpClient getClient() {
        if (client == null) throw new IllegalStateException("请先初始化 ApiClient");
        return client;
    }

    // 补上 getHttpClient 兼容旧代码
    public static OkHttpClient getHttpClient() {
        return getClient();
    }

    public static long getCurrentUserId() {
        return userSp == null ? 1 : userSp.getLong(KEY_USER_ID, 1);
    }

    // 补上 saveUserId 兼容旧代码
    public static void saveUserId(long userId) {
        if (userSp != null) {
            userSp.edit().putLong(KEY_USER_ID, userId).apply();
        }
    }

    // 补上 clearCookies 兼容旧代码
    public static void clearCookies() {
        cookiePrefs.edit().remove(COOKIE_KEY).apply();
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
                    }

                    @Override
                    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                        return new ArrayList<>(cookieStore);
                    }
                })
                .build();
    }

    public static void get(String url, Callback callback) {
        Request req = new Request.Builder().url(BASE_URL + url).build();
        getClient().newCall(req).enqueue(callback);
    }

    public static void post(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(json, JSON);
        Request req = new Request.Builder().url(BASE_URL + url).post(body).build();
        getClient().newCall(req).enqueue(callback);
    }

    public static void put(String url, String json, Callback callback) {
        RequestBody body = RequestBody.create(json, JSON);
        Request req = new Request.Builder().url(BASE_URL + url).put(body).build();
        getClient().newCall(req).enqueue(callback);
    }

    public static void delete(String url, Callback callback) {
        Request req = new Request.Builder().url(BASE_URL + url).delete().build();
        getClient().newCall(req).enqueue(callback);
    }
}