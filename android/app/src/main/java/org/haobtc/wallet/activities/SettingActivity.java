package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.azhon.appupdate.utils.ApkUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.BixinKEYMenageActivity;
import org.haobtc.wallet.activities.settings.BlueToothStatusActivity;
import org.haobtc.wallet.activities.settings.CurrencyActivity;
import org.haobtc.wallet.activities.settings.recovery_set.BackupRecoveryActivity;
import org.haobtc.wallet.event.FirstEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.tetBuckup)
    TextView tetBuckup;
    @BindView(R.id.tet_language)
    TextView tet_language;
    @BindView(R.id.tetSeverSet)
    TextView tetSeverSet;
    @BindView(R.id.tetTrsactionSet)
    TextView tetTrsactionSet;
    @BindView(R.id.tetVerification)
    TextView tetVerification;
    @BindView(R.id.tetAbout)
    TextView tetAbout;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_bixinKey)
    TextView tetBixinKey;
    @BindView(R.id.tet_Faru)
    TextView tetFaru;
    @BindView(R.id.tet_verson)
    TextView tetVerson;
    @BindView(R.id.bluetooth_status)
    TextView bluetoothStatusText;
    private boolean bluetoothStatus;
    private SharedPreferences preferences;

    @Override
    public int getLayoutId() {
        return R.layout.setting;
    }

    @SuppressLint("CommitPrefEdits")
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        bluetoothStatus = preferences.getBoolean("bluetoothStatus", false);
        if (!bluetoothStatus) {
            bluetoothStatusText.setText(getString(R.string.close));
        }else{
            bluetoothStatusText.setText(getString(R.string.open));
        }
    }

    @Override
    public void initData() {
        String versionName = ApkUtil.getVersionName(this);
        tetVerson.setText(String.format("V%s", versionName));

    }

    @OnClick({R.id.tetBuckup, R.id.tet_language, R.id.tetSeverSet, R.id.tetTrsactionSet, R.id.tetVerification, R.id.tetAbout, R.id.img_back, R.id.tet_bixinKey, R.id.tet_Faru, R.id.bluetooth_set})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_bixinKey:
                mIntent(BixinKEYMenageActivity.class);
                break;
            case R.id.tetBuckup:
                mIntent(BackupRecoveryActivity.class);
                break;
            case R.id.tet_language:
                mIntent(LanguageSettingActivity.class);
                break;
            case R.id.tetSeverSet:
                mIntent(ServerSettingActivity.class);
                break;
            case R.id.tetTrsactionSet:
                mIntent(TransactionsSettingActivity.class);
                break;
            case R.id.tetVerification:
                mIntent(VerificationKEYActivity.class);
                break;
            case R.id.tetAbout:
                mIntent(AboutActivity.class);
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_Faru:
                mIntent(CurrencyActivity.class);
                break;
            case R.id.bluetooth_set:
                mIntent(BlueToothStatusActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(FirstEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (msgVote.equals("bluetooth_status")) {
            bluetoothStatus = preferences.getBoolean("bluetoothStatus", false);
            if (!bluetoothStatus) {
                bluetoothStatusText.setText(getString(R.string.close));
            }else{
                bluetoothStatusText.setText(getString(R.string.open));
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
