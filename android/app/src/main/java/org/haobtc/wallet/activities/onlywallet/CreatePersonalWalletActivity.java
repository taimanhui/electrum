package org.haobtc.wallet.activities.onlywallet;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.CustomerDialogFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatePersonalWalletActivity extends BaseActivity {


    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.edit_walletName_setting)
    EditText editWalletNameSetting;
    @BindView(R.id.seek_bar_num)
    IndicatorSeekBar seekBarNum;
    @BindView(R.id.bn_multi_next)
    Button bnMultiNext;
    @BindView(R.id.tv_indicator)
    TextView tvIndicator;
    private SharedPreferences.Editor edit;
    private int defaultName;
    private int walletNameNum;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_personal_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        defaultName = preferences.getInt("defaultName", 0);
        init();

    }

    private void init() {
        walletNameNum = defaultName+1;
        editWalletNameSetting.setText(String.format("钱包%s", String.valueOf(walletNameNum)));

        editWalletNameSetting.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String indication = tvIndicator.getText().toString();
                if (!TextUtils.isEmpty(s.toString())){
                    if (Integer.parseInt(indication)!=0){
                        bnMultiNext.setEnabled(true);
                        bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk));
                    }else{
                        bnMultiNext.setEnabled(false);
                        bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
                    }
                }else{
                    bnMultiNext.setEnabled(false);
                    bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
                }
            }
        });

    }

    @Override
    public void initData() {
        seekbarLatoutup();
    }

    private void seekbarLatoutup() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvIndicator.getLayoutParams();
        seekBarNum.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                String walletName = editWalletNameSetting.getText().toString();
                String indicatorText = String.valueOf(progress);
                tvIndicator.setText(indicatorText);
                params.leftMargin = (int) indicatorOffset;
                tvIndicator.setLayoutParams(params);
                if (progress!=0){
                    if (!TextUtils.isEmpty(walletName)){
                        bnMultiNext.setEnabled(true);
                        bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk));
                    }else{
                        bnMultiNext.setEnabled(false);
                        bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
                    }

                }else{
                    bnMultiNext.setEnabled(false);
                    bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tvIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvIndicator.setVisibility(View.VISIBLE);
            }
        });

    }

    @OnClick({R.id.img_backCreat, R.id.bn_multi_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.bn_multi_next:
                mCreatOnlyWallet();

                break;
        }
    }

    //creat personal wallet
    private void mCreatOnlyWallet() {
        String strWalletName = editWalletNameSetting.getText().toString();
        String indication = tvIndicator.getText().toString();
        int sigNum = Integer.parseInt(indication);
        if (TextUtils.isEmpty(strWalletName)) {
            mToast(getResources().getString(R.string.set_wallet));
            return;
        }
        if (sigNum == 0) {
            mToast(getResources().getString(R.string.set_bixinkey_num));
            return;
        }

        try {
            Daemon.commands.callAttr("set_multi_wallet_info", strWalletName, 1, sigNum);
        } catch (Exception e) {
            e.printStackTrace();

        }
        edit.putInt("defaultName",walletNameNum);
        edit.apply();
        if (sigNum > 1) {
            Intent intent = new Intent(CreatePersonalWalletActivity.this, CreateOnlyChooseActivity.class);
            intent.putExtra("sigNum", sigNum);
            startActivity(intent);
        } else {
            // new version code
            showPopupAddCosigner1();
        }

    }
    private void showPopupAddCosigner1() {
        CustomerDialogFragment dialogFragment = new CustomerDialogFragment("", null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }
}
