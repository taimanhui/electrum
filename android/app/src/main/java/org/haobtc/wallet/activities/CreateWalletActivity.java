package org.haobtc.wallet.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.ManyWalletTogetherActivity;
import org.haobtc.wallet.activities.onlywallet.CreatAppWalletActivity;
import org.haobtc.wallet.activities.onlywallet.CreatePersonalWalletActivity;
import org.haobtc.wallet.activities.onlywallet.mnemonic_word.MnemonicWordActivity;

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
    @BindView(R.id.tet_AppWallet)
    TextView tetAppWallet;
    //remeber first back time
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;


    @Override
    public int getLayoutId() {
        return R.layout.create_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        edit = preferences.edit();
        edit.putBoolean("JumpOr", false);
        edit.apply();


    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_backCreat, R.id.lin_personal_walt, R.id.bn_import_wallet, R.id.lin_input_histry, R.id.bn_create_wallet, R.id.lin_input_helpWord,R.id.tet_AppWallet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.lin_personal_walt:
                mIntent(CreatePersonalWalletActivity.class);
                break;
            case R.id.bn_import_wallet:
                mIntent(ManyWalletTogetherActivity.class);
                break;
            case R.id.lin_input_histry:
                break;
            case R.id.bn_create_wallet:
                mIntent(CreateWalletPageActivity.class);
                break;
            case R.id.lin_input_helpWord:
                mIntent(MnemonicWordActivity.class);
                break;
            case R.id.tet_AppWallet:
                mIntent(CreatAppWalletActivity.class);
                break;
        }
    }

}
