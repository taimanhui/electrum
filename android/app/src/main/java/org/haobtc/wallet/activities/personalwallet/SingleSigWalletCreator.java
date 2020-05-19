package org.haobtc.wallet.activities.personalwallet;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.ExistEvent;
import org.haobtc.wallet.event.ReceiveXpub;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;
import org.haobtc.wallet.utils.MyDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SingleSigWalletCreator extends BaseActivity {


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
    public String pin = "";
    public static final String TAG = SingleSigWalletCreator.class.getSimpleName();
    private TextView textView;
    private String walletName;
    private MyDialog myDialog;
    private EditText edit_bixinName;
    private Dialog dialogBtoms;
    private int pub;
    private int defaultKeyNum;
    private int defaultKeyNameNum;
    private String strBixinname;
    private long lastNotify;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_personal_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        myDialog = MyDialog.showDialog(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        defaultName = preferences.getInt("defaultName", 0);
        defaultKeyNum = preferences.getInt("defaultKeyNum", 0);//default key name
        init();

    }

    private void init() {
        walletNameNum = defaultName + 1;
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
                if (!TextUtils.isEmpty(s.toString())) {
                    if (Integer.parseInt(indication) != 0) {
                        bnMultiNext.setEnabled(true);
                        bnMultiNext.setBackground(getDrawable(R.drawable.button_bk));
                    } else {
                        bnMultiNext.setEnabled(false);
                        bnMultiNext.setBackground(getDrawable(R.drawable.button_bk_grey));
                    }
                } else {
                    bnMultiNext.setEnabled(false);
                    bnMultiNext.setBackground(getDrawable(R.drawable.button_bk_grey));
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
                walletName = editWalletNameSetting.getText().toString();
                String indicatorText = String.valueOf(progress + 1);
                tvIndicator.setText(indicatorText);
                params.leftMargin = (int) indicatorOffset;
                tvIndicator.setLayoutParams(params);
                if (!TextUtils.isEmpty(walletName)) {
                    bnMultiNext.setEnabled(true);
                    bnMultiNext.setBackground(getDrawable(R.drawable.button_bk));
                } else {
                    bnMultiNext.setEnabled(false);
                    bnMultiNext.setBackground(getDrawable(R.drawable.button_bk_grey));
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

    @SingleClick
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
        pub = Integer.parseInt(indication);
        if (TextUtils.isEmpty(strWalletName)) {
            mToast(getString(R.string.set_wallet));
            return;
        }
        if (pub == 0) {
            mToast(getString(R.string.set_bixinkey_num));
            return;
        }

        if (pub > 1) {
            Intent intent = new Intent(SingleSigWalletCreator.this, PersonalMultiSigWalletCreator.class);
            intent.putExtra("sigNum", pub);
            intent.putExtra("walletNameNum", walletNameNum);
            intent.putExtra("walletNames", strWalletName);
            startActivity(intent);
            finish();
        } else {
            CommunicationModeSelector.runnables.clear();
            CommunicationModeSelector.runnables.add(null);
//            CommunicationModeSelector.runnables.add(runnable2);
            Intent intent = new Intent(this, CommunicationModeSelector.class);
            intent.putExtra("tag", TAG);
            startActivity(intent);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i("CODED_CONTENT", "content=----: " + content);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doInit(ReceiveXpub event) {
        if (System.currentTimeMillis() - lastNotify > 10 * 1000L) {
            lastNotify = System.currentTimeMillis();
        } else {
            return;
        }
        String xpub = event.getXpub();
        String strXpub = "[\"" + xpub + "\"]";
        try {
            Daemon.commands.callAttr("import_create_hw_wallet", walletName, 1, 1, strXpub);
        } catch (Exception e) {
            e.printStackTrace();
            myDialog.dismiss();
            String message = e.getMessage();
            EventBus.getDefault().post(new ExistEvent());
            if ("BaseException: file already exists at path".equals(message)) {
                mToast(getString(R.string.changewalletname));
            } else if (message.contains("The same xpubs have create wallet")) {
                String haveWalletName = message.substring(message.indexOf("name=")+5);
                mToast(getString(R.string.xpub_have_wallet) + haveWalletName);
            }
            return;
        }
        edit.putInt("defaultName", walletNameNum);
        edit.putInt("defaultKeyNum", defaultKeyNameNum);
        edit.apply();
        myDialog.dismiss();
        walletName = editWalletNameSetting.getText().toString();
        Intent intent = new Intent(SingleSigWalletCreator.this, CreatFinishPersonalActivity.class);
        intent.putExtra("walletNames", walletName);
        intent.putExtra("flagTag", "personal");
        intent.putExtra("strBixinname", xpub);
        startActivity(intent);
//        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
