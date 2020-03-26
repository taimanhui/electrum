package org.haobtc.wallet.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.os.Handler;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.heaton.blelibrary.ble.Ble;

public class ActivatedProcessing extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView imgBack;
    private TextView textViewConnect, textViewPIN, textViewProcess;

    public int getLayoutId() {
        return R.layout.activing_process;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        textViewConnect = findViewById(R.id.connect_state);
        textViewPIN = findViewById(R.id.pin_setting_state);
        textViewProcess = findViewById(R.id.activate_state);
        if (Ble.getInstance().getConnetedDevices().size() != 0) {
            if (Ble.getInstance().getConnetedDevices().get(0).getBleName().startsWith("BixinKEY")){
                new Handler().postDelayed(() -> processingState(false)
                , 10);
            }
        }
    }

    private void processingState(boolean isNfc) {
        Drawable drawableStart = getDrawable(R.drawable.chenggong);
        Objects.requireNonNull(drawableStart).setBounds(0, 0, drawableStart.getMinimumWidth(), drawableStart.getMinimumHeight());
        textViewConnect.setCompoundDrawables(drawableStart, null, null, null);
        while (isNfc) {
            if (!TextUtils.isEmpty(CommunicationModeSelector.pin)) {
                CommunicationModeSelector.customerUI.put("pin", CommunicationModeSelector.pin);
                break;
            }
        }

        textViewPIN.setCompoundDrawables(drawableStart, null, null, null);
        while (true) {
            int state = CommunicationModeSelector.customerUI.callAttr("get_state").toInt();
            if (state == 1) {
                CommunicationModeSelector.customerUI.put("state", 0);
                textViewProcess.setCompoundDrawables(drawableStart, null, null, null);
                new Handler().postDelayed(this::startNewPage, 500);
                break;
            }
        }
    }

    @Override
    public void initData() {
        NfcUtils.nfc(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            System.out.println("为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        } else {
            // use in udp
            new Handler().postDelayed(() -> processingState(false), 100);
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

    private void startNewPage() {//TODO:
        Intent intent = new Intent(this, ActivateSuccessActivity.class);
        startActivity(intent);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            processingState(true);
        }
    }


    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }
}
