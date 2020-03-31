package org.haobtc.wallet.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }


    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }
}
