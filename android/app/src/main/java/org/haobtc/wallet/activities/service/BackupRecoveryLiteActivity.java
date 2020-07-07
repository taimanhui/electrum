package org.haobtc.wallet.activities.service;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.event.FinishEvent;
import org.haobtc.wallet.event.PinEvent;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.nfc;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.nfcHandler;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.protocol;

public class BackupRecoveryLiteActivity extends BaseActivity {

    @BindView(R.id.radio_ble)
    RadioButton radioBle;
    @BindView(R.id.img_cancel)
    ImageView imgCancel;
    @BindView(R.id.text_prompt)
    TextView textPrompt;
    private String tag;

    @Override
    public int getLayoutId() {
        return R.layout.activity_backup_recovery_lite;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        textPrompt.setText(R.string.retouch);
        radioBle.setVisibility(View.GONE);
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

    @Override
    public void initData() {

    }

    @OnClick(R.id.img_cancel)
    public void onViewClicked(View v) {
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
            notifyNfc();
        }
    }

    private void notifyNfc() {
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
