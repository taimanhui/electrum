package org.haobtc.wallet.activities;


import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class TransactionsSettingActivity extends BaseActivity {


    public int getLayoutId() {
        return R.layout.transaction_setting;
    }

    public void initView() {
        CommonUtils.enableToolBar(this, R.string.transaction_setting);
    }

    @Override
    public void initData() {

    }

}
