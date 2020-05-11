package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.HardwareDetailsActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity;
import org.haobtc.wallet.activities.settings.recovery_set.ResetDeviceProcessing;
import org.haobtc.wallet.activities.transaction.PinNewActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.OperationTimeoutEvent;
import org.haobtc.wallet.event.PinEvent;
import org.haobtc.wallet.event.ReadingEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.fragment.mainwheel.WheelViewpagerFragment;
import org.haobtc.wallet.utils.NumKeyboardUtil;
import org.haobtc.wallet.utils.PasswordInputView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PinSettingActivity extends BaseActivity {
    @BindView(R.id.trader_pwd_set_pwd_edittext)
    PasswordInputView edtPwd;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.bn_next)
    Button bnCreateWallet;
    @BindView(R.id.pin_description)
    TextView textViewPinDescription;
    private NumKeyboardUtil keyboardUtil;
    private String tag;
    public static final String TAG = PinSettingActivity.class.getSimpleName();
    private RelativeLayout relativeLayout_key;

    @Override
    public int getLayoutId() {
        return R.layout.pin_input;
    }

    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        relativeLayout_key = findViewById(R.id.relativeLayout_key);
        keyboardUtil = new NumKeyboardUtil(this, this, edtPwd);
        int pinType = getIntent().getIntExtra("pin_type", 0);
        tag = getIntent().getStringExtra("tag");
        switch (pinType) {
            case 2:
                textViewPinDescription.setText(getString(R.string.pin_setting));
                break;
            case 3:
                textViewPinDescription.setText(getString(R.string.pin_original));
            default:
                textViewPinDescription.setText(getString(R.string.pin_input));

        }
        init();
    }

    @Override
    public void initData() {
        keyboardUtil.showKeyboard();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        edtPwd.setOnTouchListener((v, event) -> {
            relativeLayout_key.setVisibility(View.VISIBLE);
            keyboardUtil.showKeyboard();
            return false;
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                relativeLayout_key.setVisibility(View.GONE);
                keyboardUtil.hideKeyboard();
            }
        }
        return super.onTouchEvent(event);
    }


    @SingleClick
    @OnClick({R.id.img_back, R.id.bn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                // Global.py.getModule("trezorlib.customer_ui").get("CustomerUI").put("user_cancel", 1);
                finish();
                break;
            case R.id.bn_next:
                String pin = edtPwd.getText().toString();
                if (pin.length() == 6) {
                    switch (tag) {
//                        case SetNameActivity.TAG:
//                            Intent intent = new Intent(this, ActivatedProcessing.class);
//                            intent.putExtra("pin", pin);
//                            startActivity(intent);
//                            break;
                        case SettingActivity.TAG_CHANGE_PIN:
                        case HardwareDetailsActivity.TAG: // change pin
                            Intent intent1 = new Intent(this, PinNewActivity.class);
                            intent1.putExtra("pin_origin", pin);
                            startActivity(intent1);
                            break;
                        case RecoverySetActivity.TAG:
                            Intent intent2 = new Intent(this, ResetDeviceProcessing.class);
                            intent2.putExtra("pin", pin);
                            startActivity(intent2);
                            break;
                        case ConfirmOnHardware.TAG:
                            Intent intent3 = new Intent(this, SignatureProcessing.class);
                            intent3.putExtra("pin", pin);
                            startActivity(intent3);
                            break;
                        default:
                            EventBus.getDefault().post(new PinEvent(pin, ""));
                            finish();
                    }

                } else {
                    Toast.makeText(getBaseContext(), getString(R.string.pass_morethan_6), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if ("Keyboard".equals(msgVote)){
            relativeLayout_key.setVisibility(View.GONE);
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void timeout(OperationTimeoutEvent event) {
        Toast.makeText(this, "pin 输入超时", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
