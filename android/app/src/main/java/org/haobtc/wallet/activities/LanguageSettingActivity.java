package org.haobtc.wallet.activities;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class LanguageSettingActivity extends BaseActivity {

    public int getLayoutId() {
        return R.layout.language_setting;
    }

    public void initView() {
        CommonUtils.enableToolBar(this, R.string.language);
    }

    @Override
    public void initData() {

    }
}
