package org.haobtc.wallet.activities.onlywallet;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppWalletCreateFinishActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.img_Orcode)
    ImageView imgOrcode;
    @BindView(R.id.tet_bigMessage)
    TextView tetBigMessage;
    @BindView(R.id.tet_Preservation)
    TextView tetPreservation;
    @BindView(R.id.rel_Finish)
    RelativeLayout relFinish;

    @Override
    public int getLayoutId() {
        return R.layout.activity_app_wallet_create_finish;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.tet_Preservation, R.id.rel_Finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_Preservation:
                break;
            case R.id.rel_Finish:
                break;
        }
    }
}
