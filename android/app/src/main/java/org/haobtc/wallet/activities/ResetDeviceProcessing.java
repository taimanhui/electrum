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

import org.haobtc.wallet.R;
import org.haobtc.wallet.ResetDeviceSuccessActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
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
    private static final String TAG = ResetDeviceProcessing.class.getSimpleName();

    public int getLayoutId() {
        return R.layout.reset_device_processing;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        if (Ble.getInstance().getConnetedDevices().size() != 0) {
            if (Ble.getInstance().getConnetedDevices().get(0).getBleName().startsWith("BixinKEY")){
                new Handler().postDelayed(this::processingState
                , 10);
            }
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    private void processingState() {
        Drawable drawableStart = getDrawable(R.drawable.chenggong);
        Objects.requireNonNull(drawableStart).setBounds(0, 0, drawableStart.getMinimumWidth(), drawableStart.getMinimumHeight());
        try {
            int result = futureTask.get(60, TimeUnit.SECONDS).toInt();
            if (result == 1) {
                textCleanSettings.setCompoundDrawables(drawableStart, null, null, null);
                textClanPrivateKey.setCompoundDrawables(drawableStart, null, null, null);
                new Handler().postDelayed(this::startNewPage, 500);
            } else {
                Toast.makeText(this, "恢复出厂设置失败", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            Toast.makeText(this, "恢复出厂设置失败", Toast.LENGTH_LONG).show();
            Log.e(TAG, "恢复出厂设置失败" + Objects.requireNonNull(e.getMessage()));
            finish();
        }
    }

    @Override
    public void initData() {
        NfcUtils.nfc(this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            System.out.println("为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // disable nfc discovery for the app
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
            System.out.println("禁用本App的NFC感应");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NfcUtils.mNfcAdapter = null;
    }

    private void startNewPage() {
        Intent intent = new Intent(this, ResetDeviceSuccessActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            processingState();
        }
    }


    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}
