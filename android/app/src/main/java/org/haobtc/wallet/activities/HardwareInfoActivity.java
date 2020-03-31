package org.haobtc.wallet.activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.VersionUpgradeActivity;

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
    @BindView(R.id.img_back)
    ImageView imgBack;

    @Override
    public int getLayoutId() {
        return R.layout.hardware_info;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.tet_Serial, R.id.tet_versonUp, R.id.id_2,R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_Serial:
                mToast(getString(R.string.no_serialnumber));
                break;
            case R.id.tet_versonUp:
                mIntent(VersionUpgradeActivity.class);
                break;
            case R.id.id_2:
                mToast(getString(R.string.no_address));
                break;
            case R.id.img_back:
                finish();
                break;
        }
    }

}
