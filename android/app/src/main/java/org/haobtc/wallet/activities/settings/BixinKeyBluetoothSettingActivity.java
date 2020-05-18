package org.haobtc.wallet.activities.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.SettingActivity;
import org.haobtc.wallet.activities.VerificationKEYActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.event.SetBluetoothEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.xpub;

public class BixinKeyBluetoothSettingActivity extends BaseActivity {

    public static final String TAG_TRUE = BixinKeyBluetoothSettingActivity.class.getSimpleName();
    public static final String TAG_FALSE = "CHECKED_FALSE";
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
        if (change_use_ble) {
            switchHideBluetooth.setChecked(true);
        } else {
            switchHideBluetooth.setChecked(false);
        }
    }

    @Override
    public void initData() {
        blueToothSetToHardware();
//        switchHideBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    try {
//                        Daemon.commands.callAttr("change_use_ble", true);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    edit.putBoolean("change_use_ble", true);
//                    edit.apply();
//                    mToast(getString(R.string.set_success));
//                } else {
//                    try {
//                        Daemon.commands.callAttr("change_use_ble", false);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    edit.putBoolean("change_use_ble", false);
//                    edit.apply();
//                }
//            }
//        });
    }

    private void blueToothSetToHardware() {
        switchHideBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    nModeSelector(true);
                } else {
                    nModeSelector(false);
                }
            }
        });
    }

    private void nModeSelector(boolean isChecked){
        if (isChecked){
//            CommunicationModeSelector.runnables.clear();
//            CommunicationModeSelector.runnables.add(runnable);
            Intent intent = new Intent(BixinKeyBluetoothSettingActivity.this, CommunicationModeSelector.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtra("tag", TAG_TRUE);
            startActivity(intent);
        }else{
//            CommunicationModeSelector.runnables.clear();
//            CommunicationModeSelector.runnables.add(runnable);
            Intent intent = new Intent(BixinKeyBluetoothSettingActivity.this, CommunicationModeSelector.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.putExtra("tag", TAG_FALSE);
            startActivity(intent);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SetBluetoothEvent updataHint) {
        String msgStatus = updataHint.getStatus();
        if (!TextUtils.isEmpty(msgStatus)){
            Log.i("SetBluetoothEvent", "event:::::::::::: "+updataHint);
        }
    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }
}
