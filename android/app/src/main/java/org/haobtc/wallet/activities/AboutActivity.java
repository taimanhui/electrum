package org.haobtc.wallet.activities;

import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AboutActivity extends BaseActivity {

    @BindView(R.id.title_template)
    TextView titleTemplate;

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        CommonUtils.enableToolBar(this, R.string.about);
    }

    @Override
    public void initData() {

    }

}
