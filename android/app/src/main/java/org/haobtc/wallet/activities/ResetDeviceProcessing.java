package org.haobtc.wallet.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.haobtc.wallet.R;
import org.haobtc.wallet.ResetDeviceSuccessActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.fixpin.ConfirmPincodeActivity;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.heaton.blelibrary.ble.Ble;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.futureTask;

public class ResetDeviceProcessing extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.connect_state)
    TextView textViewConnect;
    @BindView(R.id.clean_setting)
    TextView textCleanSettings;
    @BindView(R.id.clean_pri)
    TextView textClanPrivateKey;
    public int getLayoutId() {
        return R.layout.reset_device_processing;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }
    @Subscribe
    public void onEventMainThread(ResultEvent resultEvent) {
        switch (resultEvent.getResult()) {
            case "1":
                Drawable drawableStart = getDrawable(R.drawable.chenggong);
                Objects.requireNonNull(drawableStart).setBounds(0, 0, drawableStart.getMinimumWidth(), drawableStart.getMinimumHeight());
                textCleanSettings.setCompoundDrawables(drawableStart, null, null, null);
                textClanPrivateKey.setCompoundDrawables(drawableStart, null, null, null);
                startActivity(new Intent(this, ResetDeviceSuccessActivity.class));
                finish();
                break;
            case "0":
                Toast.makeText(this, "恢复出厂设置失败", Toast.LENGTH_LONG).show();
                finish();
        }

    }

    @Override
    public void initData() {
        NfcUtils.nfc(this, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }
}
