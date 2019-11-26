package com.bixin.coldwallet.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bixin.coldwallet.R;
import com.bixin.coldwallet.utils.CommonUtils;

public class TransactionsSettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaction_setting);
        initView();
    }
    private void initView() {
        CommonUtils.enableToolBar(this, R.string.transaction_setting);
    }

}
