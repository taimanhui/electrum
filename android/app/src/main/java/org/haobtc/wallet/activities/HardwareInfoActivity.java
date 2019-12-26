package org.haobtc.wallet.activities;

import android.view.View;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.set.VersionUpgradeActivity;
import org.haobtc.wallet.utils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HardwareInfoActivity extends BaseActivity {
    @BindView(R.id.tet_Serial)
    TextView tetSerial;
    @BindView(R.id.tet_versonUp)
    TextView tetVersonUp;
    @BindView(R.id.id_2)
    TextView tetAddress;

    @Override
    public int getLayoutId() {
        return R.layout.hardware_info;
    }

    @Override
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        CommonUtils.enableToolBar(this, R.string.hardware_i);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.tet_Serial, R.id.tet_versonUp, R.id.id_2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_Serial:

                break;
            case R.id.tet_versonUp:
                mIntent(VersionUpgradeActivity.class);
                break;
            case R.id.id_2:
                
                break;
        }
    }
}
