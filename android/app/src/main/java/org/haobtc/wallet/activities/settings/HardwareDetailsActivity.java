package org.haobtc.wallet.activities.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.activities.service.NfcNotifyHelper;
import org.haobtc.wallet.activities.settings.recovery_set.BackupRecoveryActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.UpdateInfo;
import org.haobtc.wallet.event.ButtonRequestEvent;
import org.haobtc.wallet.event.ExistEvent;
import org.haobtc.wallet.event.FinishEvent;
import org.haobtc.wallet.event.FixBixinkeyNameEvent;
import org.haobtc.wallet.event.SetShutdownTimeEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.features;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.isNFC;

public class HardwareDetailsActivity extends BaseActivity {

    public static final String TAG = "org.haobtc.wallet.activities.settings.HardwareDetailsActivity";
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
    @BindView(R.id.change_pin)
    LinearLayout changePin;
    @BindView(R.id.lin_OnckFour)
    LinearLayout linOnckFour;
    @BindView(R.id.wipe_device)
    LinearLayout wipe_device;
    public static boolean dismiss;
    @BindView(R.id.test_shutdown_time)
    TextView testShutdownTime;
    private String bleName;
    private String device_id;
    private String label;
    private boolean isWipe;
    private SharedPreferences devices;
    @Override
    public int getLayoutId() {
        return R.layout.activity_somemore;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        inits();
    }

    private void inits() {
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        devices = getSharedPreferences("devices", MODE_PRIVATE);
        Intent intent = getIntent();
        bleName = intent.getStringExtra("bleName");
        String firmwareVersion = intent.getStringExtra("firmwareVersion");
        String bleVerson = intent.getStringExtra("bleVerson");
        device_id = intent.getStringExtra("device_id");
        label = intent.getStringExtra("label");
        if (!TextUtils.isEmpty(label)) {
            tetKeyName.setText(label);
        } else {
            tetKeyName.setText(String.format("%s", "BixinKEY"));
        }
//        tetCode.setText(firmwareVersion);
        String shutdownTime = preferences.getString(device_id, "600");
        testShutdownTime.setText(String.format("%s%s", shutdownTime, getString(R.string.second)));
    }

    @Override
    public void initData() {
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.lin_OnckOne, R.id.lin_OnckTwo, R.id.change_pin, R.id.lin_OnckFour, R.id.wipe_device, R.id.tetBluetoothSet, R.id.linear_shutdown_time, R.id.tetBuckup, R.id.tet_deleteWallet})
    public void onViewClicked(View view) {
        isWipe = false;
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.lin_OnckOne:
                Intent intent = new Intent(HardwareDetailsActivity.this, BixinKeyMessageActivity.class);
                intent.putExtra("bleName", bleName);
                intent.putExtra("label", label);
                intent.putExtra("device_id", device_id);
                startActivity(intent);
                break;
            case R.id.lin_OnckTwo:
                getUpdateInfo();
                break;
            case R.id.change_pin:
                Intent intent1 = new Intent(this, CommunicationModeSelector.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                intent1.putExtra("tag", TAG);
                startActivity(intent1);
                break;
            case R.id.lin_OnckFour:
                mIntent(ConfidentialPaymentSettings.class);
                break;
            case R.id.wipe_device:
                isWipe = true;
                mIntent(RecoverySetActivity.class);
                break;
            case R.id.tetBluetoothSet:
                mIntent(BixinKeyBluetoothSettingActivity.class);
                break;
            case R.id.linear_shutdown_time:
                Intent intent2 = new Intent(HardwareDetailsActivity.this, SetShutdownTimeActivity.class);
                intent2.putExtra("device_id", device_id);
                startActivity(intent2);
                break;
            case R.id.tetBuckup:
                mIntent(BackupRecoveryActivity.class);
                break;
            case R.id.tet_deleteWallet:
                devices.edit().remove(device_id).apply();
                EventBus.getDefault().post(new FixBixinkeyNameEvent(device_id));
                mToast(getString(R.string.delete_succse));
                finish();
                break;
        }
    }

    private void getUpdateInfo() {
        String urlPrefix = "https://key.bixin.com/";
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        String locate = preferences.getString("language", "");
        String info = preferences.getString("upgrade_info", "");
        UpdateInfo updateInfo = UpdateInfo.objectFromData(info);
        String urlNrf = updateInfo.getNrf().getUrl();
        String urlStm32 = updateInfo.getStm32().getUrl();
        String versionNrf = updateInfo.getNrf().getVersion();
        String versionStm32 = updateInfo.getStm32().getVersion().toString().replace(",", ".");
        versionStm32 = versionStm32.substring(1, versionStm32.length() - 1).replaceAll("\\s+", "");
        String descriptionNrf = "English".equals(locate) ? updateInfo.getNrf().getChangelogEn() : updateInfo.getNrf().getChangelogCn();
        String descriptionStm32 = "English".equals(locate) ? updateInfo.getStm32().getChangelogEn() : updateInfo.getStm32().getChangelogCn();
        Bundle bundle = new Bundle();
        bundle.putString("nrf_url", urlPrefix + urlNrf);
        bundle.putString("stm32_url", urlPrefix + urlStm32);
        bundle.putString("nrf_version", versionNrf);
        bundle.putString("stm32_version", versionStm32);
        bundle.putString("nrf_description", descriptionNrf);
        bundle.putString("stm32_description", descriptionStm32);
        Intent intentVersion = new Intent(this, VersionUpgradeActivity.class);
        intentVersion.putExtras(bundle);
        startActivity(intentVersion);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onButtonRequest(ButtonRequestEvent event) {
        if (isNFC) {
            if (isWipe && !features.isPinProtection()) {
                EventBus.getDefault().post(new ExistEvent());
                return;
            }
            EventBus.getDefault().removeStickyEvent(event);
            startActivity(new Intent(this, NfcNotifyHelper.class));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReading(FixBixinkeyNameEvent event) {
        tetKeyName.setText(event.getKeyname());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showtime(SetShutdownTimeEvent event) {
        testShutdownTime.setText(String.format("%s%s", event.getTime(), getString(R.string.second)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
