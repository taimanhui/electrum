package com.bixin.coldwallet.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bixin.coldwallet.R;
import com.bixin.coldwallet.utils.CommonUtils;

public class SendOne2ManyMainPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_one2many_main);
        initView();
    }

    private void initView() {
        CommonUtils.enableToolBar(this, R.string.send);


    }
}
