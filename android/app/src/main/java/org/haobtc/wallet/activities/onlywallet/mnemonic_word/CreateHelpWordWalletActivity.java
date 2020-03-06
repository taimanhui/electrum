package org.haobtc.wallet.activities.onlywallet.mnemonic_word;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

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

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_help_word_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        newWallet_type = intent.getStringExtra("newWallet_type");
        strNewseed = intent.getStringExtra("strNewseed");

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_backCreat, R.id.bn_multi_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.bn_multi_next:
                String strWalletname = editWalletNameSetting.getText().toString();
                Intent intent = new Intent(CreateHelpWordWalletActivity.this, CreatePersonalHelpPassActivity.class);
                intent.putExtra("newWallet_type", newWallet_type);
                intent.putExtra("strNewseed",strNewseed);
                intent.putExtra("strnewWalletname",strWalletname);
                startActivity(intent);
                break;
        }
    }
}
