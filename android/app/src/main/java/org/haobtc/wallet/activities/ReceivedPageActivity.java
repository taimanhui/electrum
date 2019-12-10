package org.haobtc.wallet.activities;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class ReceivedPageActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.address_info;
    }
    @Override
    public void initView() {
        CommonUtils.enableToolBar(this, R.string.receive);
    }

    @Override
    public void initData() {

    }
}

