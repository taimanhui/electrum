package org.haobtc.wallet.activities.set;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VersionUpgradeActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.btn_toUpgrade)
    Button btnToUpgrade;
    @BindView(R.id.tet_firmware)
    TextView tetFirmware;
    @BindView(R.id.checkBox_firmware)
    CheckBox checkBoxFirmware;
    @BindView(R.id.tet_bluetooth)
    TextView tetBluetooth;
    @BindView(R.id.checkBox_bluetooth)
    CheckBox checkBoxBluetooth;
    private int checkWitch = 1;

    @Override
    public int getLayoutId() {
        return R.layout.activity_version_upgrade;
    }

    @Override
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {
        checkBoxClick();
    }

    private void checkBoxClick() {
        checkBoxFirmware.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    checkBoxBluetooth.setChecked(false);
                    checkWitch = 1;
                }else{
                    checkWitch = 0;
                }
            }
        });
        checkBoxBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    checkBoxFirmware.setChecked(false);
                    checkWitch = 2;
                }else{
                    checkWitch = 0;
                }
            }
        });
    }

    @OnClick({R.id.img_back, R.id.btn_toUpgrade})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_toUpgrade:
                if (checkWitch == 0){
                    mToast(getString(R.string.please_choose_firmware));
                    return;
                }
                Intent intent = new Intent(VersionUpgradeActivity.this, UpgradeBixinKEYActivity.class);
                startActivity(intent);
                break;
        }
    }

}
