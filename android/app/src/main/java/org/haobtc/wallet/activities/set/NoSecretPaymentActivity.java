package org.haobtc.wallet.activities.set;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NoSecretPaymentActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.switch_noPass)
    Switch switchNoPass;
    @BindView(R.id.tetHour24)
    TextView tetHour24;
    @BindView(R.id.switch_noPin)
    Switch switchNoPin;
    @BindView(R.id.switch_noHard)
    Switch switchNoHard;
    @BindView(R.id.bn_multi_next)
    Button bnMultiNext;

    @Override
    public int getLayoutId() {
        return R.layout.activity_no_secret_payment;
    }

    @Override
    public void initView() {
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
