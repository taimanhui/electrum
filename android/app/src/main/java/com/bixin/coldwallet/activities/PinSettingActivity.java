package com.bixin.coldwallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bixin.coldwallet.R;
import com.bixin.coldwallet.utils.CommonUtils;


public class PinSettingActivity extends AppCompatActivity {
    EditText editText;
    TextView tvCode1, tvCode2, tvCode3, tvCode4, tvCode5, tvCode6, tvPromptLarge, tvPromptSmall, tvMistakePrompt;
    private String tag;
    private String password = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pin_input);
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

    private void startNewPage(String tags) {
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

    /**
     * 输入内容监听，投射到5个空格上
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
            if (WalletUnActivatedActivity.TAG.equals(tag)) {
                tvMistakePrompt.setVisibility(View.INVISIBLE);
            }
            // 输入完5个验证码 自动请求验证
            if (s.length() == 6) {
                if (WalletUnActivatedActivity.TAG.equals(tag)) {
                    if ("".equals(password)) {
                        password = editText.getText().toString();
                        tvPromptSmall.setText(R.string.confirm_pin);
                        tvCode1.setText("");
                        tvCode2.setText("");
                        tvCode3.setText("");
                        tvCode4.setText("");
                        tvCode5.setText("");
                        tvCode6.setText("");
                    } else {
                        if (password.equals(editText.getText().toString())) {
                            startNewPage(tag);
                            password = "";
                        } else {
                            tvMistakePrompt.setVisibility(View.VISIBLE);
                        }
                    }

                } else {
                    startNewPage(tag);
                }
            }
        }


    };
}
