package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.NumKeyboardUtil;
import org.haobtc.wallet.utils.PasswordInputView;

import java.util.Objects;

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
    private int tag;

    @Override
    public int getLayoutId() {
        return R.layout.pin_input;
    }

    public void initView() {
        ButterKnife.bind(this);
        edtPwd.setInputType(InputType.TYPE_NULL);
        keyboardUtil = new NumKeyboardUtil(this, this, edtPwd);
        tag = getIntent().getIntExtra("pin", 0);
        switch (tag) {
            case 1:
                textViewPinDescription.setText(getString(R.string.pin_input));
                break;
            case 2:
                textViewPinDescription.setText(getString(R.string.set_PIN));
                break;
            default:

        }
        init();
    }

    @Override
    public void initData() {

    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        edtPwd.setOnTouchListener((v, event) -> {
            keyboardUtil.showKeyboard();
            return false;
        });

        edtPwd.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // If the system keyboard is in pop-up state, hide it first
                try {
                    ((InputMethodManager) Objects.requireNonNull(getSystemService(INPUT_METHOD_SERVICE)))
                            .hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus())
                                            .getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    keyboardUtil.showKeyboard();
                }
            } else {
                keyboardUtil.hideKeyboard();
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


    @OnClick({R.id.img_back, R.id.bn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.bn_next:
                if (edtPwd.getText().length() == 6) {
                    Intent intent = new Intent();
                    intent.putExtra("pin", edtPwd.getText().toString());
                    intent.putExtra("tag", tag);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(getBaseContext(), "the PIN's length less than 6", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
