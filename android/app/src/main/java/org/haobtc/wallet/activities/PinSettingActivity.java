package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
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
    @BindView(R.id.bn_create_wallet)
    Button bnCreateWallet;
    private NumKeyboardUtil keyboardUtil;

    @Override
    public int getLayoutId() {
        return R.layout.pin_input;
    }

    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        edtPwd.setInputType(InputType.TYPE_NULL);
        keyboardUtil = new NumKeyboardUtil(this, this, edtPwd);

        init();

    }

    @Override
    public void initData() {

    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        edtPwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                keyboardUtil.showKeyboard();
                return false;
            }
        });

        edtPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // If the system keyboard is in pop-up state, hide it first
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus()
                                            .getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    keyboardUtil.showKeyboard();
                } else {
                    keyboardUtil.hideKeyboard();
                }
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                keyboardUtil.hideKeyboard();
            }
        }
        return super.onTouchEvent(event);
    }


    @OnClick({R.id.img_back, R.id.bn_create_wallet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.bn_create_wallet:

                break;
        }
    }

    private void startNewPage(String tags) {
        if (!TextUtils.isEmpty(tags)) {
            switch (tags) {
                case WalletUnActivatedActivity.TAG:
                    Intent intent = new Intent(this, ActivatedProcessing.class);
                    startActivity(intent);
                    break;
                case ImportWalletPageActivity.TAG:
                    Intent intent1 = new Intent(this, SelectMultiSigWalletActivity.class);
                    startActivity(intent1);
                    break;
                case TransactionDetailsActivity.TAG:
                    Intent intent2 = new Intent(this, ConfirmOnHardware.class);
                    startActivity(intent2);
                    break;
                default:
            }
        }
    }
}
