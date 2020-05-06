package org.haobtc.wallet.activities.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BixinKeyBluetoothSettingActivity extends BaseActivity {

    @BindView(R.id.switchHideBluetooth)
    Switch switchHideBluetooth;
    private SharedPreferences.Editor edit;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bixin_key_bluetooth_setting;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        boolean change_use_ble = preferences.getBoolean("change_use_ble", false);
        if (change_use_ble){
            switchHideBluetooth.setChecked(true);
        }else{
            switchHideBluetooth.setChecked(false);
        }
    }

    @Override
    public void initData() {
        switchHideBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        Daemon.commands.callAttr("change_use_ble", true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("change_use_ble", true);
                    edit.apply();
                    mToast(getString(R.string.set_success));
                } else {
                    try {
                        Daemon.commands.callAttr("change_use_ble", false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("change_use_ble", false);
                    edit.apply();
                }
            }
        });
    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }
}
