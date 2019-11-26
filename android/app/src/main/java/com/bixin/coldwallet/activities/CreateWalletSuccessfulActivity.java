package com.bixin.coldwallet.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bixin.coldwallet.R;
import com.bixin.coldwallet.utils.CommonUtils;

public class CreateWalletSuccessfulActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crete_successful);
        initView();
    }

    private void initView() {
        CommonUtils.enableToolBar(this, R.string.create_successful);
    }
}
