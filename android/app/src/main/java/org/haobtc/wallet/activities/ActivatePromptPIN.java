package org.haobtc.wallet.activities;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.common.base.Strings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.event.ButtonRequestEvent;
import org.haobtc.wallet.event.PinEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.COMMUNICATION_MODE_BLE;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.customerUI;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.isNFC;

//
// Created by liyan on 2020/6/17.
//
public class ActivatePromptPIN extends BaseActivity implements BusinessAsyncTask.Helper {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.change_pin)
    Button changePin;
//    private boolean isChangePIN;

    @Override
    public int getLayoutId() {
        return R.layout.pin_setting_after_init;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void initData() {

    }

    @SingleClick(value = 5000)
    @OnClick({R.id.img_back, R.id.change_pin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.change_pin:
                if (isNFC) {
                    Intent intent = new Intent(this, CommunicationModeSelector.class).setAction("change_pin");
                    startActivity(intent);
                } else {
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.CHANGE_PIN, COMMUNICATION_MODE_BLE);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onButtonRequest(ButtonRequestEvent event) {
//       EventBus.getDefault().post(new ExitEvent());
//        if (!isChangePIN) {
//            isChangePIN = true;
//            if (isNFC) {
//                startActivity(new Intent(this, NfcNotifyHelper.class));
//            }
//        } else {
            Intent intent = new Intent(this, ConfirmPINPrompt.class);
            startActivity(intent);
            finish();
//        }
    }
    @Subscribe
    public void setPin(PinEvent event) {
        if (!Strings.isNullOrEmpty(event.getPinCode())) {
            customerUI.put("pin", event.getPinCode());
        }
    }
    @Override
    public void onPreExecute() {

    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void onResult(String s) {
        switch (s) {
            case "1":

                break;
            case "0":
        }
    }

    @Override
    public void onCancelled() {

    }
}
