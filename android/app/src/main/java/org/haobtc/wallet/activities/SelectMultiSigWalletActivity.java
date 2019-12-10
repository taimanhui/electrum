package org.haobtc.wallet.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gyf.immersionbar.ImmersionBar;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class SelectMultiSigWalletActivity extends BaseActivity {

    public int getLayoutId() {
        return R.layout.select_mutisig_wallet;
    }

    public void initView() {
        CommonUtils.enableToolBar(this, R.string.select_mutisig);


    }

    @Override
    public void initData() {

    }
}
