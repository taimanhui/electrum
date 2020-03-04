package org.haobtc.wallet.activities.onlywallet;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_personal_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        init();

    }

    private void init() {
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
        if (sigNum > 1) {
            Intent intent = new Intent(CreatePersonalWalletActivity.this, CreateOnlyChooseActivity.class);
            intent.putExtra("sigNum", sigNum);
            startActivity(intent);
        } else {
            showSelectFeeDialogs(CreatePersonalWalletActivity.this, R.layout.bluetooth_personal);
        }

    }

    private void showSelectFeeDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtom = new Dialog(context, R.style.dialog);

        //cancel dialog
        view.findViewById(R.id.img_Cancle).setOnClickListener(v -> {
            dialogBtom.cancel();
        });


        dialogBtom.setContentView(view);
        Window window = dialogBtom.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtom.show();
    }

}
