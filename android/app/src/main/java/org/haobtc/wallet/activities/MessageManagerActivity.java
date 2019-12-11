package org.haobtc.wallet.activities;


import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class MessageManagerActivity extends BaseActivity {


    public int getLayoutId() {
        return R.layout.layout;
    }

    public void initView() {
        CommonUtils.enableToolBar(this, R.string.message_manager);

    }

    @Override
    public void initData() {

    }

}
