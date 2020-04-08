package org.haobtc.wallet.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.MultiSigWalletCreator;
import org.haobtc.wallet.activities.personalwallet.CreatAppWalletActivity;
import org.haobtc.wallet.activities.personalwallet.SingleSigWalletCreator;
import org.haobtc.wallet.activities.personalwallet.ImportHistoryWalletActivity;
import org.haobtc.wallet.activities.personalwallet.hidewallet.HideWalletActivity;
import org.haobtc.wallet.activities.personalwallet.mnemonic_word.MnemonicWordActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateWalletActivity extends BaseActivity {
    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.lin_personal_walt)
    LinearLayout linPersonalWalt;
    @BindView(R.id.bn_import_wallet)
    LinearLayout bnImportWallet;
    @BindView(R.id.lin_input_histry)
    LinearLayout linInputHistry;
    @BindView(R.id.bn_create_wallet)
    LinearLayout bnCreateWallet;
    @BindView(R.id.lin_input_helpWord)
    LinearLayout linInputHelpWord;
    @BindView(R.id.linHideWallet)
    LinearLayout linHideWallet;
    private String intentWhere;


    @Override
    public int getLayoutId() {
        return R.layout.create_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        intentWhere = intent.getStringExtra("intentWhere");
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_backCreat, R.id.lin_personal_walt, R.id.bn_import_wallet, R.id.lin_input_histry, R.id.bn_create_wallet, R.id.lin_input_helpWord, R.id.linHideWallet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                if (intentWhere.equals("main")) {
                    finish();
                } else {
                    System.exit(0);
                }
                break;
            case R.id.lin_personal_walt:
                mIntent(SingleSigWalletCreator.class);
                break;
            case R.id.bn_import_wallet:
                mIntent(MultiSigWalletCreator.class);
                break;
            case R.id.lin_input_histry:
                mIntent(ImportHistoryWalletActivity.class);
                break;
            case R.id.bn_create_wallet:
                mIntent(CreatAppWalletActivity.class);
                break;
            case R.id.lin_input_helpWord:
                mIntent(MnemonicWordActivity.class);
                break;
            case R.id.linHideWallet:
                mIntent(HideWalletActivity.class);
                break;

        }
    }

}
