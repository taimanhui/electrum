package org.haobtc.wallet.activities.set.fixpin;

import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.PasswordInputView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InputOldPINActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.trader_pwd_set_pwd_edittext)
    PasswordInputView traderPwdSetPwdEdittext;
    @BindView(R.id.btn_setPin)
    Button btnSetPin;
    @BindView(R.id.keyboard_view)
    KeyboardView keyboardView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_input_old_pin;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }


    @OnClick({R.id.img_back, R.id.btn_setPin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_setPin:
                mIntent(InputNewPINActivity.class);
                break;
        }
    }
}
