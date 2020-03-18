package org.haobtc.wallet.activities;

import android.view.View;
import android.widget.ImageView;
import android.content.Intent;
import android.widget.Button;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.ManyWalletTogetherActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivateSuccessActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.bn_back)
    Button bnBack;

    public int getLayoutId() {
        return R.layout.activated_successful;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.bn_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.bn_back:
                Intent intent = new Intent(this, CreateWalletActivity.class);
                startActivity(intent);
                finish();
        }
    }
}
