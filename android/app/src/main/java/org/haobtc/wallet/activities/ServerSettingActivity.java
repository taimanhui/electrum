package org.haobtc.wallet.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import com.gyf.immersionbar.ImmersionBar;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class ServerSettingActivity extends BaseActivity {

    public int getLayoutId() {
        return R.layout.server_setting;
    }

    public void initView() {
        CommonUtils.enableToolBar(this, R.string.server_setting);
    }

    @Override
    public void initData() {

    }
}
