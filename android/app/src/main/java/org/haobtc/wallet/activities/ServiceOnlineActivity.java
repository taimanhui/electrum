package org.haobtc.wallet.activities;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class ServiceOnlineActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_serviceonline;
    }

    @Override
    public void initView() {
        CommonUtils.enableToolBar(this, R.string.service_online);
    }

    @Override
    public void initData() {

    }

}
