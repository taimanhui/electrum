package org.haobtc.wallet.activities.settings;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.fixpin.InputOldPINActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HardwareDetailsActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_keyName)
    TextView tetKeyName;
    @BindView(R.id.lin_OnckOne)
    LinearLayout linOnckOne;
    @BindView(R.id.tet_code)
    TextView tetCode;
    @BindView(R.id.lin_OnckTwo)
    LinearLayout linOnckTwo;
    @BindView(R.id.lin_OnckThree)
    LinearLayout linOnckThree;
    @BindView(R.id.tet_noPasspay)
    TextView tetNoPasspay;
    @BindView(R.id.lin_OnckFour)
    LinearLayout linOnckFour;
    @BindView(R.id.lin_OnckFive)
    LinearLayout linOnckFive;
    @BindView(R.id.tetKeyname)
    TextView tetKeyname;

    @Override
    public int getLayoutId() {
        return R.layout.activity_somemore;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String keyListItem = intent.getStringExtra("keyListItem");
        tetKeyName.setText(keyListItem);
        tetKeyname.setText(String.format("%s%s", keyListItem, getString(R.string.settings)));
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.lin_OnckOne, R.id.lin_OnckTwo, R.id.lin_OnckThree, R.id.lin_OnckFour, R.id.lin_OnckFive})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.lin_OnckOne:
                mIntent(BixinKeyMessageActivity.class);
                break;
            case R.id.lin_OnckTwo:
                mIntent(VersionUpgradeActivity.class);
                break;
            case R.id.lin_OnckThree:
                mIntent(InputOldPINActivity.class);
                break;
            case R.id.lin_OnckFour:
                mIntent(ConfidentialPaymentSettings.class);
                break;
            case R.id.lin_OnckFive:
                mIntent(RecoverySetActivity.class);
                break;
        }
    }

}
