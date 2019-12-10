package org.haobtc.wallet.activities;

import android.widget.TextView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

public class ActivatedProcessing extends BaseActivity {
    private TextView textViewConnect, textViewPIN, textViewProcess;

    public int getLayoutId() {
        return R.layout.activing_process;
    }
    @Override
    public void initView() {
        textViewConnect = findViewById(R.id.connect_state);
        textViewPIN = findViewById(R.id.pin_setting_state);
        textViewProcess = findViewById(R.id.activate_state);
    }

    @Override
    public void initData() {

    }
}
