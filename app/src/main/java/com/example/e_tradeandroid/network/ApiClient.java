package com.example.e_tradeandroid.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final String TAG = "ApiClient";
    public static final String BASE_URL = "http://10.0.2.2:8080/";
    private static OkHttpClient client;
    private static SharedPreferences cookiePrefs;
    private static final String COOKIE_PREF_NAME = "cookies";
    private static final String COOKIE_KEY = "cookies_set";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static SharedPreferences userSp;
    private static final String SP_USER = "user_info";
    private static final String KEY_USER_ID = "user_id";
    private static boolean initialized = false;
    
    /**
     * 获取 WebSocket 连接地址
     * 将 HTTP URL 转换为 WebSocket URL
     */
    public static String getWebSocketUrl() {
        String httpUrl = BASE_URL;
        // 将 http:// 替换为 ws://，去掉末尾的斜杠，添加 WebSocket 路径
        return httpUrl.replace("http://", "ws://").replaceAll("/$", "") + "/ws/message";
    }

    public static synchronized void init(Context context) {
        // 防止重复初始化
        if (initialized && client != null) {
            Log.d(TAG, "ApiClient 已初始化，跳过重复初始化");
            return;
        }
        
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
                    // 按域名存储 Cookie
                    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
                        String host = url.host();
                        Log.d(TAG, "saveFromResponse: host=" + host + ", cookies=" + cookies);
                        
                        // 过滤过期 Cookie
                        long now = System.currentTimeMillis();
                        List<Cookie> validCookies = new ArrayList<>();
                        for (Cookie c : cookies) {
                            if (c.expiresAt() > now) {
                                validCookies.add(c);
                            }
                        }
                        
                        cookieStore.put(host, validCookies);
                        
                        // 持久化到 SharedPreferences
                        StringBuilder sb = new StringBuilder();
                        for (Cookie c : validCookies) {
                            sb.append(c.toString()).append("|||");
                        }
                        cookiePrefs.edit().putString(COOKIE_KEY + "_" + host, sb.toString()).apply();
                        Log.d(TAG, "Cookie 已保存到 SharedPreferences: " + sb);
                    }

                    @Override
                    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                        String host = url.host();
                        List<Cookie> cookies = cookieStore.get(host);
                        
                        // 如果内存中没有，从 SharedPreferences 加载
                        if (cookies == null || cookies.isEmpty()) {
                            String stored = cookiePrefs.getString(COOKIE_KEY + "_" + host, "");
                            Log.d(TAG, "loadForRequest: host=" + host + ", storedCookies=" + stored);
                            
                            cookies = new ArrayList<>();
                            if (!stored.isEmpty()) {
                                for (String part : stored.split("\\|\\|\\|")) {
                                    if (!part.trim().isEmpty()) {
                                        Cookie c = Cookie.parse(url, part.trim());
                                        if (c != null && c.expiresAt() > System.currentTimeMillis()) {
                                            cookies.add(c);
                                        }
                                    }
                                }
                            }
                            if (!cookies.isEmpty()) {
                                cookieStore.put(host, cookies);
                            }
                        } else {
                            Log.d(TAG, "loadForRequest: host=" + host + ", fromMemory, count=" + cookies.size());
                        }
                        
                        return cookies != null ? cookies : new ArrayList<>();
                    }
                })
                .build();
        
        initialized = true;
        Log.d(TAG, "ApiClient 初始化完成");
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

    /**
     * 构建图片完整 URL
     * 自动处理前导斜杠和双斜杠问题
     * @param imagePath 图片路径（可能是完整 URL 或相对路径）
     * @return 完整的图片 URL
     */
    public static String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return null;
        // 如果已经是完整 URL，直接返回
        if (imagePath.startsWith("http")) return imagePath;
        // 去掉开头的斜杠（如果有）
        String path = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        // 拼接 BASE_URL
        return BASE_URL + path;
    }

    // 补上 saveUserId 兼容旧代码
    public static void saveUserId(long userId) {
        if (userSp != null) {
            userSp.edit().putLong(KEY_USER_ID, userId).apply();
        }
    }

    // 补上 clearCookies 兼容旧代码
    public static void clearCookies() {
        if (cookiePrefs != null) {
            cookiePrefs.edit().clear().apply();
        }
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .cookieJar(new CookieJar() {
                    private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
                        return cookieStore.getOrDefault(url.host(), new ArrayList<>());
                    }
                })
                .build();
        Log.d(TAG, "Cookie 已清除");
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
