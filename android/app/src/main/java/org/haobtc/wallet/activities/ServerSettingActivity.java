package org.haobtc.wallet.activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.set.BlockChooseActivity;
import org.haobtc.wallet.activities.set.ElectrumNodeChooseActivity;
import org.haobtc.wallet.activities.set.QuetationChooseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ServerSettingActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.switch_cynchronez)
    Switch switchCynchronez;
    @BindView(R.id.rel_quotationChoose)
    RelativeLayout relQuotationChoose;
    @BindView(R.id.rel_blockChoose)
    RelativeLayout relBlockChoose;
    @BindView(R.id.rel_Electrum_Choose)
    RelativeLayout relElectrumChoose;

    public int getLayoutId() {
        return R.layout.server_setting;
    }

    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {


    }

    @OnClick({R.id.img_back, R.id.rel_quotationChoose, R.id.rel_blockChoose, R.id.rel_Electrum_Choose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.rel_quotationChoose:
                mIntent(QuetationChooseActivity.class);
                break;
            case R.id.rel_blockChoose:
                mIntent(BlockChooseActivity.class);
                break;
            case R.id.rel_Electrum_Choose:
                mIntent(ElectrumNodeChooseActivity.class);
                break;
        }
    }
}
