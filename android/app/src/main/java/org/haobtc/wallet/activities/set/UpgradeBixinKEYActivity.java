package org.haobtc.wallet.activities.set;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UpgradeBixinKEYActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_test)
    TextView tetTest;
    @BindView(R.id.progressUpgrade)
    ProgressBar progressUpgrade;
    @BindView(R.id.tetUpgradeTest)
    TextView tetUpgradeTest;
    @BindView(R.id.tetUpgradeNum)
    TextView tetUpgradeNum;

    @Override
    public int getLayoutId() {
        return R.layout.activity_upgrade_bixin_k_e_y;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {
        //Go in to finish the page after loading
//        mIntent(UpgradeFinishedActivity.class);
//        finish();

    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }
}
