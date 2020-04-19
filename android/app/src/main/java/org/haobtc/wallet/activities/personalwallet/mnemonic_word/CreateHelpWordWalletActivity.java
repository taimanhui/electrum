package org.haobtc.wallet.activities.personalwallet.mnemonic_word;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateHelpWordWalletActivity extends BaseActivity {

    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.edit_walletName_setting)
    EditText editWalletNameSetting;
    @BindView(R.id.tet_textNum)
    TextView tetTextNum;
    @BindView(R.id.bn_multi_next)
    Button bnMultiNext;
    private String newWallet_type;
    private String strNewseed;
    private int defaultName;
    private int walletNameNum;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_help_word_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        defaultName = preferences.getInt("defaultName", 0);//default wallet name
        Intent intent = getIntent();
        newWallet_type = intent.getStringExtra("newWallet_type");
        strNewseed = intent.getStringExtra("strNewseed");

    }

    @Override
    public void initData() {
        setEditTextComments();
    }

    private void setEditTextComments() {
        walletNameNum = defaultName + 1;
        editWalletNameSetting.setText(String.format("钱包%s", String.valueOf(walletNameNum)));
        editWalletNameSetting.addTextChangedListener(new TextWatcher() {
            CharSequence input;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                input = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tetTextNum.setText(String.format(Locale.CHINA, "%d/20", input.length()));
                if (input.length() > 19) {
                    Toast.makeText(CreateHelpWordWalletActivity.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }
                if (TextUtils.isEmpty(s)){
                    bnMultiNext.setEnabled(false);
                    bnMultiNext.setBackground(getDrawable(R.drawable.button_bk_grey));
                }else{
                    bnMultiNext.setEnabled(true);
                    bnMultiNext.setBackground(getDrawable(R.drawable.button_bk));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @SingleClick
    @OnClick({R.id.img_backCreat, R.id.bn_multi_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.bn_multi_next:
                String strWalletname = editWalletNameSetting.getText().toString();
                if (TextUtils.isEmpty(strWalletname)){
                    mToast(getString(R.string.please_input_walletname));
                    return;
                }
                Intent intent = new Intent(CreateHelpWordWalletActivity.this, CreatePersonalHelpPassActivity.class);
                intent.putExtra("newWallet_type", newWallet_type);
                intent.putExtra("strNewseed",strNewseed);
                intent.putExtra("walletNameNum",walletNameNum);
                intent.putExtra("strnewWalletname",strWalletname);
                startActivity(intent);
                finish();
                break;
        }
    }
}
