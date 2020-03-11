package org.haobtc.wallet.activities.onlywallet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
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
import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.yzq.zxinglibrary.common.Constant;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ActivatedProcessing;
import org.haobtc.wallet.activities.WalletUnActivatedActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.CustomerDialogFragment;
import org.haobtc.wallet.activities.manywallet.ManyWalletTogetherActivity;
import org.haobtc.wallet.adapter.AddBixinKeyAdapter;
import org.haobtc.wallet.adapter.PublicPersonAdapter;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.fragment.ReadingPubKeyDialogFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.manywallet.CustomerDialogFragment.REQUEST_ACTIVE;
import static org.haobtc.wallet.activities.manywallet.CustomerDialogFragment.xpub;

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
    private boolean executable = true;
    // new version code
    public String pin = "";
    private CustomerDialogFragment dialogFragment;
    public static final String TAG = CreatePersonalWalletActivity.class.getSimpleName();
    private TextView textView;
    private boolean isActive;
    public static boolean isNfc;
    private String walletName;
    private boolean isInit;

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
                        bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk));
                    } else {
                        bnMultiNext.setEnabled(false);
                        bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
                    }
                } else {
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
                walletName = editWalletNameSetting.getText().toString();
                String indicatorText = String.valueOf(progress);
                tvIndicator.setText(indicatorText);
                params.leftMargin = (int) indicatorOffset;
                tvIndicator.setLayoutParams(params);
                if (progress != 0) {
                    if (!TextUtils.isEmpty(walletName)) {
                        bnMultiNext.setEnabled(true);
                        bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk));
                    } else {
                        bnMultiNext.setEnabled(false);
                        bnMultiNext.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
                    }

                } else {
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
        int n = Integer.parseInt(indication);
        if (TextUtils.isEmpty(strWalletName)) {
            mToast(getResources().getString(R.string.set_wallet));
            return;
        }
        if (n == 0) {
            mToast(getResources().getString(R.string.set_bixinkey_num));
            return;
        }

        try {
            Daemon.commands.callAttr("set_multi_wallet_info", strWalletName, 1, n);
        } catch (Exception e) {
            e.printStackTrace();

        }
        if (n > 1) {
            Intent intent = new Intent(CreatePersonalWalletActivity.this, CreateOnlyChooseActivity.class);
            intent.putExtra("sigNum", n);
            intent.putExtra("walletNameNum", walletNameNum);
            intent.putExtra("walletNames", strWalletName);
            startActivity(intent);
        } else {
            // new version code
            showPopupAddCosigner1();
        }

    }

    private void showPopupAddCosigner1() {
        dialogFragment = new CustomerDialogFragment(TAG, null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private void showConfirmPubDialog(Context context, @LayoutRes int resource, String xpub) {
        //set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtoms = new Dialog(context, R.style.dialog);

        EditText edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        textView = view.findViewById(R.id.text_public_key_cosigner_popup);
        textView.setText(xpub);
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
                    Toast.makeText(CreatePersonalWalletActivity.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String strBixinname = edit_bixinName.getText().toString();
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
            try {
                //add
                Daemon.commands.callAttr("add_xpub", strSweep);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            edit.putInt("defaultName", walletNameNum);
            edit.apply();
            Intent intent = new Intent(CreatePersonalWalletActivity.this, CreatFinishPersonalActivity.class);
            intent.putExtra("walletNames", walletName);
            intent.putExtra("flagTag", "personal");
            intent.putExtra("strBixinname", strBixinname);
            startActivity(intent);

            dialogBtoms.cancel();
            dialogFragment.dismiss();

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

    private boolean isInitialized() throws Exception {
        boolean isInitialized = false;
        try {
            System.out.println("call is_initialized =====");
            isInitialized = Daemon.commands.callAttr("is_initialized").toBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return isInitialized;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            isNfc = true;
            if (executable) {
                Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
                PyObject nfcHandler = nfc.get("NFCHandle");
                nfcHandler.put("device", tags);
                executable = false;
            }
            if (!TextUtils.isEmpty(pin) && isInit) {
                CustomerDialogFragment.customerUI.put("pin", pin);
                try {
                    ReadingPubKeyDialogFragment dialog = (ReadingPubKeyDialogFragment) dialogFragment.showReadingDialog();
                    xpub = CustomerDialogFragment.futureTask.get(40, TimeUnit.SECONDS).toString();
                    dialog.dismiss();
                    showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);
                    return;
                } catch (ExecutionException | TimeoutException | InterruptedException e) {
                    dialogFragment.showReadingFailedDialog();
                    if ("com.chaquo.python.PyException: BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
                        mToast(getResources().getString(R.string.pin_wrong));
                    }
                }
            }
            try {
                isInit = isInitialized();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "communication error, get firmware info error", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isInit) {
                boolean pinCached = false;
                try {
                    pinCached = Daemon.commands.callAttr("get_pin_status").toBoolean();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // todo: get xpub
                CustomerDialogFragment.futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_xpub_from_hw", new Kwarg("_type", "p2wpkh")));
                new Thread(CustomerDialogFragment.futureTask).start();
                if (pinCached) {
                    try {
                        xpub = CustomerDialogFragment.futureTask.get().toString();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);
                }

            } else {
                // todo: Initialized
                if (isActive) {
                    new Thread(() -> {
                        try {
                            Daemon.commands.callAttr("init");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ).start();
                } else {
                    Intent intent1 = new Intent(this, WalletUnActivatedActivity.class);
                    startActivityForResult(intent1, REQUEST_ACTIVE);
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CustomerDialogFragment.PIN_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                pin = data.getStringExtra("pin");
                CustomerDialogFragment.pin = pin;
                if (isActive)  {
                    CustomerDialogFragment.handler.sendEmptyMessage(CustomerDialogFragment.SHOW_PROCESSING);
                    return;
                }
                if (!isNfc) {
                    CustomerDialogFragment.customerUI.put("pin", pin);
                    pin = "";
                }
                if (CustomerDialogFragment.isActive) {
                    CustomerDialogFragment.handler.sendEmptyMessage(CustomerDialogFragment.SHOW_PROCESSING);
                    return;
                }
                if (!isNfc) {
                    try {
                        xpub = CustomerDialogFragment.futureTask.get(40, TimeUnit.SECONDS).toString();
                        showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);
                    } catch (ExecutionException | TimeoutException | InterruptedException e) {
                        dialogFragment.showReadingFailedDialog();
                        if ("com.chaquo.python.PyException: BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
                            Toast.makeText(this, "PIN码输入有误，请重新输入", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } else if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i("CODED_CONTENT", "content=----: " + content);
//                edit_sweep.setText(content);
            }
        } else if (requestCode == REQUEST_ACTIVE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                isActive = data.getBooleanExtra("isActive", false);
            }
        }
    }
}
