package org.haobtc.wallet.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserAgreementActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;

    @Override
    public int getLayoutId() {
        return R.layout.activity_user_agreement;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }

    @OnClick(R.id.img_back)
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back){
            finish();
        }
    }
}
