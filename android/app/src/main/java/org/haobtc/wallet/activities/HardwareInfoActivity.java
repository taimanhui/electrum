package org.haobtc.wallet.activities;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class HardwareInfoActivity extends BaseActivity {
    @Override
    public int getLayoutId() {
        return R.layout.hardware_info;
    }

    @Override
    public void initView() {
        CommonUtils.enableToolBar(this, R.string.hardware_i);
    }

    @Override
    public void initData() {

    }

}
