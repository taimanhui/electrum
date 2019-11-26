package com.bixin.coldwallet.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bixin.coldwallet.R;
import com.bixin.coldwallet.utils.CommonUtils;

public class ReceivedPageActivity extends AboutActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_info);
        initView();
    }
    private void initView() {
        CommonUtils.enableToolBar(this, R.string.receive);
    }
}
