package com.example.e_tradeandroid.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.e_tradeandroid.R;

public class MyProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        TextView tvInfo = findViewById(R.id.tv_info);
        tvInfo.setText("我的资料页面（开发中）");
    }
}