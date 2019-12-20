package org.haobtc.wallet.activities;

import android.content.Intent;
import android.widget.Button;



import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

public class WalletUnActivatedActivity extends BaseActivity {
    Button buttonActivate;
    public static final String TAG = "org.wallet.activities.WalletUnActivatedActivity";


    @Override
    public int getLayoutId() {
        return R.layout.activate;
    }

    public void initView() {
        buttonActivate = findViewById(R.id.button_activate);
        buttonActivate.setOnClickListener(v -> {
            Intent intent = new Intent(this, TouchHardwareActivity.class);
            intent.putExtra(TouchHardwareActivity.FROM, TAG);
            startActivity(intent);
        });
    }

    @Override
    public void initData() {

    }
}
