package org.haobtc.wallet.activities.set;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class VersionUpgradeActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_version_upgrade;
    }

    @Override
    public void initView() {
        CommonUtils.enableToolBar(this, R.string.hardware);

    }

    @Override
    public void initData() {

    }
}
