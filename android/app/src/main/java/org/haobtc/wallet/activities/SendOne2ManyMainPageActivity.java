package org.haobtc.wallet.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gyf.immersionbar.ImmersionBar;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class SendOne2ManyMainPageActivity extends BaseActivity {


    public int getLayoutId() {
        return R.layout.send_one2many_main;
    }

    public void initView() {
        CommonUtils.enableToolBar(this, R.string.send);


    }

    @Override
    public void initData() {

    }
}
