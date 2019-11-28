package org.haobtc.wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.haobtc.wallet.R;
import org.haobtc.wallet.utils.CommonUtils;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        initView();

    }

    private void initView() {
        CommonUtils.enableToolBar(this, R.string.settings);
        TextView v1, v2, v3, v4, v5, v6, v7;
       v1 = findViewById(R.id.s1);
       v2 = findViewById(R.id.s2);
       v3 = findViewById(R.id.s3);
       v4 = findViewById(R.id.s4);
       v5 = findViewById(R.id.s5);
       v6 = findViewById(R.id.s6);
       v7 = findViewById(R.id.s7);
       v1.setOnClickListener(v -> {
           Intent intent = new Intent(this, HardwareInfoActivity.class);
           startActivity(intent);
       });
        v2.setOnClickListener(v -> {
            Intent intent = new Intent(this, MessageManagerActivity.class);
            startActivity(intent);
        });
        v3.setOnClickListener(v -> {
            Intent intent = new Intent(this, LanguageSettingActivity.class);
            startActivity(intent);
        });
        v4.setOnClickListener(v -> {
            Intent intent = new Intent(this, ServerSettingActivity.class);
            startActivity(intent);
        });
        v5.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionsSettingActivity.class);
            startActivity(intent);
        });
        v6.setOnClickListener(v -> {
            Intent intent = new Intent(this, ServiceOnlineActivity.class);
            startActivity(intent);
        });
        v7.setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        });
    }
}
