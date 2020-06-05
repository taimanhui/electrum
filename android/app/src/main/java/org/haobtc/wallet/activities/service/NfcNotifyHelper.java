package org.haobtc.wallet.activities.service;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.FinishEvent;
import org.haobtc.wallet.event.PinEvent;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.nfc;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.nfcHandler;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.protocol;

//
// Created by liyan on 2020/5/24.
//
public class NfcNotifyHelper extends AppCompatActivity implements View.OnClickListener {
    private String tag;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_nfc);
        ImageView imageViewCancel;
        RadioButton radioBle = findViewById(R.id.radio_ble);
        imageViewCancel = findViewById(R.id.img_cancel);
        findViewById(R.id.input_layout).setVisibility(View.GONE);
        imageViewCancel.setOnClickListener(this);
        radioBle.setVisibility(View.GONE);
        NfcUtils.nfc(this, true);
        tag = getIntent().getStringExtra("tag");
        EventBus.getDefault().register(this);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(wlp);
        }
    }
    @SingleClick
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.img_cancel) {
            nfc.put("IS_CANCEL", true);
            protocol.callAttr("notify");
//            new Handler().postDelayed(() -> nfc.put("IS_CANCEL", true), 2);
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            nfcHandler.put("device", tags);
            if ("PIN".equals(tag)) {
                String pin = getIntent().getStringExtra("pin");
                EventBus.getDefault().post(new PinEvent(pin, ""));
            } else if ("Passphrase".equals(tag)) {
                String passphrase = getIntent().getStringExtra("passphrase");
                EventBus.getDefault().post(new PinEvent("", passphrase));
            }
            protocol.callAttr("notify");
            EventBus.getDefault().post(new FinishEvent());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            Log.i("NFC", "为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // disable nfc discovery for the app
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
            Log.i("NFC", "禁用本App的NFC感应");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NfcUtils.mNfcAdapter = null;
        EventBus.getDefault().unregister(this);
    }
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onFinish(FinishEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        finish();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}