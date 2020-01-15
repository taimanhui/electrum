package org.haobtc.wallet.activities;

import android.view.View;
import android.widget.ImageView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionsSettingActivity extends BaseActivity {


    @BindView(R.id.img_back)
    ImageView imgBack;

    public int getLayoutId() {
        return R.layout.transaction_setting;
    }

    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

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
