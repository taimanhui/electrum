package org.haobtc.wallet.activities.personalwallet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.InitEvent;
import org.haobtc.wallet.event.ReceiveXpub;
import org.haobtc.wallet.event.SendXpubToSigwallet;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;
import org.haobtc.wallet.utils.MyDialog;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.xpub;

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
            // TODO： 弹窗
            CommunicationModeSelector.runnables.clear();
            CommunicationModeSelector.runnables.add(null);
            CommunicationModeSelector.runnables.add(runnable2);
            Intent intent = new Intent(this, CommunicationModeSelector.class);
            intent.putExtra("tag", TAG);
            startActivity(intent);
        }

    }

    private Runnable runnable2 = () -> showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);

    private void showConfirmPubDialog(Context context, @LayoutRes int resource, String xpub) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtoms = new Dialog(context, R.style.dialog);

        edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        textView = view.findViewById(R.id.text_public_key_cosigner_popup);
        textView.setText(xpub);
        defaultKeyNameNum = defaultKeyNum + 1;
        edit_bixinName.setText(String.format("pub%s", String.valueOf(defaultKeyNameNum)));
        edit_bixinName.addTextChangedListener(new TextWatcher() {
            CharSequence input;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                input = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tet_Num.setText(String.format(Locale.CHINA, "%d/20", input.length()));
                if (input.length() > 19) {
                    Toast.makeText(SingleSigWalletCreator.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            myDialog.show();
            handler.sendEmptyMessage(1);
        });

        //cancel dialog
        view.findViewById(R.id.img_cancle).setOnClickListener(v -> {
            dialogBtoms.cancel();
        });

        dialogBtoms.setContentView(view);
        Window window = dialogBtoms.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtoms.show();
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

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                strBixinname = edit_bixinName.getText().toString();
                String strSweep = textView.getText().toString();
                walletName = editWalletNameSetting.getText().toString();
                if (TextUtils.isEmpty(strBixinname)) {
                    mToast(getString(R.string.input_name));
                    return;
                }
                if (TextUtils.isEmpty(strSweep)) {
                    mToast(getString(R.string.input_public_address));
                    return;
                }
                String strXpub = "[\"" + strSweep + "\"]";
                try {
                    Daemon.commands.callAttr("import_create_hw_wallet", walletName, 1, 1, strXpub);
                } catch (Exception e) {
                    e.printStackTrace();
                    myDialog.dismiss();
                    String message = e.getMessage();
                    if ("BaseException: file already exists at path".equals(message)) {
                        mToast(getString(R.string.changewalletname));
                    } else if (message.contains("The same xpubs have create wallet")) {
                        mToast(getString(R.string.xpub_have_wallet));
                    }
                    return;
                }
                Log.i("jinxioaminisheduh", "================: ");
                edit.putInt("defaultName", walletNameNum);
                edit.putInt("defaultKeyNum",defaultKeyNameNum);
                edit.apply();
                myDialog.dismiss();
                EventBus.getDefault().post(new FirstEvent("11"));
                Intent intent = new Intent(SingleSigWalletCreator.this, CreatFinishPersonalActivity.class);
                intent.putExtra("walletNames", walletName);
                intent.putExtra("flagTag", "personal");
                intent.putExtra("strBixinname", strBixinname);
                startActivity(intent);
                finish();
                dialogBtoms.cancel();
            }

        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doInit(ReceiveXpub event) {
        String xpub = event.getXpub();
        Log.i("SingleSigWalletCreator", "xpub--------------------------------------: "+xpub);
        String strXpub = "[\"" + xpub + "\"]";
        try {
            Daemon.commands.callAttr("import_create_hw_wallet", walletName, 1, 1, strXpub);
        } catch (Exception e) {
            e.printStackTrace();
            myDialog.dismiss();
            String message = e.getMessage();
            if ("BaseException: file already exists at path".equals(message)) {
                mToast(getString(R.string.changewalletname));
            } else if (message.contains("The same xpubs have create wallet")) {
                mToast(getString(R.string.xpub_have_wallet));
            }
            return;
        }
        Log.i("SingleSigWalletCreator", "SingleSigWalletCreator: "+xpub);
        edit.putInt("defaultName", walletNameNum);
        edit.putInt("defaultKeyNum",defaultKeyNameNum);
        edit.apply();
        myDialog.dismiss();
        walletName = editWalletNameSetting.getText().toString();
        EventBus.getDefault().post(new FirstEvent("11"));
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
