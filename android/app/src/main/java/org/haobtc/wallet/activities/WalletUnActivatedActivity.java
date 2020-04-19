package org.haobtc.wallet.activities;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoveryActivity;
import org.haobtc.wallet.aop.SingleClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletUnActivatedActivity extends BaseActivity {
    public static final String TAG = "org.wallet.activities.WalletUnActivatedActivity";
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.button_activate)
    Button buttonActivate;
    @BindView(R.id.button_recover)
    Button buttonRecover;


    @Override
    public int getLayoutId() {
        return R.layout.activate;
    }

    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }


    @SingleClick
    @OnClick({R.id.img_back, R.id.button_activate, R.id.button_recover})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finishAffinity();
                break;
            case R.id.button_activate:
                startActivity(new Intent(this, SetNameActivity.class));
                finish();
                break;
            case R.id.button_recover:
                startActivity(new Intent(this, RecoveryActivity.class));
                finish();
        }
    }
}
