package org.haobtc.wallet.activities.set;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class AgreementActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.introduce;
    }

    @Override
    public void initView() {
        CommonUtils.enableToolBar(this, R.string.bixinmoney);


    }

    @Override
    public void initData() {

    }
}
