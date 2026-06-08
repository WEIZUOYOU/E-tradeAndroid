package com.example.e_tradeandroid.util;

import android.app.Activity;
import android.content.Intent;

import com.example.e_tradeandroid.R;
import com.example.e_tradeandroid.ui.MainActivity;
import com.example.e_tradeandroid.ui.MessageListActivity;
import com.example.e_tradeandroid.ui.MyProfileActivity;
import com.example.e_tradeandroid.ui.PublishActivity;

/**
 * 导航栏跳转工具类
 * 统一处理底部导航栏页面跳转，避免重复创建Activity
 */
public class NavUtils {

    /**
     * 跳转到首页
     */
    public static void goToHome(Activity from) {
        if (from instanceof MainActivity) return;
        Intent intent = new Intent(from, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        from.startActivity(intent);
        from.finish();
    }

    /**
     * 跳转到发布页面
     */
    public static void goToPublish(Activity from) {
        if (from instanceof PublishActivity) return;
        Intent intent = new Intent(from, PublishActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        from.startActivity(intent);
        from.finish();
    }

    /**
     * 跳转到消息页面
     */
    public static void goToMessages(Activity from) {
        if (from instanceof MessageListActivity) return;
        Intent intent = new Intent(from, MessageListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        from.startActivity(intent);
        from.finish();
    }

    /**
     * 跳转到个人中心
     */
    public static void goToProfile(Activity from) {
        if (from instanceof MyProfileActivity) return;
        Intent intent = new Intent(from, MyProfileActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        from.startActivity(intent);
        from.finish();
    }

    /**
     * 处理导航栏点击事件
     * @param from 当前Activity
     * @param itemId 点击的菜单项ID
     * @return 是否已处理
     */
    public static boolean handleNavClick(Activity from, int itemId) {
        if (itemId == R.id.nav_home) {
            goToHome(from);
            return true;
        } else if (itemId == R.id.nav_publish) {
            goToPublish(from);
            return true;
        } else if (itemId == R.id.nav_messages) {
            goToMessages(from);
            return true;
        } else if (itemId == R.id.nav_profile) {
            goToProfile(from);
            return true;
        }
        return false;
    }
}
