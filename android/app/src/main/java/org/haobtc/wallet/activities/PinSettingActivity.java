package org.haobtc.wallet.activities;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;


public class PinSettingActivity extends BaseActivity {
    EditText editText;
    TextView tvCode1, tvCode2, tvCode3, tvCode4, tvCode5, tvCode6, tvPromptLarge, tvPromptSmall, tvMistakePrompt;
    private String tag;
    public final String PIN = "Pin";
    private String password = "";


    @Override
    public int getLayoutId() {
        return R.layout.pin_input;
    }

    public void initView() {
        tag = getIntent().getStringExtra(TouchHardwareActivity.FROM);
        tvPromptLarge = findViewById(R.id.pin_prompt_large);
        tvPromptSmall = findViewById(R.id.pin_prompt_small);
        tvMistakePrompt = findViewById(R.id.mistake_prompt);
        if (WalletUnActivatedActivity.TAG.equals(tag)) {
            tvPromptLarge.setText(R.string.pin_setting);
            tvPromptSmall.setVisibility(View.VISIBLE);
        }
        editText = findViewById(R.id.pin_edit);
        LinearLayout linearLayout = findViewById(R.id.ll);
        linearLayout.setOnClickListener(v -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            editText.requestFocus();
        });
        CommonUtils.enableToolBar(this, 0);
        tvCode1 = findViewById(R.id.tv1);
        tvCode2 = findViewById(R.id.tv2);
        tvCode3 = findViewById(R.id.tv3);
        tvCode4 = findViewById(R.id.tv4);
        tvCode5 = findViewById(R.id.tv5);
        tvCode6 = findViewById(R.id.tv6);
        editText.addTextChangedListener(edtCodeChange);
    }

    @Override
    public void initData() {

    }

    private void startNewPage(String tags) {
        if (!TextUtils.isEmpty(tags)) {
            switch (tags) {
                case WalletUnActivatedActivity.TAG:
                    Intent intent = new Intent(this, ActivatedProcessing.class);
                    intent.putExtra(PIN, password);
                    password = "";
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

    /**
     * Input content monitoring, projecting to 5 spaces
     */
    TextWatcher edtCodeChange = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            tvMistakePrompt.setVisibility(View.INVISIBLE);
            tvCode1.setText("");
            tvCode2.setText("");
            tvCode3.setText("");
            tvCode4.setText("");
            tvCode5.setText("");
            tvCode6.setText("");

            switch (s.length()) {
                case 6:
                    tvCode6.setText("●");
                case 5:
                    tvCode5.setText("●");
                case 4:
                    tvCode4.setText("●");
                case 3:
                    tvCode3.setText("●");
                case 2:
                    tvCode2.setText("●");
                case 1:
                    tvCode1.setText("●");
                default:
                    break;
            }
            // Input 5 verification codes to automatically request verification
            if (s.length() == 6) {
                if (WalletUnActivatedActivity.TAG.equals(tag)) {
                    if ("".equals(password)) { // pin set
                        password = editText.getText().toString();
                        tvPromptSmall.setText(R.string.confirm_pin);
                        tvCode1.setText("");
                        tvCode2.setText("");
                        tvCode3.setText("");
                        tvCode4.setText("");
                        tvCode5.setText("");
                        tvCode6.setText("");
                        s.clear();
                    } else { // pin confirm
                        if (password.equals(editText.getText().toString())) { // the pin is correct
                            // todo: set pin
                            startNewPage(tag);
                        } else {
                            // the pin input twice not the same
                            tvMistakePrompt.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    // input pin
                    // todo: verify pin
                    startNewPage(tag);
                }
            }
        }
    };
}
