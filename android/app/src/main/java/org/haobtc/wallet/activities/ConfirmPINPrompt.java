package org.haobtc.wallet.activities;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.ActivateBackupSuccessActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.event.BackupFinishEvent;
import org.haobtc.wallet.event.FinishEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.COMMUNICATION_MODE_BLE;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.isNFC;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.nfc;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.protocol;

//
// Created by liyan on 2020/6/17.
//
public class ConfirmPINPrompt extends BaseActivity implements BusinessAsyncTask.Helper {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.backup)
    Button backup;

    @Override
    public int getLayoutId() {
        return R.layout.confirm_pin;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void initData() {

    }
    @SingleClick(value = 10000)
    @OnClick({R.id.img_back, R.id.backup})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.backup:
                if (isNFC) {
                    protocol.callAttr("notify");
                    nfc.put("IS_CANCEL", true);
                    Intent intent = new Intent(this, CommunicationModeSelector.class);
                    intent.setAction("backup");
                    startActivity(intent);
                } else {
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.BACK_UP, COMMUNICATION_MODE_BLE);
                }
                break;
        }
    }
    @Subscribe
    public void onFinishEvent(FinishEvent event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onException(Exception e) {

    }
    @Subscribe
    public void onBackupFinish(BackupFinishEvent event) {
        Intent intent = new Intent(this, ActivateBackupSuccessActivity.class);
        intent.putExtra("message", event.getMessage());
        startActivity(intent);
        finish();
    }
    @Override
    public void onResult(String s) {
        onBackupFinish(new BackupFinishEvent(s));
    }

    @Override
    public void onCancelled() {

    }
}
